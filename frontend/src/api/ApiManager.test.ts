import { http, HttpResponse } from 'msw'
import { server } from '@/test/server'
import { ApiError } from './ApiError'
import { ApiManager } from './ApiManager'

const base = 'http://localhost/api/v1'

function manager(token: string | null = 'abc', onUnauthorized = vi.fn()) {
  return { api: new ApiManager({ baseUrl: base, getToken: () => token, onUnauthorized }), onUnauthorized }
}

describe('ApiManager', () => {
  it('sends the bearer token, JSON body and query string, skipping empty params', async () => {
    let seen: { auth: string | null; url: string; body: unknown } | undefined
    server.use(
      http.post(`${base}/things`, async ({ request }) => {
        seen = { auth: request.headers.get('authorization'), url: request.url, body: await request.json() }
        return HttpResponse.json({ ok: true })
      }),
    )

    const { api } = manager()
    await api.post('/things', { name: 'x' }, { query: { page: 2, q: '', skip: undefined } })

    expect(seen).toEqual({ auth: 'Bearer abc', url: `${base}/things?page=2`, body: { name: 'x' } })
  })

  it('maps backend errors to ApiError with the server message', async () => {
    server.use(http.get(`${base}/x`, () => HttpResponse.json({ error: 'Room is taken' }, { status: 409 })))

    await expect(manager().api.get('/x')).rejects.toMatchObject({ status: 409, message: 'Room is taken' })
  })

  it('signals unauthorized only when a token was sent', async () => {
    server.use(http.get(`${base}/x`, () => new HttpResponse(null, { status: 401 })))

    const signedIn = manager('abc')
    await expect(signedIn.api.get('/x')).rejects.toBeInstanceOf(ApiError)
    expect(signedIn.onUnauthorized).toHaveBeenCalledOnce()

    const anonymous = manager(null)
    await expect(anonymous.api.get('/x')).rejects.toBeInstanceOf(ApiError)
    expect(anonymous.onUnauthorized).not.toHaveBeenCalled()
  })

  it('handles empty 204 responses and network failures', async () => {
    server.use(http.delete(`${base}/x`, () => new HttpResponse(null, { status: 204 })), http.get(`${base}/down`, () => HttpResponse.error()))

    await expect(manager().api.delete('/x')).resolves.toBeUndefined()
    await expect(manager().api.get('/down')).rejects.toMatchObject({ status: 0 })
  })
})
