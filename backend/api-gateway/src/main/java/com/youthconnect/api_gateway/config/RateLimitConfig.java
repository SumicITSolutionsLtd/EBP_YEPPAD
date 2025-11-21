package com.youthconnect.api_gateway.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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
 * BUCKET4J 8.x COMPATIBLE VERSION
 * ================================
 * This version uses Bucket4j 8.x API which has different syntax than 7.x
 *
 * Token Bucket Algorithm:
 * - Each client gets a "bucket" of tokens
 * - Each request consumes 1 token
 * - Tokens refill at a steady rate
 * - When bucket is empty, requests are rejected (HTTP 429)
 *
 * Rate Limit Strategies:
 * - AUTH endpoints: 20 req/min (strict - prevent brute force)
 * - USSD endpoints: 50 req/min (moderate - for USSD sessions)
 * - GENERAL endpoints: 100 req/min (standard - normal API usage)
 *
 * Location: api-gateway/src/main/java/com/youthconnect/api_gateway/config/
 *
 * @author Douglas Kings Kato
 * @version 2.0.0 (Bucket4j 8.x)
 */
@Slf4j
@Component
public class RateLimitConfig {

    // =========================================================================
    // CONFIGURATION PROPERTIES
    // Values from application.yml with sensible defaults
    // =========================================================================

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

    // =========================================================================
    // BUCKET CACHE
    // Stores rate limit buckets per client IP + endpoint type
    // Thread-safe concurrent map for high-performance access
    // =========================================================================

    /**
     * Cache for storing rate limit buckets per client IP
     * Key format: "IP_ADDRESS:ENDPOINT_TYPE" (e.g., "192.168.1.1:AUTH")
     * Value: Token bucket for that client-endpoint combination
     *
     * Thread-safe: Uses ConcurrentHashMap for concurrent access
     */
    private final Map<String, Bucket> bucketCache = new ConcurrentHashMap<>();

    // =========================================================================
    // BUCKET FACTORY METHODS
    // Creates different bucket configurations for different endpoint types
    // =========================================================================

    /**
     * Creates rate limit bucket for general API endpoints
     *
     * Configuration:
     * - Capacity: 100 tokens
     * - Refill: 100 tokens per minute
     * - Strategy: Intervally (refills at fixed intervals)
     *
     * Example: User can make 100 requests per minute
     * After 100 requests, they must wait for next refill
     *
     * @return Configured Bucket instance for general endpoints
     */
    public Bucket createGeneralBucket() {
        // Create bandwidth limit using Bucket4j 8.x API
        Bandwidth limit = Bandwidth.builder()
                .capacity(requestsPerMinute)  // Maximum tokens in bucket
                .refillIntervally(requestsPerMinute, Duration.ofMinutes(1))  // Refill rate
                .build();

        log.debug("Created general rate limit bucket: {} requests/minute", requestsPerMinute);

        // Build and return the bucket
        return Bucket.builder()
                .addLimit(limit)
                .build();
    }

    /**
     * Creates stricter rate limit bucket for authentication endpoints
     *
     * Configuration:
     * - Capacity: 20 tokens (strict limit)
     * - Refill: 20 tokens per minute
     * - Purpose: Prevent brute force attacks on login/register
     *
     * Security Rationale:
     * - 20 login attempts per minute is reasonable for legitimate users
     * - Too many attempts suggests automated attack
     * - Slows down credential stuffing attacks
     *
     * @return Configured Bucket instance for auth endpoints
     */
    public Bucket createAuthBucket() {
        Bandwidth limit = Bandwidth.builder()
                .capacity(authRequestsPerMinute)
                .refillIntervally(authRequestsPerMinute, Duration.ofMinutes(1))
                .build();

        log.debug("Created auth rate limit bucket: {} requests/minute", authRequestsPerMinute);

        return Bucket.builder()
                .addLimit(limit)
                .build();
    }

    /**
     * Creates moderate rate limit bucket for USSD endpoints
     *
     * Configuration:
     * - Capacity: 50 tokens (moderate limit)
     * - Refill: 50 tokens per minute
     * - Purpose: Handle USSD session interactions
     *
     * USSD Context:
     * - USSD sessions involve multiple back-and-forth messages
     * - Need more capacity than auth but less than general API
     * - Typical USSD session: 5-10 messages
     *
     * @return Configured Bucket instance for USSD endpoints
     */
    public Bucket createUssdBucket() {
        Bandwidth limit = Bandwidth.builder()
                .capacity(ussdRequestsPerMinute)
                .refillIntervally(ussdRequestsPerMinute, Duration.ofMinutes(1))
                .build();

        log.debug("Created USSD rate limit bucket: {} requests/minute", ussdRequestsPerMinute);

        return Bucket.builder()
                .addLimit(limit)
                .build();
    }

    // =========================================================================
    // BUCKET RESOLUTION
    // Get or create bucket for specific client + endpoint type combination
    // =========================================================================

