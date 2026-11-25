package com.example.demo

import com.example.demo.support.IntegrationTest
import org.junit.jupiter.api.Test
import org.springframework.test.context.TestPropertySource

@TestPropertySource(properties = ["payment.rate-limit.max-requests=3"])
class PaymentRateLimitIT : IntegrationTest() {
    private val payment = paymentBody(999, "STRIPE")

    @Test
    fun `payment creation is limited per user`() {
        val token = userToken()

        repeat(3) { post("/api/v1/payments", payment, token).andExpect { status { isNotFound() } } }
        post("/api/v1/payments", payment, token).andExpect {
            status { isTooManyRequests() }
            header { exists("Retry-After") }
        }

        post("/api/v1/payments", payment, userToken()).andExpect { status { isNotFound() } }
    }

    @Test
    fun `webhook calls are not limited`() {
        repeat(10) {
            webhook("STRIPE", "x").andExpect { status { isNotFound() } }
        }
    }
}
