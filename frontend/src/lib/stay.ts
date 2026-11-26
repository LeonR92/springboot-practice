import { addDays, differenceInCalendarDays, format, parseISO } from 'date-fns'

export const MAX_GUESTS = 4

export const toIso = (date: Date) => format(date, 'yyyy-MM-dd')

export interface Stay {
  checkIn: string
  checkOut: string
  guests: number
}

export function defaultStay(): Stay {
  const today = new Date()
  return { checkIn: toIso(addDays(today, 7)), checkOut: toIso(addDays(today, 9)), guests: 2 }
}

export function parseStay(params: URLSearchParams): Stay {
  const fallback = defaultStay()
  const checkIn = params.get('checkIn') ?? fallback.checkIn
  const checkOut = params.get('checkOut') ?? fallback.checkOut
  const guests = Number(params.get('guests') ?? fallback.guests)
  return { checkIn, checkOut, guests: Number.isInteger(guests) && guests > 0 ? Math.min(guests, MAX_GUESTS) : fallback.guests }
}

export const stayToParams = (stay: Stay) =>
  new URLSearchParams({ checkIn: stay.checkIn, checkOut: stay.checkOut, guests: String(stay.guests) })

export const nightsBetween = (checkIn: string, checkOut: string) => differenceInCalendarDays(parseISO(checkOut), parseISO(checkIn))

export const isValidStay = (stay: Stay) => nightsBetween(stay.checkIn, stay.checkOut) > 0
