package com.example.demo.user.dto

import com.example.demo.user.model.User
import com.example.demo.user.model.UserRole

data class UserResponse(
    val id: Long,
    val name: String,
    val email: String,
    val role: UserRole,
)

fun User.toResponse() =
    UserResponse(
        id = requireNotNull(id),
        name = name,
        email = email,
        role = role,
    )
