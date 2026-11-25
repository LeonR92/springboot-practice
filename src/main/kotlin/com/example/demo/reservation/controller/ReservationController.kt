package com.example.demo.reservation.controller

import com.example.demo.reservation.dto.ReservationRequest
import com.example.demo.reservation.dto.ReservationResponse
import com.example.demo.reservation.service.ReservationService
import jakarta.validation.Valid
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/v1/reservations")
class ReservationController(
    private val service: ReservationService,
) {
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    fun getReservations(pageable: Pageable): Page<ReservationResponse> = service.getAll(pageable)

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}")
    fun getReservationById(
        @PathVariable id: Long,
    ): ReservationResponse = service.getById(id)

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createReservation(
        @Valid @RequestBody request: ReservationRequest,
    ): ReservationResponse = service.create(request)

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    fun updateReservation(
        @PathVariable id: Long,
        @Valid @RequestBody request: ReservationRequest,
    ): ReservationResponse = service.update(id, request)

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteReservation(
        @PathVariable id: Long,
    ) = service.delete(id)
}
