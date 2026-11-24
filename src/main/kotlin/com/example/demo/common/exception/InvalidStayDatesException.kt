package com.example.demo.common.exception

class InvalidStayDatesException :
    RuntimeException("Check-out must be after check-in")
