package com.youthconnect.user_service.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.ArrayList;

/**
 * Application Properties Configuration for Youth Connect Uganda User Service
 *
 * This class provides type-safe access to application configuration properties
 * defined in application.yml. It includes validation and default values for
 * all configurable aspects of the application.
 *
 */
@Data
@Component
@ConfigurationProperties(prefix = "app")
@Validated
public class ApplicationProperties {

    @NotBlank(message = "Application name must not be blank")
    private String name = "Youth Connect Uganda User Service";

    @NotBlank(message = "Application version must not be blank")
    private String version = "1.0.0";

    @NotBlank(message = "Environment must be specified")
    private String environment = "development";

    @Valid
    @NotNull
    private Security security = new Security();

    @Valid
    @NotNull
    private Cache cache = new Cache();

    @Valid
    @NotNull
    private Upload upload = new Upload();

    @Valid
    @NotNull
    private Notification notification = new Notification();

    @Valid
    @NotNull
    private Ussd ussd = new Ussd();

    @Valid
    @NotNull
    private Audit audit = new Audit();

    @Valid
    @NotNull
    private Validation validation = new Validation();

    /**
     * Security Configuration Properties
     */
    @Data
    public static class Security {

        @NotBlank(message = "Internal API key must be specified")
        private String internalApiKey = "internal-secret-key-2024";

        @Valid
        @NotNull
        private RateLimit rateLimit = new RateLimit();

        @Valid
        @NotNull
        private Password password = new Password();

        @Valid
        @NotNull
        private Jwt jwt = new Jwt();

        // FIXED: Added CORS configuration
        @Valid
        @NotNull
        private Cors cors = new Cors();

        @Data
        public static class RateLimit {
            private boolean enabled = true;

            @Min(value = 1, message = "Requests per minute must be at least 1")
            @Max(value = 10000, message = "Requests per minute cannot exceed 10000")
            private long requestsPerMinute = 100;

            @Min(value = 1, message = "Burst capacity must be at least 1")
            private long burstCapacity = 150;

            @Min(value = 1, message = "Auth requests per minute must be at least 1")
            @Max(value = 100, message = "Auth requests per minute cannot exceed 100")
            private long authRequestsPerMinute = 20;
        }

        @Data
        public static class Password {
            @Min(value = 6, message = "Password minimum length must be at least 6")
            @Max(value = 20, message = "Password minimum length cannot exceed 20")
            private int minLength = 8;

            @Min(value = 20, message = "Password maximum length must be at least 20")
            @Max(value = 256, message = "Password maximum length cannot exceed 256")
            private int maxLength = 128;

            private boolean requireUppercase = true;
            private boolean requireLowercase = true;
            private boolean requireDigit = true;
            private boolean requireSpecial = true;
        }

        @Data
        public static class Jwt {
            @NotBlank(message = "JWT header name must not be blank")
            private String header = "Authorization";

            @NotBlank(message = "JWT prefix must not be blank")
            private String prefix = "Bearer ";

            @NotBlank(message = "JWT issuer must not be blank")
            private String issuer = "youthconnect-uganda";

            @NotBlank(message = "JWT audience must not be blank")
            private String audience = "youthconnect-users";
        }

        // FIXED: Added complete CORS configuration
        @Data
        public static class Cors {
            private List<String> allowedOrigins = new ArrayList<>(List.of(
                    "http://localhost:3000",
                    "http://localhost:3001",
                    "https://youthconnect.ug",
                    "https://www.youthconnect.ug"
            ));

            private List<String> allowedMethods = new ArrayList<>(List.of(
                    "GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"
            ));

            private List<String> allowedHeaders = new ArrayList<>(List.of("*"));

            private List<String> exposedHeaders = new ArrayList<>(List.of(
                    "Authorization", "Content-Type", "X-Total-Count"
            ));

            private boolean allowCredentials = true;

            @Min(value = 0, message = "CORS max age cannot be negative")
            @Max(value = 86400, message = "CORS max age cannot exceed 24 hours")
            private long maxAge = 3600;
        }
    }

    @Data
    public static class Cache {
        private boolean enabled = true;

