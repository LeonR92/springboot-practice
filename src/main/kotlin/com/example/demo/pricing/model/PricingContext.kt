package com.example.demo.pricing.model

import com.example.demo.hotel.model.Room
import java.time.LocalDate
import java.time.temporal.ChronoUnit

data class PricingContext(
    val room: Room,
    val checkIn: LocalDate,
    val checkOut: LocalDate,
    val couponCode: String? = null,
    // When re-pricing an existing reservation, it must not count towards the occupancy it is priced against
    val excludeReservationId: Long? = null
) {
    val hotelId: Long = requireNotNull(room.hotel.id)
    val nights: Long = ChronoUnit.DAYS.between(checkIn, checkOut)
}
