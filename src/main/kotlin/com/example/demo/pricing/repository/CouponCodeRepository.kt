package com.example.demo.pricing.repository

import com.example.demo.pricing.model.CouponCode
import org.springframework.data.jpa.repository.JpaRepository

interface CouponCodeRepository : JpaRepository<CouponCode, Long> {
    fun findByCodeAndActiveTrue(code: String): CouponCode?
}
