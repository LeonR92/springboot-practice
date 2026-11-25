package com.example.demo.common.exception

class PaymentNotConfirmableException(
    id: Long,
    status: String,
) : RuntimeException("Payment $id cannot be confirmed, current status: $status")
