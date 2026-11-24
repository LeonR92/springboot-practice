package com.example.demo.pricing.rules

import com.example.demo.pricing.model.PricingContext
import com.example.demo.pricing.repository.WeekendRateRepository
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.DayOfWeek

@Component
@Order(PricingOrder.WEEKEND)
class WeekendRule(
    private val repository: WeekendRateRepository
) : PricingRule {

    override fun apply(
        context: PricingContext,
        currentPrice: BigDecimal
    ): BigDecimal {

        val weekendNights = context.checkIn
            .datesUntil(context.checkOut)
            .filter { it.dayOfWeek == DayOfWeek.SATURDAY || it.dayOfWeek == DayOfWeek.SUNDAY }
            .count()

        if (weekendNights == 0L) return currentPrice

        val rate = repository.findByHotelIdAndActiveTrue(context.hotelId)
            ?: return currentPrice

        // The weekend nights' share of the price so far (scale 10, so no cents are lost before the final rounding)
        val weekendShare = currentPrice
            .multiply(weekendNights.toBigDecimal())
            .divide(context.nights.toBigDecimal(), 10, RoundingMode.HALF_UP)

        return currentPrice + weekendShare * (rate.priceMultiplier - BigDecimal.ONE)
    }
}
