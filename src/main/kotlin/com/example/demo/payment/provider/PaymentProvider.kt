package com.example.demo.payment.provider

import com.example.demo.payment.model.Payment
import com.example.demo.payment.model.PaymentMethod
import com.example.demo.payment.model.PaymentStatus
import com.example.demo.reservation.model.Reservation

interface PaymentProvider {
    val method: PaymentMethod

    // Status a new payment starts in
    val initialStatus: PaymentStatus

    // True when the stay is confirmed as soon as the payment is created (invoice: nobody pays within the hold)
    val confirmsReservationOnCreate: Boolean get() = false

    // Talks to the provider (session, token). Runs outside any transaction, so slow calls and retries hold no lock.
    fun initiate(payment: Payment) {}

    // Called when the money is confirmed (webhook for Stripe/PayPal, manual for invoice).
    // Provider-specific checks go here (signature, amount); throw to reject.
    fun confirm(
        payment: Payment,
        reservation: Reservation,
    ) {}
}
