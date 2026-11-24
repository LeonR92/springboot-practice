package com.example.demo.hotel.dto

import com.example.demo.hotel.model.RoomTier
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.PositiveOrZero
import jakarta.validation.constraints.Size
import java.math.BigDecimal

data class RoomTypeRequest(
    @field:NotBlank
    @field:Size(max = 255)
    val name: String,

    val tier: RoomTier,

    @field:PositiveOrZero
    val basePrice: BigDecimal,

    @field:Min(1)
    val capacity: Int,

    @field:Size(max = 255)
    val description: String? = null
)
