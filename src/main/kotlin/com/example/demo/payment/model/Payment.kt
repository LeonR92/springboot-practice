package com.example.demo.payment.model

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

enum class PaymentMethod {
    STRIPE,
    PAYPAL,
    INVOICE,
}

enum class PaymentStatus {
    PENDING,
    SUCCEEDED,
    FAILED,
    OUTSTANDING,
    ;

    companion object {
        val OPEN = setOf(PENDING, OUTSTANDING)
    }
}

@Entity
@Table(name = "payments")
class Payment(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    @Column(nullable = false)
    var userId: Long,
    @Column(nullable = false)
    var reservationId: Long,
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var method: PaymentMethod,
    @Column(nullable = false, precision = 10, scale = 2)
    var amount: BigDecimal,
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: PaymentStatus = PaymentStatus.PENDING,
    // Provider reference (e.g. Stripe/PayPal token); null for INVOICE
    var token: String? = null,
    @Column(nullable = false)
    var createdAt: Instant = Instant.now(),
)
