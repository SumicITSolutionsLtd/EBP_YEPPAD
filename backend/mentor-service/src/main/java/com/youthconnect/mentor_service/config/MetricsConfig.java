package com.youthconnect.mentor_service.config;

import io.micrometer.core.aop.TimedAspect;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * ============================================================================
 * METRICS CONFIGURATION
 * ============================================================================
 *
 * Configures Micrometer metrics for application monitoring and observability.
 * Integrates with Prometheus for metrics collection and Grafana for visualization.
 *
 * METRICS CATEGORIES:
 * 1. Business Metrics - Domain-specific KPIs
 * 2. Technical Metrics - System performance indicators
 * 3. Custom Metrics - Application-specific measurements
 *
 * BUSINESS METRICS:
 * - mentorship.sessions.created - Total sessions created
 * - mentorship.sessions.completed - Sessions successfully completed
 * - mentorship.sessions.cancelled - Cancelled sessions count
 * - mentorship.reviews.submitted - Reviews submitted
 * - mentorship.reviews.average_rating - Average rating across all reviews
 * - mentorship.mentors.active - Currently active mentors
 * - mentorship.sessions.duration - Average session duration
 *
 * TECHNICAL METRICS:
 * - http.server.requests - HTTP request metrics (Spring Boot Actuator)
 * - jvm.memory.used - JVM memory usage
 * - jvm.gc.pause - Garbage collection pauses
 * - jdbc.connections.active - Active database connections
 * - cache.gets - Cache access metrics
 * - cache.puts - Cache write metrics
 * - cache.evictions - Cache eviction count
 *
 * INTEGRATION:
 * - Prometheus scrapes metrics from /actuator/prometheus endpoint
 * - Grafana dashboards visualize metrics in real-time
 * - Alerts configured for critical thresholds
 *
 * @author Douglas Kings Kato
 * @version 1.0.0
 * @since 2025-01-21
 * ============================================================================
 */
@Configuration
@Slf4j
public class MetricsConfig {

    /**
     * Timed Aspect Bean
     * Enables @Timed annotation support for method execution timing
     *
     * USAGE:
     * @Timed(value = "mentorship.session.booking", description = "Time taken to book session")
     * public Session bookSession(SessionRequest request) { ... }
     *
     * BENEFITS:
     * - Automatic timing of annotated methods
     * - Percentile calculations (p50, p95, p99)
     * - Integration with Prometheus/Grafana
     *
     * @param registry Micrometer meter registry
     * @return TimedAspect for @Timed annotation processing
     */
    @Bean
    public TimedAspect timedAspect(MeterRegistry registry) {
        log.info("Configuring TimedAspect for @Timed annotation support");
        return new TimedAspect(registry);
    }

    /**
     * Mentorship Metrics Bean
     * Provides business-specific metrics for mentorship operations
     *
     * METRICS PROVIDED:
     * - Session creation counter
     * - Session completion counter
     * - Session cancellation counter
     * - Review submission counter
     * - Active mentor gauge
     * - Session duration timer
     *
     * @param registry Micrometer meter registry
     * @return MentorshipMetrics bean
     */
    @Bean
    public MentorshipMetrics mentorshipMetrics(MeterRegistry registry) {
        log.info("Initializing mentorship business metrics");
        return new MentorshipMetrics(registry);
    }
}

/**
 * Mentorship Metrics Service
 * Encapsulates all mentorship-related metrics
 */
@Slf4j
class MentorshipMetrics {

    private final MeterRegistry registry;

    // Counters
    private final Counter sessionsCreatedCounter;
    private final Counter sessionsCompletedCounter;
    private final Counter sessionsCancelledCounter;
    private final Counter sessionsNoShowCounter;
    private final Counter reviewsSubmittedCounter;

    // Timers
    private final Timer sessionDurationTimer;
    private final Timer sessionBookingTimer;
    private final Timer reviewSubmissionTimer;

