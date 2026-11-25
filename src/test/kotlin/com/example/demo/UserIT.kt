package com.example.demo

import com.example.demo.support.IntegrationTest
import org.junit.jupiter.api.Test

class UserIT : IntegrationTest() {
    private val url = "/api/v1/users"
    private val target = "$url?email=n@test.com"
    private val newUser = """{"name":"N","email":"n@test.com","password":"password1"}"""

    @Test
    fun `plain users cannot manage users`() {
        val token = userToken()

        get(url, token).andExpect { status { isForbidden() } }
        post(url, newUser, token).andExpect { status { isForbidden() } }
        patch(target, """{"name":"X"}""", token).andExpect { status { isForbidden() } }
        delete(target, token).andExpect { status { isForbidden() } }
    }

    @Test
    fun `admin can create list update and delete users`() {
        val token = adminToken()

        post(url, newUser, token).andExpect { status { isCreated() } }
        get(url, token).andExpect { jsonPath("$.length()") { value(2) } }
        patch(target, """{"name":"Renamed"}""", token).andExpect {
            status { isOk() }
            jsonPath("$.name") { value("Renamed") }
        }
        delete(target, token).andExpect { status { isNoContent() } }
        delete(target, token).andExpect { status { isNotFound() } }
    }
}
