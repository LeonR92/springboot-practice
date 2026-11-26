import { useState } from 'react'
import { isBefore, parseISO, startOfDay } from 'date-fns'
import { CalendarIcon } from 'lucide-react'
import type { DateRange } from 'react-day-picker'
import { Button } from '@/components/ui/button'
import { Calendar } from '@/components/ui/calendar'
import { Popover, PopoverContent, PopoverTrigger } from '@/components/ui/popover'
import { formatDayMonth, formatNights } from '@/lib/format'
import { nightsBetween, toIso } from '@/lib/stay'

interface Props {
  checkIn: string
  checkOut: string
  onChange: (range: { checkIn: string; checkOut: string }) => void
}

export function DateRangePicker({ checkIn, checkOut, onChange }: Props) {
  const [open, setOpen] = useState(false)
  const [today] = useState(() => startOfDay(new Date()))
  const [draft, setDraft] = useState<DateRange>()
  const [choosingEnd, setChoosingEnd] = useState(false)

  const committed: DateRange = { from: parseISO(checkIn), to: parseISO(checkOut) }
  const nights = nightsBetween(checkIn, checkOut)

  const handleOpenChange = (next: boolean) => {
    setOpen(next)
    setChoosingEnd(false)
    setDraft(committed)
  }

  const handleDayClick = (day: Date) => {
    const start = draft?.from
    if (!choosingEnd || !start || !isBefore(start, day)) {
      setDraft({ from: day })
      setChoosingEnd(true)
      return
    }
    onChange({ checkIn: toIso(start), checkOut: toIso(day) })
    setOpen(false)
  }

  return (
    <Popover open={open} onOpenChange={handleOpenChange}>
      <PopoverTrigger
        render={<Button variant="outline" aria-label="Select dates" className="h-14 w-full justify-start gap-3 px-4 text-left font-normal" />}
      >
        <CalendarIcon className="size-4 shrink-0 text-muted-foreground" />
        <span className="grid flex-1 grid-cols-2 gap-4">
          <Segment label="Check-in" value={formatDayMonth(checkIn)} />
          <Segment label="Check-out" value={formatDayMonth(checkOut)} />
        </span>
        <span className="hidden text-xs text-muted-foreground sm:inline">{formatNights(nights)}</span>
      </PopoverTrigger>
      <PopoverContent className="w-auto p-0" align="start">
        <p className="border-b px-4 py-3 text-sm font-medium" aria-live="polite">
          {choosingEnd ? 'Now choose your check-out date' : 'Choose your check-in date'}
        </p>
        <Calendar
          mode="range"
          numberOfMonths={2}
          selected={draft}
          onDayClick={handleDayClick}
          disabled={{ before: today }}
          defaultMonth={committed.from}
        />
      </PopoverContent>
    </Popover>
  )
}

function Segment({ label, value }: { label: string; value: string }) {
  return (
    <span className="flex flex-col leading-tight">
      <span className="text-[10px] uppercase tracking-wider text-muted-foreground">{label}</span>
      <span className="text-sm">{value}</span>
    </span>
  )
}
