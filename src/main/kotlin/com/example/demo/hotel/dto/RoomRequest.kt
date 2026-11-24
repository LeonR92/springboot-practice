package com.example.demo.hotel.dto

import com.example.demo.hotel.model.RoomStatus
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class RoomRequest(
    @field:NotBlank
    @field:Size(max = 50)
    val roomNumber: String,

    val roomTypeId: Long,

    val floor: Int? = null,

    val status: RoomStatus = RoomStatus.ACTIVE
)
