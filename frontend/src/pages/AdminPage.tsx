import { useState } from 'react'
import { ArrowDown, ArrowUp, ArrowUpDown } from 'lucide-react'
import type { ReservationStatus } from '@/api/types'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select'
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table'
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs'
import { LoadingList, Notice, errorMessage } from '@/components/shared/PageState'
import { Pagination } from '@/components/shared/Pagination'
import { useAllReservations, useDeleteHotel, useDeleteReservation, useSetReservationStatus, useUsers } from '@/hooks/useAdmin'
import { useHotels } from '@/hooks/useHotels'
import { formatPriceExact, formatShortDate } from '@/lib/format'

const statuses: ReservationStatus[] = ['PENDING', 'CONFIRMED', 'CANCELLED', 'EXPIRED']

export default function AdminPage() {
  return (
    <div className="mx-auto max-w-6xl px-6 py-10">
      <h1 className="text-4xl">Administration</h1>
      <Tabs defaultValue="reservations" className="mt-8">
        <TabsList>
          <TabsTrigger value="reservations">Reservations</TabsTrigger>
          <TabsTrigger value="hotels">Hotels</TabsTrigger>
          <TabsTrigger value="users">Users</TabsTrigger>
        </TabsList>
        <TabsContent value="reservations"><ReservationsTable /></TabsContent>
        <TabsContent value="hotels"><HotelsTable /></TabsContent>
        <TabsContent value="users"><UsersTable /></TabsContent>
      </Tabs>
    </div>
  )
}

const PAGE_SIZE = 15

type SortField = 'id' | 'checkIn' | 'totalPrice' | 'status'

function ReservationsTable() {
  const [page, setPage] = useState(1)
  const [sort, setSort] = useState<{ field: SortField; dir: 'asc' | 'desc' }>({ field: 'id', dir: 'desc' })
  const reservations = useAllReservations({ page: page - 1, size: PAGE_SIZE, sort: `${sort.field},${sort.dir}` })
  const setStatus = useSetReservationStatus()
  const remove = useDeleteReservation()

  if (reservations.isPending) return <LoadingList rows={1} />
  if (reservations.isError) return <Notice title="Could not load reservations" tone="error">{errorMessage(reservations.error)}</Notice>

  const toggleSort = (field: SortField) => {
    setSort((current) => ({ field, dir: current.field === field && current.dir === 'asc' ? 'desc' : 'asc' }))
    setPage(1)
  }
  const header = (field: SortField, label: string) => (
    <TableHead aria-sort={sort.field === field ? (sort.dir === 'asc' ? 'ascending' : 'descending') : 'none'}>
      <button type="button" className="inline-flex items-center gap-1 font-medium" onClick={() => toggleSort(field)}>
        {label}
        {sort.field === field ? (
          sort.dir === 'asc' ? <ArrowUp className="size-3.5" /> : <ArrowDown className="size-3.5" />
        ) : (
          <ArrowUpDown className="size-3.5 text-muted-foreground" />
        )}
      </button>
    </TableHead>
  )

  const { content, totalElements, totalPages } = reservations.data

  return (
    <div className="space-y-4">
      <p className="text-sm text-muted-foreground">{totalElements} reservations</p>
      <div className="overflow-x-auto">
        <Table>
          <TableHeader>
            <TableRow>
              {header('id', '#')}
              <TableHead>Guest</TableHead>
              {header('checkIn', 'Stay')}
              {header('totalPrice', 'Total')}
              {header('status', 'Status')}
              <TableHead />
            </TableRow>
          </TableHeader>
          <TableBody>
            {content.map((r) => (
              <TableRow key={r.id}>
                <TableCell>{r.id}</TableCell>
                <TableCell>{r.guestName}<span className="block text-xs text-muted-foreground">{r.guestEmail}</span></TableCell>
                <TableCell>{formatShortDate(r.checkIn)} → {formatShortDate(r.checkOut)} · Room {r.roomNumber}</TableCell>
                <TableCell>{formatPriceExact(r.totalPrice)}</TableCell>
                <TableCell>
                  <Select value={r.status} onValueChange={(status) => setStatus.mutate({ reservation: r, status: status as ReservationStatus })}>
                    <SelectTrigger className="w-36" aria-label={`Status of reservation ${r.id}`}><SelectValue /></SelectTrigger>
                    <SelectContent>
                      {statuses.map((s) => <SelectItem key={s} value={s}>{s}</SelectItem>)}
                    </SelectContent>
                  </Select>
                </TableCell>
                <TableCell className="text-right">
                  <Button variant="ghost" size="sm" onClick={() => remove.mutate(r.id)}>Delete</Button>
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </div>
      <Pagination page={page} totalPages={totalPages} onChange={setPage} />
    </div>
  )
}

function HotelsTable() {
  const hotels = useHotels()
  const remove = useDeleteHotel()

  if (hotels.isPending) return <LoadingList rows={1} />
  if (hotels.isError) return <Notice title="Could not load hotels" tone="error">{errorMessage(hotels.error)}</Notice>

  return (
    <Table>
      <TableHeader>
        <TableRow><TableHead>Name</TableHead><TableHead>Location</TableHead><TableHead>Amenities</TableHead><TableHead /></TableRow>
      </TableHeader>
      <TableBody>
        {hotels.data.map((h) => (
          <TableRow key={h.id}>
            <TableCell>{h.name}</TableCell>
            <TableCell>{h.address.city}, {h.address.country}</TableCell>
            <TableCell>{h.amenities.length}</TableCell>
            <TableCell className="text-right">
              <Button variant="ghost" size="sm" onClick={() => remove.mutate(h.id)}>Delete</Button>
            </TableCell>
          </TableRow>
        ))}
      </TableBody>
    </Table>
  )
}

function UsersTable() {
  const users = useUsers()

  if (users.isPending) return <LoadingList rows={1} />
  if (users.isError) return <Notice title="Could not load users" tone="error">{errorMessage(users.error)}</Notice>

  return (
    <Table>
      <TableHeader>
        <TableRow><TableHead>Name</TableHead><TableHead>Email</TableHead><TableHead>Role</TableHead></TableRow>
      </TableHeader>
      <TableBody>
        {users.data.map((u) => (
          <TableRow key={u.id}>
            <TableCell>{u.name}</TableCell>
            <TableCell>{u.email}</TableCell>
            <TableCell><Badge variant={u.role === 'ADMIN' ? 'default' : 'secondary'}>{u.role}</Badge></TableCell>
          </TableRow>
        ))}
      </TableBody>
    </Table>
  )
}
