package com.example.demo.invoice

import com.example.demo.common.exception.PaymentNotFoundException
import com.example.demo.common.exception.ReservationNotFoundException
import com.example.demo.email.EmailAttachment
import com.example.demo.email.EmailMessage
import com.example.demo.email.EmailProvider
import com.example.demo.payment.repository.PaymentRepository
import com.example.demo.reservation.repository.ReservationRepository
import org.springframework.stereotype.Component

@Component
class SendInvoiceViaEmail(
    private val paymentRepository: PaymentRepository,
    private val reservationRepository: ReservationRepository,
    private val pdfGenerator: InvoicePdfGenerator,
    private val emailProvider: EmailProvider,
) {
    fun send(paymentId: Long) {
        val payment = paymentRepository.findById(paymentId).orElseThrow { PaymentNotFoundException(paymentId) }
        val reservation =
            reservationRepository
                .findById(payment.reservationId)
                .orElseThrow { ReservationNotFoundException(payment.reservationId) }

        val pdf = pdfGenerator.generate(payment, reservation)

        emailProvider.send(
            EmailMessage(
                to = reservation.guestEmail,
                subject = "Your invoice #$paymentId",
                body = "Hello ${reservation.guestName}, thank you for your payment. Your invoice is attached.",
                attachments = listOf(EmailAttachment("invoice-$paymentId.pdf", "application/pdf", pdf)),
            ),
        )
    }
}
