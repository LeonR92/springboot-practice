package com.example.demo.reservation.dto

import com.example.demo.common.exception.InvalidStayDatesException
import com.example.demo.reservation.model.ReservationStatus
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import java.time.LocalDate

interface StayRequest {
    val roomId: Long
    val checkIn: LocalDate
    val checkOut: LocalDate
    val couponCode: String?
}

// What a guest sends. There is no status: a booking always starts PENDING.
data class CreateReservationRequest(
    override val roomId: Long,
    @field:NotBlank
    @field:Size(max = 255)
    val guestName: String,
    @field:NotBlank
    @field:Email
    @field:Size(max = 255)
    val guestEmail: String,
    override val checkIn: LocalDate,
    override val checkOut: LocalDate,
    @field:Size(max = 50)
    override val couponCode: String? = null,
) : StayRequest

// Admin edit: can also set the status.
data class UpdateReservationRequest(
    override val roomId: Long,
    @field:NotBlank
    @field:Size(max = 255)
    val guestName: String,
    @field:NotBlank
    @field:Email
    @field:Size(max = 255)
    val guestEmail: String,
    override val checkIn: LocalDate,
    override val checkOut: LocalDate,
    val status: ReservationStatus = ReservationStatus.PENDING,
    @field:Size(max = 50)
    override val couponCode: String? = null,
) : StayRequest

// The one place that decides what counts as "no coupon": blank means none.
fun StayRequest.normalizedCoupon(): String? = couponCode?.trim()?.takeIf { it.isNotEmpty() }

fun StayRequest.validateDates() {
    if (!checkOut.isAfter(checkIn)) {
        throw InvalidStayDatesException()
    }
}

data class AvailabilitySearchRequest(
    val hotelId: Long,
    val roomTypeId: Long? = null,
    val checkIn: LocalDate,
    val checkOut: LocalDate,
)

fun AvailabilitySearchRequest.validateBookingDates() {
    if (!checkOut.isAfter(checkIn)) {
        throw InvalidStayDatesException()
    }
}
