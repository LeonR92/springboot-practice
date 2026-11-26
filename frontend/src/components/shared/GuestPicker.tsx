import { Minus, Plus, Users } from 'lucide-react'
import { Button } from '@/components/ui/button'
import { Popover, PopoverContent, PopoverTrigger } from '@/components/ui/popover'
import { MAX_GUESTS } from '@/lib/stay'

interface Props {
  value: number
  onChange: (guests: number) => void
}

export function GuestPicker({ value, onChange }: Props) {
  return (
    <Popover>
      <PopoverTrigger render={<Button variant="outline" aria-label="Guests" className="h-14 w-full justify-start gap-3 px-4 text-left font-normal" />}>
        <Users className="size-4 shrink-0 text-muted-foreground" />
        <span className="flex flex-col leading-tight">
          <span className="text-[10px] uppercase tracking-wider text-muted-foreground">Guests</span>
          <span className="text-sm">{value === 1 ? '1 guest' : `${value} guests`}</span>
        </span>
      </PopoverTrigger>
      <PopoverContent className="w-72 p-4" align="end">
        <div className="flex items-center justify-between">
          <div>
            <p className="font-medium">Guests</p>
            <p className="text-xs text-muted-foreground">Rooms sleep up to {MAX_GUESTS}</p>
          </div>
          <div className="flex items-center gap-3">
            <Button type="button" variant="outline" size="icon" aria-label="Decrease guests" disabled={value <= 1} onClick={() => onChange(value - 1)}>
              <Minus className="size-4" />
            </Button>
            <span className="w-4 text-center tabular-nums" aria-live="polite">{value}</span>
            <Button type="button" variant="outline" size="icon" aria-label="Increase guests" disabled={value >= MAX_GUESTS} onClick={() => onChange(value + 1)}>
              <Plus className="size-4" />
            </Button>
          </div>
        </div>
      </PopoverContent>
    </Popover>
  )
}
