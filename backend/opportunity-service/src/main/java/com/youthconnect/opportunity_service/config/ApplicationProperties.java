package com.youthconnect.opportunity_service.config;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

/**
 * Centralized configuration properties for the Opportunity Service.
 * All configurable values should be defined here and injected via application.properties.
 * This ensures type-safety and validation for all configuration.
 */
@Configuration
@ConfigurationProperties(prefix = "youthconnect.opportunity")
@Validated
@Data
public class ApplicationProperties {

    /**
     * Service identification and metadata
     */
    @NotBlank
    private String serviceName = "opportunity-service";

    @NotBlank
    private String version = "1.0.0";

    /**
     * Opportunity-specific business rules
     */
    private OpportunityConfig opportunity = new OpportunityConfig();

    /**
     * Application processing configuration
     */
    private ApplicationConfig application = new ApplicationConfig();

    /**
     * Integration with other microservices
     */
    private IntegrationConfig integration = new IntegrationConfig();

    /**
     * File upload configuration
     */
    private FileUploadConfig fileUpload = new FileUploadConfig();

    /**
     * Cache configuration
     */
    private CacheConfig cache = new CacheConfig();

    @Data
    public static class OpportunityConfig {
        /**
         * Default number of days before deadline to send reminder
         */
        @Min(1)
        private int reminderDaysBeforeDeadline = 7;

        /**
         * Maximum number of opportunities a single user can post per month
         */
        @Min(1)
        private int maxOpportunitiesPerMonth = 50;

        /**
         * Auto-close opportunities after deadline (days)
         */
        @Min(0)
        private int autoCloseDaysAfterDeadline = 3;

        /**
         * Enable AI-powered opportunity matching
         */
        private boolean enableAiMatching = true;

        /**
         * Minimum matching score for recommendations (0.0 - 1.0)
         */
        @Min(0)
        private double minMatchingScore = 0.6;
    }

    @Data
    public static class ApplicationConfig {
        /**
         * Maximum number of applications a youth can submit per day
         */
        @Min(1)
        private int maxApplicationsPerDay = 20;

        /**
         * Days to keep applications in "PENDING" before auto-review
         */
        @Min(1)
        private int autoReviewAfterDays = 14;

        /**
         * Enable duplicate application detection
         */
        private boolean enableDuplicateDetection = true;

        /**
         * Maximum attachments per application
         */
        @Min(1)
        private int maxAttachmentsPerApplication = 5;
    }

    @Data
    public static class IntegrationConfig {
        /**
         * User Service base URL
         */
        @NotBlank
        private String userServiceUrl = "http://user-service";

        /**
         * Notification Service base URL
         */
        @NotBlank
        private String notificationServiceUrl = "http://notification-service";

        /**
         * Analytics Service base URL
         */
        @NotBlank
        private String analyticsServiceUrl = "http://analytics-service";

        /**
         * Timeout for inter-service calls (milliseconds)
         */
        @Min(1000)
        private int serviceCallTimeout = 5000;

        /**
         * Retry attempts for failed service calls
         */
        @Min(0)
        private int retryAttempts = 3;
    }

    @Data
    public static class FileUploadConfig {
        /**
         * Maximum file size for application attachments (bytes)
         */
        @Min(1)
        private long maxFileSize = 10485760; // 10MB

        /**
         * Allowed file extensions
         */
        @NotNull
        private String[] allowedExtensions = {
                "pdf", "doc", "docx", "jpg", "jpeg", "png"
        };

        /**
         * Upload directory path
         */
        @NotBlank
        private String uploadPath = "./uploads/applications";
    }

    @Data
    public static class CacheConfig {
        /**
         * Cache TTL for opportunity listings (seconds)
         */
        @Min(60)
        private int opportunityListTtl = 300; // 5 minutes

        /**
         * Cache TTL for single opportunity details (seconds)
         */
        @Min(60)
        private int opportunityDetailTtl = 600; // 10 minutes

        /**
         * Maximum cache size (number of entries)
         */
        @Min(100)
        private int maxCacheSize = 1000;
    }
}