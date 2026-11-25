package com.example.demo

import com.example.demo.support.IntegrationTest
import org.junit.jupiter.api.Test

class AuthIT : IntegrationTest() {
    private val register = "/api/v1/auth/register"
    private val login = "/api/v1/auth/login"

    private fun registerBody(
        email: String = "ann@test.com",
        password: String = "password1",
    ) = """{"name":"Ann","email":"$email","password":"$password"}"""

    private fun loginBody(
        email: String = "ann@test.com",
        password: String = "password1",
    ) = """{"email":"$email","password":"$password"}"""

    @Test
    fun `register creates a user and rejects duplicates`() {
        post(register, registerBody()).andExpect {
            status { isCreated() }
            jsonPath("$.email") { value("ann@test.com") }
            jsonPath("$.role") { value("USER") }
        }
        post(register, registerBody()).andExpect { status { isConflict() } }
    }

    @Test
    fun `register validates input`() {
        post(register, registerBody(password = "short1")).andExpect { status { isBadRequest() } }
        post(register, registerBody(password = "onlyletters")).andExpect { status { isBadRequest() } }
        post(register, registerBody(email = "not-an-email")).andExpect { status { isBadRequest() } }
    }

    @Test
    fun `login returns a token that opens protected routes`() {
        post(register, registerBody())

        val token =
            post(login, loginBody())
                .andExpect { status { isOk() } }
                .body()
                .json<String>("$.token")

        get("/api/v1/hotels", "Bearer $token").andExpect { status { isOk() } }
    }

    @Test
    fun `login rejects bad credentials`() {
        post(register, registerBody())

        post(login, loginBody(password = "wrongpass1")).andExpect { status { isUnauthorized() } }
        post(login, loginBody(email = "nobody@test.com")).andExpect { status { isUnauthorized() } }
    }

    @Test
    fun `protected routes need a valid token`() {
        get("/api/v1/hotels").andExpect { status { isUnauthorized() } }
        get("/api/v1/hotels", "Bearer garbage").andExpect { status { isUnauthorized() } }
    }
}
