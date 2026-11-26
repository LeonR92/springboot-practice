import { useSearchParams } from 'react-router-dom'
import { Input } from '@/components/ui/input'
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select'
import { LoadingList, Notice, errorMessage } from '@/components/shared/PageState'
import { HotelCard } from '@/components/shared/HotelCard'
import { Pagination } from '@/components/shared/Pagination'
import { StayForm } from '@/components/shared/StayForm'
import { useHotelOffers } from '@/hooks/useHotels'
import { filterAndSort, isSortKey, paginate, sortLabels, type SortKey } from '@/lib/catalog'
import { parseStay, stayToParams } from '@/lib/stay'

const PAGE_SIZE = 6

export default function HotelsPage() {
  const [params, setParams] = useSearchParams()
  const stay = parseStay(params)
  const query = params.get('q') ?? ''
  const sort: SortKey = isSortKey(params.get('sort')) ? (params.get('sort') as SortKey) : 'recommended'
  const { offers, loading, error } = useHotelOffers(stay)

  const update = (changes: Record<string, string | null>, resetPage = true) => {
    const next = new URLSearchParams(params)
    Object.entries(changes).forEach(([key, value]) => (value ? next.set(key, value) : next.delete(key)))
    if (resetPage) next.delete('page')
    setParams(next, { replace: 'q' in changes })
  }

  const available = offers.filter((offer) => offer.rooms.length > 0)
  const visible = filterAndSort(available, query, sort)
  const { items, page, totalPages } = paginate(visible, Number(params.get('page') ?? 1) || 1, PAGE_SIZE)
  const search = stayToParams(stay).toString()

  return (
    <div className="mx-auto max-w-6xl px-4 py-10 sm:px-6">
      <h1 className="text-4xl">Find your hotel</h1>
      <StayForm
        key={`${stay.checkIn}-${stay.checkOut}-${stay.guests}`}
        className="mt-6"
        initial={stay}
        onSubmit={(next) => update(Object.fromEntries(stayToParams(next)), false)}
      />

      <div className="mt-8 flex flex-wrap items-center justify-between gap-3 border-t pt-6">
        <p className="text-sm text-muted-foreground" aria-live="polite">
          {loading ? 'Checking availability…' : `${visible.length} ${visible.length === 1 ? 'hotel' : 'hotels'} available`}
        </p>
        <div className="flex w-full flex-wrap gap-3 sm:w-auto">
          <Input
            aria-label="Search by name or destination"
            placeholder="Search name or destination"
            className="h-10 w-full sm:w-64"
            defaultValue={query}
            onChange={(event) => update({ q: event.target.value.trim() || null })}
          />
          <Select value={sort} onValueChange={(value) => update({ sort: value === 'recommended' ? null : value })}>
            <SelectTrigger aria-label="Sort hotels" className="h-10 w-full min-w-56 sm:w-56">
              <SelectValue>{sortLabels[sort]}</SelectValue>
            </SelectTrigger>
            <SelectContent className="min-w-56">
              {(Object.keys(sortLabels) as SortKey[]).map((key) => (
                <SelectItem key={key} value={key}>
                  {sortLabels[key]}
                </SelectItem>
              ))}
            </SelectContent>
          </Select>
        </div>
      </div>

      <div className="mt-6 space-y-6">
        {error ? (
          <Notice title="We could not load hotels" tone="error">
            {errorMessage(error)}
          </Notice>
        ) : loading ? (
          <LoadingList />
        ) : visible.length === 0 ? (
          <Notice title={available.length === 0 ? 'No rooms available' : 'No hotels match your search'}>
            {available.length === 0 ? 'Try different dates or fewer guests.' : 'Try a different name or destination.'}
          </Notice>
        ) : (
          <>
            {items.map(({ hotel, rooms, fromPrice }) => (
              <HotelCard key={hotel.id} hotel={hotel} fromPrice={fromPrice} roomsLeft={rooms.length} search={search} />
            ))}
            <Pagination page={page} totalPages={totalPages} onChange={(next) => update({ page: next > 1 ? String(next) : null }, false)} />
          </>
        )}
      </div>
    </div>
  )
}
