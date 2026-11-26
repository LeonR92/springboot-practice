import { ApiError, messageFromBody } from './ApiError'

type Query = Record<string, string | number | boolean | null | undefined>

export interface RequestOptions {
  query?: Query
  body?: unknown
  headers?: Record<string, string>
  signal?: AbortSignal
}

export interface ApiManagerConfig {
  baseUrl: string
  getToken: () => string | null
  onUnauthorized?: () => void
}

export class ApiManager {
  private readonly config: ApiManagerConfig

  constructor(config: ApiManagerConfig) {
    this.config = config
  }

  get<T>(path: string, options?: RequestOptions) {
    return this.request<T>('GET', path, options)
  }

  post<T>(path: string, body?: unknown, options?: RequestOptions) {
    return this.request<T>('POST', path, { ...options, body })
  }

  put<T>(path: string, body?: unknown, options?: RequestOptions) {
    return this.request<T>('PUT', path, { ...options, body })
  }

  delete<T = void>(path: string, options?: RequestOptions) {
    return this.request<T>('DELETE', path, options)
  }

  private async request<T>(method: string, path: string, { query, body, headers, signal }: RequestOptions = {}): Promise<T> {
    const token = this.config.getToken()
    const response = await fetch(this.buildUrl(path, query), {
      method,
      signal,
      headers: {
        Accept: 'application/json',
        ...(body !== undefined && { 'Content-Type': 'application/json' }),
        ...(token && { Authorization: `Bearer ${token}` }),
        ...headers,
      },
      body: body === undefined ? undefined : JSON.stringify(body),
    }).catch(() => {
      throw new ApiError(0, 'Unable to reach the server. Check your connection.')
    })

    const payload = await this.parse(response)
    if (!response.ok) {
      if (response.status === 401 && token) this.config.onUnauthorized?.()
      throw new ApiError(response.status, messageFromBody(response.status, payload))
    }
    return payload as T
  }

  private buildUrl(path: string, query?: Query) {
    const params = new URLSearchParams()
    Object.entries(query ?? {}).forEach(([key, value]) => {
      if (value !== undefined && value !== null && value !== '') params.set(key, String(value))
    })
    const qs = params.toString()
    return `${this.config.baseUrl}${path}${qs ? `?${qs}` : ''}`
  }

  private async parse(response: Response): Promise<unknown> {
    if (response.status === 204) return undefined
    const text = await response.text()
    if (!text) return undefined
    try {
      return JSON.parse(text)
    } catch {
      return text
    }
  }
}
