package com.example.demo.hotel.service

import com.example.demo.common.exception.AmenityNotFoundException
import com.example.demo.common.exception.HotelNotFoundException
import com.example.demo.hotel.dto.HotelRequest
import com.example.demo.hotel.dto.HotelResponse
import com.example.demo.hotel.dto.toResponse
import com.example.demo.hotel.model.Amenity
import com.example.demo.hotel.model.Hotel
import com.example.demo.hotel.repository.AmenityRepository
import com.example.demo.hotel.repository.HotelRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class HotelService(
    private val repository: HotelRepository,
    private val amenityRepository: AmenityRepository
) {
    // CRUD
    @Transactional(readOnly = true)
    fun getHotels(): List<HotelResponse> =
        repository.findAll().map { it.toResponse() }

    @Transactional(readOnly = true)
    fun getHotelById(id: Long): HotelResponse =
        findHotel(id).toResponse()

    @Transactional
    fun createHotel(request: HotelRequest): HotelResponse {
        val hotel = Hotel(
            name = request.name,
            address = request.address,
            checkInTime = request.checkInTime,
            checkOutTime = request.checkOutTime,
            amenities = findAmenities(request.amenityIds)
        )
        return repository.save(hotel).toResponse()
    }

    @Transactional
    fun updateHotel(id: Long, request: HotelRequest): HotelResponse {
        val hotel = findHotel(id)

        hotel.name = request.name
        hotel.address = request.address
        hotel.checkInTime = request.checkInTime
        hotel.checkOutTime = request.checkOutTime
        hotel.amenities = findAmenities(request.amenityIds)

        return hotel.toResponse()
    }

    @Transactional
    fun deleteHotel(id: Long) {
        repository.delete(findHotel(id))
    }

    private fun findHotel(id: Long): Hotel =
        repository.findById(id).orElseThrow { HotelNotFoundException(id) }

    private fun findAmenities(ids: Set<Long>): MutableSet<Amenity> {
        val found = amenityRepository.findAllById(ids)
        val missing = ids - found.mapNotNull { it.id }.toSet()
        if (missing.isNotEmpty()) {
            throw AmenityNotFoundException(missing.first())
        }
        return found.toMutableSet()
    }
}
