package com.youthconnect.file.service.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entity representing file records in the database
 * Maps to the file_records table in the production schema
 */
@Entity
@Table(name = "file_records")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "file_id")
    private Long fileId;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "file_name", nullable = false, length = 255)
    private String fileName;

    @Column(name = "original_name", length = 255)
    private String originalName;

    @Column(name = "file_path", nullable = false, length = 500)
    private String filePath;

    @Column(name = "file_size")
    private Long fileSize;

    @Column(name = "content_type", length = 100)
    private String contentType;

    @Column(name = "file_category", nullable = false)
    @Enumerated(EnumType.STRING)
    private FileCategory category;

    @Column(name = "is_public")
    @Builder.Default
    private Boolean isPublic = false;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "upload_time", nullable = false)
    private LocalDateTime uploadTime;

    @Column(name = "last_accessed")
    private LocalDateTime lastAccessed;

    /**
     * File categories supported by the platform
     */
    public enum FileCategory {
        PROFILE_PICTURE,
        DOCUMENT,
        AUDIO_MODULE,
        VIDEO_CONTENT,
        APPLICATION_ATTACHMENT,
        SYSTEM
    }

    /**
     * Lifecycle callback - set upload time before persist
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
     */
    public void markAccessed() {
        this.lastAccessed = LocalDateTime.now();
    }
}