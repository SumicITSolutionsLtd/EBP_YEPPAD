package com.youthconnect.mentor_service.config;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import java.util.List;

/**
 * ============================================================================
 * APPLICATION PROPERTIES CONFIGURATION
 * ============================================================================
 *
 * Type-safe configuration properties for mentor-service.
 * Binds to application.yml properties with validation.
 *
 * USAGE:
 * - Inject this class where configuration is needed
 * - All properties validated on startup
 * - Environment-specific values in application-{profile}.yml
 *
 * VALIDATION:
 * - Uses Jakarta Bean Validation annotations
 * - Application fails fast on startup if invalid config
 *
 * @author Douglas Kings Kato
 * @version 1.0.0
 * @since 2025-01-21
 * ============================================================================
 */
@Configuration
@ConfigurationProperties(prefix = "app")
@Validated
@Data
public class ApplicationProperties {

    /**
     * Mentorship session configuration
     */
    private MentorshipConfig mentorship = new MentorshipConfig();

    /**
     * Review and rating configuration
     */
    private ReviewsConfig reviews = new ReviewsConfig();

    /**
     * Matching algorithm configuration
     */
    private MatchingConfig matching = new MatchingConfig();

    /**
     * Cache configuration
     */
    private CacheConfig cache = new CacheConfig();

    /**
     * Feature flags
     */
    private FeaturesConfig features = new FeaturesConfig();

    /**
     * Mentorship session settings
     */
    @Data
    public static class MentorshipConfig {
        /**
         * Default session duration in minutes
         * Default: 60 minutes (1 hour)
         */
        @Min(15)
        @Max(240)
        private Integer defaultSessionDuration = 60;

        /**
         * Maximum sessions per mentor per week
         * Prevents mentor burnout
         */
        @Min(1)
        @Max(50)
        private Integer maxSessionsPerWeek = 10;

        /**
         * Minimum gap between sessions in minutes
         * Allows mentor prep time between sessions
         */
        @Min(0)
        @Max(180)
        private Integer minSessionGap = 30;

        /**
         * Session reminder times in hours before session
         * Example: [24, 1] = 24 hours and 1 hour before
         */
        @NotEmpty
        private List<Integer> reminderTimes = List.of(24, 1);

        /**
         * Auto-cancel threshold in hours
         * Sessions auto-cancelled if no-show after this time
         */
        @Min(1)
        @Max(72)
        private Integer autoCancelThreshold = 24;
    }

    /**
     * Review and rating configuration
     */
    @Data
    public static class ReviewsConfig {
        /**
         * Minimum rating value (1-5 scale)
         */
        @Min(1)
        @Max(5)
        private Integer minRating = 1;

        /**
         * Maximum rating value (1-5 scale)
         */
        @Min(1)
        @Max(5)
        private Integer maxRating = 5;

        /**
         * Enable review moderation
         * If true, low ratings or flagged content reviewed by NGO
         */
        @NotNull
        private Boolean moderationEnabled = true;

        /**
         * Minimum comment length in characters
         */
        @Min(0)
        @Max(100)
        private Integer minCommentLength = 10;

        /**
         * Maximum comment length in characters
         */
        @Min(50)
        @Max(5000)
        private Integer maxCommentLength = 500;
    }

    /**
     * Mentor matching algorithm configuration
     */
    @Data
    public static class MatchingConfig {
        /**
         * Minimum match score to show in recommendations (0-100)
         */
        @Min(0)
        @Max(100)
        private Integer minMatchScore = 60;

        /**
         * Weight factors for matching algorithm
         * All weights should sum to 1.0
         */
        private WeightsConfig weights = new WeightsConfig();

        @Data
        public static class WeightsConfig {
            /**
             * Weight for expertise area match (0.0 - 1.0)
             */
            @Min(0)
            @Max(1)
            private Double expertise = 0.4;

            /**
             * Weight for mentor rating (0.0 - 1.0)
             */
            @Min(0)
            @Max(1)
            private Double rating = 0.3;

            /**
             * Weight for availability match (0.0 - 1.0)
             */
            @Min(0)
            @Max(1)
            private Double availability = 0.2;

            /**
             * Weight for location proximity (0.0 - 1.0)
             */
            @Min(0)
            @Max(1)
            private Double location = 0.1;
        }
    }

    /**
     * Cache TTL configuration
     */
    @Data
    public static class CacheConfig {
        private TtlConfig ttl = new TtlConfig();

        @Data
        public static class TtlConfig {
            /**
             * Cache TTL for mentor profiles in seconds
             */
            @Min(60)
            private Integer mentors = 1800; // 30 minutes

            /**
             * Cache TTL for sessions in seconds
             */
            @Min(60)
            private Integer sessions = 300; // 5 minutes

            /**
             * Cache TTL for availability in seconds
             */
            @Min(60)
            private Integer availability = 600; // 10 minutes

            /**
             * Cache TTL for reviews in seconds
             */
            @Min(300)
            private Integer reviews = 3600; // 1 hour

            /**
             * Cache TTL for statistics in seconds
             */
            @Min(300)
            private Integer statistics = 1800; // 30 minutes
        }
    }

    /**
     * Feature flags for enabling/disabling features
     */
    @Data
    public static class FeaturesConfig {
        /**
         * Enable video conferencing integration
         */
        @NotNull
        private Boolean videoCallsEnabled = false;

        /**
         * Enable calendar integration (Google, Outlook)
         */
        @NotNull
        private Boolean calendarIntegrationEnabled = false;

        /**
         * Enable AI-powered mentor matching
         */
        @NotNull
        private Boolean aiMatchingEnabled = true;

        /**
         * Enable automated session reminders
         */
        @NotNull
        private Boolean autoRemindersEnabled = true;
    }
}