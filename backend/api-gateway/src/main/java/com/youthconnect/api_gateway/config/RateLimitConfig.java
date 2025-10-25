package com.youthconnect.api_gateway.config;

import io.github.bucket4j.Bucket;
import io.github.bucket4j.Bucket4j;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Refill;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Rate Limiting Configuration for API Gateway
 *
 * Implements token bucket algorithm to prevent API abuse at the gateway level.
 * Different limits for different endpoint types (auth, general, USSD).
 *
 * Location: api-gateway/src/main/java/com/youthconnect/api_gateway/config/
 */
@Slf4j
@Component
public class RateLimitConfig {

    @Value("${app.security.rate-limit.requests-per-minute:100}")
    private long requestsPerMinute;

    @Value("${app.security.rate-limit.burst-capacity:150}")
    private long burstCapacity;

    @Value("${app.security.rate-limit.auth-requests-per-minute:20}")
    private long authRequestsPerMinute;

    @Value("${app.security.rate-limit.ussd-requests-per-minute:50}")
    private long ussdRequestsPerMinute;

    @Value("${app.security.rate-limit.enabled:true}")
    private boolean rateLimitEnabled;

    /**
     * Cache for storing rate limit buckets per client IP
     * Key: IP address + endpoint type
     * Value: Token bucket for that client
     */
    private final Map<String, Bucket> bucketCache = new ConcurrentHashMap<>();

    /**
     * Creates rate limit bucket for general API endpoints
     * 100 requests per minute with burst capacity
     */
    public Bucket createGeneralBucket() {
        Bandwidth limit = Bandwidth.classic(
                requestsPerMinute,
                Refill.intervally(requestsPerMinute, Duration.ofMinutes(1))
        );

        log.debug("Created general rate limit bucket: {} requests/minute", requestsPerMinute);
        return Bucket4j.builder().addLimit(limit).build();
    }

    /**
     * Creates stricter rate limit bucket for authentication endpoints
     * Maximum 20 auth requests per minute to prevent brute force attacks
     */
    public Bucket createAuthBucket() {
        Bandwidth limit = Bandwidth.classic(
                authRequestsPerMinute,
                Refill.intervally(authRequestsPerMinute, Duration.ofMinutes(1))
        );

        log.debug("Created auth rate limit bucket: {} requests/minute", authRequestsPerMinute);
        return Bucket4j.builder().addLimit(limit).build();
    }

    /**
     * Creates moderate rate limit bucket for USSD endpoints
     * 50 requests per minute for USSD sessions
     */
    public Bucket createUssdBucket() {
        Bandwidth limit = Bandwidth.classic(
                ussdRequestsPerMinute,
                Refill.intervally(ussdRequestsPerMinute, Duration.ofMinutes(1))
        );

        log.debug("Created USSD rate limit bucket: {} requests/minute", ussdRequestsPerMinute);
        return Bucket4j.builder().addLimit(limit).build();
    }

    /**
     * Get or create bucket for a specific client identifier
     *
     * @param key Client identifier (usually IP address)
     * @param endpointType Type of endpoint (auth, ussd, general)
     * @return Token bucket for rate limiting
     */
    public Bucket resolveBucket(String key, EndpointType endpointType) {
        String cacheKey = key + ":" + endpointType.name();

        return bucketCache.computeIfAbsent(cacheKey, k -> {
            switch (endpointType) {
                case AUTH:
                    return createAuthBucket();
                case USSD:
                    return createUssdBucket();
                case GENERAL:
                default:
                    return createGeneralBucket();
            }
        });
    }

    /**
     * Check if rate limiting is enabled
     */
    public boolean isRateLimitEnabled() {
        return rateLimitEnabled;
    }

    /**
     * Clear bucket cache (useful for testing or admin operations)
     */
    public void clearCache() {
        bucketCache.clear();
        log.info("Rate limit bucket cache cleared");
    }

    /**
     * Get current cache size (for monitoring)
     */
    public int getCacheSize() {
        return bucketCache.size();
    }

    /**
     * Endpoint types for different rate limiting strategies
     */
    public enum EndpointType {
        AUTH,    // Strict: 20 req/min
        USSD,    // Moderate: 50 req/min
        GENERAL  // Standard: 100 req/min
    }
}