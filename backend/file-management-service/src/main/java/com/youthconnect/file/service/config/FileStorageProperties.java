package com.youthconnect.file.service.config;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import jakarta.validation.constraints.NotBlank;

/**
 * File storage configuration properties
 */
@Data
@Component
@ConfigurationProperties(prefix = "app.file.storage")
public class FileStorageProperties {

    @NotBlank(message = "Storage path is required")
    private String path = "uploads/";

    @NotBlank(message = "Base URL is required")
    private String baseUrl = "http://localhost:8089";

    private String type = "LOCAL"; // LOCAL, S3, MINIO

    // File size limits
    private long maxFileSize = 52428800; // 50MB
    private long maxRequestSize = 104857600; // 100MB

    // Storage path getter with validation
    public String getStoragePath() {
        return path.endsWith("/") ? path : path + "/";
    }
}
