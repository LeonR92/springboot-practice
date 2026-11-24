package com.example.demo.common.exception

class AmenityAlreadyExistsException(name: String) :
    RuntimeException("Amenity already exists: $name")
