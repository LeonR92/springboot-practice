package com.example.demo.auth.controller

import com.example.demo.auth.dto.LoginRequest
import com.example.demo.auth.dto.LoginResponse
import com.example.demo.auth.service.AuthService
import com.example.demo.user.dto.CreateUserRequest
import com.example.demo.user.dto.UserResponse
import com.example.demo.user.service.UserService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/v1/auth")
class AuthController(
    private val authService: AuthService,
    private val userService: UserService,
) {
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    fun register(
        @Valid @RequestBody body: CreateUserRequest,
    ): UserResponse = userService.createUser(body)

    @PostMapping("/login")
    fun login(
        @Valid @RequestBody body: LoginRequest,
    ): LoginResponse = authService.login(body)
}
