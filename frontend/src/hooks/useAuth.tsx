import { createContext, useCallback, useContext, useEffect, useMemo, useState, type ReactNode } from 'react'
import { useQueryClient } from '@tanstack/react-query'
import { onUnauthorized } from '@/api/client'
import { decodeToken, loadSession, tokenStore, type Session } from '@/api/session'
import type { LoginInput, RegisterInput } from '@/api/types'
import { authService } from '@/services/auth.service'

interface AuthState {
  session: Session | null
  isAdmin: boolean
  login: (input: LoginInput) => Promise<void>
  register: (input: RegisterInput) => Promise<void>
  logout: () => void
}

const AuthContext = createContext<AuthState | null>(null)

export function AuthProvider({ children }: { children: ReactNode }) {
  const [session, setSession] = useState<Session | null>(loadSession)
  const queryClient = useQueryClient()

  const logout = useCallback(() => {
    tokenStore.clear()
    setSession(null)
    queryClient.clear()
  }, [queryClient])

  const login = useCallback(async (input: LoginInput) => {
    const { token } = await authService.login(input)
    const next = decodeToken(token)
    if (!next) throw new Error('Received an invalid session from the server.')
    tokenStore.set(token)
    setSession(next)
  }, [])

  const register = useCallback(
    async (input: RegisterInput) => {
      await authService.register(input)
      await login({ email: input.email, password: input.password })
    },
    [login],
  )

  useEffect(() => {
    onUnauthorized(logout)
    return () => onUnauthorized(undefined)
  }, [logout])

  useEffect(() => {
    if (!session) return
    const timer = setTimeout(logout, Math.max(session.expiresAt - Date.now(), 0))
    return () => clearTimeout(timer)
  }, [session, logout])

  const value = useMemo(
    () => ({ session, isAdmin: session?.role === 'ADMIN', login, register, logout }),
    [session, login, register, logout],
  )
  return <AuthContext value={value}>{children}</AuthContext>
}

export function useAuth() {
  const context = useContext(AuthContext)
  if (!context) throw new Error('useAuth must be used inside AuthProvider')
  return context
}
