package com.example.demo.payment.provider

import com.example.demo.payment.model.Payment
import com.example.demo.payment.model.PaymentMethod
import com.example.demo.payment.model.PaymentStatus
import com.example.demo.reservation.model.Reservation

interface PaymentProvider {
    val method: PaymentMethod

    // Status a new payment starts in
    val initialStatus: PaymentStatus

    // Method-specific work right after the payment is created (provider session, token, reservation handling)
    fun initiate(
        payment: Payment,
        reservation: Reservation,
    ) {}

    // Called when the money is confirmed (webhook for Stripe/PayPal, manual for invoice).
    // Provider-specific checks go here (signature, amount); throw to reject.
    fun confirm(
        payment: Payment,
        reservation: Reservation,
    ) {}
}
