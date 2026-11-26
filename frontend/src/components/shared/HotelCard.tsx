import { Link } from 'react-router-dom'
import { MapPin } from 'lucide-react'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import { formatPrice } from '@/lib/format'
import { hotelPhoto } from '@/lib/images'
import type { Hotel } from '@/api/types'

interface Props {
  hotel: Hotel
  fromPrice: number | null
  roomsLeft?: number
  search: string
}

export function HotelCard({ hotel, fromPrice, roomsLeft, search }: Props) {
  return (
    <article className="grid overflow-hidden rounded-md border bg-card md:grid-cols-[320px_1fr]">
      <img src={hotelPhoto(hotel.id, 700)} alt={hotel.name} loading="lazy" className="h-56 w-full object-cover md:h-full" />
      <div className="flex flex-col gap-4 p-6">
        <div>
          <h2 className="text-2xl">{hotel.name}</h2>
          <p className="mt-1 flex items-center gap-1 text-sm text-muted-foreground">
            <MapPin className="size-3.5" />
            {[hotel.address.street, hotel.address.city, hotel.address.country].filter(Boolean).join(', ')}
          </p>
        </div>
        <div className="flex flex-wrap gap-2">
          {hotel.amenities.slice(0, 5).map((amenity) => (
            <Badge key={amenity.id} variant="secondary">
              {amenity.name}
            </Badge>
          ))}
        </div>
        <div className="mt-auto flex items-end justify-between gap-4">
          <div>
            {fromPrice !== null ? (
              <>
                <p className="text-xs uppercase tracking-wider text-muted-foreground">From</p>
                <p className="text-2xl font-heading">
                  {formatPrice(fromPrice)} <span className="font-sans text-sm text-muted-foreground">/ night</span>
                </p>
                {roomsLeft !== undefined && roomsLeft <= 3 && <p className="text-xs text-primary">Only {roomsLeft} rooms left</p>}
              </>
            ) : (
              <p className="text-sm text-muted-foreground">No rooms for these dates</p>
            )}
          </div>
          <Button render={<Link to={`/hotels/${hotel.id}?${search}`} />}>View rooms</Button>
        </div>
      </div>
    </article>
  )
}
