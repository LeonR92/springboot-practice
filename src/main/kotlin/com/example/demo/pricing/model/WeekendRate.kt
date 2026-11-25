package com.example.demo.pricing.model

import com.example.demo.hotel.model.Hotel
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.math.BigDecimal

@Entity
@Table(name = "weekend_rates")
class WeekendRate(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "hotel_id", nullable = false, unique = true)
    var hotel: Hotel,
    // e.g. 1.20 = weekend nights cost 20% more
    @Column(nullable = false, precision = 5, scale = 2)
    var priceMultiplier: BigDecimal = BigDecimal.ONE,
    @Column(nullable = false)
    var active: Boolean = true,
)
