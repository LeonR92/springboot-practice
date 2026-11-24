package com.example.demo.hotel.controller

import com.example.demo.hotel.dto.HotelRequest
import com.example.demo.hotel.dto.HotelResponse
import com.example.demo.hotel.service.HotelService
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
@RequestMapping("/v1/hotels")
class HotelController(
    private val service: HotelService
) {

    @GetMapping
    fun getHotels(): List<HotelResponse> = service.getHotels()

    @GetMapping("/{id}")
    fun getHotelById(@PathVariable id: Long): HotelResponse = service.getHotelById(id)

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createHotel(@Valid @RequestBody request: HotelRequest): HotelResponse =
        service.createHotel(request)

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    fun updateHotel(
        @PathVariable id: Long,
        @Valid @RequestBody request: HotelRequest
    ): HotelResponse = service.updateHotel(id, request)

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteHotel(@PathVariable id: Long) = service.deleteHotel(id)
}
