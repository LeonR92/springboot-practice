package com.example.demo.user.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern

data class CreateUserRequest(
    @field:NotBlank
    val name: String,

    @field:NotBlank
    @field:Email
    val email: String
)

data class UpdateUserRequest(
    @field:Pattern(regexp = ".*\\S.*", message = "must not be blank")
    val name: String? = null,

    @field:Pattern(regexp = ".*\\S.*", message = "must not be blank")
    @field:Email
    val email: String? = null
)
