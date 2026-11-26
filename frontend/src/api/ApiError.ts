export class ApiError extends Error {
  readonly status: number

  constructor(status: number, message: string) {
    super(message)
    this.name = 'ApiError'
    this.status = status
  }
}

const fallbacks: Record<number, string> = {
  401: 'Please sign in to continue.',
  403: 'You are not allowed to do that.',
  404: 'We could not find what you were looking for.',
  409: 'That is no longer available.',
  429: 'Too many attempts. Please wait a moment and try again.',
}

export function messageFromBody(status: number, body: unknown): string {
  if (body && typeof body === 'object') {
    const { error, message } = body as { error?: unknown; message?: unknown }
    if (typeof error === 'string' && error) return error
    if (typeof message === 'string' && message) return message
  }
  return fallbacks[status] ?? (status >= 500 ? 'Something went wrong on our side.' : 'The request was rejected.')
}
