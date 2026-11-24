package com.example.demo.hotel.repository

import com.example.demo.hotel.model.Hotel
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository

interface HotelRepository : JpaRepository<Hotel, Long> {

    // Load amenities in the same query so listing hotels doesn't trigger one extra query per hotel
    @EntityGraph(attributePaths = ["amenities"])
    override fun findAll(): List<Hotel>
}
