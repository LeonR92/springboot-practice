package com.example.demo.pricing.rules

import com.example.demo.pricing.model.PricingContext
import java.math.BigDecimal

interface PricingRule {
    fun apply(
        context: PricingContext,
        currentPrice: BigDecimal,
    ): BigDecimal
}
