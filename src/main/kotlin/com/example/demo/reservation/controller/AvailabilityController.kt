package com.example.demo.reservation.controller

import com.example.demo.hotel.dto.RoomResponse
import com.example.demo.reservation.dto.AvailabilitySearchRequest
import com.example.demo.reservation.service.AvailabilityService
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate

@RestController
@RequestMapping("/v1/hotels/{hotelId}/availability")
class AvailabilityController(
    private val service: AvailabilityService,
) {
    @GetMapping
    fun findAvailableRooms(
        @PathVariable hotelId: Long,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) checkIn: LocalDate,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) checkOut: LocalDate,
        @RequestParam(required = false) roomTypeId: Long?,
        pageable: Pageable,
    ): Page<RoomResponse> =
        service.findAvailability(
            AvailabilitySearchRequest(hotelId, roomTypeId, checkIn, checkOut),
            pageable,
        )
}
