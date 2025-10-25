package com.youthconnect.edge_functions.config;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import io.github.resilience4j.timelimiter.TimeLimiterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * Resilience4j Configuration for Edge Functions Service
 *
 * Implements resilience patterns for fault tolerance:
 * 1. Circuit Breaker - Prevents cascading failures
 * 2. Retry - Automatic retry with exponential backoff
 * 3. Rate Limiter - Prevents overwhelming services
 * 4. Time Limiter - Request timeout management
 *
 * These patterns ensure the platform remains stable even when
 * dependent services experience issues.
 *
 * @author Douglas Kings Kato
 * @version 1.0
 */
@Configuration
@Slf4j
public class ResilienceConfig {

    // ============================================
    // CIRCUIT BREAKER CONFIGURATION
    // ============================================

    /**
     * Circuit Breaker Registry with default configuration
     *
     * Circuit Breaker Pattern:
     * - CLOSED: Normal operation, requests flow through
     * - OPEN: Too many failures, requests fail fast
     * - HALF_OPEN: Testing if service recovered
     *
     * This prevents repeated calls to failing services,
     * giving them time to recover.
     */
    @Bean
    public CircuitBreakerRegistry circuitBreakerRegistry() {
        CircuitBreakerConfig defaultConfig = CircuitBreakerConfig.custom()
                // Open circuit after 50% of calls fail
                .failureRateThreshold(50)

                // Minimum number of calls before calculating failure rate
                .minimumNumberOfCalls(5)

                // Time to wait before transitioning from OPEN to HALF_OPEN
                .waitDurationInOpenState(Duration.ofSeconds(10))

                // Number of calls allowed in HALF_OPEN state
                .permittedNumberOfCallsInHalfOpenState(3)

                // Size of sliding window (in calls) for calculating failure rate
                .slidingWindowSize(10)

                // Sliding window type: COUNT_BASED (count of calls)
                .slidingWindowType(CircuitBreakerConfig.SlidingWindowType.COUNT_BASED)

                // Automatically transition from OPEN to HALF_OPEN
                .automaticTransitionFromOpenToHalfOpenEnabled(true)

                // Exceptions that should be recorded as failures
                .recordExceptions(
                        RuntimeException.class,
                        Exception.class
                )

                // Exceptions that should be ignored (not counted as failures)
                .ignoreExceptions(
                        IllegalArgumentException.class
                )

                .build();

        CircuitBreakerRegistry registry = CircuitBreakerRegistry.of(defaultConfig);

        // Log circuit breaker state transitions
        registry.circuitBreaker("default").getEventPublisher()
                .onStateTransition(event ->
                        log.warn("Circuit Breaker State Transition: {} -> {} for {}",
                                event.getStateTransition().getFromState(),
                                event.getStateTransition().getToState(),
                                event.getCircuitBreakerName())
                )
                .onFailureRateExceeded(event ->
                        log.error("Circuit Breaker Failure Rate Exceeded: {} - Failure Rate: {}%",
                                event.getCircuitBreakerName(),
                                event.getFailureRate())
                );

        return registry;
    }

    // ============================================
    // RETRY CONFIGURATION
    // ============================================

    /**
     * Retry Registry with exponential backoff
     *
     * Automatically retries failed requests with increasing delays.
     * Useful for transient failures (network blips, temporary overload).
     *
     * Backoff example:
     * - Attempt 1: Immediate
     * - Attempt 2: Wait 1 second
     * - Attempt 3: Wait 2 seconds (1s * 2x multiplier)
     * - Stop after 3 attempts
     */
    @Bean
    public RetryRegistry retryRegistry() {
        RetryConfig defaultConfig = RetryConfig.custom()
                // Maximum number of retry attempts (including initial call)
                .maxAttempts(3)

                // Wait duration between retries
                .waitDuration(Duration.ofMillis(1000))

                // Exponential backoff multiplier
                .intervalFunction(io.github.resilience4j.core.IntervalFunction
                        .ofExponentialBackoff(1000, 2)) // 1s, 2s, 4s, ...

                // Exceptions that should trigger retry
                .retryExceptions(
                        RuntimeException.class,
                        Exception.class
                )

                // Exceptions that should NOT trigger retry
                .ignoreExceptions(
                        IllegalArgumentException.class,
                        SecurityException.class
                )

                .build();

        RetryRegistry registry = RetryRegistry.of(defaultConfig);

        // Log retry attempts
        registry.retry("default").getEventPublisher()
                .onRetry(event ->
                        log.warn("Retry attempt {} for {} - Last exception: {}",
                                event.getNumberOfRetryAttempts(),
                                event.getName(),
                                event.getLastThrowable().getMessage())
                )
                .onError(event ->
                        log.error("All retry attempts failed for {}: {}",
                                event.getName(),
                                event.getLastThrowable().getMessage())
                );

        return registry;
    }

