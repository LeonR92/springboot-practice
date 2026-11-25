package com.example.demo.payment.dto

import com.example.demo.payment.model.Payment
import com.example.demo.payment.model.PaymentMethod
import com.example.demo.payment.model.PaymentStatus
import java.math.BigDecimal
import java.time.Instant

data class PaymentResponse(
    val id: Long,
    val userId: Long,
    val reservationId: Long,
    val method: PaymentMethod,
    val amount: BigDecimal,
    val status: PaymentStatus,
    val token: String?,
    val createdAt: Instant,
)

fun Payment.toResponse() =
    PaymentResponse(
        id = requireNotNull(id),
        userId = userId,
        reservationId = reservationId,
        method = method,
        amount = amount,
        status = status,
        token = token,
        createdAt = createdAt,
    )
