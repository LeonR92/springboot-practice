package com.example.demo.hotel.model

import jakarta.persistence.Column
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.math.BigDecimal

enum class RoomTier {
    STANDARD,
    SUPERIOR,
    DELUXE,
    SUITE
}

@Entity
@Table(name = "room_types")
class RoomType(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(nullable = false, unique = true)
    var name: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var tier: RoomTier,

    // default price per night; hotel-specific rates come from pricing rules later
    @Column(nullable = false, precision = 10, scale = 2)
    var basePrice: BigDecimal,

    @Column(nullable = false)
    var capacity: Int,

    @Column(length = 255)
    var description: String? = null
)
