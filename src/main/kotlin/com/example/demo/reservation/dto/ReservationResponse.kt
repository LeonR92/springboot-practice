package com.example.demo.reservation.dto

import com.example.demo.reservation.model.Reservation
import com.example.demo.reservation.model.ReservationStatus
import java.time.LocalDate

data class ReservationResponse(
    val id: Long,
    val roomId: Long,
    val hotelId: Long,
    val roomNumber: String,
    val guestName: String,
    val guestEmail: String,
    val checkIn: LocalDate,
    val checkOut: LocalDate,
    val status: ReservationStatus
)

fun Reservation.toResponse() = ReservationResponse(
    id = requireNotNull(id),
    roomId = requireNotNull(room.id),
    hotelId = requireNotNull(room.hotel.id),
    roomNumber = room.roomNumber,
    guestName = guestName,
    guestEmail = guestEmail,
    checkIn = checkIn,
    checkOut = checkOut,
    status = status
)
