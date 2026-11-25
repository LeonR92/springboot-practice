package com.example.demo.common.exception

class PaymentAlreadyPendingException(reservationId: Long) :
    RuntimeException("A payment is already pending for reservation $reservationId")
