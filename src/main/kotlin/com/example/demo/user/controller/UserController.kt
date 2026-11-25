package com.example.demo.user.controller

import com.example.demo.user.dto.CreateUserRequest
import com.example.demo.user.dto.UpdateUserRequest
import com.example.demo.user.dto.UserResponse
import com.example.demo.user.service.UserService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/v1/users")
class UserController(
    private val service: UserService,
) {
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    fun getUsers(): List<UserResponse> = service.getUsers()

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createUser(
        @Valid @RequestBody body: CreateUserRequest,
    ): UserResponse = service.createUser(body)

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping
    fun updateUser(
        @RequestParam email: String,
        @Valid @RequestBody body: UpdateUserRequest,
    ): UserResponse = service.updateUserByEmail(email, body)

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteUser(
        @RequestParam email: String,
    ) = service.deleteUserByEmail(email)
}
