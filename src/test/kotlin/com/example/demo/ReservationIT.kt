package com.example.demo

import com.example.demo.pricing.model.CouponCode
import com.example.demo.pricing.repository.CouponCodeRepository
import com.example.demo.reservation.service.ReservationService
import com.example.demo.support.IntegrationTest
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import kotlin.test.assertEquals

class ReservationIT : IntegrationTest() {

    @Autowired lateinit var coupons: CouponCodeRepository
    @Autowired lateinit var reservationService: ReservationService

    @Test
    fun `creating a reservation prices the stay`() {
        val seed = seedRooms()

        post("/api/v1/reservations", reservationBody(seed.roomIds[0]), userToken()).andExpect {
            status { isCreated() }
            jsonPath("$.status") { value("PENDING") }
            jsonPath("$.totalPrice") { value(160.0) }
        }
    }

    @Test
    fun `coupons change the price and invalid ones are rejected`() {
        val seed = seedRooms()
        val token = userToken()
        coupons.save(CouponCode(discountValue = 10.toBigDecimal(), code = "SAVE10", active = true))

        post("/api/v1/reservations", reservationBody(seed.roomIds[0], coupon = "SAVE10"), token)
            .andExpect { jsonPath("$.totalPrice") { value(144.0) } }
        post("/api/v1/reservations", reservationBody(seed.roomIds[1], coupon = "NOPE"), token)
            .andExpect { status { isBadRequest() } }
    }

    @Test
    fun `invalid dates and overlapping stays are rejected`() {
        val seed = seedRooms()
        val token = userToken()
        val room = seed.roomIds[0]

        post("/api/v1/reservations", reservationBody(room, "2026-12-12", "2026-12-10"), token).andExpect { status { isBadRequest() } }
        post("/api/v1/reservations", reservationBody(room), token).andExpect { status { isCreated() } }
        post("/api/v1/reservations", reservationBody(room, "2026-12-11", "2026-12-13"), token).andExpect { status { isConflict() } }
        post("/api/v1/reservations", reservationBody(room, "2026-12-12", "2026-12-14"), token).andExpect { status { isCreated() } }
    }

    @Test
    fun `reading and deleting reservations is admin only`() {
        val seed = seedRooms()
        val user = userToken()
        val admin = adminToken()
        val id = reserve(seed.roomIds[0], user)

        get("/api/v1/reservations", user).andExpect { status { isForbidden() } }
        get("/api/v1/reservations/$id", user).andExpect { status { isForbidden() } }
        delete("/api/v1/reservations/$id", user).andExpect { status { isForbidden() } }

        get("/api/v1/reservations/$id", admin).andExpect { jsonPath("$.guestName") { value("Guest") } }
        get("/api/v1/reservations", admin).andExpect { jsonPath("$.content.length()") { value(1) } }
        delete("/api/v1/reservations/$id", admin).andExpect { status { isNoContent() } }
        get("/api/v1/reservations/$id", admin).andExpect { status { isNotFound() } }
    }

    @Test
    fun `availability hides booked rooms and ignores expired holds`() {
        val seed = seedRooms()
        val token = userToken()
        val availability = "/api/v1/hotels/${seed.hotelId}/availability?checkIn=2026-12-10&checkOut=2026-12-12"

        get(availability, token).andExpect { jsonPath("$.content.length()") { value(2) } }

        val id = reserve(seed.roomIds[0], token)
        get(availability, token).andExpect { jsonPath("$.content.length()") { value(1) } }

        expireHold(id)
        get(availability, token).andExpect { jsonPath("$.content.length()") { value(2) } }
    }

    @Test
    fun `cleanup expires stale holds`() {
        val seed = seedRooms(1)
        val id = reserve(seed.roomIds[0], userToken())
        expireHold(id)

        assertEquals(1, reservationService.expireStaleReservations())

        get("/api/v1/reservations/$id", adminToken()).andExpect { jsonPath("$.status") { value("EXPIRED") } }
    }
}
