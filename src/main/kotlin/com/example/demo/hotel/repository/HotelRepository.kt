package com.example.demo.hotel.repository

import com.example.demo.hotel.model.Hotel
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.math.BigDecimal
import java.time.LocalDate

interface HotelRepository : JpaRepository<Hotel, Long> {
    // Load amenities in the same query so listing hotels doesn't trigger one extra query per hotel
    @EntityGraph(attributePaths = ["amenities"])
    override fun findAll(): List<Hotel>

    // Occupancy rate for pricing: share of the hotel's rooms with a CONFIRMED reservation on that date.
    // PENDING holds are deliberately not counted, so an unpaid hold can't push prices up.
    // excludeReservationId keeps a reservation that is being re-priced from counting itself.
    @Query(
        value = """
            SELECT COALESCE(
                COUNT(DISTINCT res.room_id) * 1.0 /
                NULLIF(COUNT(DISTINCT r.id), 0), 0
            )
            FROM rooms r
            LEFT JOIN reservations res
                ON res.room_id = r.id
                AND res.check_in <= :date
                AND res.check_out > :date
                AND res.status = 'CONFIRMED'
                AND (:excludeReservationId IS NULL OR res.id <> :excludeReservationId)
            WHERE r.hotel_id = :hotelId
        """,
        nativeQuery = true,
    )
    fun findOccupancyRate(
        @Param("hotelId") hotelId: Long,
        @Param("date") date: LocalDate,
        @Param("excludeReservationId") excludeReservationId: Long? = null,
    ): BigDecimal
}
