package com.example.demo.hotel.dto

import com.example.demo.hotel.model.Address
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import java.time.LocalTime

data class HotelRequest(
    @field:NotBlank
    @field:Size(max = 255)
    val name: String,

    @field:Valid
    val address: Address,

    val checkInTime: LocalTime = LocalTime.of(15, 0),

    val checkOutTime: LocalTime = LocalTime.of(10, 0),

    val amenityIds: Set<Long> = emptySet()
)
