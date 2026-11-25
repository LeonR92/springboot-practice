package com.example.demo.common.exception

class ReservationNotPayableException(id: Long, status: String) :
    RuntimeException("Reservation $id cannot be paid, current status: $status")
