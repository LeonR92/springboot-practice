package com.example.demo.payment.listener

import com.example.demo.invoice.SendInvoiceViaEmail
import com.example.demo.payment.event.PaymentConfirmationEvent
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener

@Component
class PaymentEventListener(
    private val sendInvoiceViaEmail: SendInvoiceViaEmail
) {

    private val log = LoggerFactory.getLogger(javaClass)

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun handleEvent(event: PaymentConfirmationEvent) {
        try {
            sendInvoiceViaEmail.send(event.paymentId)
        } catch (e: Exception) {
            log.error("Could not send invoice for payment {}", event.paymentId, e)
        }
    }
}
