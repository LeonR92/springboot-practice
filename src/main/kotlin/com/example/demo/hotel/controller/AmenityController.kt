package com.example.demo.hotel.controller

import com.example.demo.hotel.dto.AmenityRequest
import com.example.demo.hotel.dto.AmenityResponse
import com.example.demo.hotel.service.AmenityService
import jakarta.validation.Valid
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
@RequestMapping("/v1/amenities")
class AmenityController(
    private val service: AmenityService,
) {
    @GetMapping
    fun getAmenities(): List<AmenityResponse> = service.getAll()

    @GetMapping("/{id}")
    fun getAmenityById(
        @PathVariable id: Long,
    ): AmenityResponse = service.getById(id)

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createAmenity(
        @Valid @RequestBody request: AmenityRequest,
    ): AmenityResponse = service.create(request)

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    fun updateAmenity(
        @PathVariable id: Long,
        @Valid @RequestBody request: AmenityRequest,
    ): AmenityResponse = service.update(id, request)

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteAmenity(
        @PathVariable id: Long,
    ) = service.delete(id)

    @GetMapping("/hotels/{hotelId}")
    fun getAmenityViaHotelId(
        @PathVariable hotelId: Long,
    ): List<AmenityResponse> = service.getAmenitiesByHotelId(hotelId)
}
