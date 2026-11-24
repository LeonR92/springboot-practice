package com.example.demo.hotel.dto

import com.example.demo.hotel.model.Room
import com.example.demo.hotel.model.RoomStatus

data class RoomResponse(
    val id: Long,
    val hotelId: Long,
    val roomNumber: String,
    val floor: Int?,
    val status: RoomStatus,
    val roomType: RoomTypeResponse
)

fun Room.toResponse() = RoomResponse(
    id = requireNotNull(id),
    hotelId = requireNotNull(hotel.id),
    roomNumber = roomNumber,
    floor = floor,
    status = status,
    roomType = roomType.toResponse()
)
