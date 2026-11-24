package com.example.demo.hotel.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.persistence.Enumerated
import jakarta.persistence.EnumType

enum class AmenityCategory {
    GENERAL,
    WELLNESS,
    FOOD,
    TRANSPORT
}

@Entity
@Table(name = "amenities")
class Amenity(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(nullable = false, unique = true)
    var name: String,

    @Column(length = 255)
    var description: String? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var category: AmenityCategory = AmenityCategory.GENERAL,

    var icon: String? = null,

    @Column(nullable = false)
    var isFree: Boolean = true
)
