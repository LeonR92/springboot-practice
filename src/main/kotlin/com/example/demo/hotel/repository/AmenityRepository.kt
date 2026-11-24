package com.example.demo.hotel.repository

import com.example.demo.hotel.model.Amenity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface AmenityRepository : JpaRepository<Amenity, Long> {

    fun existsByName(name: String): Boolean

    fun existsByNameAndIdNot(name: String, id: Long): Boolean

    @Query("""
        select a
        from Hotel h
        Join h.amenities a
        where h.id = :hotelId
    """)
    fun findAmenitiesByHotelId(@Param("hotelId") hotelId:Long):List<Amenity>
}


