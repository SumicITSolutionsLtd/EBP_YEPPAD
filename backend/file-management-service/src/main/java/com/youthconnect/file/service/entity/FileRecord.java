package com.youthconnect.file.service.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity representing file records in the database
 * Maps to the file_records table in the production schema
 *
 * Changed userId from Long to UUID to match auth-service
 * This ensures consistency across all microservices that reference user IDs
 *
 * DESIGN DECISIONS:
 * - fileId: Auto-increment Long for database efficiency
 * - userId: UUID to match centralized auth-service user IDs
 * - Soft delete pattern (isActive flag) for data retention
 * - Audit timestamps (uploadTime, lastAccessed) for analytics
 *
 * @author Douglas Kings Kato
 * @version 2.0.0 (UUID Fixed)
 */
@Entity
@Table(name = "file_records", indexes = {
        @Index(name = "idx_user_category", columnList = "user_id, file_category"),
        @Index(name = "idx_file_name", columnList = "file_name"),
        @Index(name = "idx_upload_time", columnList = "upload_time"),
        @Index(name = "idx_is_active", columnList = "is_active")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileRecord {

    /**
     * Primary key - Auto-increment for performance
     * Database manages ID generation
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "file_id")
    private Long fileId;

    /**
     * ✅ FIXED: Changed from Long to UUID
     *
     * References user ID from auth-service (UUID format)
     * Nullable for system files (e.g., learning modules, public content)
     *
     * Column definition explicitly set to UUID for MySQL compatibility
     */
    @Column(name = "user_id", columnDefinition = "BINARY(16)")
    private UUID userId;

    /**
     * Stored filename (may differ from original for security)
     * Format: {type}_{userId}_{timestamp}.{extension}
     * Example: profile_123e4567-e89b-12d3-a456-426614174000_20250110_143022.jpg
     */
    @Column(name = "file_name", nullable = false, length = 255)
    private String fileName;

    /**
     * Original filename uploaded by user (for display purposes)
     * Preserves user's original filename for UX
     */
    @Column(name = "original_name", length = 255)
    private String originalName;

    /**
     * Physical file path on server/storage
     * Example: /uploads/users/{userId}/profile-pictures/{fileName}
     */
    @Column(name = "file_path", nullable = false, length = 500)
    private String filePath;

    /**
     * File size in bytes
     * Used for storage quota calculations and progress indicators
     */
    @Column(name = "file_size")
    private Long fileSize;

    /**
     * MIME content type
     * Examples: image/jpeg, application/pdf, audio/mpeg
     */
    @Column(name = "content_type", length = 100)
    private String contentType;

    /**
     * File category enum
     * Determines storage location, access rules, and retention policies
     */
    @Column(name = "file_category", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private FileCategory category;

    /**
     * Public access flag
     * - true: Anyone can download (e.g., learning modules, public profiles)
     * - false: Requires authentication and authorization
     */
    @Column(name = "is_public")
    @Builder.Default
    private Boolean isPublic = false;

    /**
     * Soft delete flag
     * - true: File is active and accessible
     * - false: File is logically deleted (can be purged by cleanup job)
     */
    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    /**
     * Upload timestamp (UTC)
     * Set automatically on first persist
     */
    @Column(name = "upload_time", nullable = false)
    private LocalDateTime uploadTime;

    /**
     * Last access timestamp (UTC)
     * Updated when file is downloaded/viewed
     * Used for analytics and cold storage decisions
     */
    @Column(name = "last_accessed")
    private LocalDateTime lastAccessed;

    /**
     * File categories supported by the platform
     *
     * CATEGORY RULES:
     * - PROFILE_PICTURE: Public within platform, optimized versions generated
     * - DOCUMENT: Private, requires owner access, virus scanned
     * - AUDIO_MODULE: Public, multiple language versions, CDN cached
     * - VIDEO_CONTENT: Public/Private based on context, streaming optimized
     * - APPLICATION_ATTACHMENT: Private, reviewers can access, retention policy applies
     * - SYSTEM: Platform files, admin access only
     */
    public enum FileCategory {
        PROFILE_PICTURE,           // User profile pictures (optimized: thumb, medium, original)
        DOCUMENT,                  // CVs, certificates, application documents
        AUDIO_MODULE,              // Multi-language learning audio files
        VIDEO_CONTENT,             // Video lessons and tutorials
        APPLICATION_ATTACHMENT,    // Job/opportunity application files
        SYSTEM                     // System-level files (logos, templates, etc.)
    }

    /**
     * Lifecycle callback - set default values before persist
     *
     * JPA will call this method automatically before INSERT
     * Ensures required fields have valid defaults
     */
    @PrePersist
    public void prePersist() {
        if (uploadTime == null) {
            uploadTime = LocalDateTime.now();
        }
        if (isActive == null) {
            isActive = true;
        }
        if (isPublic == null) {
            isPublic = false;
        }
    }

    /**
     * Update last accessed timestamp
     *
     * Called when file is downloaded, viewed, or accessed
     * Helps track file usage for:
     * - Analytics dashboards
     * - Cold storage decisions (move rarely accessed files to cheaper storage)
     * - Audit trails
     */
    public void markAccessed() {
        this.lastAccessed = LocalDateTime.now();
    }

    /**
     * Soft delete the file record
     * Physical file deletion can happen in background job
     */
    public void softDelete() {
        this.isActive = false;
    }

    /**
     * Check if file belongs to user
     *
     * @param checkUserId UUID to check
     * @return true if file belongs to user
     */
    public boolean belongsToUser(UUID checkUserId) {
        return this.userId != null && this.userId.equals(checkUserId);
    }

    /**
     * Check if file is publicly accessible
     *
     * @return true if file can be accessed without authentication
     */
    public boolean isPubliclyAccessible() {
        return Boolean.TRUE.equals(this.isPublic) && Boolean.TRUE.equals(this.isActive);
    }
}