package com.example.demo.reservation.model

import com.example.demo.hotel.model.Room
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
import java.time.Instant
import java.time.LocalDate

enum class ReservationStatus {
    PENDING,
    CONFIRMED,
    CANCELLED,
    EXPIRED
}

@Entity
@Table(name = "reservations")
class Reservation(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "room_id", nullable = false)
    var room: Room,

    @Column(nullable = false)
    var guestName: String,

    @Column(nullable = false)
    var guestEmail: String,

    @Column(nullable = false)
    var checkIn: LocalDate,

    @Column(nullable = false)
    var checkOut: LocalDate,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: ReservationStatus = ReservationStatus.PENDING,

    @Column(nullable = false)
    var lockUntil: Instant
)
