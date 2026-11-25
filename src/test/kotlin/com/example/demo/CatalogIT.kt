package com.example.demo

import com.example.demo.support.IntegrationTest
import org.junit.jupiter.api.Test

class CatalogIT : IntegrationTest() {
    private val hotelBody = """{"name":"Grand","address":{"country":"DE","city":"Berlin"}}"""
    private val typeBody = """{"name":"Deluxe","tier":"DELUXE","basePrice":150.00,"capacity":2}"""

    @Test
    fun `admin manages a hotel with amenities`() {
        val token = adminToken()
        val amenityId = create("/api/v1/amenities", """{"name":"Pool","category":"WELLNESS"}""", token)
        val hotelId =
            create(
                "/api/v1/hotels",
                """{"name":"Grand","address":{"country":"DE","city":"Berlin"},"amenityIds":[$amenityId]}""",
                token,
            )

        get("/api/v1/hotels/$hotelId", token).andExpect {
            jsonPath("$.name") { value("Grand") }
            jsonPath("$.amenities[0].name") { value("Pool") }
        }
        put("/api/v1/hotels/$hotelId", """{"name":"Grander","address":{"country":"DE","city":"Berlin"}}""", token)
            .andExpect { jsonPath("$.name") { value("Grander") } }
        delete("/api/v1/hotels/$hotelId", token).andExpect { status { isNoContent() } }
        get("/api/v1/hotels/$hotelId", token).andExpect { status { isNotFound() } }
    }

    @Test
    fun `rooms belong to a hotel and room numbers are unique per hotel`() {
        val token = adminToken()
        val hotelId = create("/api/v1/hotels", hotelBody, token)
        val typeId = create("/api/v1/room-types", typeBody, token)
        val room = """{"roomNumber":"101","roomTypeId":$typeId}"""

        post("/api/v1/hotels/$hotelId/rooms", room, token).andExpect {
            status { isCreated() }
            jsonPath("$.roomType.name") { value("Deluxe") }
        }
        post("/api/v1/hotels/$hotelId/rooms", room, token).andExpect { status { isConflict() } }
        delete("/api/v1/room-types/$typeId", token).andExpect { status { isConflict() } }
    }

    @Test
    fun `room types reject duplicates and invalid input`() {
        val token = adminToken()

        post("/api/v1/room-types", typeBody, token).andExpect { status { isCreated() } }
        post("/api/v1/room-types", typeBody, token).andExpect { status { isConflict() } }
        post("/api/v1/room-types", """{"name":"Bad","tier":"SUITE","basePrice":10,"capacity":0}""", token)
            .andExpect { status { isBadRequest() } }
    }

    @Test
    fun `plain users can read but not write`() {
        val admin = adminToken()
        val user = userToken()
        val hotelId = create("/api/v1/hotels", hotelBody, admin)

        get("/api/v1/hotels/$hotelId", user).andExpect { status { isOk() } }
        get("/api/v1/amenities", user).andExpect { status { isOk() } }
        get("/api/v1/room-types", user).andExpect { status { isOk() } }
        post("/api/v1/hotels", hotelBody, user).andExpect { status { isForbidden() } }
        post("/api/v1/amenities", """{"name":"Gym","category":"GENERAL"}""", user).andExpect { status { isForbidden() } }
        post("/api/v1/room-types", typeBody, user).andExpect { status { isForbidden() } }
        post("/api/v1/hotels/$hotelId/rooms", """{"roomNumber":"1","roomTypeId":1}""", user).andExpect { status { isForbidden() } }
    }

    @Test
    fun `unknown ids return 404`() {
        val token = adminToken()

        get("/api/v1/hotels/999", token).andExpect { status { isNotFound() } }
        get("/api/v1/room-types/999", token).andExpect { status { isNotFound() } }
        get("/api/v1/amenities/999", token).andExpect { status { isNotFound() } }
    }
}
