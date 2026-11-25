package com.example.demo.pricing.rules

import com.example.demo.common.exception.InvalidCouponException
import com.example.demo.pricing.model.DiscountType
import com.example.demo.pricing.model.PricingContext
import com.example.demo.pricing.repository.CouponCodeRepository
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.Instant

@Component
@Order(PricingOrder.COUPON)
class CouponRule(
    private val repository: CouponCodeRepository,
) : PricingRule {
    override fun apply(
        context: PricingContext,
        currentPrice: BigDecimal,
    ): BigDecimal {
        val code = context.couponCode ?: return currentPrice

        val now = Instant.now()
        val coupon =
            repository
                .findByCodeAndActiveTrue(code)
                ?.takeIf { (it.validFrom?.isAfter(now) != true) && (it.validUntil?.isBefore(now) != true) }
                ?: throw InvalidCouponException(code)

        return when (coupon.discountType) {
            DiscountType.FIXED_AMOUNT -> {
                (currentPrice - coupon.discountValue).max(BigDecimal.ZERO)
            }

            DiscountType.PERCENTAGE -> {
                val percent = coupon.discountValue.coerceIn(BigDecimal.ZERO, HUNDRED)
                currentPrice * (BigDecimal.ONE - percent.divide(HUNDRED, 4, RoundingMode.HALF_UP))
            }
        }
    }
}

private val HUNDRED = BigDecimal.valueOf(100)
