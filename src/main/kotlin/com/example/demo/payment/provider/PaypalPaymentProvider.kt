package com.example.demo.payment.provider

import com.example.demo.payment.model.Payment
import com.example.demo.payment.model.PaymentMethod
import com.example.demo.payment.model.PaymentStatus
import com.example.demo.reservation.model.Reservation
import com.example.demo.common.exception.PaymentProviderUnavailableException
import org.springframework.resilience.annotation.Retryable
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class PaypalPaymentProvider : PaymentProvider {
    override val method = PaymentMethod.PAYPAL
    override val initialStatus = PaymentStatus.PENDING

    @Retryable(includes = [PaymentProviderUnavailableException::class], maxRetries = 3, delay = 200, multiplier = 2.0)
    override fun initiate(payment: Payment, reservation: Reservation) {
        // Fake: pretend PayPal created an order
        payment.token = "fake_paypal_${UUID.randomUUID()}"
    }
}
