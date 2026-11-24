package com.example.demo.reservation.dto

import com.example.demo.common.exception.InvalidStayDatesException
import com.example.demo.reservation.model.ReservationStatus
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import java.time.LocalDate

data class ReservationRequest(
    val roomId: Long,

    @field:NotBlank
    @field:Size(max = 255)
    val guestName: String,

    @field:NotBlank
    @field:Email
    @field:Size(max = 255)
    val guestEmail: String,

    val checkIn: LocalDate,

    val checkOut: LocalDate,

    val status: ReservationStatus = ReservationStatus.PENDING,

    @field:Size(max = 50)
    val couponCode: String? = null
)

// The one place that decides what counts as "no coupon": blank means none.
fun ReservationRequest.normalizedCoupon(): String? =
    couponCode?.trim()?.takeIf { it.isNotEmpty() }

fun ReservationRequest.validateDates() {
    if (!checkOut.isAfter(checkIn)) {
        throw InvalidStayDatesException()
    }
}


data class AvailabilitySearchRequest(
    val hotelId: Long,
    val roomTypeId: Long? = null,
    val checkIn: LocalDate,
    val checkOut: LocalDate
)

fun AvailabilitySearchRequest.validateBookingDates() {
    if (!checkOut.isAfter(checkIn)) {
        throw InvalidStayDatesException()
    }
}