package com.example.demo.common.exception

class InvalidCouponException(code: String) :
    RuntimeException("Coupon code is invalid or expired: $code")
