package com.example.demo.reservation.repository

import com.example.demo.reservation.model.Reservation
import com.example.demo.reservation.model.ReservationStatus
import jakarta.persistence.LockModeType
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.Instant
import java.time.LocalDate

interface ReservationRepository : JpaRepository<Reservation, Long> {
    // Load the room in the same query so listing reservations doesn't run one extra query per row
    @EntityGraph(attributePaths = ["room"])
    override fun findAll(pageable: Pageable): Page<Reservation>

    @EntityGraph(attributePaths = ["room"])
    fun findByUserId(
        userId: Long,
        pageable: Pageable,
    ): Page<Reservation>

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from Reservation r where r.id = :id")
    fun findByIdForUpdate(
        @Param("id") id: Long,
    ): Reservation?

    // True if a blocking reservation (CONFIRMED, or PENDING with an unexpired hold) overlaps [checkIn, checkOut).
    // excludeId lets an update ignore the reservation being edited.
    @Query(
        """
        select count(res) > 0 from Reservation res
        where res.room.id = :roomId
          and res.checkIn < :checkOut
          and res.checkOut > :checkIn
          and (res.status = com.example.demo.reservation.model.ReservationStatus.CONFIRMED
               or (res.status = com.example.demo.reservation.model.ReservationStatus.PENDING
                   and res.lockUntil > :now))
          and (:excludeId is null or res.id <> :excludeId)
    """,
    )
    fun existsBlockingReservation(
        @Param("roomId") roomId: Long,
        @Param("checkIn") checkIn: LocalDate,
        @Param("checkOut") checkOut: LocalDate,
        @Param("now") now: Instant,
        @Param("excludeId") excludeId: Long?,
    ): Boolean

    // Marks every PENDING hold whose lockUntil has passed with the given status, in one UPDATE. Returns the row count.
    @Modifying(clearAutomatically = true)
    @Query(
        """
        update Reservation r set r.status = :to
        where r.status = :from and r.lockUntil <= :now
    """,
    )
    fun expireHolds(
        @Param("from") from: ReservationStatus,
        @Param("to") to: ReservationStatus,
        @Param("now") now: Instant,
    ): Int
}
