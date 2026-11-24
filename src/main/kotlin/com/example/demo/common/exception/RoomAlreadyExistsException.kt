package com.example.demo.common.exception

class RoomAlreadyExistsException(roomNumber: String) :
    RuntimeException("Room already exists in this hotel: $roomNumber")
