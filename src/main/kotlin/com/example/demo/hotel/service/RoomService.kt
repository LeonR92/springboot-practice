package com.example.demo.hotel.service

import com.example.demo.common.exception.HotelNotFoundException
import com.example.demo.common.exception.RoomAlreadyExistsException
import com.example.demo.common.exception.RoomNotFoundException
import com.example.demo.common.exception.RoomTypeNotFoundException
import com.example.demo.hotel.dto.RoomRequest
import com.example.demo.hotel.dto.RoomResponse
import com.example.demo.hotel.dto.toResponse
import com.example.demo.hotel.model.Room
import com.example.demo.hotel.model.RoomType
import com.example.demo.hotel.repository.HotelRepository
import com.example.demo.hotel.repository.RoomRepository
import com.example.demo.hotel.repository.RoomTypeRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class RoomService(
    private val repository: RoomRepository,
    private val hotelRepository: HotelRepository,
    private val roomTypeRepository: RoomTypeRepository,
) {
    @Transactional(readOnly = true)
    fun getRooms(hotelId: Long): List<RoomResponse> {
        requireHotelExists(hotelId)
        return repository.findAllByHotelId(hotelId).map { it.toResponse() }
    }

    @Transactional(readOnly = true)
    fun getRoom(
        hotelId: Long,
        id: Long,
    ): RoomResponse = findRoom(hotelId, id).toResponse()

    @Transactional
    fun createRoom(
        hotelId: Long,
        request: RoomRequest,
    ): RoomResponse {
        requireHotelExists(hotelId)
        val roomType = requireRoomType(request.roomTypeId)

        if (repository.existsByHotelIdAndRoomNumber(hotelId, request.roomNumber)) {
            throw RoomAlreadyExistsException(request.roomNumber)
        }

        val room =
            Room(
                hotel = hotelRepository.getReferenceById(hotelId),
                roomType = roomType,
                roomNumber = request.roomNumber,
                floor = request.floor,
                status = request.status,
            )
        return repository.save(room).toResponse()
    }

    @Transactional
    fun updateRoom(
        hotelId: Long,
        id: Long,
        request: RoomRequest,
    ): RoomResponse {
        val room = findRoom(hotelId, id)
        val roomType = requireRoomType(request.roomTypeId)

        if (repository.existsByHotelIdAndRoomNumberAndIdNot(hotelId, request.roomNumber, id)) {
            throw RoomAlreadyExistsException(request.roomNumber)
        }

        room.roomType = roomType
        room.roomNumber = request.roomNumber
        room.floor = request.floor
        room.status = request.status

        return room.toResponse()
    }

    @Transactional
    fun deleteRoom(
        hotelId: Long,
        id: Long,
    ) {
        repository.delete(findRoom(hotelId, id))
    }

    private fun findRoom(
        hotelId: Long,
        id: Long,
    ): Room = repository.findByIdAndHotelId(id, hotelId) ?: throw RoomNotFoundException(id)

    private fun requireHotelExists(hotelId: Long) {
        if (!hotelRepository.existsById(hotelId)) throw HotelNotFoundException(hotelId)
    }

    private fun requireRoomType(id: Long): RoomType = roomTypeRepository.findById(id).orElseThrow { RoomTypeNotFoundException(id) }
}
