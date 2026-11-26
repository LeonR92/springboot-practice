import { zodResolver } from '@hookform/resolvers/zod'
import { useForm } from 'react-hook-form'
import { z } from 'zod'
import { Button } from '@/components/ui/button'
import { config } from '@/config'
import { Input } from '@/components/ui/input'
import { Field } from './Field'

const schema = z.object({
  holder: z.string().min(1, 'Enter the name on the card'),
  number: z.string().regex(/^\d{4}( ?\d{4}){3}$/, 'Enter a 16-digit card number'),
  expiry: z
    .string()
    .regex(/^(0[1-9]|1[0-2])\/\d{2}$/, 'Use MM/YY')
    .refine((value) => {
      const [month, year] = value.split('/').map(Number)
      return new Date(2000 + year, month) > new Date()
    }, 'This card has expired'),
  cvc: z.string().regex(/^\d{3,4}$/, '3 or 4 digits'),
})

const testCard = { number: '4242 4242 4242 4242', expiry: '12/34', cvc: '123' }
const emptyCard = { number: '', expiry: '', cvc: '' }

interface Props {
  holder: string
  label: string
  pending: boolean
  onSubmit: () => void
}

export function CardForm({ holder, label, pending, onSubmit }: Props) {
  const form = useForm({
    resolver: zodResolver(schema),
    defaultValues: { holder, ...(config.fakePayments ? testCard : emptyCard) },
  })
  const { errors } = form.formState

  return (
    <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-4" noValidate>
      {config.fakePayments && (
        <p className="rounded-md bg-accent px-3 py-2 text-xs text-muted-foreground">Test mode: no real charge is made and card details are never sent anywhere.</p>
      )}
      <Field label="Name on card" error={errors.holder?.message}>
        <Input id="card-holder" autoComplete="cc-name" {...form.register('holder')} />
      </Field>
      <Field label="Card number" error={errors.number?.message}>
        <Input id="card-number" inputMode="numeric" autoComplete="cc-number" {...form.register('number')} />
      </Field>
      <div className="grid grid-cols-2 gap-4">
        <Field label="Expiry" error={errors.expiry?.message}>
          <Input id="card-expiry" placeholder="MM/YY" autoComplete="cc-exp" {...form.register('expiry')} />
        </Field>
        <Field label="CVC" error={errors.cvc?.message}>
          <Input id="card-cvc" inputMode="numeric" autoComplete="cc-csc" {...form.register('cvc')} />
        </Field>
      </div>
      <Button type="submit" size="lg" disabled={pending}>
        {label}
      </Button>
    </form>
  )
}
