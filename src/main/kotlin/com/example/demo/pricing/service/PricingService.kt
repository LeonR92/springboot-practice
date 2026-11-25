package com.example.demo.pricing.service

import com.example.demo.pricing.model.PricingContext
import com.example.demo.pricing.rules.PricingRule
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.math.RoundingMode

@Service
class PricingService(
    // Spring injects the rules sorted by their @Order (see PricingOrder)
    private val rules: List<PricingRule>,
) {
    fun execute(
        context: PricingContext,
        basePrice: BigDecimal,
    ): BigDecimal =
        rules
            .fold(basePrice) { currentPrice, rule ->
                rule.apply(context, currentPrice)
            }.setScale(2, RoundingMode.HALF_UP)
}
