package com.example.demo

import com.example.demo.support.IntegrationTest
import org.junit.jupiter.api.Test

class HealthIT : IntegrationTest() {

    @Test
    fun `health endpoints are public and up`() {
        listOf("/actuator/health", "/actuator/health/liveness", "/actuator/health/readiness").forEach {
            get(it).andExpect {
                status { isOk() }
                jsonPath("$.status") { value("UP") }
            }
        }
    }

    @Test
    fun `other actuator endpoints are not exposed`() {
        get("/actuator/env").andExpect { status { isUnauthorized() } }
    }
}
