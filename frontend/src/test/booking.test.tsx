import { screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { http, HttpResponse } from 'msw'
import { API, pageOf, reservation, server, tokenFor } from './server'
import { renderApp } from './render'

const search = 'checkIn=2030-05-10&checkOut=2030-05-12&guests=2'

describe('booking journey', () => {
  it('lets a visitor browse, sign in, reserve and start a payment', async () => {
    const user = userEvent.setup()
    let created: unknown
    let paid: unknown
    let confirmed = false
    let simulated: string | undefined
    server.use(
      http.post(`${API}/auth/login`, () => HttpResponse.json({ token: tokenFor('USER') })),
      http.post(`${API}/reservations`, async ({ request }) => {
        created = await request.json()
        return HttpResponse.json(reservation, { status: 201 })
      }),
      http.get(`${API}/reservations/me`, () => HttpResponse.json(pageOf([{ ...reservation, status: confirmed ? 'CONFIRMED' : 'PENDING' }]))),
      http.post(`${API}/payments`, async ({ request }) => {
        paid = await request.json()
        return HttpResponse.json({ id: 1, reservationId: 77, method: 'STRIPE', amount: 400, status: 'PENDING', token: 'tok', userId: 5, createdAt: '' }, { status: 201 })
      }),
      http.post(`${API}/payments/:id/simulate-success`, ({ params }) => {
        simulated = String(params.id)
        confirmed = true
        return HttpResponse.json({ id: 1, reservationId: 77, method: 'STRIPE', amount: 400, status: 'SUCCEEDED', userId: 5, createdAt: '' })
      }),
    )

    renderApp(`/hotels?${search}`)

    expect(await screen.findByRole('heading', { name: 'Grand Test Hotel' })).toBeInTheDocument()
    expect(screen.getByText(/€200/)).toBeInTheDocument()
    await user.click(screen.getByRole('link', { name: 'View rooms' }))

    await user.click(await screen.findByRole('button', { name: 'Select' }))

    expect(await screen.findByRole('heading', { name: 'Welcome back' })).toBeInTheDocument()
    await user.type(screen.getByLabelText('Email'), 'ada@example.com')
    await user.type(screen.getByLabelText('Password'), 'password1')
    await user.click(screen.getByRole('button', { name: 'Sign in' }))

    expect(await screen.findByRole('heading', { name: 'Guest details' })).toBeInTheDocument()
    expect(screen.getByText('€400')).toBeInTheDocument()
    expect(screen.getByLabelText('Guest name')).toHaveValue('Ada Lovelace')
    expect(screen.getByLabelText('Guest email')).toHaveValue('ada@example.com')
    await user.click(screen.getByRole('button', { name: 'Continue to payment' }))

    expect(await screen.findByRole('heading', { name: 'Payment' })).toBeInTheDocument()
    expect(created).toEqual({ roomId: 10, guestName: 'Ada Lovelace', guestEmail: 'ada@example.com', checkIn: '2030-05-10', checkOut: '2030-05-12' })

    expect(screen.getByLabelText('Card number')).toHaveValue('4242 4242 4242 4242')
    await user.click(screen.getByRole('button', { name: /^Pay/ }))

    expect(await screen.findByRole('heading', { name: 'Booking confirmed' })).toBeInTheDocument()
    expect(paid).toEqual({ reservationId: 77, method: 'STRIPE' })
    expect(simulated).toBe('1')
  })

  it('shows the server message when the room was taken in the meantime', async () => {
    const user = userEvent.setup()
    server.use(http.post(`${API}/reservations`, () => HttpResponse.json({ error: 'Room 10 is not available' }, { status: 409 })))

    renderApp(`/book/10?hotelId=1&${search}`, 'USER')

    await user.click(await screen.findByRole('button', { name: 'Continue to payment' }))

    expect(await screen.findByText('Room 10 is not available')).toBeInTheDocument()
    expect(screen.getByRole('heading', { name: 'Guest details' })).toBeInTheDocument()
  })

  it('validates guest details before calling the API', async () => {
    const user = userEvent.setup()
    renderApp(`/book/10?hotelId=1&${search}`, 'USER')

    await user.clear(await screen.findByLabelText('Guest name'))
    await user.clear(screen.getByLabelText('Guest email'))
    await user.click(screen.getByRole('button', { name: 'Continue to payment' }))

    expect(await screen.findByText('Enter the guest name')).toBeInTheDocument()
    expect(screen.getByText('Enter a valid email')).toBeInTheDocument()
  })
})

describe('catalog states', () => {
  it('tells the visitor when no room is available', async () => {
    server.use(http.get(`${API}/hotels/:id/availability`, () => HttpResponse.json(pageOf([]))))

    renderApp(`/hotels?${search}`)

    expect(await screen.findByText('No rooms available')).toBeInTheDocument()
  })

  it('hides rooms that are too small for the party', async () => {
    renderApp('/hotels?checkIn=2030-05-10&checkOut=2030-05-12&guests=4')

    expect(await screen.findByText('No rooms available')).toBeInTheDocument()
  })

  it('shows an error when the hotel list cannot be loaded', async () => {
    server.use(http.get(`${API}/hotels`, () => HttpResponse.json({ error: 'boom' }, { status: 500 })))

    renderApp(`/hotels?${search}`)

    expect(await screen.findByRole('alert', {}, { timeout: 5000 })).toHaveTextContent('boom')
  })
})

describe('account and access control', () => {
  it('redirects anonymous visitors to sign in and remembers where they were going', async () => {
    renderApp('/trips')

    expect(await screen.findByRole('heading', { name: 'Welcome back' })).toBeInTheDocument()
  })

  it('lists my trips and offers payment for pending ones', async () => {
    server.use(http.get(`${API}/reservations/me`, () => HttpResponse.json(pageOf([reservation]))))

    renderApp('/trips', 'USER')

    expect(await screen.findByText(/Ada Lovelace/)).toBeInTheDocument()
    expect(screen.getByText('Awaiting payment')).toBeInTheDocument()
    expect(screen.getByRole('link', { name: 'Pay now' })).toHaveAttribute('href', '/booking/77')
  })

  it('keeps regular users out of the admin area', async () => {
    renderApp('/admin', 'USER')

    expect(await screen.findByRole('heading', { name: /Stay somewhere worth remembering/ })).toBeInTheDocument()
    expect(screen.queryByRole('heading', { name: 'Administration' })).not.toBeInTheDocument()
  })

  it('lets admins change a reservation status', async () => {
    const user = userEvent.setup()
    let update: { status?: string } | undefined
    server.use(
      http.get(`${API}/reservations`, () => HttpResponse.json(pageOf([reservation]))),
      http.put(`${API}/reservations/77`, async ({ request }) => {
        update = (await request.json()) as { status?: string }
        return HttpResponse.json({ ...reservation, status: 'CONFIRMED' })
      }),
    )

    renderApp('/admin', 'ADMIN')

    await user.click(await screen.findByRole('combobox', { name: 'Status of reservation 77' }))
    await user.click(await screen.findByRole('option', { name: 'CONFIRMED' }))

    await waitFor(() => expect(update?.status).toBe('CONFIRMED'))
  })
})

describe('payment', () => {
  const pending = http.get(`${API}/reservations/me`, () => HttpResponse.json(pageOf([reservation])))

  it('blocks an invalid card before anything is sent', async () => {
    const user = userEvent.setup()
    let created = false
    server.use(
      pending,
      http.post(`${API}/payments`, () => {
        created = true
        return HttpResponse.json({}, { status: 201 })
      }),
    )
    renderApp('/booking/77', 'USER')

    const number = await screen.findByLabelText('Card number')
    await user.clear(number)
    await user.type(number, '1234')
    await user.click(screen.getByRole('button', { name: /^Pay/ }))

    expect(await screen.findByText('Enter a 16-digit card number')).toBeInTheDocument()
    expect(created).toBe(false)
  })

  it('waits for the invoice to be settled instead of simulating a payment', async () => {
    const user = userEvent.setup()
    let simulated = false
    server.use(
      pending,
      http.post(`${API}/payments`, () =>
        HttpResponse.json({ id: 2, reservationId: 77, method: 'INVOICE', amount: 400, status: 'OUTSTANDING', userId: 5, createdAt: '' }, { status: 201 }),
      ),
      http.post(`${API}/payments/:id/simulate-success`, () => {
        simulated = true
        return HttpResponse.json({})
      }),
    )
    renderApp('/booking/77', 'USER')

    await user.click(await screen.findByLabelText(/Invoice/))
    await user.click(screen.getByRole('button', { name: /^Pay/ }))

    expect(await screen.findByText('Invoice requested')).toBeInTheDocument()
    expect(simulated).toBe(false)
  })
})
