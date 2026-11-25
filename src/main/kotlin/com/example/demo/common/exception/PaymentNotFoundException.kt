package com.example.demo.common.exception

class PaymentNotFoundException(
    message: String,
) : RuntimeException(message) {
    constructor(id: Long) : this("Payment with ID $id not found")
}
