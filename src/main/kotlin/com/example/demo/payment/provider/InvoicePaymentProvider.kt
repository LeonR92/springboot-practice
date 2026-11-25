package com.example.demo.payment.provider

import com.example.demo.payment.model.PaymentMethod
import com.example.demo.payment.model.PaymentStatus
import org.springframework.stereotype.Component

@Component
class InvoicePaymentProvider : PaymentProvider {
    override val method = PaymentMethod.INVOICE
    override val initialStatus = PaymentStatus.OUTSTANDING
    override val confirmsReservationOnCreate = true
}
