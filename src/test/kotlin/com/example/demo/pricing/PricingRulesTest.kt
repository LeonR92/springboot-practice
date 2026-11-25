package com.example.demo.pricing

import com.example.demo.common.exception.InvalidCouponException
import com.example.demo.hotel.model.Address
import com.example.demo.hotel.model.Hotel
import com.example.demo.hotel.model.Room
import com.example.demo.hotel.model.RoomTier
import com.example.demo.hotel.model.RoomType
import com.example.demo.hotel.repository.HotelRepository
import com.example.demo.pricing.model.CouponCode
import com.example.demo.pricing.model.DiscountType
import com.example.demo.pricing.model.PricingContext
import com.example.demo.pricing.model.WeekendRate
import com.example.demo.pricing.repository.CouponCodeRepository
import com.example.demo.pricing.repository.WeekendRateRepository
import com.example.demo.pricing.rules.CouponRule
import com.example.demo.pricing.rules.OccupancyRule
import com.example.demo.pricing.rules.WeekendRule
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.Instant
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import kotlin.test.assertEquals

class PricingRulesTest {

    private val hotel = Hotel(id = 1, name = "H", address = Address(country = "DE", city = "Berlin"))
    private val room = Room(
        id = 1,
        hotel = hotel,
        roomType = RoomType(id = 1, name = "T", tier = RoomTier.STANDARD, basePrice = BigDecimal(100), capacity = 2),
        roomNumber = "101"
    )

    private fun context(checkIn: String, checkOut: String, coupon: String? = null) =
        PricingContext(room, LocalDate.parse(checkIn), LocalDate.parse(checkOut), coupon)

    private fun assertPrice(expected: String, actual: BigDecimal) =
        assertEquals(BigDecimal(expected), actual.setScale(2, RoundingMode.HALF_UP))

    private fun occupancy(rate: String, price: String): BigDecimal {
        val repository = mock(HotelRepository::class.java)
        `when`(repository.findOccupancyRate(1, LocalDate.parse("2026-12-10"), null)).thenReturn(BigDecimal(rate))
        return OccupancyRule(repository).apply(context("2026-12-10", "2026-12-12"), BigDecimal(price))
    }

    private fun weekend(multiplier: String?, checkIn: String, checkOut: String): BigDecimal {
        val repository = mock(WeekendRateRepository::class.java)
        `when`(repository.findByHotelIdAndActiveTrue(1))
            .thenReturn(multiplier?.let { WeekendRate(hotel = hotel, priceMultiplier = BigDecimal(it)) })
        return WeekendRule(repository).apply(context(checkIn, checkOut), BigDecimal(300))
    }

    private fun code(value: Int, type: DiscountType = DiscountType.PERCENTAGE, validUntil: Instant? = null) =
        CouponCode(discountValue = BigDecimal(value), code = "CODE", active = true, discountType = type, validUntil = validUntil)

    private fun coupon(coupon: CouponCode?, price: String): BigDecimal {
        val repository = mock(CouponCodeRepository::class.java)
        `when`(repository.findByCodeAndActiveTrue("CODE")).thenReturn(coupon)
        return CouponRule(repository).apply(context("2026-12-10", "2026-12-12", "CODE"), BigDecimal(price))
    }

    @Test
    fun `occupancy sets the multiplier by tier`() {
        assertPrice("80.00", occupancy("0.00", "100"))
        assertPrice("90.00", occupancy("0.30", "100"))
        assertPrice("100.00", occupancy("0.50", "100"))
        assertPrice("130.00", occupancy("0.95", "100"))
    }

    @Test
    fun `weekend rate only raises the weekend nights`() {
        assertPrice("400.00", weekend("1.50", "2026-12-11", "2026-12-14"))
        assertPrice("300.00", weekend("1.50", "2026-12-07", "2026-12-10"))
        assertPrice("300.00", weekend(null, "2026-12-11", "2026-12-14"))
    }

    @Test
    fun `percentage and fixed coupons reduce the price`() {
        assertPrice("180.00", coupon(code(10), "200"))
        assertPrice("150.00", coupon(code(50, DiscountType.FIXED_AMOUNT), "200"))
        assertPrice("0.00", coupon(code(50, DiscountType.FIXED_AMOUNT), "30"))
    }

    @Test
    fun `unknown or expired coupons are rejected`() {
        assertThrows<InvalidCouponException> { coupon(null, "200") }
        val expired = code(10, validUntil = Instant.now().minus(1, ChronoUnit.DAYS))
        assertThrows<InvalidCouponException> { coupon(expired, "200") }
    }
}
