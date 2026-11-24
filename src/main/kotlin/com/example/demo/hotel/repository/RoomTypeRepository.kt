package com.example.demo.hotel.repository

import com.example.demo.hotel.model.RoomType
import org.springframework.data.jpa.repository.JpaRepository

interface RoomTypeRepository : JpaRepository<RoomType, Long> {

    fun existsByName(name: String): Boolean

    fun existsByNameAndIdNot(name: String, id: Long): Boolean
}