        @Min(value = 60, message = "Cache TTL must be at least 60 seconds")
        @Max(value = 86400, message = "Cache TTL cannot exceed 24 hours")
        private int ttl = 3600;

        @Min(value = 100, message = "Cache max entries must be at least 100")
        @Max(value = 100000, message = "Cache max entries cannot exceed 100000")
        private long maxEntries = 1000;

        private boolean enableStats = true;
    }

    @Data
    public static class Upload {
        @NotBlank(message = "Max file size must be specified")
        private String maxFileSize = "5MB";

        @NotBlank(message = "Max request size must be specified")
        private String maxRequestSize = "10MB";

        @NotBlank(message = "Upload directory must be specified")
        private String uploadDir = "uploads/";

        private List<String> allowedExtensions = new ArrayList<>(List.of(
                "jpg", "jpeg", "png", "pdf", "doc", "docx"
        ));
    }

    @Data
    public static class Notification {
        private String serviceUrl = "http://localhost:8084";

        @Min(value = 1000, message = "Notification timeout must be at least 1000ms")
        @Max(value = 30000, message = "Notification timeout cannot exceed 30000ms")
        private int timeout = 5000;

        private boolean emailEnabled = true;
        private boolean smsEnabled = true;
        private boolean welcomeMessageEnabled = true;
    }

    @Data
    public static class Ussd {
        private String serviceUrl = "http://localhost:8083";

        @Min(value = 1000, message = "USSD timeout must be at least 1000ms")
        @Max(value = 15000, message = "USSD timeout cannot exceed 15000ms")
        private int timeout = 5000;

        @Min(value = 0, message = "Retry attempts cannot be negative")
        @Max(value = 5, message = "Retry attempts cannot exceed 5")
        private int retryAttempts = 3;

        private boolean circuitBreakerEnabled = true;

        @NotBlank(message = "Default USSD password must not be blank")
        private String defaultPassword = "ussd_default_password";

        @NotBlank(message = "Synthetic email domain must not be blank")
        private String syntheticEmailDomain = "ussd.youthconnect.ug";
    }

    @Data
    public static class Audit {
        private boolean enabled = true;
        private boolean includeRequestBody = false;
        private boolean includeResponseBody = false;

        @Min(value = 1, message = "Audit retention days must be at least 1")
        @Max(value = 365, message = "Audit retention days cannot exceed 365")
        private int retentionDays = 90;
    }

    @Data
    public static class Validation {
        @Valid
        @NotNull
        private Phone phone = new Phone();

        @Valid
        @NotNull
        private Name name = new Name();

        @Valid
        @NotNull
        private Email email = new Email();

        @Valid
        @NotNull
        private Registration registration = new Registration();

        @Data
        public static class Phone {
            @NotBlank(message = "Phone pattern must not be blank")
            private String pattern = "^\\+?[0-9\\s\\-().]{10,15}$";

            @NotBlank(message = "Default country code must not be blank")
            private String defaultCountryCode = "+256";
        }

        @Data
        public static class Name {
            @Min(value = 1, message = "Name minimum length must be at least 1")
            private int minLength = 2;

            @Min(value = 10, message = "Name maximum length must be at least 10")
            @Max(value = 100, message = "Name maximum length cannot exceed 100")
            private int maxLength = 50;

            @NotBlank(message = "Name pattern must not be blank")
            private String pattern = "^[a-zA-Z\\s'.-]+$";
        }

        @Data
        public static class Email {
            @NotBlank(message = "Email pattern must not be blank")
            private String pattern = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";

            @Min(value = 50, message = "Email max length must be at least 50")
            @Max(value = 320, message = "Email max length cannot exceed 320")
            private int maxLength = 254;
        }

        @Data
        public static class Registration {
            private boolean emailVerificationRequired = false;
            private boolean autoActivate = true;
            private boolean welcomeMessageEnabled = true;
        }
    }

    // Convenience methods
    public boolean isDevelopment() {
        return "development".equalsIgnoreCase(environment);
    }

    public boolean isProduction() {
        return "production".equalsIgnoreCase(environment);
    }
}