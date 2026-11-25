package com.example.demo.payment.service

import com.example.demo.common.exception.PaymentAlreadyPendingException
import com.example.demo.common.exception.PaymentNotConfirmableException
import com.example.demo.common.exception.PaymentNotFoundException
import com.example.demo.common.exception.ReservationLockExpiredException
import com.example.demo.common.exception.ReservationNotFoundException
import com.example.demo.common.exception.ReservationNotPayableException
import com.example.demo.payment.dto.PaymentRequest
import com.example.demo.payment.dto.PaymentResponse
import com.example.demo.payment.dto.toResponse
import com.example.demo.payment.event.PaymentConfirmationEvent
import com.example.demo.payment.model.Payment
import com.example.demo.payment.model.PaymentMethod
import com.example.demo.payment.model.PaymentStatus
import com.example.demo.payment.provider.PaymentProvider
import com.example.demo.payment.repository.PaymentRepository
import com.example.demo.reservation.model.ReservationStatus
import com.example.demo.reservation.repository.ReservationRepository
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Service
class PaymentService(
    private val repository: PaymentRepository,
    private val reservationRepository: ReservationRepository,
    private val eventPublisher: ApplicationEventPublisher,
    providers: List<PaymentProvider>
) {
    private val paymentProviders = providers.associateBy { it.method }

    @Transactional
    fun createPayment(request: PaymentRequest): PaymentResponse {
        val reservation = reservationRepository.findByIdForUpdate(request.reservationId)
            ?: throw ReservationNotFoundException(request.reservationId)

        if (reservation.status != ReservationStatus.PENDING) {
            throw ReservationNotPayableException(request.reservationId, reservation.status.name)
        }
        if (!reservation.lockUntil.isAfter(Instant.now())) {
            throw ReservationLockExpiredException(request.reservationId)
        }
        if (repository.existsByReservationIdAndStatusIn(request.reservationId, PaymentStatus.OPEN)) {
            throw PaymentAlreadyPendingException(request.reservationId)
        }

        val provider = paymentProviders.getValue(request.method)
        val payment = Payment(
            userId = request.userId,
            reservationId = request.reservationId,
            method = request.method,
            amount = reservation.totalPrice,
            status = provider.initialStatus
        )
        provider.initiate(payment, reservation)

        return repository.save(payment).toResponse()
    }

    @Transactional
    fun confirmPayment(paymentId: Long): PaymentResponse =
        confirm(repository.findById(paymentId).orElseThrow { PaymentNotFoundException(paymentId) })

    @Transactional
    fun confirmByToken(method: PaymentMethod, token: String): PaymentResponse =
        confirm(
            repository.findByTokenAndMethod(token, method)
                ?: throw PaymentNotFoundException("Payment with token $token not found")
        )

    private fun confirm(payment: Payment): PaymentResponse {
        if (payment.status == PaymentStatus.SUCCEEDED) return payment.toResponse()

        if (payment.status !in PaymentStatus.OPEN) {
            throw PaymentNotConfirmableException(requireNotNull(payment.id), payment.status.name)
        }

        val reservation = reservationRepository.findByIdForUpdate(payment.reservationId)
            ?: throw ReservationNotFoundException(payment.reservationId)

        // PENDING -> CONFIRMED; CONFIRMED (invoice) stays. EXPIRED/CANCELLED means money arrived for a dead
        // reservation: reject so it can be refunded instead of silently confirming.
        if (reservation.status != ReservationStatus.PENDING && reservation.status != ReservationStatus.CONFIRMED) {
            throw ReservationNotPayableException(reservation.id!!, reservation.status.name)
        }

        paymentProviders.getValue(payment.method).confirm(payment, reservation)

        payment.status = PaymentStatus.SUCCEEDED
        reservation.status = ReservationStatus.CONFIRMED

        // Invoice PDF + email run
        eventPublisher.publishEvent(PaymentConfirmationEvent(requireNotNull(payment.id)))

        return payment.toResponse()
    }
}
