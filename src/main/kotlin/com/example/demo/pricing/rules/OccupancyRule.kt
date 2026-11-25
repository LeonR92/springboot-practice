package com.example.demo.pricing.rules

import com.example.demo.hotel.repository.HotelRepository
import com.example.demo.pricing.model.PricingContext
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.util.TreeMap

@Component
@Order(PricingOrder.OCCUPANCY)
class OccupancyRule(
    private val repository: HotelRepository,
) : PricingRule {
    override fun apply(
        context: PricingContext,
        currentPrice: BigDecimal,
    ): BigDecimal {
        val occupancy = repository.findOccupancyRate(context.hotelId, context.checkIn, context.excludeReservationId)

        val multiplier = RATES.floorEntry(occupancy)?.value ?: BigDecimal.ONE

        return currentPrice * multiplier
    }
}

private val RATES =
    TreeMap(
        mapOf(
            BigDecimal("0.00") to BigDecimal("0.80"), // 20% discount
            BigDecimal("0.30") to BigDecimal("0.90"),
            BigDecimal("0.50") to BigDecimal("1.00"),
            BigDecimal("0.90") to BigDecimal("1.30"), // 30% surcharge
        ),
    )
