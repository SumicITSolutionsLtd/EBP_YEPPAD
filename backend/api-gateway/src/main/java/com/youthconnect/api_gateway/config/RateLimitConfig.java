// RateLimitConfig.java
package com.youthconnect.user_service.config;

import io.github.bucket4j.Bucket;
import io.github.bucket4j.Bucket4j;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Refill;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Rate Limiting Configuration for API Protection
 *
 * Implements token bucket algorithm to prevent API abuse and ensure
 * fair usage across all clients. Different limits for different endpoints.
 */
@Slf4j
@Configuration
public class RateLimitConfig {

    @Value("${app.security.rate-limit.requests-per-minute:100}")
    private long requestsPerMinute;

    @Value("${app.security.rate-limit.burst-capacity:150}")
    private long burstCapacity;

    @Value("${app.security.rate-limit.enabled:true}")
    private boolean rateLimitEnabled;

    /**
     * Cache for storing rate limit buckets per client IP
     */
    private final Map<String, Bucket> bucketCache = new ConcurrentHashMap<>();

    /**
     * Creates rate limit bucket for general API endpoints
     */
    public Bucket createBucket() {
        Bandwidth limit = Bandwidth.classic(requestsPerMinute,
                Refill.intervally(requestsPerMinute, Duration.ofMinutes(1)));
        return Bucket4j.builder().addLimit(limit).build();
    }

    /**
     * Creates stricter rate limit bucket for authentication endpoints
     */
    public Bucket createAuthBucket() {
        long authRequestsPerMinute = Math.min(requestsPerMinute / 5, 20); // Max 20 auth requests per minute
        Bandwidth limit = Bandwidth.classic(authRequestsPerMinute,
                Refill.intervally(authRequestsPerMinute, Duration.ofMinutes(1)));
        return Bucket4j.builder().addLimit(limit).build();
    }

    /**
     * Get or create bucket for a specific client identifier
     */
    public Bucket resolveBucket(String key, boolean isAuthEndpoint) {
        return bucketCache.computeIfAbsent(key, k ->
                isAuthEndpoint ? createAuthBucket() : createBucket());
    }

    public boolean isRateLimitEnabled() {
        return rateLimitEnabled;
    }
}