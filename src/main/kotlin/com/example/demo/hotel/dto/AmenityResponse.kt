package com.example.demo.hotel.dto

import com.example.demo.hotel.model.Amenity
import com.example.demo.hotel.model.AmenityCategory

data class AmenityResponse(
    val id: Long,
    val name: String,
    val description: String?,
    val category: AmenityCategory,
    val icon: String?,
    val isFree: Boolean
)

fun Amenity.toResponse(): AmenityResponse {
    return AmenityResponse(
        id = requireNotNull(id),
        name = name,
        description = description,
        category = category,
        icon = icon,
        isFree = isFree
    )
}
