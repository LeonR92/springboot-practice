package com.example.demo.payment.dto

import com.example.demo.payment.model.PaymentMethod

data class PaymentRequest(
    val userId: Long,

    val reservationId: Long,

    val method: PaymentMethod
)