    // ============================================
    // RATE LIMITER CONFIGURATION
    // ============================================

    /**
     * Rate Limiter Registry
     *
     * Prevents overwhelming downstream services by limiting
     * the number of requests per time period.
     *
     * Use cases:
     * - Protecting external APIs (OpenAI, Africa's Talking)
     * - Preventing abuse of expensive operations
     * - Fair resource allocation
     */
    @Bean
    public RateLimiterRegistry rateLimiterRegistry() {
        // Default rate limiter: 100 requests per minute
        RateLimiterConfig defaultConfig = RateLimiterConfig.custom()
                // Maximum number of requests allowed per refresh period
                .limitForPeriod(100)

                // Duration of refresh period
                .limitRefreshPeriod(Duration.ofMinutes(1))

                // Maximum time a thread will wait for permission
                .timeoutDuration(Duration.ofSeconds(5))

                .build();

        RateLimiterRegistry registry = RateLimiterRegistry.of(defaultConfig);

        // OpenAI-specific rate limiter (more restrictive)
        RateLimiterConfig openAIConfig = RateLimiterConfig.custom()
                .limitForPeriod(20)                         // 20 requests
                .limitRefreshPeriod(Duration.ofMinutes(1))  // per minute
                .timeoutDuration(Duration.ofSeconds(10))
                .build();

        registry.rateLimiter("openai", openAIConfig);

        // USSD-specific rate limiter (higher limits)
        RateLimiterConfig ussdConfig = RateLimiterConfig.custom()
                .limitForPeriod(1000)                       // 1000 requests
                .limitRefreshPeriod(Duration.ofMinutes(1))  // per minute
                .timeoutDuration(Duration.ofSeconds(1))
                .build();

        registry.rateLimiter("ussd", ussdConfig);

        // Log rate limit events
        registry.rateLimiter("default").getEventPublisher()
                .onFailure(event ->
                        log.warn("Rate limit exceeded for {}: {}",
                                event.getRateLimiterName(),
                                event.getCreationTime())
                );

        return registry;
    }

    // ============================================
    // TIME LIMITER CONFIGURATION
    // ============================================

    /**
     * Time Limiter Registry
     *
     * Enforces timeout for operations to prevent them
     * from running indefinitely and blocking resources.
     */
    @Bean
    public TimeLimiterRegistry timeLimiterRegistry() {
        // Default timeout: 5 seconds
        TimeLimiterConfig defaultConfig = TimeLimiterConfig.custom()
                .timeoutDuration(Duration.ofSeconds(5))
                .cancelRunningFuture(true)
                .build();

        TimeLimiterRegistry registry = TimeLimiterRegistry.of(defaultConfig);

        // OpenAI-specific timeout (longer for AI operations)
        TimeLimiterConfig openAIConfig = TimeLimiterConfig.custom()
                .timeoutDuration(Duration.ofSeconds(30))
                .cancelRunningFuture(true)
                .build();

        registry.timeLimiter("openai", openAIConfig);

        // USSD-specific timeout (must be fast)
        TimeLimiterConfig ussdConfig = TimeLimiterConfig.custom()
                .timeoutDuration(Duration.ofSeconds(3))
                .cancelRunningFuture(true)
                .build();

        registry.timeLimiter("ussd", ussdConfig);

        // Log timeout events
        registry.timeLimiter("default").getEventPublisher()
                .onTimeout(event ->
                        log.error("Operation timed out for {}: Timeout duration: {}",
                                event.getTimeLimiterName(),
                                event.getEventType())
                );

        return registry;
    }
}