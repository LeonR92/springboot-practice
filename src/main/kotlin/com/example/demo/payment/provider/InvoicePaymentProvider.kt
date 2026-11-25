package com.example.demo.payment.provider

import com.example.demo.payment.model.Payment
import com.example.demo.payment.model.PaymentMethod
import com.example.demo.payment.model.PaymentStatus
import com.example.demo.reservation.model.Reservation
import com.example.demo.reservation.model.ReservationStatus
import org.springframework.stereotype.Component

@Component
class InvoicePaymentProvider : PaymentProvider {
    override val method = PaymentMethod.INVOICE
    override val initialStatus = PaymentStatus.OUTSTANDING

    // Nobody pays within the 15 minute hold, so the stay is confirmed now and the payment stays OUTSTANDING
    override fun initiate(payment: Payment, reservation: Reservation) {
        reservation.status = ReservationStatus.CONFIRMED
    }
}
