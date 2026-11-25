package com.example.demo.payment.service

import com.example.demo.common.exception.InvalidWebhookSignatureException
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.security.MessageDigest

@Component
class WebhookVerifier(
    @Value("\${payment.webhook-secret}") private val expected: String
) {
    private val expectedBytes = expected.toByteArray()

    init {
        require(expected.length >= 16) { "payment.webhook-secret must be at least 16 characters" }
    }

    fun verify(provided: String?) {
        val ok = provided != null &&
            MessageDigest.isEqual(provided.toByteArray(), expectedBytes)
        if (!ok) throw InvalidWebhookSignatureException()
    }
}
