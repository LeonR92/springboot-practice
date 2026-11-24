package com.example.demo.common.exception

class RoomNotAvailableException(roomId: Long) :
    RuntimeException("Room $roomId is not available for the selected dates")
