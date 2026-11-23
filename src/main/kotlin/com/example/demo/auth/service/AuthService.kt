package com.example.demo.auth.service

import com.example.demo.auth.dto.LoginRequest
import com.example.demo.auth.dto.LoginResponse
import com.example.demo.common.exception.InvalidCredentialsException
import com.example.demo.user.service.UserService
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

@Service
class AuthService(
    private val passwordEncoder: PasswordEncoder,
    private val jwtService: JwtService,
    private val userService: UserService
) {

    fun login(request: LoginRequest): LoginResponse {
        val user = userService.findByEmail(request.email)
            ?: throw InvalidCredentialsException()

        if (!passwordEncoder.matches(request.password, user.hashedPassword)) {
            throw InvalidCredentialsException()
        }

        return LoginResponse(jwtService.generateToken(requireNotNull(user.id)))
    }
}
