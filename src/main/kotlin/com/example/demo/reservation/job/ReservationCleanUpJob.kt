package com.example.demo.reservation.job

import com.example.demo.reservation.service.ReservationService
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class ReservationCleanUpJob(
    private val reservationService: ReservationService
) {
    private val log = LoggerFactory.getLogger(javaClass)

    // every minute, on the minute
    @Scheduled(cron = "0 * * * * *")
    fun cleanup() {
        val expired = reservationService.expireStaleReservations()
        if (expired > 0) log.info("Expired {} stale reservation hold(s)", expired)
    }
}
