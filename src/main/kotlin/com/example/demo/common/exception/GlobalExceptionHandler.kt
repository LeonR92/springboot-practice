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

    @ExceptionHandler(RoomTypeNotFoundException::class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    fun handleRoomTypeNotFound(e: RoomTypeNotFoundException) = mapOf("error" to e.message)

    @ExceptionHandler(RoomNotFoundException::class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    fun handleRoomNotFound(e: RoomNotFoundException) = mapOf("error" to e.message)

    @ExceptionHandler(RoomTypeAlreadyExistsException::class)
    @ResponseStatus(HttpStatus.CONFLICT)
    fun handleRoomTypeExists(e: RoomTypeAlreadyExistsException) = mapOf("error" to e.message)

    @ExceptionHandler(RoomAlreadyExistsException::class)
    @ResponseStatus(HttpStatus.CONFLICT)
    fun handleRoomExists(e: RoomAlreadyExistsException) = mapOf("error" to e.message)

    @ExceptionHandler(RoomTypeInUseException::class)
    @ResponseStatus(HttpStatus.CONFLICT)
    fun handleRoomTypeInUse(e: RoomTypeInUseException) = mapOf("error" to e.message)

    @ExceptionHandler(ReservationNotFoundException::class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    fun handleReservationNotFound(e: ReservationNotFoundException) = mapOf("error" to e.message)

    @ExceptionHandler(InvalidStayDatesException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleInvalidStayDates(e: InvalidStayDatesException) = mapOf("error" to e.message)

    @ExceptionHandler(RoomNotAvailableException::class)
    @ResponseStatus(HttpStatus.CONFLICT)
    fun handleRoomNotAvailable(e: RoomNotAvailableException) = mapOf("error" to e.message)

    @ExceptionHandler(InvalidCredentialsException::class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    fun handleInvalidCredentials(e: InvalidCredentialsException) = mapOf("error" to e.message)
}
