package com.example.demo.hotel.service

import com.example.demo.common.exception.AmenityAlreadyExistsException
import com.example.demo.common.exception.AmenityNotFoundException
import com.example.demo.hotel.dto.AmenityRequest
import com.example.demo.hotel.dto.AmenityResponse
import com.example.demo.hotel.dto.toResponse
import com.example.demo.hotel.model.Amenity
import com.example.demo.hotel.repository.AmenityRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AmenityService(
    private val repository: AmenityRepository
) {
    // CRUD
    fun create(request: AmenityRequest): AmenityResponse {
        if (repository.existsByName(request.name)) {
            throw AmenityAlreadyExistsException(request.name)
        }
        val amenity = Amenity(
            name = request.name,
            description = request.description,
            category = request.category,
            icon = request.icon,
            isFree = request.isFree
        )
        return repository.save(amenity).toResponse()
    }

    fun getAll(): List<AmenityResponse> =
        repository.findAll().map { it.toResponse() }

    fun getById(id: Long): AmenityResponse =
        findAmenity(id).toResponse()

    fun getAmenitiesByHotelId(hotelId: Long): List<AmenityResponse> =
        repository.findAmenitiesByHotelId(hotelId).map { it.toResponse() }

    @Transactional
    fun update(id: Long, request: AmenityRequest): AmenityResponse {
        val amenity = findAmenity(id)

        if (repository.existsByNameAndIdNot(request.name, id)) {
            throw AmenityAlreadyExistsException(request.name)
        }

        amenity.name = request.name
        amenity.description = request.description
        amenity.category = request.category
        amenity.icon = request.icon
        amenity.isFree = request.isFree

        return amenity.toResponse()
    }

    @Transactional
    fun delete(id: Long) {
        repository.delete(findAmenity(id))
    }

    private fun findAmenity(id: Long): Amenity =
        repository.findById(id).orElseThrow { AmenityNotFoundException(id) }
}
