import { useState } from 'react'
import { zodResolver } from '@hookform/resolvers/zod'
import { useForm } from 'react-hook-form'
import { Link, Navigate, useLocation, useNavigate } from 'react-router-dom'
import { z } from 'zod'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Field } from '@/components/shared/Field'
import { errorMessage } from '@/components/shared/PageState'
import { useAuth } from '@/hooks/useAuth'

const loginSchema = z.object({ email: z.email('Enter a valid email'), password: z.string().min(1, 'Enter your password') })
const registerSchema = loginSchema.extend({
  name: z.string().min(1, 'Enter your name'),
  password: z.string().min(8, 'At least 8 characters').regex(/[A-Za-z]/, 'Include a letter').regex(/\d/, 'Include a digit'),
})

type Values = { name?: string; email: string; password: string }

export default function AuthPage({ mode }: { mode: 'login' | 'register' }) {
  const { session, login, register: signUp } = useAuth()
  const navigate = useNavigate()
  const location = useLocation()
  const [error, setError] = useState<string | null>(null)
  const isLogin = mode === 'login'
  const from = (location.state as { from?: string } | null)?.from ?? '/'

  const form = useForm<Values>({ resolver: zodResolver(isLogin ? loginSchema : registerSchema) as never })
  const { errors, isSubmitting } = form.formState

  if (session) return <Navigate to={from} replace />

  const onSubmit = form.handleSubmit(async (values) => {
    setError(null)
    try {
      if (isLogin) await login(values)
      else await signUp({ name: values.name ?? '', email: values.email, password: values.password })
      navigate(from, { replace: true })
    } catch (e) {
      setError(errorMessage(e))
    }
  })

  return (
    <div className="mx-auto max-w-md px-6 py-16">
      <h1 className="text-4xl">{isLogin ? 'Welcome back' : 'Create your account'}</h1>
      <form onSubmit={onSubmit} className="mt-8 space-y-5" noValidate>
        {!isLogin && (
          <Field label="Name" error={errors.name?.message}>
            <Input id="name" autoComplete="name" {...form.register('name')} />
          </Field>
        )}
        <Field label="Email" error={errors.email?.message}>
          <Input id="email" type="email" autoComplete="email" {...form.register('email')} />
        </Field>
        <Field label="Password" error={errors.password?.message}>
          <Input id="password" type="password" autoComplete={isLogin ? 'current-password' : 'new-password'} {...form.register('password')} />
        </Field>
        {error && (
          <p role="alert" className="text-sm text-destructive">
            {error}
          </p>
        )}
        <Button type="submit" size="lg" className="w-full" disabled={isSubmitting}>
          {isLogin ? 'Sign in' : 'Create account'}
        </Button>
      </form>
      <p className="mt-6 text-sm text-muted-foreground">
        {isLogin ? 'New here? ' : 'Already have an account? '}
        <Link to={isLogin ? '/register' : '/login'} state={location.state} className="text-primary underline">
          {isLogin ? 'Create an account' : 'Sign in'}
        </Link>
      </p>
    </div>
  )
}
