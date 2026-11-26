import { api } from '@/api/client'
import type { CreateReservationInput, Page, Reservation, ReservationStatus } from '@/api/types'

export type ReservationQuery = { page: number; size: number; sort: string }

export const reservationService = {
  create: (input: CreateReservationInput) => api.post<Reservation>('/reservations', input),
  mine: () => api.get<Page<Reservation>>('/reservations/me', { query: { size: 50, sort: 'checkIn,desc' } }),
  all: (query: ReservationQuery) => api.get<Page<Reservation>>('/reservations', { query }),
  setStatus: (reservation: Reservation, status: ReservationStatus) =>
    api.put<Reservation>(`/reservations/${reservation.id}`, {
      roomId: reservation.roomId,
      guestName: reservation.guestName,
      guestEmail: reservation.guestEmail,
      checkIn: reservation.checkIn,
      checkOut: reservation.checkOut,
      couponCode: reservation.couponCode ?? undefined,
      status,
    }),
  remove: (id: number) => api.delete(`/reservations/${id}`),
}
