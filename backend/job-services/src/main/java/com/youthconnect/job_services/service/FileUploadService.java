package com.youthconnect.job_services.service;

import com.youthconnect.job_services.dto.response.FileUploadResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

/**
 * ═══════════════════════════════════════════════════════════════════════════
 * FILE UPLOAD SERVICE INTERFACE
 * ═══════════════════════════════════════════════════════════════════════════
 *
 * Handles secure file uploads for job applications with the following features:
 *
 * ✅ FEATURES:
 *   - Resume/CV upload (PDF, DOC, DOCX)
 *   - Cover letter upload
 *   - File validation (size, type, content)
 *   - Secure UUID-based file naming
 *   - User-based access control
 *   - Automatic virus scanning integration (planned)
 *   - Cloud storage support (S3, Azure Blob - planned)
 *
 * 🔒 SECURITY:
 *   - All files stored with UUID names (prevents path traversal)
 *   - User ownership validation
 *   - File type whitelist enforcement
 *   - Size limit enforcement (10MB default)
 *
 * 📁 STORAGE:
 *   - Local filesystem (development)
 *   - AWS S3 (production - planned)
 *
 * @author Douglas Kings Kato
 * @version 3.0.0
 * @since 2025-01-29
 */
public interface FileUploadService {

    /**
     * Upload resume/CV for job application
     *
     * Validates file type (PDF, DOC, DOCX only), checks file size (max 10MB),
     * stores with UUID-based filename for security.
     *
     * @param file Resume file from multipart request
     * @param userId User uploading the file (from JWT token)
     * @return FileUploadResponse with file ID, URL, and metadata
     * @throws IllegalArgumentException if file validation fails
     */
    FileUploadResponse uploadResume(MultipartFile file, UUID userId);

    /**
     * Upload cover letter document (optional)
     *
     * @param file Cover letter file
     * @param userId User uploading the file
     * @return FileUploadResponse with file ID and metadata
     */
    FileUploadResponse uploadCoverLetter(MultipartFile file, UUID userId);

    /**
     * Get file metadata by file ID
     *
     * Validates user has permission to access file.
     *
     * @param fileId UUID of uploaded file
     * @param userId User requesting file details
     * @return FileUploadResponse with file metadata
     * @throws ResourceNotFoundException if file not found
     * @throws ForbiddenException if user doesn't own file
     */
    FileUploadResponse getFileDetails(UUID fileId, UUID userId);

    /**
     * Get download URL for file
     *
     * @param fileId UUID of file
     * @param userId User requesting download
     * @return Download URL (public or pre-signed)
     */
    String getDownloadUrl(UUID fileId, UUID userId);

    /**
     * Delete uploaded file (soft delete)
     *
     * Marks file as deleted but doesn't remove physical file.
     * Physical deletion happens during cleanup job.
     *
     * @param fileId UUID of file to delete
     * @param userId User requesting deletion
     */
    void deleteFile(UUID fileId, UUID userId);

    /**
     * Permanently delete file (hard delete)
     *
     * ⚠️ CAUTION: This permanently removes file from storage
     *
     * @param fileId UUID of file to permanently delete
     */
    void permanentlyDeleteFile(UUID fileId);

    /**
     * Validate file before upload
     *
     * Checks:
     * 1. File not null
     * 2. File not empty
     * 3. File size within limits (max 10MB)
     * 4. File type allowed (PDF, DOC, DOCX)
     * 5. Filename valid
     *
     * @param file Multipart file to validate
     * @throws IllegalArgumentException if validation fails
     */
    void validateFile(MultipartFile file);

    /**
     * Check if file exists and is not deleted
     *
     * @param fileId UUID of file
     * @return true if file exists, false otherwise
     */
    boolean fileExists(UUID fileId);

    /**
     * Check if user owns file
     *
     * @param fileId UUID of file
     * @param userId User to check ownership
     * @return true if user owns file, false otherwise
     */
    boolean isFileOwner(UUID fileId, UUID userId);

    /**
     * Scan file for viruses (async operation)
     *
     * ⚠️ PLANNED: Integration with ClamAV or VirusTotal API
     *
     * @param fileId UUID of file to scan
     */
    void scanFileForVirus(UUID fileId);
}