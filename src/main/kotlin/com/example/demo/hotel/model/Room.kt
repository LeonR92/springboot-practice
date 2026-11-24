package com.example.demo.hotel.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table

enum class RoomStatus {
    ACTIVE,
    OUT_OF_SERVICE
}

@Entity
@Table(name = "rooms")
class Room(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "hotel_id", nullable = false)
    var hotel: Hotel,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "room_type_id", nullable = false)
    var roomType: RoomType,

    @Column(nullable = false)
    var roomNumber: String,

    var floor: Int? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: RoomStatus = RoomStatus.ACTIVE
)
