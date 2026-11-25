package com.example.demo.payment.controller

import com.example.demo.common.security.isAdmin
import com.example.demo.common.security.userId
import com.example.demo.payment.dto.PaymentRequest
import com.example.demo.payment.dto.PaymentResponse
import com.example.demo.payment.dto.PaymentWebhookRequest
import com.example.demo.payment.model.PaymentMethod
import com.example.demo.payment.service.PaymentService
import com.example.demo.payment.service.WebhookVerifier
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/v1/payments")
class PaymentController(
    private val service: PaymentService,
    private val webhookVerifier: WebhookVerifier,
) {
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createPayment(
        @Valid @RequestBody request: PaymentRequest,
        authentication: Authentication,
    ): PaymentResponse =
        service.createPayment(
            request,
            payerId = authentication.userId,
            isAdmin = authentication.isAdmin,
        )

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}/confirm")
    fun confirmPayment(
        @PathVariable id: Long,
    ): PaymentResponse = service.confirmPayment(id)

    @PostMapping("/webhook/{method}")
    fun webhook(
        @PathVariable method: PaymentMethod,
        @RequestHeader("X-Webhook-Secret", required = false) secret: String?,
        @Valid @RequestBody request: PaymentWebhookRequest,
    ): PaymentResponse {
        webhookVerifier.verify(secret)
        return service.resolveByToken(method, request.token, request.success)
    }
}