    /**
     * Get or create bucket for a specific client identifier
     *
     * This method implements the "get or create" pattern:
     * 1. Check if bucket exists in cache
     * 2. If not, create new bucket based on endpoint type
     * 3. Store in cache for future requests
     * 4. Return the bucket
     *
     * Thread Safety:
     * - Uses ConcurrentHashMap.computeIfAbsent() for atomic get-or-create
     * - Multiple threads can safely call this method concurrently
     *
     * Cache Key Format:
     * - Format: "CLIENT_IP:ENDPOINT_TYPE"
     * - Examples: "192.168.1.1:AUTH", "10.0.0.5:GENERAL"
     *
     * @param key Client identifier (usually IP address)
     * @param endpointType Type of endpoint (AUTH, USSD, GENERAL)
     * @return Token bucket for rate limiting this client
     */
    public Bucket resolveBucket(String key, EndpointType endpointType) {
        // Create composite cache key from IP and endpoint type
        String cacheKey = key + ":" + endpointType.name();

        // Atomically get existing bucket or create new one
        return bucketCache.computeIfAbsent(cacheKey, k -> {
            // Switch based on endpoint type to create appropriate bucket
            switch (endpointType) {
                case AUTH:
                    log.debug("Creating new AUTH bucket for client: {}", key);
                    return createAuthBucket();

                case USSD:
                    log.debug("Creating new USSD bucket for client: {}", key);
                    return createUssdBucket();

                case GENERAL:
                default:
                    log.debug("Creating new GENERAL bucket for client: {}", key);
                    return createGeneralBucket();
            }
        });
    }

    // =========================================================================
    // UTILITY METHODS
    // Configuration and cache management
    // =========================================================================

    /**
     * Check if rate limiting is enabled
     *
     * Allows runtime enable/disable of rate limiting via configuration.
     * Useful for:
     * - Testing (disable during integration tests)
     * - Emergency situations (temporarily disable if causing issues)
     * - Gradual rollout (enable for subset of traffic)
     *
     * @return true if rate limiting is enabled, false otherwise
     */
    public boolean isRateLimitEnabled() {
        return rateLimitEnabled;
    }

    /**
     * Clear bucket cache
     *
     * Removes all cached buckets, forcing recreation on next request.
     *
     * Use Cases:
     * - Testing: Reset rate limits between tests
     * - Admin operations: Clear specific client's rate limit
     * - Configuration changes: Apply new limits immediately
     *
     * WARNING: This resets rate limits for ALL clients!
     * Use with caution in production.
     */
    public void clearCache() {
        int previousSize = bucketCache.size();
        bucketCache.clear();
        log.info("Rate limit bucket cache cleared. Removed {} entries", previousSize);
    }

    /**
     * Get current cache size
     *
     * Returns number of unique client-endpoint combinations being tracked.
     *
     * Monitoring:
     * - High cache size may indicate memory pressure
     * - Can set up alerts if cache grows too large
     * - Typical size: 100-1000 entries in production
     *
     * @return Number of cached buckets
     */
    public int getCacheSize() {
        return bucketCache.size();
    }

    /**
     * Get bucket cache statistics (for monitoring/debugging)
     *
     * Provides detailed cache metrics for observability.
     *
     * @return Map of cache statistics
     */
    public Map<String, Object> getCacheStats() {
        return Map.of(
                "cacheSize", bucketCache.size(),
                "rateLimitEnabled", rateLimitEnabled,
                "generalLimit", requestsPerMinute,
                "authLimit", authRequestsPerMinute,
                "ussdLimit", ussdRequestsPerMinute
        );
    }

    // =========================================================================
    // ENDPOINT TYPE ENUM
    // Defines different rate limiting strategies
    // =========================================================================

    /**
     * Endpoint types for different rate limiting strategies
     *
     * Each type has different limits based on security/performance requirements:
     *
     * AUTH (Strictest):
     * - Limit: 20 req/min
     * - Purpose: Prevent brute force attacks on authentication
     * - Endpoints: /api/auth/login, /api/auth/register, etc.
     *
     * USSD (Moderate):
     * - Limit: 50 req/min
     * - Purpose: Handle USSD session interactions
     * - Endpoints: /api/ussd/**
     *
     * GENERAL (Standard):
     * - Limit: 100 req/min
     * - Purpose: Normal API usage
     * - Endpoints: All other API endpoints
     */
    public enum EndpointType {
        /**
         * Authentication endpoints - Strictest limits (20 req/min)
         * Protects login, register, password reset from brute force
         */
        AUTH,

        /**
         * USSD endpoints - Moderate limits (50 req/min)
         * Handles USSD session back-and-forth communications
         */
        USSD,

        /**
         * General API endpoints - Standard limits (100 req/min)
         * Default rate limit for most API operations
         */
        GENERAL
    }
}