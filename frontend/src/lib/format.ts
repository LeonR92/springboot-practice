import { format, parseISO } from 'date-fns'

const money = new Intl.NumberFormat('en-GB', { style: 'currency', currency: 'EUR', maximumFractionDigits: 0 })
const moneyExact = new Intl.NumberFormat('en-GB', { style: 'currency', currency: 'EUR' })

export const formatPrice = (value: number) => money.format(value)
export const formatPriceExact = (value: number) => moneyExact.format(value)
export const formatDate = (iso: string) => format(parseISO(iso), 'EEE d MMM yyyy')
export const formatDayMonth = (iso: string) => format(parseISO(iso), 'EEE d MMM')
export const formatTime = (time: string) => time.slice(0, 5)
export const formatNights = (nights: number) => `${nights} ${nights === 1 ? 'night' : 'nights'}`
export const formatShortDate = (iso: string) => format(parseISO(iso), 'd MMM')
