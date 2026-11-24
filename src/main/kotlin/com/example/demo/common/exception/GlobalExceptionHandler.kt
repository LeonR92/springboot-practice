package com.example.demo.common.exception

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(EmailAlreadyExistsException::class)
    @ResponseStatus(HttpStatus.CONFLICT)
    fun handleEmailExists(e: EmailAlreadyExistsException) = mapOf("error" to e.message)

    @ExceptionHandler(UserNotFoundException::class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    fun handleUserNotFound(e: UserNotFoundException) = mapOf("error" to e.message)

    @ExceptionHandler(AmenityNotFoundException::class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    fun handleAmenityNotFound(e: AmenityNotFoundException) = mapOf("error" to e.message)

    @ExceptionHandler(AmenityAlreadyExistsException::class)
    @ResponseStatus(HttpStatus.CONFLICT)
    fun handleAmenityExists(e: AmenityAlreadyExistsException) = mapOf("error" to e.message)

    @ExceptionHandler(HotelNotFoundException::class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    fun handleHotelNotFound(e: HotelNotFoundException) = mapOf("error" to e.message)

    @ExceptionHandler(InvalidCredentialsException::class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    fun handleInvalidCredentials(e: InvalidCredentialsException) = mapOf("error" to e.message)
}
