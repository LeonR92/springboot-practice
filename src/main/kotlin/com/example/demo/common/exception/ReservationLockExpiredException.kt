package com.example.demo.common.exception

class ReservationLockExpiredException(id: Long) :
    RuntimeException("Reservation $id hold has expired")
