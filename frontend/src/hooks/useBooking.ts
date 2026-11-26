import { useQuery } from '@tanstack/react-query'
import type { PaymentMethod } from '@/api/types'
import { config } from '@/config'
import { paymentService } from '@/services/payment.service'
import { reservationService } from '@/services/reservation.service'
import { queryKeys } from './queryKeys'
import { useInvalidatingMutation } from './useInvalidatingMutation'

export const useCreateReservation = () => useInvalidatingMutation(reservationService.create, queryKeys.availability, queryKeys.reservations)

export const usePaymentFlow = (reservationId: number) =>
  useInvalidatingMutation(async (method: PaymentMethod) => {
    const payment = await paymentService.pay(reservationId, method)
    if (config.fakePayments && method !== 'INVOICE') await paymentService.simulateSuccess(payment.id)
    return payment
  }, queryKeys.reservations)

const mineKey = [...queryKeys.reservations, 'mine']
const fetchMine = async () => (await reservationService.mine()).content

export const useMyReservations = () => useQuery({ queryKey: mineKey, queryFn: fetchMine })

export const useMyReservation = (id: number, poll: boolean) =>
  useQuery({
    queryKey: mineKey,
    queryFn: fetchMine,
    select: (rows) => rows.find((r) => r.id === id),
    refetchInterval: (query) => (poll && query.state.data?.find((r) => r.id === id)?.status === 'PENDING' ? 3000 : false),
  })
