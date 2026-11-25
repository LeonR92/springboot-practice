package com.example.demo.invoice

import com.example.demo.payment.model.Payment
import com.example.demo.reservation.model.Reservation
import org.springframework.stereotype.Component

@Component
class InvoicePdfGenerator {

    fun generate(payment: Payment, reservation: Reservation): ByteArray =
        """
        %PDF-1.4
        FAKE INVOICE #${payment.id}
        Guest: ${reservation.guestName}
        Stay: ${reservation.checkIn} - ${reservation.checkOut}
        Paid: ${payment.amount} via ${payment.method}
        """.trimIndent().toByteArray()
}
