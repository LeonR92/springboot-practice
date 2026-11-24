package com.example.demo.reservation.service

import com.example.demo.hotel.dto.RoomResponse
import com.example.demo.hotel.dto.toResponse
import com.example.demo.hotel.model.RoomStatus
import com.example.demo.hotel.repository.RoomRepository
import com.example.demo.reservation.dto.AvailabilitySearchRequest
import com.example.demo.reservation.dto.validateBookingDates
import com.example.demo.reservation.repository.ReservationRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.time.LocalDate

@Service
class AvailabilityService(
    private val roomRepository: RoomRepository,
    private val reservationRepository: ReservationRepository
) {

    @Transactional(readOnly = true)
    fun findAvailability(
        request: AvailabilitySearchRequest,
        pageable: Pageable
    ): Page<RoomResponse> {
        request.validateBookingDates()
        return roomRepository.findAvailableRooms(
            hotelId = request.hotelId,
            roomTypeId = request.roomTypeId,
            checkIn = request.checkIn,
            checkOut = request.checkOut,
            now = Instant.now(),
            pageable = pageable
        ).map { it.toResponse() }
    }


    fun isRoomAvailable(
        roomId: Long,
        checkIn: LocalDate,
        checkOut: LocalDate,
        excludeReservationId: Long? = null
    ): Boolean =
        roomRepository.existsByIdAndStatus(roomId, RoomStatus.ACTIVE) &&
            !reservationRepository.existsBlockingReservation(
                roomId, checkIn, checkOut, Instant.now(), excludeReservationId
            )
}
