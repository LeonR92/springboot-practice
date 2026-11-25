package com.example.demo.common.security

import org.springframework.security.core.Authentication

// The JWT subject is the user id (see JwtService)
val Authentication.userId: Long get() = name.toLong()

val Authentication.isAdmin: Boolean get() = authorities.any { it.authority == "ROLE_ADMIN" }
