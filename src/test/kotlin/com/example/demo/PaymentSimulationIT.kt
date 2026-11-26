package com.example.demo

import com.example.demo.support.IntegrationTest
import org.junit.jupiter.api.Test
import org.springframework.test.context.TestPropertySource
import kotlin.test.assertEquals

@TestPropertySource(properties = ["payment.simulation.enabled=true"])
class PaymentSimulationIT : IntegrationTest() {
    @Test
    fun `owner can simulate a provider confirmation and other users cannot`() {
        val owner = userToken()
        val reservationId = reserve(seedRooms(1).roomIds[0], owner)
        val paymentId = create("/api/v1/payments", paymentBody(reservationId, "STRIPE"), owner)

        post("/api/v1/payments/$paymentId/simulate-success", token = userToken()).andExpect { status { isForbidden() } }
        assertEquals("PENDING", statusOf("reservations", reservationId))

        post("/api/v1/payments/$paymentId/simulate-success", token = owner).andExpect {
            status { isOk() }
            jsonPath("$.status") { value("SUCCEEDED") }
        }
        assertEquals("CONFIRMED", statusOf("reservations", reservationId))
    }

    @Test
    fun `invoices cannot be simulated`() {
        val owner = userToken()
        val reservationId = reserve(seedRooms(1).roomIds[0], owner)
        val paymentId = create("/api/v1/payments", paymentBody(reservationId, "INVOICE"), owner)

        post("/api/v1/payments/$paymentId/simulate-success", token = owner).andExpect { status { isConflict() } }
    }
}
