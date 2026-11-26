import { useState } from 'react'
import { Search } from 'lucide-react'
import { Button } from '@/components/ui/button'
import { isValidStay, type Stay } from '@/lib/stay'
import { DateRangePicker } from './DateRangePicker'
import { GuestPicker } from './GuestPicker'

interface Props {
  initial: Stay
  onSubmit: (stay: Stay) => void
  className?: string
}

export function StayForm({ initial, onSubmit, className }: Props) {
  const [stay, setStay] = useState(initial)

  return (
    <form
      className={className}
      onSubmit={(event) => {
        event.preventDefault()
        if (isValidStay(stay)) onSubmit(stay)
      }}
    >
      <div className="grid gap-3 md:grid-cols-[2fr_1fr_auto]">
        <DateRangePicker checkIn={stay.checkIn} checkOut={stay.checkOut} onChange={(range) => setStay({ ...stay, ...range })} />
        <GuestPicker value={stay.guests} onChange={(guests) => setStay({ ...stay, guests })} />
        <Button type="submit" size="lg" className="h-14 gap-2 px-8">
          <Search className="size-4" />
          Find hotels
        </Button>
      </div>
    </form>
  )
}
