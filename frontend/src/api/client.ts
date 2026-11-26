import { ApiManager } from './ApiManager'
import { tokenStore } from './session'

let unauthorizedHandler: (() => void) | undefined

export const onUnauthorized = (handler: (() => void) | undefined) => {
  unauthorizedHandler = handler
}

export const api = new ApiManager({
  baseUrl: import.meta.env.VITE_API_BASE ?? '/api/v1',
  getToken: tokenStore.get,
  onUnauthorized: () => unauthorizedHandler?.(),
})
