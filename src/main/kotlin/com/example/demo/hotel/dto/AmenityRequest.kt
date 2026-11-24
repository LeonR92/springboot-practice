package com.example.demo.hotel.dto

import com.example.demo.hotel.model.AmenityCategory
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class AmenityRequest(
    @field:NotBlank
    @field:Size(max = 255)
    val name: String,

    @field:Size(max = 255)
    val description: String? = null,

    val category: AmenityCategory = AmenityCategory.GENERAL,

    @field:Size(max = 255)
    val icon: String? = null,

    val isFree: Boolean = true
)
