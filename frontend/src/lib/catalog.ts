import type { HotelOffer } from '@/api/types'

export type SortKey = 'recommended' | 'price-asc' | 'price-desc' | 'name'

export const sortLabels: Record<SortKey, string> = {
  recommended: 'Recommended',
  'price-asc': 'Price: low to high',
  'price-desc': 'Price: high to low',
  name: 'Name: A to Z',
}

export const isSortKey = (value: string | null): value is SortKey => value !== null && value in sortLabels

export function filterAndSort(offers: HotelOffer[], query: string, sort: SortKey): HotelOffer[] {
  const needle = query.trim().toLowerCase()
  const matches = needle
    ? offers.filter(({ hotel }) => [hotel.name, hotel.address.city, hotel.address.country].some((part) => part.toLowerCase().includes(needle)))
    : offers

  const price = (offer: HotelOffer) => offer.fromPrice ?? Number.POSITIVE_INFINITY
  const sorters: Record<SortKey, (a: HotelOffer, b: HotelOffer) => number> = {
    recommended: (a, b) => b.hotel.amenities.length - a.hotel.amenities.length || a.hotel.name.localeCompare(b.hotel.name),
    'price-asc': (a, b) => price(a) - price(b) || a.hotel.name.localeCompare(b.hotel.name),
    'price-desc': (a, b) => (b.fromPrice ?? -1) - (a.fromPrice ?? -1) || a.hotel.name.localeCompare(b.hotel.name),
    name: (a, b) => a.hotel.name.localeCompare(b.hotel.name),
  }
  return [...matches].sort(sorters[sort])
}

export function paginate<T>(items: T[], page: number, size: number) {
  const totalPages = Math.max(1, Math.ceil(items.length / size))
  const current = Math.min(Math.max(page, 1), totalPages)
  return { items: items.slice((current - 1) * size, current * size), page: current, totalPages }
}
