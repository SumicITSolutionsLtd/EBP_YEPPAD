package com.youthconnect.file.service.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entity for file management - maps to file_records table
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

    @Column(name = "file_name", nullable = false)
    private String fileName;

    @Column(name = "original_name")
    private String originalName;

    @Column(name = "file_path", nullable = false)
    private String filePath;

    @Column(name = "file_size")
    private Long fileSize;

    @Column(name = "content_type")
    private String contentType;

    @Column(name = "file_category", nullable = false)
    @Enumerated(EnumType.STRING)
    private FileCategory category;

    @Column(name = "is_public")
    private Boolean isPublic = false;

    @Column(name = "access_token")
    private String accessToken;

    @Column(name = "upload_time", nullable = false)
    private LocalDateTime uploadTime;

    @Column(name = "last_accessed")
    private LocalDateTime lastAccessed;

    @Column(name = "is_active")
    private Boolean isActive = true;

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

    public enum FileCategory {
        PROFILE_PICTURE, DOCUMENT, AUDIO_MODULE, VIDEO_CONTENT, APPLICATION_ATTACHMENT, SYSTEM
    }

    public void markAccessed() {
        this.lastAccessed = LocalDateTime.now();
    }
}
