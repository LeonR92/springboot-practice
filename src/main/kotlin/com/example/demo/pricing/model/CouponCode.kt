package com.example.demo.pricing.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.math.BigDecimal
import java.time.Instant

enum class DiscountType {
    PERCENTAGE,
    FIXED_AMOUNT,
}

@Entity
@Table(name = "coupons")
class CouponCode(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    // PERCENTAGE: 0-100 (10 = 10% off). FIXED_AMOUNT: money off the whole stay.
    @Column(nullable = false, precision = 10, scale = 2)
    var discountValue: BigDecimal,
    @Column(nullable = false, unique = true)
    var code: String,
    @Column(nullable = false)
    var active: Boolean = false,
    @Column(nullable = true)
    var validFrom: Instant? = null,
    @Column(nullable = true)
    var validUntil: Instant? = null,
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var discountType: DiscountType = DiscountType.PERCENTAGE,
)
