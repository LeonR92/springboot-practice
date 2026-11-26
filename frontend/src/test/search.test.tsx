import { screen, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { http, HttpResponse } from 'msw'
import type { Hotel } from '@/api/types'
import { API, hotel, pageOf, reservation, room, server } from './server'
import { renderApp } from './render'

const search = 'checkIn=2030-05-10&checkOut=2030-05-12&guests=2'

const manyHotels: Hotel[] = Array.from({ length: 8 }, (_, i) => ({
  ...hotel,
  id: i + 1,
  name: `Hotel ${String.fromCharCode(65 + i)}`,
  address: { ...hotel.address, city: i === 7 ? 'Lisbon' : 'Berlin' },
}))

function useManyHotels() {
  server.use(
    http.get(`${API}/hotels`, () => HttpResponse.json(manyHotels)),
    http.get(`${API}/hotels/:id/availability`, ({ params }) => {
      const price = 400 - Number(params.id) * 10
      return HttpResponse.json(pageOf([{ ...room, roomType: { ...room.roomType, basePrice: price } }]))
    }),
  )
}

const hotelNames = () => screen.getAllByRole('heading', { level: 2 }).map((h) => h.textContent)

describe('hotel search', () => {
  it('pages through results six at a time', async () => {
    const user = userEvent.setup()
    useManyHotels()
    renderApp(`/hotels?${search}`)

    expect(await screen.findByText('8 hotels available')).toBeInTheDocument()
    expect(hotelNames()).toHaveLength(6)

    await user.click(screen.getByRole('button', { name: /Next/ }))

    expect(await screen.findByText('Page 2 of 2')).toBeInTheDocument()
    expect(hotelNames()).toHaveLength(2)
  })

  it('sorts by price and filters by destination', async () => {
    const user = userEvent.setup()
    useManyHotels()
    renderApp(`/hotels?${search}&sort=price-asc`)

    await screen.findByText('8 hotels available')
    expect(hotelNames()[0]).toBe('Hotel H')

    await user.type(screen.getByLabelText('Search by name or destination'), 'lisbon')

    expect(await screen.findByText('1 hotel available')).toBeInTheDocument()
    expect(hotelNames()).toEqual(['Hotel H'])
  })

  it('offers a way out when the filter matches nothing', async () => {
    const user = userEvent.setup()
    useManyHotels()
    renderApp(`/hotels?${search}`)

    await screen.findByText('8 hotels available')
    await user.type(screen.getByLabelText('Search by name or destination'), 'zzz')

    expect(await screen.findByText('No hotels match your search')).toBeInTheDocument()
  })
})

describe('stay pickers', () => {
  it('picks check-in then check-out instead of extending the old range', async () => {
    const user = userEvent.setup()
    const seen: string[] = []
    server.use(
      http.get(`${API}/hotels/:id/availability`, ({ request }) => {
        seen.push(new URL(request.url).search)
        return HttpResponse.json(pageOf([room]))
      }),
    )
    renderApp(`/hotels/1?checkIn=2030-05-10&checkOut=2030-05-12&guests=2`)

    await user.click(await screen.findByRole('button', { name: 'Select dates' }))
    expect(screen.getByText('Choose your check-in date')).toBeInTheDocument()

    await user.click(screen.getByRole('button', { name: /May 20th, 2030/ }))
    expect(screen.getByText('Now choose your check-out date')).toBeInTheDocument()
    await user.click(screen.getByRole('button', { name: /May 23rd, 2030/ }))

    const trigger = await screen.findByRole('button', { name: 'Select dates' })
    expect(within(trigger).getByText('Mon 20 May')).toBeInTheDocument()
    expect(within(trigger).getByText('Thu 23 May')).toBeInTheDocument()
    expect(seen.at(-1)).toContain('checkIn=2030-05-20&checkOut=2030-05-23')
  })

  it('adjusts guests with a stepper and hides rooms that are too small', async () => {
    const user = userEvent.setup()
    renderApp(`/hotels/1?${search}`)

    await user.click(await screen.findByRole('button', { name: 'Guests' }))
    await user.click(screen.getByRole('button', { name: 'Increase guests' }))
    expect(await screen.findByText('Deluxe King')).toBeInTheDocument()

    await user.click(screen.getByRole('button', { name: 'Increase guests' }))
    expect(screen.getByRole('button', { name: 'Increase guests' })).toBeDisabled()
    expect(await screen.findByText('No rooms for this stay')).toBeInTheDocument()
  })
})

describe('admin reservations table', () => {
  it('pages and sorts on the server', async () => {
    const user = userEvent.setup()
    const requests: URLSearchParams[] = []
    server.use(
      http.get(`${API}/reservations`, ({ request }) => {
        const params = new URL(request.url).searchParams
        requests.push(params)
        return HttpResponse.json(pageOf([reservation], { number: Number(params.get('page')), totalPages: 3, totalElements: 41 }))
      }),
    )
    renderApp('/admin', 'ADMIN')

    expect(await screen.findByText('41 reservations')).toBeInTheDocument()
    expect(requests[0].get('sort')).toBe('id,desc')

    await user.click(screen.getByRole('button', { name: /Next/ }))
    await screen.findByText('Page 2 of 3')
    expect(requests.at(-1)?.get('page')).toBe('1')

    await user.click(screen.getByRole('button', { name: 'Total' }))
    await screen.findByText('Page 1 of 3')
    expect(requests.at(-1)?.get('sort')).toBe('totalPrice,asc')
    expect(requests.at(-1)?.get('page')).toBe('0')
  })
})
