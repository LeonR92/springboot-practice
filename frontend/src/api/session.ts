import type { UserRole } from './types'

const KEY = 'session-token'

export interface Session {
  token: string
  userId: number
  role: UserRole
  email: string
  name: string
  expiresAt: number
}

export function decodeToken(token: string): Session | null {
  try {
    const payload = JSON.parse(atob(token.split('.')[1].replace(/-/g, '+').replace(/_/g, '/')))
    const userId = Number(payload.sub)
    if (!Number.isFinite(userId)) return null
    return {
      token,
      userId,
      role: payload.role === 'ADMIN' ? 'ADMIN' : 'USER',
      email: String(payload.email ?? ''),
      name: String(payload.name ?? ''),
      expiresAt: Number(payload.exp) * 1000,
    }
  } catch {
    return null
  }
}

function safely<T>(action: () => T, fallback: T): T {
  try {
    return action()
  } catch {
    return fallback
  }
}

export const tokenStore = {
  get: () => safely(() => localStorage.getItem(KEY), null),
  set: (token: string) => safely(() => localStorage.setItem(KEY, token), undefined),
  clear: () => safely(() => localStorage.removeItem(KEY), undefined),
}

export function loadSession(): Session | null {
  const token = tokenStore.get()
  const session = token ? decodeToken(token) : null
  if (!session || session.expiresAt <= Date.now()) {
    tokenStore.clear()
    return null
  }
  return session
}
