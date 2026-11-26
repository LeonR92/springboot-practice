import { Navigate, Outlet, useLocation } from 'react-router-dom'
import { useAuth } from '@/hooks/useAuth'

export function RequireAuth({ admin = false }: { admin?: boolean }) {
  const { session, isAdmin } = useAuth()
  const location = useLocation()

  if (!session) return <Navigate to="/login" replace state={{ from: location.pathname + location.search }} />
  if (admin && !isAdmin) return <Navigate to="/" replace />
  return <Outlet />
}
