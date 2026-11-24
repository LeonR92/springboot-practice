package com.example.demo.hotel.service

import com.example.demo.common.exception.RoomTypeAlreadyExistsException
import com.example.demo.common.exception.RoomTypeInUseException
import com.example.demo.common.exception.RoomTypeNotFoundException
import com.example.demo.hotel.dto.RoomTypeRequest
import com.example.demo.hotel.dto.RoomTypeResponse
import com.example.demo.hotel.dto.toResponse
import com.example.demo.hotel.model.RoomType
import com.example.demo.hotel.repository.RoomRepository
import com.example.demo.hotel.repository.RoomTypeRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class RoomTypeService(
    private val repository: RoomTypeRepository,
    private val roomRepository: RoomRepository
) {
    @Transactional(readOnly = true)
    fun getAll(): List<RoomTypeResponse> =
        repository.findAll().map { it.toResponse() }

    @Transactional(readOnly = true)
    fun getById(id: Long): RoomTypeResponse =
        findRoomType(id).toResponse()

    @Transactional
    fun create(request: RoomTypeRequest): RoomTypeResponse {
        if (repository.existsByName(request.name)) {
            throw RoomTypeAlreadyExistsException(request.name)
        }
        val roomType = RoomType(
            name = request.name,
            tier = request.tier,
            basePrice = request.basePrice,
            capacity = request.capacity,
            description = request.description
        )
        return repository.save(roomType).toResponse()
    }

    @Transactional
    fun update(id: Long, request: RoomTypeRequest): RoomTypeResponse {
        val roomType = findRoomType(id)

        if (repository.existsByNameAndIdNot(request.name, id)) {
            throw RoomTypeAlreadyExistsException(request.name)
        }

        roomType.name = request.name
        roomType.tier = request.tier
        roomType.basePrice = request.basePrice
        roomType.capacity = request.capacity
        roomType.description = request.description

        return roomType.toResponse()
    }

    @Transactional
    fun delete(id: Long) {
        val roomType = findRoomType(id)
        if (roomRepository.existsByRoomTypeId(id)) {
            throw RoomTypeInUseException(id)
        }
        repository.delete(roomType)
    }

    private fun findRoomType(id: Long): RoomType =
        repository.findById(id).orElseThrow { RoomTypeNotFoundException(id) }
}
