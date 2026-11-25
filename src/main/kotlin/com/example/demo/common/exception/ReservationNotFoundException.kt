package com.example.demo.common.exception

class ReservationNotFoundException(
    id: Long,
) : RuntimeException("Reservation with ID $id not found")
