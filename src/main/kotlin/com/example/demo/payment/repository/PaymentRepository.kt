package com.example.demo.payment.repository

import com.example.demo.payment.model.Payment
import com.example.demo.payment.model.PaymentMethod
import com.example.demo.payment.model.PaymentStatus
import org.springframework.data.jpa.repository.JpaRepository

interface PaymentRepository : JpaRepository<Payment, Long> {

    fun existsByReservationIdAndStatusIn(reservationId: Long, statuses: Collection<PaymentStatus>): Boolean

    fun findByTokenAndMethod(token: String, method: PaymentMethod): Payment?
}
