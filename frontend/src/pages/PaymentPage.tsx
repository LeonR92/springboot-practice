import { useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import type { PaymentMethod } from '@/api/types'
import { Button } from '@/components/ui/button'
import { CardForm } from '@/components/shared/CardForm'
import { LoadingList, Notice } from '@/components/shared/PageState'
import { useAuth } from '@/hooks/useAuth'
import { useMyReservation, usePaymentFlow } from '@/hooks/useBooking'
import { formatDate, formatPriceExact } from '@/lib/format'

const methods: { value: PaymentMethod; label: string; hint: string }[] = [
  { value: 'STRIPE', label: 'Card', hint: 'Pay securely by card' },
  { value: 'PAYPAL', label: 'PayPal', hint: 'Use your PayPal account' },
  { value: 'INVOICE', label: 'Invoice', hint: 'We email an invoice you pay later' },
]

export default function PaymentPage() {
  const id = Number(useParams().id)
  const [method, setMethod] = useState<PaymentMethod>('STRIPE')
  const flow = usePaymentFlow(id)
  const payment = flow.data
  const reservation = useMyReservation(id, flow.isSuccess)
  const { session } = useAuth()

  if (reservation.isPending) return <div className="mx-auto max-w-2xl px-6 py-10"><LoadingList rows={1} /></div>
  if (!reservation.data) {
    return (
      <div className="mx-auto max-w-2xl px-6 py-16">
        <Notice title="Reservation not found">
          <Link to="/trips" className="underline">Go to my trips</Link>
        </Notice>
      </div>
    )
  }

  const { status, totalPrice } = reservation.data

  const amount = formatPriceExact(totalPrice)
  const label = flow.isPending ? 'Processing…' : method === 'PAYPAL' ? `Continue with PayPal · ${amount}` : `Pay ${amount}`
  const submit = () => flow.mutate(method)

  return (
    <div className="mx-auto max-w-2xl px-6 py-10">
      <h1 className="text-4xl">
        {status === 'CONFIRMED' ? 'Booking confirmed' : status === 'EXPIRED' || status === 'CANCELLED' ? 'Reservation closed' : 'Payment'}
      </h1>
      <div className="mt-6 flex justify-between border-y py-4 text-sm">
        <span>
          {formatDate(reservation.data.checkIn)} → {formatDate(reservation.data.checkOut)}
        </span>
        <span className="font-heading text-xl">{formatPriceExact(totalPrice)}</span>
      </div>

      {status === 'CONFIRMED' && (
        <Notice title="You're all set">
          A confirmation has been sent to {reservation.data.guestEmail}. <Link to="/trips" className="underline">View my trips</Link>
        </Notice>
      )}
      {(status === 'EXPIRED' || status === 'CANCELLED') && (
        <div className="mt-6">
          <Notice title="This reservation is no longer active">The hold expired or the booking was cancelled.</Notice>
          <Button className="mt-4" render={<Link to="/hotels" />}>Search again</Button>
        </div>
      )}

      {status === 'PENDING' && !payment && (
        <div className="mt-6 space-y-3">
          {methods.map((m) => (
            <label
              key={m.value}
              className={`flex cursor-pointer items-center gap-4 rounded-md border p-4 ${method === m.value ? 'border-primary bg-accent' : ''}`}
            >
              <input type="radio" name="method" className="accent-[var(--primary)]" checked={method === m.value} onChange={() => setMethod(m.value)} />
              <span>
                <span className="block font-medium">{m.label}</span>
                <span className="block text-sm text-muted-foreground">{m.hint}</span>
              </span>
            </label>
          ))}
          <div className="pt-3">
            {method === 'STRIPE' ? (
              <CardForm holder={session?.name ?? ''} label={label} pending={flow.isPending} onSubmit={submit} />
            ) : (
              <Button size="lg" onClick={submit} disabled={flow.isPending}>
                {label}
              </Button>
            )}
          </div>
        </div>
      )}

      {status === 'PENDING' && payment && (
        <div className="mt-6">
          <Notice title={payment.method === 'INVOICE' ? 'Invoice requested' : 'Waiting for payment confirmation'}>
            {payment.method === 'INVOICE'
              ? 'We will confirm your booking once the invoice is settled.'
              : 'This page updates automatically as soon as your payment provider confirms.'}
          </Notice>
        </div>
      )}
    </div>
  )
}
