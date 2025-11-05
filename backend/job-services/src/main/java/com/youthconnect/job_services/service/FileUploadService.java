package com.youthconnect.job_services.service;

import com.youthconnect.job_services.dto.response.FileUploadResponse;
import org.springframework.web.multipart.MultipartFile;

/**
 * File Upload Service Interface
 *
 * Handles file uploads for job applications.
 *
 * @author Douglas Kings Kato
 * @since 1.0.0
 */
public interface FileUploadService {

    /**
     * Upload resume file
     *
     * @param file Resume file (PDF, DOC, DOCX)
     * @param userId User uploading the file
     * @return File upload response with fileId
     */
    FileUploadResponse uploadResume(MultipartFile file, Long userId);

    /**
     * Upload cover letter file
     *
     * @param file Cover letter file
     * @param userId User uploading the file
     * @return File upload response with fileId
     */
    FileUploadResponse uploadCoverLetter(MultipartFile file, Long userId);

    /**
     * Get file details
     *
     * @param fileId File ID
     * @param userId User requesting file
     * @return File metadata
     */
    FileUploadResponse getFileDetails(Long fileId, Long userId);

    /**
     * Delete file
     *
     * @param fileId File ID to delete
     * @param userId User requesting deletion
     */
    void deleteFile(Long fileId, Long userId);
}
