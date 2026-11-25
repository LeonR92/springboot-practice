package com.example.demo.pricing.repository

import com.example.demo.pricing.model.WeekendRate
import org.springframework.data.jpa.repository.JpaRepository

interface WeekendRateRepository : JpaRepository<WeekendRate, Long> {
    fun findByHotelIdAndActiveTrue(hotelId: Long): WeekendRate?
}
