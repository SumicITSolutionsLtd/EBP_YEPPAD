package com.youthconnect.file.service.config;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

/**
 * File storage configuration properties
 * Binds to app.file.storage.* properties in application.yml
 */
@Data
@Component
@Validated
@ConfigurationProperties(prefix = "app.file.storage")
public class FileStorageProperties {

    /**
     * Storage type: LOCAL, S3, MINIO
     */
    @NotBlank(message = "Storage type is required")
    private String type = "LOCAL";

    /**
     * Local filesystem path for file storage
     */
    @NotBlank(message = "Storage path is required")
    private String path = "uploads/";

    /**
     * Base URL for file access
     */
    @NotBlank(message = "Base URL is required")
    private String baseUrl = "http://localhost:8089";

    /**
     * Maximum file size in bytes (50MB default)
     */
    private long maxFileSize = 52428800L;

    /**
     * Maximum request size in bytes (100MB default)
     */
    private long maxRequestSize = 104857600L;

    /**
     * Get storage path with trailing slash
     */
    public String getStoragePath() {
        return path.endsWith("/") ? path : path + "/";
    }
}