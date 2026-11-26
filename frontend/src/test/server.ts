import { http, HttpResponse } from 'msw'
import { setupServer } from 'msw/node'
import type { Hotel, Reservation, Room } from '@/api/types'

export const API = 'http://localhost/api/v1'

export const hotel: Hotel = {
  id: 1,
  name: 'Grand Test Hotel',
  address: { country: 'Germany', city: 'Berlin', street: 'Unter den Linden 1' },
  checkInTime: '15:00:00',
  checkOutTime: '10:00:00',
  amenities: [{ id: 1, name: 'Free WiFi', category: 'GENERAL', isFree: true }],
}

export const room: Room = {
  id: 10,
  hotelId: 1,
  roomNumber: '101',
  status: 'ACTIVE',
  roomType: { id: 1, name: 'Deluxe King', tier: 'DELUXE', basePrice: 200, capacity: 3 },
}

export const reservation: Reservation = {
  id: 77,
  roomId: 10,
  hotelId: 1,
  roomNumber: '101',
  guestName: 'Ada Lovelace',
  guestEmail: 'ada@example.com',
  checkIn: '2030-05-10',
  checkOut: '2030-05-12',
  status: 'PENDING',
  totalPrice: 400,
  userId: 5,
}

export function tokenFor(role: 'USER' | 'ADMIN', userId = 5) {
  const encode = (value: object) => btoa(JSON.stringify(value))
  const exp = Math.floor(Date.now() / 1000) + 600
  return `${encode({ alg: 'HS256' })}.${encode({ sub: String(userId), role, email: 'ada@example.com', name: 'Ada Lovelace', exp })}.sig`
}

export const pageOf = <T,>(content: T[], extra: Partial<{ number: number; totalPages: number; totalElements: number }> = {}) => ({
  content,
  number: 0,
  size: content.length,
  totalPages: 1,
  totalElements: content.length,
  ...extra,
})

export const handlers = [
  http.get(`${API}/hotels`, () => HttpResponse.json([hotel])),
  http.get(`${API}/hotels/:id`, () => HttpResponse.json(hotel)),
  http.get(`${API}/hotels/:id/availability`, () => HttpResponse.json(pageOf([room]))),
  http.get(`${API}/hotels/:hotelId/rooms/:roomId`, () => HttpResponse.json(room)),
]

export const server = setupServer(...handlers)
