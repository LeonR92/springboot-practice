package com.example.demo.hotel.repository

import com.example.demo.hotel.model.Room
import jakarta.persistence.LockModeType
import com.example.demo.hotel.model.RoomStatus
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.Instant
import java.time.LocalDate

interface RoomRepository : JpaRepository<Room, Long> {

    // Load the room type in the same query so listing rooms doesn't run one extra query per room
    @EntityGraph(attributePaths = ["roomType"])
    fun findAllByHotelId(hotelId: Long): List<Room>

    @EntityGraph(attributePaths = ["roomType"])
    fun findByIdAndHotelId(id: Long, hotelId: Long): Room?

    fun existsByHotelIdAndRoomNumber(hotelId: Long, roomNumber: String): Boolean

    fun existsByHotelIdAndRoomNumberAndIdNot(hotelId: Long, roomNumber: String, id: Long): Boolean

    fun existsByRoomTypeId(roomTypeId: Long): Boolean


    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from Room r where r.id = :id")
    fun findByIdForUpdate(@Param("id") id: Long): Room?

    fun existsByIdAndStatus(id: Long, status: RoomStatus): Boolean

    @EntityGraph(attributePaths = ["roomType"])
    @Query("""
        select r from Room r
        where r.hotel.id = :hotelId
          and r.status = com.example.demo.hotel.model.RoomStatus.ACTIVE
          and (:roomTypeId is null or r.roomType.id = :roomTypeId)
          and not exists (
              select 1 from Reservation res
              where res.room = r
                and res.checkIn < :checkOut
                and res.checkOut > :checkIn
                and (res.status = com.example.demo.reservation.model.ReservationStatus.CONFIRMED
                     or (res.status = com.example.demo.reservation.model.ReservationStatus.PENDING
                         and res.lockUntil > :now))
          )
        order by r.roomNumber
    """)
    fun findAvailableRooms(
        @Param("hotelId") hotelId: Long,
        @Param("roomTypeId") roomTypeId: Long?,
        @Param("checkIn") checkIn: LocalDate,
        @Param("checkOut") checkOut: LocalDate,
        @Param("now") now: Instant,
        pageable: Pageable
    ): Page<Room>
}
