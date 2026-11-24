package com.example.demo.hotel.model

import jakarta.persistence.Column
import jakarta.persistence.Embeddable
import jakarta.persistence.Embedded
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.JoinTable
import jakarta.persistence.ManyToMany
import jakarta.persistence.Table
import jakarta.validation.constraints.DecimalMax
import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import java.time.LocalTime

@Entity
@Table(name = "hotels")
class Hotel (

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(nullable = false)
    var name: String,

    @Embedded
    var address: Address,

    @Column(nullable = false)
    var checkInTime: LocalTime = LocalTime.of(15,0),

    @Column(nullable = false)
    var checkOutTime: LocalTime = LocalTime.of(10,0),
    
    @ManyToMany
    @JoinTable(
        name = "hotel_amenities",
        joinColumns = [JoinColumn(name = "hotel_id")],
        inverseJoinColumns = [JoinColumn(name = "amenity_id")]
    )
    var amenities: MutableSet<Amenity> = mutableSetOf()

)


@Embeddable
data class Address(
    @field:NotBlank
    @field:Size(max = 255)
    var country: String,

    @field:NotBlank
    @field:Size(max = 255)
    var city: String,

    @field:Size(max = 255)
    var street: String? = null,

    @field:DecimalMin("-90.0")
    @field:DecimalMax("90.0")
    var latitude: Double? = null,

    @field:DecimalMin("-180.0")
    @field:DecimalMax("180.0")
    var longitude: Double? = null
)
