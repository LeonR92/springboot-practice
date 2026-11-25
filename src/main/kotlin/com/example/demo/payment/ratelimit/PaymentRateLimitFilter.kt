package com.example.demo.payment.ratelimit

import io.github.bucket4j.Bucket
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.time.Duration
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

@Component
class PaymentRateLimitFilter(
    @Value("\${payment.rate-limit.max-requests}") private val maxRequests: Long,
    @Value("\${payment.rate-limit.window-seconds}") private val windowSeconds: Long,
) : OncePerRequestFilter() {
    private val buckets = ConcurrentHashMap<String, Bucket>()

    private fun newBucket(): Bucket =
        Bucket
            .builder()
            .addLimit { it.capacity(maxRequests).refillGreedy(maxRequests, Duration.ofSeconds(windowSeconds)) }
            .build()

    override fun shouldNotFilter(request: HttpServletRequest): Boolean {
        val path = request.requestURI
        return request.method != "POST" ||
            !path.startsWith("/api/v1/payments") ||
            path.startsWith("/api/v1/payments/webhook")
    }

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        chain: FilterChain,
    ) {
        val bucket = buckets.computeIfAbsent(clientKey(request)) { newBucket() }
        val probe = bucket.tryConsumeAndReturnRemaining(1)

        if (!probe.isConsumed) {
            val retryAfter = TimeUnit.NANOSECONDS.toSeconds(probe.nanosToWaitForRefill).coerceAtLeast(1)
            response.status = HttpStatus.TOO_MANY_REQUESTS.value()
            response.setHeader("Retry-After", retryAfter.toString())
            response.contentType = MediaType.APPLICATION_JSON_VALUE
            response.writer.write("""{"error":"Too many payment requests, try again later"}""")
            return
        }
        chain.doFilter(request, response)
    }

    private fun clientKey(request: HttpServletRequest): String {
        val auth = SecurityContextHolder.getContext().authentication
        return if (auth != null && auth.isAuthenticated && auth.name != "anonymousUser") {
            "user:${auth.name}"
        } else {
            "ip:${request.remoteAddr}"
        }
    }

    @Scheduled(fixedDelay = 60_000)
    fun evictIdle() {
        buckets.entries.removeIf { it.value.availableTokens >= maxRequests }
    }
}
