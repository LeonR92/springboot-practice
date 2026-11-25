package com.example.demo.reservation.service

import com.example.demo.common.exception.InvalidStayDatesException
import com.example.demo.common.exception.ReservationNotFoundException
import com.example.demo.common.exception.RoomNotAvailableException
import com.example.demo.common.exception.RoomNotFoundException
import com.example.demo.hotel.model.Room
import com.example.demo.hotel.repository.RoomRepository
import com.example.demo.pricing.model.PricingContext
import com.example.demo.pricing.service.PricingService
import com.example.demo.reservation.dto.ReservationRequest
import com.example.demo.reservation.dto.ReservationResponse
import com.example.demo.reservation.dto.normalizedCoupon
import com.example.demo.reservation.dto.toResponse
import com.example.demo.reservation.dto.validateDates
import com.example.demo.reservation.model.Reservation
import com.example.demo.reservation.model.ReservationStatus
import com.example.demo.reservation.repository.ReservationRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.Duration
import java.time.Instant
import java.time.LocalDate

private val lockDurationMinutes = 15L

// TODO: confirm payment
@Service
class ReservationService(
    private val repository: ReservationRepository,
    private val roomRepository: RoomRepository,
    private val availabilityService: AvailabilityService,
    private val pricingService: PricingService,
) {
    @Transactional(readOnly = true)
    fun getAll(pageable: Pageable): Page<ReservationResponse> = repository.findAll(pageable).map { it.toResponse() }

    @Transactional(readOnly = true)
    fun getById(id: Long): ReservationResponse = findReservation(id).toResponse()

    @Transactional
    fun create(request: ReservationRequest): ReservationResponse {
        request.validateDates()
        val room = lockRoom(request.roomId)
        requireAvailable(request)
        val reservation =
            Reservation(
                room = room,
                guestName = request.guestName,
                guestEmail = request.guestEmail,
                checkIn = request.checkIn,
                checkOut = request.checkOut,
                status = request.status,
                lockUntil = Instant.now().plus(Duration.ofMinutes(lockDurationMinutes)),
                totalPrice = calculateTotalPrice(room, request.checkIn, request.checkOut, request.normalizedCoupon()),
                couponCode = request.normalizedCoupon(),
            )
        return repository.save(reservation).toResponse()
    }

    @Transactional
    fun update(
        id: Long,
        request: ReservationRequest,
    ): ReservationResponse {
        request.validateDates()
        val reservation = findReservation(id)

        val room = lockRoom(request.roomId)
        requireAvailable(request, excludeReservationId = id)

        // Re-price only when the stay or the coupon changes; otherwise keep the price the guest was quoted.
        val coupon = request.normalizedCoupon() ?: reservation.couponCode
        val stayChanged =
            reservation.room.id != room.id ||
                reservation.checkIn != request.checkIn ||
                reservation.checkOut != request.checkOut
        if (stayChanged || coupon != reservation.couponCode) {
            reservation.totalPrice = calculateTotalPrice(room, request.checkIn, request.checkOut, coupon, excludeReservationId = id)
            reservation.couponCode = coupon
        }

        reservation.room = room
        reservation.guestName = request.guestName
        reservation.guestEmail = request.guestEmail
        reservation.checkIn = request.checkIn
        reservation.checkOut = request.checkOut
        reservation.status = request.status

        return reservation.toResponse()
    }

    @Transactional
    fun delete(id: Long) {
        repository.delete(findReservation(id))
    }

    // Releases holds that were never confirmed in time. Availability already ignores expired holds,
    // so this only keeps the stored status honest.
    @Transactional
    fun expireStaleReservations(): Int = repository.expireHolds(ReservationStatus.PENDING, ReservationStatus.EXPIRED, Instant.now())

    private fun calculateTotalPrice(
        room: Room,
        checkIn: LocalDate,
        checkOut: LocalDate,
        couponCode: String?,
        excludeReservationId: Long? = null,
    ): BigDecimal {
        val context = PricingContext(room, checkIn, checkOut, couponCode, excludeReservationId)
        val basePrice = room.roomType.basePrice.multiply(BigDecimal.valueOf(context.nights))
        return pricingService.execute(context, basePrice)
    }

    private fun findReservation(id: Long): Reservation = repository.findById(id).orElseThrow { ReservationNotFoundException(id) }

    // Must run after lockRoom, so the check and the save can't interleave with another booking of the same room.
    // A cancelled or expired reservation holds no room, so it is not checked.
    private fun requireAvailable(
        request: ReservationRequest,
        excludeReservationId: Long? = null,
    ) {
        if (request.status == ReservationStatus.CANCELLED || request.status == ReservationStatus.EXPIRED) return
        if (!availabilityService.isRoomAvailable(request.roomId, request.checkIn, request.checkOut, excludeReservationId)) {
            throw RoomNotAvailableException(request.roomId)
        }
    }

    private fun lockRoom(id: Long): Room = roomRepository.findByIdForUpdate(id) ?: throw RoomNotFoundException(id)
}
