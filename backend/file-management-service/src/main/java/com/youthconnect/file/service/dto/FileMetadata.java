package com.youthconnect.file.service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * Data Transfer Object for file metadata
 *
 * ✅ FIXED: Changed userId from Long to UUID
 *
 * Lightweight metadata response - doesn't include full file record
 * Used for quick metadata queries without database lookup
 *
 * @author Douglas Kings Kato
 * @version 2.0.0 (UUID Fixed)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileMetadata {

    /**
     * Filename (security-safe stored name)
     */
    private String fileName;

    /**
     * Physical file path on server
     */
    private String filePath;

    /**
     * File size in bytes
     */
    private Long fileSize;

    /**
     * MIME content type
     */
    private String contentType;

    /**
     * File type/category as string
     */
    private String fileType;

    /**
     * ✅ FIXED: Changed from Long to UUID
     * Owner's user ID (null for system files)
     */
    private UUID userId;

    /**
     * Upload timestamp (UTC)
     */
    private Instant uploadTime;

    /**
     * Last modification timestamp (UTC)
     */
    private Instant lastModified;
}