package com.example.demo.user.service

import com.example.demo.common.exception.EmailAlreadyExistsException
import com.example.demo.common.exception.UserNotFoundException
import com.example.demo.user.dto.CreateUserRequest
import com.example.demo.user.dto.UpdateUserRequest
import com.example.demo.user.dto.UserResponse
import com.example.demo.user.dto.toResponse
import com.example.demo.user.model.User
import com.example.demo.user.repository.UserRepository
import org.springframework.stereotype.Service

@Service
class UserService(
    private val repository: UserRepository
) {

    fun getUsers(): List<UserResponse> =
        repository.findAll().map { it.toResponse() }

    fun createUser(request: CreateUserRequest): UserResponse {
        if (repository.findByEmail(request.email) != null) {
            throw EmailAlreadyExistsException(request.email)
        }
        val user = repository.save(
            User(name = request.name, email = request.email)
        )
        return user.toResponse()
    }

    fun deleteUserByEmail(email: String) {
        val userToDelete = repository.findByEmail(email)
            ?: throw UserNotFoundException(email)
        repository.delete(userToDelete)
    }

    fun updateUserByEmail(email: String, request: UpdateUserRequest): UserResponse {
        val user = repository.findByEmail(email)
            ?: throw UserNotFoundException(email)

        request.email?.let { newEmail ->
            if (newEmail != user.email && repository.findByEmail(newEmail) != null) {
                throw EmailAlreadyExistsException(newEmail)
            }
            user.email = newEmail
        }
        request.name?.let { user.name = it }

        return repository.save(user).toResponse()
    }
}
