package com.example.demo.common.exception

class RoomTypeInUseException(id: Long) :
    RuntimeException("Room type $id still has rooms assigned")
