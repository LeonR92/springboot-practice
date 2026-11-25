package com.example.demo

import com.example.demo.support.IntegrationTest
import org.awaitility.kotlin.await
import org.awaitility.kotlin.atMost
import org.awaitility.kotlin.during
import org.awaitility.kotlin.untilAsserted
import org.junit.jupiter.api.Test
import java.time.Duration
import kotlin.test.assertEquals

class PaymentIT : IntegrationTest() {

    private class Started(val paymentId: Long, val reservationId: Long, val token: String)

    private fun startPayment(
        method: String,
        user: String = userToken(),
        reservationId: Long = reserve(seedRooms(1).roomIds[0], user)
    ): Started {
        val body = post("/api/v1/payments", paymentBody(reservationId, method), user)
            .andExpect { status { isCreated() } }.body()
        return Started(idOf(body), reservationId, body.json<String?>("$.token").orEmpty())
    }

    @Test
    fun `stripe payment is confirmed by the webhook and the guest gets an invoice`() {
        val payment = startPayment("STRIPE")
        assertEquals("PENDING", statusOf("payments", payment.paymentId))

        webhook("STRIPE", payment.token).andExpect {
            status { isOk() }
            jsonPath("$.status") { value("SUCCEEDED") }
        }

        assertEquals("CONFIRMED", statusOf("reservations", payment.reservationId))
        awaitEmails(1)
        assertEquals("guest@test.com", emails.sent.single().to)
    }

    @Test
    fun `replayed webhook is idempotent`() {
        val payment = startPayment("PAYPAL")

        webhook("PAYPAL", payment.token).andExpect { status { isOk() } }
        webhook("PAYPAL", payment.token).andExpect { status { isOk() } }

        await atMost Duration.ofSeconds(5) during Duration.ofMillis(200) untilAsserted {
            assertEquals(1, emails.sent.size)
        }
    }

    @Test
    fun `invoice stays outstanding until an admin confirms it`() {
        val payment = startPayment("INVOICE")

        assertEquals("OUTSTANDING", statusOf("payments", payment.paymentId))
        assertEquals("CONFIRMED", statusOf("reservations", payment.reservationId))

        post("/api/v1/payments/${payment.paymentId}/confirm", token = userToken()).andExpect { status { isForbidden() } }
        post("/api/v1/payments/${payment.paymentId}/confirm", token = adminToken()).andExpect {
            status { isOk() }
            jsonPath("$.status") { value("SUCCEEDED") }
        }
        post("/api/v1/payments/999/confirm", token = adminToken()).andExpect { status { isNotFound() } }

        awaitEmails(1)
    }

    @Test
    fun `a reservation can only have one open payment`() {
        val user = userToken()
        val payment = startPayment("STRIPE", user)

        post("/api/v1/payments", paymentBody(payment.reservationId, "PAYPAL"), user).andExpect { status { isConflict() } }
    }

    @Test
    fun `payment is refused for expired or already confirmed reservations`() {
        val user = userToken()
        val seed = seedRooms(2)
        val expired = reserve(seed.roomIds[0], user)
        expireHold(expired)
        val confirmed = reserve(seed.roomIds[1], user)
        startPayment("INVOICE", user, confirmed)

        post("/api/v1/payments", paymentBody(expired, "STRIPE"), user).andExpect { status { isConflict() } }
        post("/api/v1/payments", paymentBody(confirmed, "STRIPE"), user).andExpect { status { isConflict() } }
    }

    @Test
    fun `webhook rejects bad secrets and unknown tokens`() {
        val payment = startPayment("STRIPE")

        webhook("STRIPE", payment.token, secret = null).andExpect { status { isUnauthorized() } }
        webhook("STRIPE", payment.token, secret = "wrong").andExpect { status { isUnauthorized() } }
        webhook("STRIPE", "unknown-token").andExpect { status { isNotFound() } }
        webhook("PAYPAL", payment.token).andExpect { status { isNotFound() } }
        webhook("STRIPE", "").andExpect { status { isBadRequest() } }

        assertEquals("PENDING", statusOf("payments", payment.paymentId))
    }

    @Test
    fun `money for a dead reservation is not confirmed`() {
        val payment = startPayment("STRIPE")
        jdbc.update("UPDATE reservations SET status = 'EXPIRED' WHERE id = ?", payment.reservationId)

        webhook("STRIPE", payment.token).andExpect { status { isConflict() } }

        assertEquals("PENDING", statusOf("payments", payment.paymentId))
        assertEquals(0, emails.sent.size)
    }
}