    /**
     * Constructor
     * Initializes all metrics
     *
     * @param registry Micrometer meter registry
     */
    public MentorshipMetrics(MeterRegistry registry) {
        this.registry = registry;

        // Initialize counters
        this.sessionsCreatedCounter = Counter.builder("mentorship.sessions.created")
                .description("Total number of mentorship sessions created")
                .tag("service", "mentor-service")
                .register(registry);

        this.sessionsCompletedCounter = Counter.builder("mentorship.sessions.completed")
                .description("Total number of sessions successfully completed")
                .tag("service", "mentor-service")
                .register(registry);

        this.sessionsCancelledCounter = Counter.builder("mentorship.sessions.cancelled")
                .description("Total number of sessions cancelled")
                .tag("service", "mentor-service")
                .register(registry);

        this.sessionsNoShowCounter = Counter.builder("mentorship.sessions.noshow")
                .description("Total number of sessions where mentee didn't attend")
                .tag("service", "mentor-service")
                .register(registry);

        this.reviewsSubmittedCounter = Counter.builder("mentorship.reviews.submitted")
                .description("Total number of reviews submitted")
                .tag("service", "mentor-service")
                .register(registry);

        // Initialize timers
        this.sessionDurationTimer = Timer.builder("mentorship.session.duration")
                .description("Duration of mentorship sessions")
                .tag("service", "mentor-service")
                .publishPercentiles(0.5, 0.95, 0.99) // p50, p95, p99
                .register(registry);

        this.sessionBookingTimer = Timer.builder("mentorship.session.booking.time")
                .description("Time taken to book a session")
                .tag("service", "mentor-service")
                .register(registry);

        this.reviewSubmissionTimer = Timer.builder("mentorship.review.submission.time")
                .description("Time taken to submit a review")
                .tag("service", "mentor-service")
                .register(registry);

        log.info("Mentorship metrics initialized successfully");
    }

    /**
     * Record session creation
     */
    public void recordSessionCreated() {
        sessionsCreatedCounter.increment();
        log.debug("Incremented sessions created counter");
    }

    /**
     * Record session completion
     */
    public void recordSessionCompleted() {
        sessionsCompletedCounter.increment();
        log.debug("Incremented sessions completed counter");
    }

    /**
     * Record session cancellation
     */
    public void recordSessionCancelled() {
        sessionsCancelledCounter.increment();
        log.debug("Incremented sessions cancelled counter");
    }

    /**
     * Record session no-show
     */
    public void recordSessionNoShow() {
        sessionsNoShowCounter.increment();
        log.debug("Incremented sessions no-show counter");
    }

    /**
     * Record review submission
     */
    public void recordReviewSubmitted() {
        reviewsSubmittedCounter.increment();
        log.debug("Incremented reviews submitted counter");
    }

    /**
     * Record session duration
     *
     * @param durationMinutes Duration in minutes
     */
    public void recordSessionDuration(long durationMinutes) {
        sessionDurationTimer.record(durationMinutes, java.util.concurrent.TimeUnit.MINUTES);
        log.debug("Recorded session duration: {} minutes", durationMinutes);
    }

    /**
     * Get session booking timer for manual recording
     *
     * @return Timer for session booking operations
     */
    public Timer getSessionBookingTimer() {
        return sessionBookingTimer;
    }

    /**
     * Get review submission timer for manual recording
     *
     * @return Timer for review submission operations
     */
    public Timer getReviewSubmissionTimer() {
        return reviewSubmissionTimer;
    }

    /**
     * Register active mentors gauge
     * Dynamically reports current count of active mentors
     *
     * @param activeMentorsSupplier Supplier that returns current active mentor count
     */
    public void registerActiveMentorsGauge(java.util.function.Supplier<Number> activeMentorsSupplier) {
        Gauge.builder("mentorship.mentors.active", activeMentorsSupplier)
                .description("Current number of active mentors")
                .tag("service", "mentor-service")
                .register(registry);
        log.info("Registered active mentors gauge");
    }

    /**
     * Register average rating gauge
     * Dynamically reports current average rating
     *
     * @param averageRatingSupplier Supplier that returns current average rating
     */
    public void registerAverageRatingGauge(java.util.function.Supplier<Number> averageRatingSupplier) {
        Gauge.builder("mentorship.reviews.average_rating", averageRatingSupplier)
                .description("Average rating across all reviews")
                .tag("service", "mentor-service")
                .register(registry);
        log.info("Registered average rating gauge");
    }
}