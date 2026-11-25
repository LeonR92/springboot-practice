package com.example.demo.common.exception

class AmenityNotFoundException(
    id: Long,
) : RuntimeException("Amenity with ID $id not found")
