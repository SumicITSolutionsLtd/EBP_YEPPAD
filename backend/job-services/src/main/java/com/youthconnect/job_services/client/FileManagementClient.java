package com.youthconnect.job_services.client;

import com.youthconnect.job_services.dto.response.FileUploadResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * File Management Service Client
 *
 * Feign client for communication with file-management-service.
 * Handles file uploads, retrieval, and deletion.
 *
 * @author Douglas Kings Kato
 * @since 1.0.0
 */
@FeignClient(name = "file-management-service", path = "/api/v1/files")
public interface FileManagementClient {

    /**
     * Upload file to storage
     *
     * @param file File to upload
     * @param userId User uploading the file
     * @param category File category (RESUME, COVER_LETTER, etc.)
     * @return File upload response with fileId
     */
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    FileUploadResponse uploadFile(
            @RequestPart("file") MultipartFile file,
            @RequestParam("userId") Long userId,
            @RequestParam("category") String category
    );

    /**
     * Get file details
     *
     * @param fileId File ID
     * @return File metadata
     */
    @GetMapping("/{fileId}")
    FileUploadResponse getFileDetails(@PathVariable("fileId") Long fileId);

    /**
     * Delete file
     *
     * @param fileId File ID to delete
     * @param userId User requesting deletion
     */
    @DeleteMapping("/{fileId}")
    void deleteFile(
            @PathVariable("fileId") Long fileId,
            @RequestParam("userId") Long userId
    );

    /**
     * Get file download URL
     *
     * @param fileId File ID
     * @return Temporary download URL (pre-signed)
     */
    @GetMapping("/{fileId}/download-url")
    String getDownloadUrl(@PathVariable("fileId") Long fileId);
}