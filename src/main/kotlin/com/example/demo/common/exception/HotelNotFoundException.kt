package com.example.demo.common.exception

class HotelNotFoundException(
    id: Long,
) : RuntimeException("Hotel with ID $id not found")
