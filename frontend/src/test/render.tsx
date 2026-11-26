import { render } from '@testing-library/react'
import { MemoryRouter } from 'react-router-dom'
import App from '@/App'
import { AppProviders, createQueryClient } from '@/AppProviders'
import { tokenStore } from '@/api/session'
import { tokenFor } from './server'

export function renderApp(route: string, signedInAs?: 'USER' | 'ADMIN') {
  if (signedInAs) tokenStore.set(tokenFor(signedInAs))
  return render(
    <MemoryRouter initialEntries={[route]}>
      <AppProviders client={createQueryClient()}>
        <App />
      </AppProviders>
    </MemoryRouter>,
  )
}
