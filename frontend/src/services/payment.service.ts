import { api } from '@/api/client'
import type { Payment, PaymentMethod } from '@/api/types'

export const paymentService = {
  pay: (reservationId: number, method: PaymentMethod) => api.post<Payment>('/payments', { reservationId, method }),
  simulateSuccess: (paymentId: number) => api.post<Payment>(`/payments/${paymentId}/simulate-success`),
}
