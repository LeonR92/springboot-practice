package com.example.demo.hotel.dto

import com.example.demo.hotel.model.RoomTier
import com.example.demo.hotel.model.RoomType
import java.math.BigDecimal

data class RoomTypeResponse(
    val id: Long,
    val name: String,
    val tier: RoomTier,
    val basePrice: BigDecimal,
    val capacity: Int,
    val description: String?,
)

fun RoomType.toResponse() =
    RoomTypeResponse(
        id = requireNotNull(id),
        name = name,
        tier = tier,
        basePrice = basePrice,
        capacity = capacity,
        description = description,
    )
