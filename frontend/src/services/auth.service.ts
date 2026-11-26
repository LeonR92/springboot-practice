import { api } from '@/api/client'
import type { LoginInput, RegisterInput, User } from '@/api/types'

export const authService = {
  login: (input: LoginInput) => api.post<{ token: string }>('/auth/login', input),
  register: (input: RegisterInput) => api.post<User>('/auth/register', input),
}
