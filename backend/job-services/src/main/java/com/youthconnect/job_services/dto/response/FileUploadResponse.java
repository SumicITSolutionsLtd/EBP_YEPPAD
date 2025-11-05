package com.youthconnect.job_services.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * File Upload Response DTO
 *
 * Response for file upload operations.
 * Contains file metadata and access information.
 *
 * @author Douglas Kings Kato
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileUploadResponse {

    /**
     * Unique file identifier
     */
    private UUID fileId;

    /**
     * Original filename
     */
    private String fileName;

    /**
     * File MIME type
     */
    private String contentType;

    /**
     * File size in bytes
     */
    private Long fileSize;

    /**
     * File category (RESUME, COVER_LETTER, etc.)
     */
    private String category;

    /**
     * User who uploaded the file
     */
    private UUID userId;

    /**
     * File storage URL (for internal use)
     */
    private String fileUrl;

    /**
     * Public download URL (temporary, pre-signed)
     */
    private String downloadUrl;

    /**
     * Upload timestamp
     */
    private LocalDateTime uploadedAt;

    /**
     * File status (UPLOADED, PROCESSING, READY, FAILED)
     */
    private String status;

    /**
     * Virus scan result (CLEAN, INFECTED, PENDING)
     */
    private String scanResult;
}