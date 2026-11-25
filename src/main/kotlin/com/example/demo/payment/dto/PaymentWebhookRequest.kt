package com.example.demo.payment.dto

import jakarta.validation.constraints.NotBlank

data class PaymentWebhookRequest(
    @field:NotBlank
    val token: String,
)
