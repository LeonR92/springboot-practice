package com.example.demo.common.exception

class RoomTypeNotFoundException(
    id: Long,
) : RuntimeException("Room type with ID $id not found")
