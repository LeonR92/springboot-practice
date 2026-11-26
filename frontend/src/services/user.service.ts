import { api } from '@/api/client'
import type { User } from '@/api/types'

export const userService = {
  list: () => api.get<User[]>('/users'),
}
