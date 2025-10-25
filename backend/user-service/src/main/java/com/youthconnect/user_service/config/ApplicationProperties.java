package com.youthconnect.user_service.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;
import java.util.List;

/**
 * ============================================================================
 * ApplicationProperties - Centralized Configuration Management
 * ============================================================================
 *
 * Type-safe configuration properties for Entrepreneurship Booster Platform Uganda User Service.
 * This class provides validated, structured access to all application settings
 * defined in application.yml or environment variables.
 *
 * <p>Configuration is organized into logical sections:</p>
 * <ul>
 *   <li>Security - Authentication, authorization, CORS, rate limiting, JWT, passwords</li>
 *   <li>Cache - Caching strategy and TTL settings</li>
 *   <li>Upload - File upload management and storage</li>
 *   <li>Notification - Integration with notification service</li>
 *   <li>USSD - USSD service integration and synthetic user management</li>
 *   <li>Audit - Audit logging and compliance settings</li>
 *   <li>Validation - Input validation rules for various fields</li>
 * </ul>
 *
 * <p><b>Usage Example:</b></p>
 * <pre>
 * {@code
 * @Autowired
 * private ApplicationProperties appProperties;
 *
 * String uploadDir = appProperties.getUpload().getUploadDirectory();
 * boolean isProd = appProperties.isProduction();
 * }
 * </pre>
 *
 * @author Douglas Kings Kato
 * @version 2.0.0
 * @since 1.0.0
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "app")
@Validated
public class ApplicationProperties {

    // ========================================================================
    // TOP-LEVEL APPLICATION PROPERTIES
    // ========================================================================

    /**
     * Application name - displayed in logs and monitoring tools
     */
    @NotBlank(message = "Application name must not be blank")
    private String name = "Youth Connect Uganda User Service";

    /**
     * Application version - used for versioning APIs and tracking deployments
     */
    @NotBlank(message = "Application version must not be blank")
    private String version = "1.0.0";

    /**
     * Runtime environment: development, staging, production, docker
     */
    @NotBlank(message = "Environment must be specified")
    private String environment = "development";

    /**
     * Security configuration including authentication, CORS, rate limiting, and JWT
     */
    @Valid
    @NotNull(message = "Security configuration must not be null")
    private Security security = new Security();

    /**
     * Cache configuration for performance optimization
     */
    @Valid
    @NotNull(message = "Cache configuration must not be null")
    private Cache cache = new Cache();

    /**
     * File upload configuration and storage settings
     */
    @Valid
    @NotNull(message = "Upload configuration must not be null")
    private Upload upload = new Upload();

    /**
     * Notification service integration settings
     */
    @Valid
    @NotNull(message = "Notification configuration must not be null")
    private Notification notification = new Notification();

    /**
     * USSD service integration and synthetic user management
     */
    @Valid
    @NotNull(message = "USSD configuration must not be null")
    private Ussd ussd = new Ussd();

    /**
     * Audit logging configuration for compliance and monitoring
     */
    @Valid
    @NotNull(message = "Audit configuration must not be null")
    private Audit audit = new Audit();

    /**
     * Input validation rules and patterns
     */
    @Valid
    @NotNull(message = "Validation configuration must not be null")
    private Validation validation = new Validation();

    // ========================================================================
    // NESTED CONFIGURATION: SECURITY
    // ========================================================================

    /**
     * Security Configuration
     *
     * Manages all security-related settings including:
     * - Internal API authentication
     * - CORS policies for cross-origin requests
     * - Rate limiting to prevent abuse
     * - Password strength requirements
     * - JWT token configuration
     */
    @Data
    public static class Security {

        /**
         * Internal API key for service-to-service authentication
         * Should be overridden in production via environment variables
         */
        @NotBlank(message = "Internal API key must be specified")
        private String internalApiKey = "internal-secret-key-2024";

        @Valid
        @NotNull
        private Cors cors = new Cors();

        @Valid
        @NotNull
        private RateLimit rateLimit = new RateLimit();

        @Valid
        @NotNull
        private Password password = new Password();

        @Valid
        @NotNull
        private Jwt jwt = new Jwt();

        /**
         * CORS (Cross-Origin Resource Sharing) Configuration
         *
         * Defines which origins, methods, and headers are allowed
         * for cross-origin requests to the API.
         */
        @Data
        public static class Cors {
            /**
             * List of allowed origins (domains) that can access the API
             * Use specific domains in production, avoid wildcards
             */
            @NotEmpty(message = "At least one allowed origin must be specified")
            private List<String> allowedOrigins = new ArrayList<>(List.of(
                    "http://localhost:3000",           // React dev server
                    "http://localhost:3001",           // Alternative dev port
                    "https://youthconnect.ug",         // Production domain
                    "https://www.youthconnect.ug",     // Production with www
                    "https://app.youthconnect.ug"      // App subdomain
            ));

            /**
             * HTTP methods allowed for CORS requests
             */
            @NotEmpty(message = "At least one allowed method must be specified")
            private List<String> allowedMethods = new ArrayList<>(List.of(
                    "GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"
            ));

            /**
             * Headers that are allowed in CORS requests
             * "*" allows all headers (use cautiously in production)
             */
            @NotEmpty(message = "At least one allowed header must be specified")
            private List<String> allowedHeaders = new ArrayList<>(List.of("*"));

            /**
             * Headers that are exposed to the client in CORS responses
             */
            @NotEmpty(message = "At least one exposed header must be specified")
            private List<String> exposedHeaders = new ArrayList<>(List.of(
                    "Authorization",
                    "Content-Type",
                    "X-Total-Count",
                    "X-Page-Number",
                    "X-Page-Size"
            ));

            /**
             * Allow cookies and authentication credentials in CORS requests
             * Required for JWT token authentication
             */
            private boolean allowCredentials = true;

            /**
             * Maximum age (seconds) for CORS preflight cache
             * Reduces preflight OPTIONS requests
             */
            @Min(value = 0, message = "CORS max age cannot be negative")
            @Max(value = 86400, message = "CORS max age cannot exceed 24 hours")
            private long maxAge = 3600; // 1 hour
        }

        /**
         * Rate Limiting Configuration
         *
         * Protects the API from abuse and DDoS attacks by limiting
         * the number of requests per time period.
         */
        @Data
        public static class RateLimit {
            /**
             * Enable/disable rate limiting globally
             */
            private boolean enabled = true;

            /**
             * Maximum requests allowed per minute for general endpoints
             */
            @Min(value = 1, message = "Requests per minute must be at least 1")
            @Max(value = 10000, message = "Requests per minute cannot exceed 10000")
            private long requestsPerMinute = 100;

            /**
             * Burst capacity - allows temporary spikes above the rate limit
             * Useful for legitimate traffic bursts
             */
            @Min(value = 1, message = "Burst capacity must be at least 1")
            private long burstCapacity = 150;

            /**
             * Stricter rate limit for authentication endpoints
             * Prevents brute force attacks
             */
            @Min(value = 1, message = "Auth requests per minute must be at least 1")
            @Max(value = 100, message = "Auth requests per minute cannot exceed 100")
            private long authRequestsPerMinute = 20;

            /**
             * Alternative property name for consistency across services
             */
            @Min(value = 1, message = "Burst size must be at least 1")
            @Max(value = 1000, message = "Burst size cannot exceed 1000")
            private int burstSize = 20;
        }

        /**
         * Password Policy Configuration
         *
         * Defines password strength requirements to ensure user account security.
         */
        @Data
        public static class Password {
            /**
             * Minimum password length
             */
            @Min(value = 6, message = "Password minimum length must be at least 6")
            @Max(value = 20, message = "Password minimum length cannot exceed 20")
            private int minLength = 8;

            /**
             * Maximum password length
             */
            @Min(value = 20, message = "Password maximum length must be at least 20")
            @Max(value = 256, message = "Password maximum length cannot exceed 256")
            private int maxLength = 128;

            /**
             * Require at least one uppercase letter (A-Z)
             */
            private boolean requireUppercase = true;

            /**
             * Require at least one lowercase letter (a-z)
             */
            private boolean requireLowercase = true;

            /**
             * Require at least one digit (0-9)
             */
            private boolean requireDigit = true;

            /**
             * Require at least one special character (!@#$%^&*()_+-=[]{}|;:,.<>?)
             */
            private boolean requireSpecial = true;
        }

        /**
         * JWT (JSON Web Token) Configuration
         *
         * Settings for token-based authentication and authorization.
         */
        @Data
        public static class Jwt {
            /**
             * HTTP header name for JWT tokens
             */
            @NotBlank(message = "JWT header name must not be blank")
            private String header = "Authorization";

            /**
             * Token prefix in the Authorization header
             * Format: "Bearer <token>"
             */
            @NotBlank(message = "JWT prefix must not be blank")
            private String prefix = "Bearer ";

            /**
             * JWT issuer claim - identifies who issued the token
             */
            @NotBlank(message = "JWT issuer must not be blank")
            private String issuer = "youthconnect-uganda";

            /**
             * JWT audience claim - identifies intended recipients
             */
            @NotBlank(message = "JWT audience must not be blank")
            private String audience = "youthconnect-users";
        }
    }

    // ========================================================================
    // NESTED CONFIGURATION: CACHE
    // ========================================================================

    /**
     * Cache Configuration
     *
     * Settings for application-level caching to improve performance
     * and reduce database load.
     */
    @Data
    public static class Cache {
        /**
         * Enable/disable caching globally
         */
        private boolean enabled = true;

        /**
         * Time-to-live (TTL) for cache entries in seconds
         * After this period, cached data is invalidated
         */
        @Min(value = 60, message = "Cache TTL must be at least 60 seconds")
        @Max(value = 86400, message = "Cache TTL cannot exceed 24 hours")
        private int ttl = 3600; // 1 hour default

        /**
         * Maximum number of entries in the cache
         * Prevents unlimited memory growth
         */
        @Min(value = 100, message = "Cache max entries must be at least 100")
        @Max(value = 100000, message = "Cache max entries cannot exceed 100000")
        private long maxEntries = 1000;

        /**
         * Enable cache statistics collection
         * Useful for monitoring hit rates and performance
         */
        private boolean enableStats = true;
    }

    // ========================================================================
    // NESTED CONFIGURATION: UPLOAD
    // ========================================================================

    /**
     * File Upload Configuration
     *
     * Manages file upload settings including storage paths, size limits,
     * allowed file types, and additional processing options.
     */
    @Data
    public static class Upload {
        /**
         * Base directory for file uploads
         *
         * Development: ./uploads (relative to application root)
         * Production: /var/youthconnect/uploads or cloud storage path
         */
        @NotBlank(message = "Upload directory must be specified")
        private String uploadDir = "./uploads";

        /**
         * Maximum individual file size
         * Format: "5MB", "10MB", etc. (for Spring Boot)
         * Numeric value in bytes for programmatic checks
         */
        @NotBlank(message = "Max file size must be specified")
        private String maxFileSize = "10MB";

        /**
         * Maximum file size in bytes for validation
         */
        @Min(value = 1024, message = "Max file size must be at least 1KB")
        @Max(value = 104857600, message = "Max file size cannot exceed 100MB")
        private long maxFileSizeBytes = 10485760L; // 10MB

        /**
         * Maximum total request size (file + metadata + form data)
         */
        @NotBlank(message = "Max request size must be specified")
        private String maxRequestSize = "15MB";

        /**
         * Maximum request size in bytes
         */
        @Min(value = 1024, message = "Max request size must be at least 1KB")
        @Max(value = 104857600, message = "Max request size cannot exceed 100MB")
        private long maxRequestSizeBytes = 15728640L; // 15MB

        /**
         * Allowed file extensions for uploads
         * Used for validation and security filtering
         */
        @NotEmpty(message = "At least one allowed extension must be specified")
        private List<String> allowedExtensions = new ArrayList<>(List.of(
                "jpg", "jpeg", "png", "gif",    // Images
                "pdf", "doc", "docx", "txt",    // Documents
                "mp3", "wav", "m4a", "ogg"      // Audio files
        ));

        /**
         * Enable virus scanning for uploaded files
         * Requires ClamAV or similar antivirus service integration
         */
        private boolean virusScanEnabled = false;

        /**
         * Enable automatic file compression to save storage
         */
        private boolean autoCompression = true;

        /**
         * Generate thumbnails for image uploads
         * Useful for profile pictures and previews
         */
        private boolean generateThumbnails = true;

        /**
         * Thumbnail width in pixels
         */
        @Min(value = 50, message = "Thumbnail width must be at least 50px")
        @Max(value = 1000, message = "Thumbnail width cannot exceed 1000px")
        private int thumbnailWidth = 200;

        /**
         * Thumbnail height in pixels
         */
        @Min(value = 50, message = "Thumbnail height must be at least 50px")
        @Max(value = 1000, message = "Thumbnail height cannot exceed 1000px")
        private int thumbnailHeight = 200;

        /**
         * Returns the base upload directory path
         *
         * @return Upload directory path
         */
        public String getUploadDirectory() {
            return this.uploadDir;
        }

        /**
         * Alternative method name for consistency
         *
         * @return Upload directory path
         */
        public String getDirectory() {
            return this.uploadDir;
        }

        /**
         * Constructs full path for user-specific uploads
         *
         * @param userId User ID
         * @param category File category (e.g., "profile-pictures", "documents")
         * @return Full path: uploads/users/{userId}/{category}
         */
        public String getUserUploadPath(Long userId, String category) {
            return String.format("%s/users/%d/%s", uploadDir, userId, category);
        }

        /**
         * Constructs full path for module-specific audio files
         *
         * @param moduleKey Unique module identifier
         * @return Full path: uploads/modules/{moduleKey}
         */
        public String getModuleUploadPath(String moduleKey) {
            return String.format("%s/modules/%s", uploadDir, moduleKey);
        }

        /**
         * Constructs full path for temporary uploads
         *
         * @return Full path: uploads/temp
         */
        public String getTempUploadPath() {
            return String.format("%s/temp", uploadDir);
        }
    }

    // ========================================================================
    // NESTED CONFIGURATION: NOTIFICATION
    // ========================================================================

    /**
     * Notification Service Configuration
     *
     * Settings for integration with the notification service
     * for sending emails, SMS, and push notifications.
     */
    @Data
    public static class Notification {
        /**
         * Notification service base URL
         */
        @NotBlank(message = "Notification service URL must be specified")
        private String serviceUrl = "http://localhost:8084";

        /**
         * HTTP client timeout for notification service calls (milliseconds)
         */
        @Min(value = 1000, message = "Notification timeout must be at least 1000ms")
        @Max(value = 30000, message = "Notification timeout cannot exceed 30000ms")
        private int timeout = 5000;

        /**
         * Enable email notifications
         */
        private boolean emailEnabled = true;

        /**
         * Enable SMS notifications
         */
        private boolean smsEnabled = true;

        /**
         * Send welcome message upon user registration
         */
        private boolean welcomeMessageEnabled = true;
    }

    // ========================================================================
    // NESTED CONFIGURATION: USSD
    // ========================================================================

    /**
     * USSD Service Configuration
     *
     * Settings for USSD service integration and management of
     * synthetic users created through USSD sessions.
     */
    @Data
    public static class Ussd {
        /**
         * USSD service base URL
         */
        @NotBlank(message = "USSD service URL must be specified")
        private String serviceUrl = "http://localhost:8083";

        /**
         * HTTP client timeout for USSD service calls (milliseconds)
         */
        @Min(value = 1000, message = "USSD timeout must be at least 1000ms")
        @Max(value = 15000, message = "USSD timeout cannot exceed 15000ms")
        private int timeout = 5000;

        /**
         * Number of retry attempts for failed USSD service calls
         */
        @Min(value = 0, message = "Retry attempts cannot be negative")
        @Max(value = 5, message = "Retry attempts cannot exceed 5")
        private int retryAttempts = 3;

        /**
         * Enable circuit breaker pattern for USSD service
         * Prevents cascading failures when service is down
         */
        private boolean circuitBreakerEnabled = true;

        /**
         * Default password for synthetic USSD users
         * Used when users register through USSD without providing a password
         */
        @NotBlank(message = "Default USSD password must not be blank")
        private String defaultPassword = "ussd_default_password";

        /**
         * Email domain for synthetic email addresses
         * Format: {phoneNumber}@ussd.youthconnect.ug
         */
        @NotBlank(message = "Synthetic email domain must not be blank")
        private String syntheticEmailDomain = "ussd.youthconnect.ug";
    }

    // ========================================================================
    // NESTED CONFIGURATION: AUDIT
    // ========================================================================

    /**
     * Audit Logging Configuration
     *
     * Settings for audit trail and compliance logging.
     * Tracks user actions, API calls, and system events.
     */
    @Data
    public static class Audit {
        /**
         * Enable/disable audit logging globally
         */
        private boolean enabled = true;

        /**
         * Include request body in audit logs
         * WARNING: May contain sensitive data
         */
        private boolean includeRequestBody = false;

        /**
         * Include response body in audit logs
         * WARNING: May contain sensitive data
         */
        private boolean includeResponseBody = false;

        /**
         * Log requests that exceed threshold as slow requests
         */
        private boolean logSlowRequests = true;

        /**
         * Threshold for slow request logging (milliseconds)
         */
        @Min(value = 100, message = "Slow request threshold must be at least 100ms")
        @Max(value = 60000, message = "Slow request threshold cannot exceed 60000ms")
        private int slowRequestThresholdMs = 5000;

        /**
         * Number of days to retain audit logs
         * Used for automatic cleanup of old logs
         */
        @Min(value = 1, message = "Audit retention days must be at least 1")
        @Max(value = 365, message = "Audit retention days cannot exceed 365")
        private int retentionDays = 90;
    }

    // ========================================================================
    // NESTED CONFIGURATION: VALIDATION
    // ========================================================================

    /**
     * Input Validation Configuration
     *
     * Defines validation rules and patterns for various input fields
     * throughout the application.
     */
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

        /**
         * Phone Number Validation Rules
         */
        @Data
        public static class Phone {
            /**
             * Regular expression pattern for phone number validation
             * Allows international format with optional + prefix
             */
            @NotBlank(message = "Phone pattern must not be blank")
            private String pattern = "^\\+?[0-9\\s\\-().]{10,15}$";

            /**
             * Default country code for Uganda
             * Prepended to phone numbers without country code
             */
            @NotBlank(message = "Default country code must not be blank")
            private String defaultCountryCode = "+256";
        }

        /**
         * Name Field Validation Rules
         */
        @Data
        public static class Name {
            /**
             * Minimum name length
             */
            @Min(value = 1, message = "Name minimum length must be at least 1")
            private int minLength = 2;

            /**
             * Maximum name length
             */
            @Min(value = 10, message = "Name maximum length must be at least 10")
            @Max(value = 100, message = "Name maximum length cannot exceed 100")
            private int maxLength = 50;

            /**
             * Regular expression pattern for name validation
             * Allows letters, spaces, apostrophes, hyphens, and periods
             */
            @NotBlank(message = "Name pattern must not be blank")
            private String pattern = "^[a-zA-Z\\s'.-]+$";
        }

        /**
         * Email Validation Rules
         */
        @Data
        public static class Email {
            /**
             * Regular expression pattern for email validation
             */
            @NotBlank(message = "Email pattern must not be blank")
            private String pattern = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";

            /**
             * Maximum email length (RFC 5321 standard)
             */
            @Min(value = 50, message = "Email max length must be at least 50")
            @Max(value = 320, message = "Email max length cannot exceed 320")
            private int maxLength = 254;
        }

        /**
         * User Registration Validation Rules
         */
        @Data
        public static class Registration {
            /**
             * Require email verification before account activation
             */
            private boolean emailVerificationRequired = false;

            /**
             * Automatically activate user accounts upon registration
             */
            private boolean autoActivate = true;

            /**
             * Send welcome message to new users
             */
            private boolean welcomeMessageEnabled = true;
        }
    }

    // ========================================================================
    // CONVENIENCE METHODS
    // ========================================================================

    /**
     * Checks if the application is running in development environment
     *
     * @return true if environment is "development"
     */
    public boolean isDevelopment() {
        return "development".equalsIgnoreCase(environment);
    }

    /**
     * Checks if the application is running in production environment
     *
     * @return true if environment is "production"
     */
    public boolean isProduction() {
        return "production".equalsIgnoreCase(environment);
    }

    /**
     * Checks if the application is running in Docker environment
     *
     * @return true if environment is "docker"
     */
    public boolean isDocker() {
        return "docker".equalsIgnoreCase(environment);
    }

    /**
     * Checks if the application is running in staging environment
     *
     * @return true if environment is "staging"
     */
    public boolean isStaging() {
        return "staging".equalsIgnoreCase(environment);
    }

    /**
     * Returns formatted application information string
     *
     * @return Format: "ApplicationName vVersion (environment)"
     * @example "Entrepreneurship Booster Platform Uganda User Service v1.0.0 (production)"
     */
    public String getApplicationInfo() {
        return String.format("%s v%s (%s)", name, version, environment);
    }

    /**
     * Returns a simple application identifier
     *
     * @return Format: "name-version"
     * @example "entrepreneurship-booster-platform-1.0.0"
     */
    public String getApplicationId() {
        return String.format("%s-%s",
                name.toLowerCase().replace(" ", "-"),
                version
        );
    }
}