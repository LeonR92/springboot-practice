package com.example.demo.hotel.controller

import com.example.demo.hotel.dto.RoomTypeRequest
import com.example.demo.hotel.dto.RoomTypeResponse
import com.example.demo.hotel.service.RoomTypeService
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
@RequestMapping("/v1/room-types")
class RoomTypeController(
    private val service: RoomTypeService,
) {
    @GetMapping
    fun getRoomTypes(): List<RoomTypeResponse> = service.getAll()

    @GetMapping("/{id}")
    fun getRoomTypeById(
        @PathVariable id: Long,
    ): RoomTypeResponse = service.getById(id)

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createRoomType(
        @Valid @RequestBody request: RoomTypeRequest,
    ): RoomTypeResponse = service.create(request)

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    fun updateRoomType(
        @PathVariable id: Long,
        @Valid @RequestBody request: RoomTypeRequest,
    ): RoomTypeResponse = service.update(id, request)

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteRoomType(
        @PathVariable id: Long,
    ) = service.delete(id)
}
