package com.example.demo.hotel.dto

import com.example.demo.hotel.model.Address
import com.example.demo.hotel.model.Hotel
import java.time.LocalTime

data class HotelResponse(
    val id: Long,
    val name: String,
    val address: Address,
    val checkInTime: LocalTime,
    val checkOutTime: LocalTime,
    val amenities: List<AmenityResponse>,
)

fun Hotel.toResponse() =
    HotelResponse(
        id = requireNotNull(id),
        name = name,
        address = address,
        checkInTime = checkInTime,
        checkOutTime = checkOutTime,
        amenities = amenities.map { it.toResponse() },
    )
