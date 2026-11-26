import { Link, NavLink } from 'react-router-dom'
import { User } from 'lucide-react'
import { Button } from '@/components/ui/button'
import { DropdownMenu, DropdownMenuContent, DropdownMenuItem, DropdownMenuTrigger } from '@/components/ui/dropdown-menu'
import { useAuth } from '@/hooks/useAuth'
import { useNavigate } from 'react-router-dom'

export function Header() {
  const { session, isAdmin, logout } = useAuth()
  const navigate = useNavigate()

  return (
    <header className="sticky top-0 z-40 border-b bg-background/95 backdrop-blur">
      <div className="mx-auto flex h-16 max-w-6xl items-center justify-between gap-2 px-4 sm:px-6">
        <Link to="/" className="whitespace-nowrap font-heading text-xl tracking-tight text-primary sm:text-2xl">
          Maison Stay
        </Link>
        <nav className="flex items-center gap-0.5 whitespace-nowrap text-sm sm:gap-1">
          <NavLink to="/hotels" className="hidden rounded px-3 py-2 hover:bg-accent sm:block">
            Hotels
          </NavLink>
          {session && (
            <NavLink to="/trips" className="rounded px-2 py-2 hover:bg-accent sm:px-3">
              My trips
            </NavLink>
          )}
          {isAdmin && (
            <NavLink to="/admin" className="rounded px-2 py-2 hover:bg-accent sm:px-3">
              Admin
            </NavLink>
          )}
          {session ? (
            <DropdownMenu>
              <DropdownMenuTrigger render={<Button variant="outline" size="icon" aria-label="Account" />}>
                <User className="size-4" />
              </DropdownMenuTrigger>
              <DropdownMenuContent align="end">
                <DropdownMenuItem onClick={() => navigate('/trips')}>My trips</DropdownMenuItem>
                <DropdownMenuItem
                  onClick={() => {
                    logout()
                    navigate('/')
                  }}
                >
                  Sign out
                </DropdownMenuItem>
              </DropdownMenuContent>
            </DropdownMenu>
          ) : (
            <Button render={<Link to="/login" />}>Sign in</Button>
          )}
        </nav>
      </div>
    </header>
  )
}
