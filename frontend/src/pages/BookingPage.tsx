import { zodResolver } from '@hookform/resolvers/zod'
import { useForm } from 'react-hook-form'
import { useNavigate, useParams, useSearchParams } from 'react-router-dom'
import { z } from 'zod'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Field } from '@/components/shared/Field'
import { LoadingList, Notice, errorMessage } from '@/components/shared/PageState'
import { useAuth } from '@/hooks/useAuth'
import { useCreateReservation } from '@/hooks/useBooking'
import { useHotel, useRoom } from '@/hooks/useHotels'
import { formatDate, formatNights, formatPrice } from '@/lib/format'
import { hotelPhoto } from '@/lib/images'
import { nightsBetween, parseStay } from '@/lib/stay'

const schema = z.object({
  guestName: z.string().min(1, 'Enter the guest name'),
  guestEmail: z.email('Enter a valid email'),
  couponCode: z.string().optional(),
})
type Values = z.infer<typeof schema>

export default function BookingPage() {
  const roomId = Number(useParams().roomId)
  const [params] = useSearchParams()
  const hotelId = Number(params.get('hotelId'))
  const stay = parseStay(params)
  const navigate = useNavigate()
  const room = useRoom(hotelId, roomId)
  const hotel = useHotel(hotelId)
  const create = useCreateReservation()
  const { session } = useAuth()
  const form = useForm<Values>({
    resolver: zodResolver(schema),
    defaultValues: { guestName: session?.name ?? '', guestEmail: session?.email ?? '' },
  })
  const { errors } = form.formState

  if (room.isError || hotel.isError) {
    return (
      <div className="mx-auto max-w-3xl px-6 py-16">
        <Notice title="We could not load this room" tone="error">
          {errorMessage(room.error ?? hotel.error)}
        </Notice>
      </div>
    )
  }
  if (!room.data || !hotel.data) return <div className="mx-auto max-w-3xl px-6 py-10"><LoadingList rows={1} /></div>

  const nights = nightsBetween(stay.checkIn, stay.checkOut)
  const type = room.data.roomType

  const onSubmit = form.handleSubmit((values) =>
    create.mutate(
      {
        roomId,
        guestName: values.guestName,
        guestEmail: values.guestEmail,
        checkIn: stay.checkIn,
        checkOut: stay.checkOut,
        couponCode: values.couponCode?.trim() || undefined,
      },
      { onSuccess: (reservation) => navigate(`/booking/${reservation.id}`, { replace: true }) },
    ),
  )

  return (
    <div className="mx-auto grid max-w-5xl gap-10 px-6 py-10 md:grid-cols-[1.4fr_1fr]">
      <div>
        <h1 className="text-4xl">Guest details</h1>
        <form onSubmit={onSubmit} className="mt-8 space-y-5" noValidate>
          <Field label="Guest name" error={errors.guestName?.message}>
            <Input id="guestName" autoComplete="name" {...form.register('guestName')} />
          </Field>
          <Field label="Guest email" error={errors.guestEmail?.message}>
            <Input id="guestEmail" type="email" autoComplete="email" {...form.register('guestEmail')} />
          </Field>
          <Field label="Coupon code (optional)">
            <Input id="couponCode" {...form.register('couponCode')} />
          </Field>
          <Button type="submit" size="lg" disabled={create.isPending}>
            {create.isPending ? 'Reserving…' : 'Continue to payment'}
          </Button>
          <p className="text-xs text-muted-foreground">Your room is held for 15 minutes once you continue.</p>
        </form>
      </div>

      <aside className="h-fit overflow-hidden rounded-md border bg-card">
        <img src={hotelPhoto(hotelId, 600)} alt="" className="h-40 w-full object-cover" />
        <div className="space-y-3 p-6">
          <h2 className="text-2xl">{hotel.data.name}</h2>
          <p className="text-sm text-muted-foreground">
            {type.name} · Room {room.data.roomNumber}
          </p>
          <div className="space-y-1 border-t pt-3 text-sm">
            <p>{formatDate(stay.checkIn)} → {formatDate(stay.checkOut)}</p>
            <p className="text-muted-foreground">{formatNights(nights)}</p>
          </div>
          <div className="flex items-baseline justify-between border-t pt-3">
            <span className="text-sm text-muted-foreground">Estimated total</span>
            <span className="font-heading text-2xl">{formatPrice(type.basePrice * nights)}</span>
          </div>
          <p className="text-xs text-muted-foreground">Final price is confirmed after you continue.</p>
        </div>
      </aside>
    </div>
  )
}
