package com.youthconnect.file.service.dto;

import com.youthconnect.file.service.entity.FileRecord;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * File Record DTO
 *
 * Data Transfer Object for file records
 * ✅ FIXED: Changed userId from Long to UUID
 *
 * Used instead of returning entities directly from controllers
 * Follows best practice: Never expose JPA entities in REST APIs
 *
 * BENEFITS:
 * - Decouples API from database schema
 * - Allows different representations (e.g., with/without sensitive data)
 * - Easier API versioning
 * - Prevents lazy-loading issues
 *
 * @author Douglas Kings Kato
 * @version 2.0.0 (UUID Fixed)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileRecordDto {

    /**
     * File's database ID (internal)
     */
    private Long fileId;

    /**
     * ✅ FIXED: Changed from Long to UUID
     * Owner's user ID from auth-service
     */
    private UUID userId;

    /**
     * Stored filename (security-safe)
     */
    private String fileName;

    /**
     * Original filename (user-uploaded)
     */
    private String originalName;

    /**
     * Public URL for file access
     * Format: {baseUrl}/api/files/download/{category}/{fileName}?userId={userId}
     */
    private String fileUrl;

    /**
     * File size in bytes
     */
    private Long fileSize;

    /**
     * MIME content type
     */
    private String contentType;

    /**
     * File category (enum as string)
     */
    private String category;

    /**
     * Public access flag
     */
    private Boolean isPublic;

    /**
     * Upload timestamp
     */
    private LocalDateTime uploadTime;

    /**
     * Last access timestamp (can be null if never accessed)
     */
    private LocalDateTime lastAccessed;

    /**
     * Convert entity to DTO
     *
     * Factory method pattern for clean entity-to-DTO conversion
     *
     * @param entity FileRecord entity from database
     * @param baseUrl Base URL for building file access URLs
     * @return FileRecordDto with public URL
     */
    public static FileRecordDto fromEntity(FileRecord entity, String baseUrl) {
        return FileRecordDto.builder()
                .fileId(entity.getFileId())
                .userId(entity.getUserId())  // ✅ Now UUID, no conversion needed
                .fileName(entity.getFileName())
                .originalName(entity.getOriginalName())
                .fileUrl(buildFileUrl(entity, baseUrl))
                .fileSize(entity.getFileSize())
                .contentType(entity.getContentType())
                .category(entity.getCategory().name())
                .isPublic(entity.getIsPublic())
                .uploadTime(entity.getUploadTime())
                .lastAccessed(entity.getLastAccessed())
                .build();
    }

    /**
     * Build file URL based on category and access rules
     *
     * URL PATTERNS:
     * - User files: {baseUrl}/api/files/download/users/{userId}/{category}/{fileName}
     * - Public files: {baseUrl}/api/files/download/{category}/{fileName}
     * - Modules: {baseUrl}/api/files/download/modules/{moduleKey}/{fileName}
     *
     * @param entity FileRecord entity
     * @param baseUrl Base URL (e.g., http://localhost:8089)
     * @return Complete file access URL
     */
    private static String buildFileUrl(FileRecord entity, String baseUrl) {
        String category = entity.getCategory().name().toLowerCase().replace("_", "-");

        // User-specific files include userId in path
        if (entity.getUserId() != null) {
            return String.format("%s/api/files/download/users/%s/%s/%s",
                    baseUrl,
                    entity.getUserId().toString(),  // ✅ UUID.toString()
                    category,
                    entity.getFileName());
        }
        // Public/system files don't include userId
        else {
            return String.format("%s/api/files/download/%s/%s",
                    baseUrl,
                    category,
                    entity.getFileName());
        }
    }

    /**
     * Get human-readable file size
     *
     * @return Formatted file size (e.g., "2.5 MB", "150 KB")
     */
    public String getFormattedFileSize() {
        if (fileSize == null) return "Unknown";

        if (fileSize < 1024) return fileSize + " B";
        if (fileSize < 1024 * 1024) return String.format("%.1f KB", fileSize / 1024.0);
        if (fileSize < 1024 * 1024 * 1024) return String.format("%.1f MB", fileSize / (1024.0 * 1024.0));
        return String.format("%.1f GB", fileSize / (1024.0 * 1024.0 * 1024.0));
    }

    /**
     * Check if file has been accessed since upload
     *
     * @return true if file has been accessed at least once
     */
    public boolean hasBeenAccessed() {
        return lastAccessed != null;
    }
}