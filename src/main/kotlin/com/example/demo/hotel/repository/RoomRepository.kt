package com.example.demo.hotel.repository

import com.example.demo.hotel.model.Room
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository

interface RoomRepository : JpaRepository<Room, Long> {

    // Load the room type in the same query so listing rooms doesn't run one extra query per room
    @EntityGraph(attributePaths = ["roomType"])
    fun findAllByHotelId(hotelId: Long): List<Room>

    @EntityGraph(attributePaths = ["roomType"])
    fun findByIdAndHotelId(id: Long, hotelId: Long): Room?

    fun existsByHotelIdAndRoomNumber(hotelId: Long, roomNumber: String): Boolean

    fun existsByHotelIdAndRoomNumberAndIdNot(hotelId: Long, roomNumber: String, id: Long): Boolean

    fun existsByRoomTypeId(roomTypeId: Long): Boolean
}
