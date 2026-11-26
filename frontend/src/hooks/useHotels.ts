import { queryOptions, useQueries, useQuery } from '@tanstack/react-query'
import type { HotelOffer } from '@/api/types'
import { isValidStay, type Stay } from '@/lib/stay'
import { hotelService } from '@/services/hotel.service'
import { queryKeys } from './queryKeys'

const availabilityOptions = (hotelId: number, stay: Stay) =>
  queryOptions({
    queryKey: [...queryKeys.availability, hotelId, stay.checkIn, stay.checkOut],
    queryFn: async () => (await hotelService.availability({ hotelId, checkIn: stay.checkIn, checkOut: stay.checkOut })).content,
    enabled: isValidStay(stay),
  })

export const useHotels = () => useQuery({ queryKey: queryKeys.hotels, queryFn: hotelService.list })

export const useHotel = (id: number) => useQuery({ queryKey: [...queryKeys.hotels, id], queryFn: () => hotelService.get(id) })

export const useAvailableRooms = (hotelId: number, stay: Stay) => useQuery(availabilityOptions(hotelId, stay))

export const useRoom = (hotelId: number, roomId: number) =>
  useQuery({ queryKey: ['rooms', hotelId, roomId], queryFn: () => hotelService.room(hotelId, roomId) })

export function useHotelOffers(stay: Stay) {
  const hotels = useHotels()
  const availability = useQueries({ queries: (hotels.data ?? []).map((hotel) => availabilityOptions(hotel.id, stay)) })

  const loading = hotels.isPending || availability.some((q) => q.isPending && q.fetchStatus !== 'idle')
  const offers: HotelOffer[] = (hotels.data ?? []).map((hotel, i) => {
    const rooms = (availability[i]?.data ?? []).filter((room) => room.roomType.capacity >= stay.guests)
    const prices = rooms.map((room) => room.roomType.basePrice)
    return { hotel, rooms, fromPrice: prices.length ? Math.min(...prices) : null }
  })

  return { offers, loading, error: hotels.error ?? availability.find((q) => q.error)?.error ?? null }
}
