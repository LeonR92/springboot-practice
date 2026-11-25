package com.example.demo.hotel.controller

import com.example.demo.hotel.dto.RoomRequest
import com.example.demo.hotel.dto.RoomResponse
import com.example.demo.hotel.service.RoomService
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
@RequestMapping("/v1/hotels/{hotelId}/rooms")
class RoomController(
    private val service: RoomService,
) {
    @GetMapping
    fun getRooms(
        @PathVariable hotelId: Long,
    ): List<RoomResponse> = service.getRooms(hotelId)

    @GetMapping("/{id}")
    fun getRoomById(
        @PathVariable hotelId: Long,
        @PathVariable id: Long,
    ): RoomResponse = service.getRoom(hotelId, id)

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createRoom(
        @PathVariable hotelId: Long,
        @Valid @RequestBody request: RoomRequest,
    ): RoomResponse = service.createRoom(hotelId, request)

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    fun updateRoom(
        @PathVariable hotelId: Long,
        @PathVariable id: Long,
        @Valid @RequestBody request: RoomRequest,
    ): RoomResponse = service.updateRoom(hotelId, id, request)

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteRoom(
        @PathVariable hotelId: Long,
        @PathVariable id: Long,
    ) = service.deleteRoom(hotelId, id)
}
