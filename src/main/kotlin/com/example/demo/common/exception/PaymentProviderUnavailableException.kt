package com.example.demo.common.exception

// Transient provider failure (timeout, 5xx). The only exception @Retryable retries on.
class PaymentProviderUnavailableException(
    provider: String,
    cause: Throwable? = null,
) : RuntimeException("Payment provider $provider is unavailable", cause)
