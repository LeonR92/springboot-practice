import { Link } from 'react-router-dom'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import { LoadingList, Notice, errorMessage } from '@/components/shared/PageState'
import { useMyReservations } from '@/hooks/useBooking'
import { useHotels } from '@/hooks/useHotels'
import { formatDate, formatPriceExact } from '@/lib/format'
import type { ReservationStatus } from '@/api/types'

const statusLabel: Record<ReservationStatus, string> = {
  PENDING: 'Awaiting payment',
  CONFIRMED: 'Confirmed',
  CANCELLED: 'Cancelled',
  EXPIRED: 'Expired',
}

export default function TripsPage() {
  const trips = useMyReservations()
  const hotels = useHotels()
  const hotelName = (id: number) => hotels.data?.find((h) => h.id === id)?.name ?? `Hotel #${id}`

  return (
    <div className="mx-auto max-w-4xl px-6 py-10">
      <h1 className="text-4xl">My trips</h1>
      <div className="mt-8 space-y-4">
        {trips.isPending ? (
          <LoadingList rows={2} />
        ) : trips.isError ? (
          <Notice title="Could not load your trips" tone="error">{errorMessage(trips.error)}</Notice>
        ) : trips.data.length === 0 ? (
          <Notice title="No trips yet">
            <Link to="/hotels" className="underline">Find a hotel</Link>
          </Notice>
        ) : (
          trips.data.map((trip) => (
            <article key={trip.id} className="flex flex-wrap items-center justify-between gap-4 rounded-md border bg-card p-6">
              <div>
                <p className="text-sm text-muted-foreground">{hotelName(trip.hotelId)} · Room {trip.roomNumber}</p>
                <h2 className="text-xl">{formatDate(trip.checkIn)} → {formatDate(trip.checkOut)}</h2>
                <p className="mt-1 text-sm">{trip.guestName} · {formatPriceExact(trip.totalPrice)}</p>
              </div>
              <div className="flex items-center gap-3">
                <Badge variant={trip.status === 'CONFIRMED' ? 'default' : 'secondary'}>{statusLabel[trip.status]}</Badge>
                {trip.status === 'PENDING' && <Button size="sm" render={<Link to={`/booking/${trip.id}`} />}>Pay now</Button>}
              </div>
            </article>
          ))
        )}
      </div>
    </div>
  )
}
