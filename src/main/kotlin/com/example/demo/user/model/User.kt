package com.example.demo.user.model

import jakarta.persistence.*


enum class UserRole {
    ADMIN,
    USER
}

@Entity
@Table(name= "users")
class User(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(nullable=false)
    var name: String,

    @Column(nullable=false, unique=true)
    var email: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable=false)
    var role: UserRole = UserRole.USER
)