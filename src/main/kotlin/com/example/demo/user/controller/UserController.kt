package com.example.demo.user.controller

import com.example.demo.user.dto.CreateUserRequest
import com.example.demo.user.dto.UpdateUserRequest
import com.example.demo.user.dto.UserResponse
import com.example.demo.user.service.UserService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/v1/users")
class UserController(
    private val service: UserService
) {

    @GetMapping
    fun getUsers(): List<UserResponse> = service.getUsers()

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createUser(@Valid @RequestBody body: CreateUserRequest): UserResponse =
        service.createUser(body)

    @PatchMapping
    fun updateUser(
        @RequestParam email: String,
        @Valid @RequestBody body: UpdateUserRequest
    ): UserResponse = service.updateUserByEmail(email, body)

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteUser(@RequestParam email: String) = service.deleteUserByEmail(email)
}
