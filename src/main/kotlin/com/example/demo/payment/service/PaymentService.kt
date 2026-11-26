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
import org.springframework.security.access.AccessDeniedException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.support.TransactionTemplate
import java.time.Instant

@Service
class PaymentService(
    private val repository: PaymentRepository,
    private val reservationRepository: ReservationRepository,
    private val eventPublisher: ApplicationEventPublisher,
    private val transactions: TransactionTemplate,
    providers: List<PaymentProvider>,
) {
    private val paymentProviders = providers.associateBy { it.method }

    // The provider call (with its retries) must not run while the reservation row is locked, hence three steps.
    fun createPayment(
        request: PaymentRequest,
        payerId: Long,
        isAdmin: Boolean,
    ): PaymentResponse {
        val provider = paymentProviders.getValue(request.method)
        val payment = requireNotNull(transactions.execute { open(request, payerId, isAdmin, provider) })

        try {
            provider.initiate(payment)
        } catch (e: Exception) {
            transactions.executeWithoutResult {
                repository.findById(requireNotNull(payment.id)).ifPresent { it.status = PaymentStatus.FAILED }
            }
            throw e
        }

        val saved = if (payment.token == null) payment else requireNotNull(transactions.execute { repository.save(payment) })
        return saved.toResponse()
    }

    private fun open(
        request: PaymentRequest,
        payerId: Long,
        isAdmin: Boolean,
        provider: PaymentProvider,
    ): Payment {
        val reservation =
            reservationRepository.findByIdForUpdate(request.reservationId)
                ?: throw ReservationNotFoundException(request.reservationId)

        requireOwnerOrAdmin(reservation.userId, payerId, isAdmin, "Reservation ${request.reservationId}")
        if (reservation.status != ReservationStatus.PENDING) {
            throw ReservationNotPayableException(request.reservationId, reservation.status.name)
        }
        if (!reservation.lockUntil.isAfter(Instant.now())) {
            throw ReservationLockExpiredException(request.reservationId)
        }
        if (repository.existsByReservationIdAndStatusIn(request.reservationId, PaymentStatus.OPEN)) {
            throw PaymentAlreadyPendingException(request.reservationId)
        }

        if (provider.confirmsReservationOnCreate) reservation.status = ReservationStatus.CONFIRMED

        return repository.save(
            Payment(
                userId = payerId,
                reservationId = request.reservationId,
                method = request.method,
                amount = reservation.totalPrice,
                status = provider.initialStatus,
            ),
        )
    }

    @Transactional
    fun confirmPayment(paymentId: Long): PaymentResponse =
        confirm(repository.findById(paymentId).orElseThrow { PaymentNotFoundException(paymentId) })

    @Transactional
    fun simulateSuccess(
        paymentId: Long,
        callerId: Long,
        isAdmin: Boolean,
    ): PaymentResponse {
        val payment = repository.findById(paymentId).orElseThrow { PaymentNotFoundException(paymentId) }
        requireOwnerOrAdmin(payment.userId, callerId, isAdmin, "Payment $paymentId")
        if (payment.method == PaymentMethod.INVOICE) throw PaymentNotConfirmableException(paymentId, "invoices are confirmed by an admin")
        return confirm(payment)
    }

    @Transactional
    fun resolveByToken(
        method: PaymentMethod,
        token: String,
        success: Boolean,
    ): PaymentResponse {
        val payment =
            repository.findByTokenAndMethod(token, method)
                ?: throw PaymentNotFoundException("Payment with token $token not found")
        return if (success) confirm(payment) else fail(payment)
    }

    private fun confirm(payment: Payment): PaymentResponse {
        if (payment.status == PaymentStatus.SUCCEEDED) return payment.toResponse()

        requireOpen(payment)

        val reservation =
            reservationRepository.findByIdForUpdate(payment.reservationId)
                ?: throw ReservationNotFoundException(payment.reservationId)

        // PENDING -> CONFIRMED; CONFIRMED (invoice) stays. EXPIRED/CANCELLED means money arrived for a dead
        // reservation: reject so it can be refunded instead of silently confirming.
        if (reservation.status != ReservationStatus.PENDING && reservation.status != ReservationStatus.CONFIRMED) {
            throw ReservationNotPayableException(requireNotNull(reservation.id), reservation.status.name)
        }

        paymentProviders.getValue(payment.method).confirm(payment, reservation)

        payment.status = PaymentStatus.SUCCEEDED
        reservation.status = ReservationStatus.CONFIRMED

        eventPublisher.publishEvent(PaymentConfirmationEvent(requireNotNull(payment.id)))

        return payment.toResponse()
    }

    private fun requireOwnerOrAdmin(
        ownerId: Long?,
        callerId: Long,
        isAdmin: Boolean,
        what: String,
    ) {
        if (!isAdmin && ownerId != callerId) throw AccessDeniedException("$what belongs to another user")
    }

    private fun requireOpen(payment: Payment) {
        if (payment.status !in PaymentStatus.OPEN) {
            throw PaymentNotConfirmableException(requireNotNull(payment.id), payment.status.name)
        }
    }

    // The reservation stays PENDING, so the guest can retry until the hold expires
    private fun fail(payment: Payment): PaymentResponse {
        if (payment.status == PaymentStatus.FAILED) return payment.toResponse()

        requireOpen(payment)

        payment.status = PaymentStatus.FAILED
        return payment.toResponse()
    }
}
