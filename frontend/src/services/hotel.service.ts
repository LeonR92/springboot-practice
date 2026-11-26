import { api } from '@/api/client'
import type { Amenity, Hotel, Page, Room } from '@/api/types'

export interface AvailabilityQuery {
  hotelId: number
  checkIn: string
  checkOut: string
  roomTypeId?: number
}

export const hotelService = {
  list: () => api.get<Hotel[]>('/hotels'),
  get: (id: number) => api.get<Hotel>(`/hotels/${id}`),
  remove: (id: number) => api.delete(`/hotels/${id}`),
  rooms: (hotelId: number) => api.get<Room[]>(`/hotels/${hotelId}/rooms`),
  room: (hotelId: number, roomId: number) => api.get<Room>(`/hotels/${hotelId}/rooms/${roomId}`),
  availability: ({ hotelId, ...query }: AvailabilityQuery) =>
    api.get<Page<Room>>(`/hotels/${hotelId}/availability`, { query: { ...query, size: 100 } }),
  amenities: () => api.get<Amenity[]>('/amenities'),
}
