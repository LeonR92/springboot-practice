package com.example.demo.payment.controller

import com.example.demo.common.security.isAdmin
import com.example.demo.common.security.userId
import com.example.demo.payment.dto.PaymentResponse
import com.example.demo.payment.service.PaymentService
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/v1/payments")
@ConditionalOnProperty("payment.simulation.enabled", havingValue = "true")
class PaymentSimulationController(
    private val service: PaymentService,
) {
    @PostMapping("/{id}/simulate-success")
    fun simulateSuccess(
        @PathVariable id: Long,
        authentication: Authentication,
    ): PaymentResponse = service.simulateSuccess(id, authentication.userId, authentication.isAdmin)
}
