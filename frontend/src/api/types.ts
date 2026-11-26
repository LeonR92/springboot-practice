export type UserRole = 'ADMIN' | 'USER'
export type ReservationStatus = 'PENDING' | 'CONFIRMED' | 'CANCELLED' | 'EXPIRED'
export type PaymentMethod = 'STRIPE' | 'PAYPAL' | 'INVOICE'
export type PaymentStatus = 'PENDING' | 'SUCCEEDED' | 'FAILED' | 'OUTSTANDING'
export type RoomTier = 'STANDARD' | 'SUPERIOR' | 'DELUXE' | 'SUITE'

export interface Page<T> {
  content: T[]
  number: number
  size: number
  totalElements: number
  totalPages: number
}

export interface Address {
  country: string
  city: string
  street?: string | null
  latitude?: number | null
  longitude?: number | null
}

export interface Amenity {
  id: number
  name: string
  description?: string | null
  category: string
  icon?: string | null
  isFree: boolean
}

export interface Hotel {
  id: number
  name: string
  address: Address
  checkInTime: string
  checkOutTime: string
  amenities: Amenity[]
}

export interface RoomType {
  id: number
  name: string
  tier: RoomTier
  basePrice: number
  capacity: number
  description?: string | null
}

export interface Room {
  id: number
  hotelId: number
  roomNumber: string
  floor?: number | null
  status: string
  roomType: RoomType
}

export interface Reservation {
  id: number
  roomId: number
  hotelId: number
  roomNumber: string
  guestName: string
  guestEmail: string
  checkIn: string
  checkOut: string
  status: ReservationStatus
  totalPrice: number
  couponCode?: string | null
  userId?: number | null
}

export interface CreateReservationInput {
  roomId: number
  guestName: string
  guestEmail: string
  checkIn: string
  checkOut: string
  couponCode?: string
}

export interface Payment {
  id: number
  userId: number
  reservationId: number
  method: PaymentMethod
  amount: number
  status: PaymentStatus
  token?: string | null
  createdAt: string
}

export interface User {
  id: number
  name: string
  email: string
  role: UserRole
}

export interface RegisterInput {
  name: string
  email: string
  password: string
}

export interface LoginInput {
  email: string
  password: string
}

export interface HotelOffer {
  hotel: Hotel
  rooms: Room[]
  fromPrice: number | null
}
