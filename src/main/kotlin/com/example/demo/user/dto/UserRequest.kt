package com.example.demo.user.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

data class CreateUserRequest(
    @field:NotBlank
    val name: String,
    @field:NotBlank
    @field:Email
    val email: String,
    @field:Size(min = 8, message = "must be at least 8 characters")
    @field:Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).*$", message = "must contain a letter and a digit")
    val password: String,
)

data class UpdateUserRequest(
    @field:Pattern(regexp = ".*\\S.*", message = "must not be blank")
    val name: String? = null,
    @field:Pattern(regexp = ".*\\S.*", message = "must not be blank")
    @field:Email
    val email: String? = null,
)
