import { Outlet, Route, Routes } from 'react-router-dom'
import { Toaster } from '@/components/ui/sonner'
import { Footer } from '@/components/shared/Footer'
import { Header } from '@/components/shared/Header'
import { RequireAuth } from '@/components/shared/RequireAuth'
import { lazy, Suspense } from 'react'
import { LoadingList } from '@/components/shared/PageState'
import HomePage from '@/pages/HomePage'
import HotelsPage from '@/pages/HotelsPage'
import HotelDetailPage from '@/pages/HotelDetailPage'
import AuthPage from '@/pages/AuthPage'
import BookingPage from '@/pages/BookingPage'
import PaymentPage from '@/pages/PaymentPage'
import TripsPage from '@/pages/TripsPage'

const AdminPage = lazy(() => import('@/pages/AdminPage'))

function Layout() {
  return (
    <div className="flex min-h-screen flex-col">
      <Header />
      <main className="flex-1">
        <Outlet />
      </main>
      <Footer />
      <Toaster position="top-center" />
    </div>
  )
}

export default function App() {
  return (
    <Routes>
      <Route element={<Layout />}>
        <Route index element={<HomePage />} />
        <Route path="hotels" element={<HotelsPage />} />
        <Route path="hotels/:id" element={<HotelDetailPage />} />
        <Route path="login" element={<AuthPage mode="login" />} />
        <Route path="register" element={<AuthPage mode="register" />} />
        <Route element={<RequireAuth />}>
          <Route path="book/:roomId" element={<BookingPage />} />
          <Route path="booking/:id" element={<PaymentPage />} />
          <Route path="trips" element={<TripsPage />} />
        </Route>
        <Route element={<RequireAuth admin />}>
          <Route
            path="admin"
            element={
              <Suspense fallback={<LoadingList rows={1} />}>
                <AdminPage />
              </Suspense>
            }
          />
        </Route>
        <Route path="*" element={<p className="px-6 py-24 text-center text-muted-foreground">Page not found.</p>} />
      </Route>
    </Routes>
  )
}
