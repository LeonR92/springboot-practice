package com.example.demo.common.exception

class RoomTypeAlreadyExistsException(
    name: String,
) : RuntimeException("Room type already exists: $name")
