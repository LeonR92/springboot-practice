import { Link, useNavigate, useParams, useSearchParams } from 'react-router-dom'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import { DateRangePicker } from '@/components/shared/DateRangePicker'
import { GuestPicker } from '@/components/shared/GuestPicker'
import { LoadingList, Notice, errorMessage } from '@/components/shared/PageState'
import { useAvailableRooms, useHotel } from '@/hooks/useHotels'
import { formatNights, formatPrice, formatTime } from '@/lib/format'
import { hotelPhoto } from '@/lib/images'
import { nightsBetween, parseStay, stayToParams } from '@/lib/stay'

export default function HotelDetailPage() {
  const id = Number(useParams().id)
  const [params, setParams] = useSearchParams()
  const navigate = useNavigate()
  const stay = parseStay(params)
  const hotel = useHotel(id)
  const rooms = useAvailableRooms(id, stay)
  const nights = nightsBetween(stay.checkIn, stay.checkOut)

  if (hotel.isError) {
    return (
      <div className="mx-auto max-w-6xl px-6 py-16">
        <Notice title="Hotel not found" tone="error">
          {errorMessage(hotel.error)}
        </Notice>
      </div>
    )
  }
  if (!hotel.data) return <div className="mx-auto max-w-6xl px-6 py-10"><LoadingList rows={2} /></div>

  const { address, amenities } = hotel.data
  const fitting = (rooms.data ?? []).filter((room) => room.roomType.capacity >= stay.guests)
  const byType = Object.values(Object.groupBy(fitting, (room) => room.roomType.id))

  return (
    <div>
      <div className="grid h-[260px] grid-cols-1 gap-1 sm:h-[360px] sm:grid-cols-3">
        {[0, 1, 2].map((offset) => (
          <div key={offset} className={`relative overflow-hidden ${offset > 0 ? 'hidden sm:block' : ''}`}>
            <img
              src={hotelPhoto(id, 900, offset)}
              alt={offset === 0 ? hotel.data.name : ''}
              className="absolute inset-0 size-full object-cover"
            />
          </div>
        ))}
      </div>

      <div className="mx-auto max-w-6xl px-4 py-10 sm:px-6">
        <p className="text-xs uppercase tracking-[0.2em] text-primary">
          {address.city}, {address.country}
        </p>
        <h1 className="mt-2 text-5xl">{hotel.data.name}</h1>
        <p className="mt-3 text-sm text-muted-foreground">
          Check-in from {formatTime(hotel.data.checkInTime)} · Check-out until {formatTime(hotel.data.checkOutTime)}
        </p>
        <div className="mt-5 flex flex-wrap gap-2">
          {amenities.map((a) => (
            <Badge key={a.id} variant="secondary">
              {a.name}
            </Badge>
          ))}
        </div>

        <div className="mt-12 flex flex-wrap items-center justify-between gap-4 border-y py-4">
          <h2 className="text-3xl">Choose your room</h2>
          <div className="grid w-full gap-3 sm:w-auto sm:grid-cols-[22rem_11rem]">
            <DateRangePicker
              checkIn={stay.checkIn}
              checkOut={stay.checkOut}
              onChange={(range) => setParams(stayToParams({ ...stay, ...range }))}
            />
            <GuestPicker value={stay.guests} onChange={(guests) => setParams(stayToParams({ ...stay, guests }))} />
          </div>
        </div>

        <div className="mt-6 space-y-4">
          {rooms.isPending ? (
            <LoadingList rows={2} />
          ) : rooms.isError ? (
            <Notice title="Could not check availability" tone="error">
              {errorMessage(rooms.error)}
            </Notice>
          ) : byType.length === 0 ? (
            <Notice title="No rooms for this stay">Try other dates or fewer guests.</Notice>
          ) : (
            byType.map((group) => {
              const room = group![0]
              const type = room.roomType
              return (
                <article key={type.id} className="flex flex-wrap items-center justify-between gap-6 rounded-md border bg-card p-6">
                  <div className="max-w-xl">
                    <p className="text-xs uppercase tracking-widest text-muted-foreground">{type.tier}</p>
                    <h3 className="text-2xl">{type.name}</h3>
                    <p className="mt-1 text-sm text-muted-foreground">
                      Sleeps {type.capacity} · {group!.length} available
                    </p>
                    {type.description && <p className="mt-2 text-sm">{type.description}</p>}
                  </div>
                  <div className="text-right">
                    <p className="font-heading text-3xl">{formatPrice(type.basePrice)}</p>
                    <p className="text-xs text-muted-foreground">per night · {formatPrice(type.basePrice * nights)} for {formatNights(nights)}</p>
                    <Button
                      className="mt-3"
                      onClick={() => navigate(`/book/${room.id}?hotelId=${id}&${stayToParams(stay)}`)}
                    >
                      Select
                    </Button>
                  </div>
                </article>
              )
            })
          )}
        </div>
        <p className="mt-6 text-xs text-muted-foreground">
          Prices shown are base rates; weekend and occupancy adjustments and coupons are applied at booking. <Link to="/hotels" className="underline">Back to all hotels</Link>
        </p>
      </div>
    </div>
  )
}
