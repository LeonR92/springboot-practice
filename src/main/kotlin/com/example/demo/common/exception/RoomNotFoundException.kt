package com.example.demo.common.exception

class RoomNotFoundException(id: Long) :
    RuntimeException("Room with ID $id not found")
