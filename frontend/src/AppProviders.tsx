import type { ReactNode } from 'react'
import { MutationCache, QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { toast } from 'sonner'
import { ApiError } from '@/api/ApiError'
import { errorMessage } from '@/components/shared/PageState'
import { AuthProvider } from '@/hooks/useAuth'

export const createQueryClient = () =>
  new QueryClient({
    mutationCache: new MutationCache({ onError: (error) => toast.error(errorMessage(error)) }),
    defaultOptions: {
      queries: {
        staleTime: 30_000,
        refetchOnWindowFocus: false,
        retry: (count, error) => !(error instanceof ApiError && error.status < 500 && error.status !== 0) && count < 2,
      },
    },
  })

const queryClient = createQueryClient()

export function AppProviders({ children, client = queryClient }: { children: ReactNode; client?: QueryClient }) {
  return (
    <QueryClientProvider client={client}>
      <AuthProvider>{children}</AuthProvider>
    </QueryClientProvider>
  )
}
