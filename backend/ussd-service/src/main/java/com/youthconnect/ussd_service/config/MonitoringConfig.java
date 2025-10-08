package com.youthconnect.ussd_service.config;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.Tags;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Micrometer Metrics Configuration.
 *
 * <p>Provides custom metrics for monitoring:
 * <ul>
 *   <li>USSD request counters</li>
 *   <li>Security event counters</li>
 *   <li>Response time timers</li>
 *   <li>Active session gauges</li>
 * </ul>
 *
 * @author YouthConnect Uganda Development Team
 * @version 2.0.0
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class MonitoringConfig {

    private final MeterRegistry meterRegistry;

    // Atomic counters for gauges
    private final AtomicInteger activeSessionsCount = new AtomicInteger(0);
    private final AtomicInteger queuedRequestsCount = new AtomicInteger(0);

    /**
     * ✓ FIXED: Creates base counter WITHOUT fixed tags.
     * Tags should be added when incrementing.
     */
    @Bean
    public Counter ussdRequestCounter() {
        return Counter.builder("ussd.requests.total")
                .description("Total number of USSD requests processed")
                .register(meterRegistry);
    }

    /**
     * Helper method to increment counter with dynamic tags.
     * ✓ PROPER USAGE: Tags are specified at increment time
     */
    public void recordUssdRequest(String status, String source, String operator) {
        meterRegistry.counter("ussd.requests.total",
                Tags.of("status", status,
                        "source", source,
                        "operator", operator)
        ).increment();
    }

    /**
     * Security event counter (base without tags).
     */
    @Bean
    public Counter securityEventCounter() {
        return Counter.builder("ussd.security.events")
                .description("Security events and potential threats detected")
                .register(meterRegistry);
    }

    /**
     * Helper method to record security events with dynamic tags.
     */
    public void recordSecurityEvent(String eventType, String severity, String ipAddress) {
        meterRegistry.counter("ussd.security.events",
                Tags.of("event_type", eventType,
                        "severity", severity,
                        "ip_address", ipAddress)
        ).increment();
    }

    /**
     * Timer for measuring USSD request processing duration.
     */
    @Bean
    public Timer ussdRequestTimer() {
        return Timer.builder("ussd.request.duration")
                .description("Time taken to process USSD requests")
                .register(meterRegistry);
    }

    /**
     * Registration counter (base without tags).
     */
    @Bean
    public Counter registrationCounter() {
        return Counter.builder("ussd.registration.attempts")
                .description("User registration attempts via USSD")
                .register(meterRegistry);
    }

    /**
     * Helper method to record registration with dynamic tags.
     */
    public void recordRegistration(String status, String failureReason) {
        Tags tags = failureReason != null
                ? Tags.of("status", status, "failure_reason", failureReason)
                : Tags.of("status", status);

        meterRegistry.counter("ussd.registration.attempts", tags).increment();
    }

    /**
     * Gateway interaction counter.
     */
    @Bean
    public Counter gatewayInteractionCounter() {
        return Counter.builder("ussd.gateway.interactions")
                .description("Interactions with backend gateway services")
                .register(meterRegistry);
    }

    /**
     * Helper method to record gateway interactions.
     */
    public void recordGatewayInteraction(String service, String operation, String status) {
        meterRegistry.counter("ussd.gateway.interactions",
                Tags.of("service", service,
                        "operation", operation,
                        "status",status)
        ).increment();
    }

    /**
     * Gateway response timer.
     */
    @Bean
    public Timer gatewayResponseTimer() {
        return Timer.builder("ussd.gateway.response.time")
                .description("Response time for gateway service calls")
                .register(meterRegistry);
    }

    /**
     * Menu navigation counter.
     */
    @Bean
    public Counter menuNavigationCounter() {
        return Counter.builder("ussd.menu.navigation")
                .description("Menu navigation patterns and user choices")
                .register(meterRegistry);
    }

    /**
     * Session event counter.
     */
    @Bean
    public Counter sessionEventCounter() {
        return Counter.builder("ussd.session.events")
                .description("USSD session lifecycle events")
                .register(meterRegistry);
    }

    /**
     * Business operation counter.
     */
    @Bean
    public Counter businessOperationCounter() {
        return Counter.builder("ussd.business.operations")
                .description("Business operations performed via USSD")
                .register(meterRegistry);
    }

    /**
     * Error counter.
     */
    @Bean
    public Counter errorCounter() {
        return Counter.builder("ussd.errors")
                .description("Errors categorized by type and severity")
                .register(meterRegistry);
    }

    /**
     * Database operation timer.
     */
    @Bean
    public Timer databaseOperationTimer() {
        return Timer.builder("ussd.database.operation.time")
                .description("Time taken for database operations")
                .register(meterRegistry);
    }

    /**
     * Active sessions gauge.
     */
    @Bean
    public Gauge activeSessionsGauge() {
        return Gauge.builder("ussd.sessions.active", activeSessionsCount, AtomicInteger::get)
                .description("Number of currently active USSD sessions")
                .register(meterRegistry);
    }

    /**
     * Queued requests gauge.
     */
    @Bean
    public Gauge queuedRequestsGauge() {
        return Gauge.builder("ussd.requests.queued", queuedRequestsCount, AtomicInteger::get)
                .description("Number of requests currently queued for processing")
                .register(meterRegistry);
    }

    /**
     * Health check counter.
     */
    @Bean
    public Counter healthCheckCounter() {
        return Counter.builder("ussd.health.checks")
                .description("Health check results for system dependencies")
                .register(meterRegistry);
    }

    /**
     * Health check timer.
     */
    @Bean
    public Timer healthCheckTimer() {
        return Timer.builder("ussd.health.check.duration")
                .description("Time taken to complete health checks")
                .register(meterRegistry);
    }

    // === Utility Methods for Gauge Management ===

    public void updateActiveSessionsCount(int delta) {
        activeSessionsCount.addAndGet(delta);
    }

    public void setActiveSessionsCount(int count) {
        activeSessionsCount.set(count);
    }

    public void updateQueuedRequestsCount(int delta) {
        queuedRequestsCount.addAndGet(delta);
    }

    public void setQueuedRequestsCount(int count) {
        queuedRequestsCount.set(count);
    }

    public int getCurrentActiveSessionsCount() {
        return activeSessionsCount.get();
    }

    public int getCurrentQueuedRequestsCount() {
        return queuedRequestsCount.get();
    }
}