import { keepPreviousData, useQuery } from '@tanstack/react-query'
import type { Reservation, ReservationStatus } from '@/api/types'
import { hotelService } from '@/services/hotel.service'
import { reservationService, type ReservationQuery } from '@/services/reservation.service'
import { userService } from '@/services/user.service'
import { queryKeys } from './queryKeys'
import { useInvalidatingMutation } from './useInvalidatingMutation'

export const useAllReservations = (query: ReservationQuery) =>
  useQuery({
    queryKey: [...queryKeys.reservations, 'all', query],
    queryFn: () => reservationService.all(query),
    placeholderData: keepPreviousData,
  })

export const useSetReservationStatus = () =>
  useInvalidatingMutation(
    ({ reservation, status }: { reservation: Reservation; status: ReservationStatus }) => reservationService.setStatus(reservation, status),
    queryKeys.reservations,
  )

export const useDeleteReservation = () => useInvalidatingMutation(reservationService.remove, queryKeys.reservations, queryKeys.availability)

export const useDeleteHotel = () => useInvalidatingMutation(hotelService.remove, queryKeys.hotels, queryKeys.availability)

export const useUsers = () => useQuery({ queryKey: queryKeys.users, queryFn: userService.list })
