package com.youthconnect.job_services.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.List;

/**
 * ═══════════════════════════════════════════════════════════════════════════
 * FILE UPLOAD CONFIGURATION PROPERTIES - FIXED (v3.0.5)
 * ═══════════════════════════════════════════════════════════════════════════
 *
 * Type-safe configuration properties for file upload settings.
 * Binds to `app.upload.*` properties in application.yml
 *
 * FIXES APPLIED:
 * Added @Component annotation to ensure single bean creation
 * Added @Validated for JSR-380 bean validation
 * Added validation constraints for type safety
 * Added @PostConstruct validation method
 * Added helper methods for common operations
 *
 * Configuration Example (application.yml):
 * -----------------------------------------
 * app:
 *   upload:
 *     max-file-size: 10485760       # 10MB in bytes
 *     allowed-types:
 *       - application/pdf
 *       - application/msword
 *       - application/vnd.openxmlformats-officedocument.wordprocessingml.document
 *     directory: ./uploads           # Relative or absolute path
 *     base-url: http://localhost:8000
 *
 * @author Douglas Kings Kato
 * @version 3.0.5 (Bean Conflict Fix)
 * @since 2025-11-26
 */
@Data
@Component  //  Ensures single bean creation
@Validated  //  Enables JSR-380 validation
@ConfigurationProperties(prefix = "app.upload")
public class FileUploadProperties {

    /**
     * Maximum file size in bytes
     *
     * Default: 10MB (10,485,760 bytes)
     *
     * Validation: Must be positive number
     */
    @Positive(message = "Max file size must be positive")
    @NotNull(message = "Max file size cannot be null")
    private Long maxFileSize = 10485760L; // 10MB

    /**
     * Allowed MIME types for file uploads
     *
     * Supported formats:
     * - application/pdf (PDF documents)
     * - application/msword (Microsoft Word 97-2003)
     * - application/vnd.openxmlformats-officedocument.wordprocessingml.document (Word 2007+)
     *
     * Validation: List cannot be empty
     */
    @NotEmpty(message = "Allowed types list cannot be empty")
    private List<String> allowedTypes = List.of(
            "application/pdf",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
    );

    /**
     * Upload directory path
     *
     * Can be relative or absolute:
     * - Relative: ./uploads (relative to application working directory)
     * - Absolute: /var/app/uploads (absolute system path)
     * - Environment variable: ${UPLOAD_DIR:./uploads}
     *
     * Validation: Cannot be empty
     */
    @NotEmpty(message = "Upload directory cannot be empty")
    private String directory = "./uploads";

    /**
     * Base URL for file access
     *
     * Used to construct download URLs for uploaded files
     *
     * Examples:
     * - Development: http://localhost:8000
     * - Production: https://api.youthconnect.ug
     */
    private String baseUrl = "http://localhost:8000";

    // =========================================================================
    // HELPER METHODS
    // =========================================================================

    /**
     * Check if a content type is allowed
     *
     * Performs case-insensitive comparison against allowed types list
     *
     * @param contentType MIME type to check (e.g., "application/pdf")
     * @return true if content type is in allowed list, false otherwise
     */
    public boolean isAllowedType(String contentType) {
        if (contentType == null || allowedTypes == null) {
            return false;
        }
        return allowedTypes.stream()
                .anyMatch(type -> type.equalsIgnoreCase(contentType.trim()));
    }

    /**
     * Get max file size in megabytes
     *
     * Converts bytes to MB for human-readable display
     *
     * @return file size in megabytes (e.g., 10.0 for 10MB)
     */
    public double getMaxFileSizeMB() {
        return maxFileSize / (1024.0 * 1024.0);
    }

    /**
     * Check if file size is within limits
     *
     * @param fileSize File size in bytes
     * @return true if file is within allowed size, false otherwise
     */
    public boolean isFileSizeValid(long fileSize) {
        return fileSize > 0 && fileSize <= maxFileSize;
    }

    /**
     * Get formatted allowed types string
     *
     * Useful for error messages
     *
     * @return Comma-separated list of allowed types
     */
    public String getAllowedTypesString() {
        return String.join(", ", allowedTypes);
    }

    // =========================================================================
    // VALIDATION ON STARTUP
    // =========================================================================

    /**
     * Validate configuration on application startup
     *
     * This method runs after all properties are bound.
     * Validates configuration and logs settings for debugging.
     *
     * @throws IllegalStateException if configuration is invalid
     */
    @jakarta.annotation.PostConstruct
    public void validateAndLog() {
        // Validate max file size
        if (maxFileSize <= 0) {
            throw new IllegalStateException("Max file size must be positive. Current value: " + maxFileSize);
        }

        // Validate allowed types
        if (allowedTypes == null || allowedTypes.isEmpty()) {
            throw new IllegalStateException("Allowed types list cannot be empty");
        }

        // Validate directory
        if (directory == null || directory.trim().isEmpty()) {
            throw new IllegalStateException("Upload directory cannot be empty");
        }

        // Log configuration (for debugging and verification)
        System.out.println("═══════════════════════════════════════════════════════════");
        System.out.println("File Upload Configuration Loaded Successfully:");
        System.out.println("  Max File Size: " + getMaxFileSizeMB() + " MB (" + maxFileSize + " bytes)");
        System.out.println("  Allowed Types: " + getAllowedTypesString());
        System.out.println("  Directory: " + directory);
        System.out.println("  Base URL: " + baseUrl);
        System.out.println("═══════════════════════════════════════════════════════════");
    }
}