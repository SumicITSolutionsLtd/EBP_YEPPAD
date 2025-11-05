package com.youthconnect.job_services.controller;

import com.youthconnect.job_services.common.ApiResponse;
import com.youthconnect.job_services.dto.response.FileUploadResponse;
import com.youthconnect.job_services.service.FileUploadService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * File Upload Controller
 *
 * Handles resume/CV uploads for job applications.
 * Integrates with file-management-service for secure storage.
 *
 * @author Douglas Kings Kato
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/v1/files")
@RequiredArgsConstructor
@Tag(name = "File Upload", description = "Resume and document upload endpoints")
public class FileUploadController {

    private final FileUploadService fileUploadService;

    /**
     * Upload resume/CV for job application
     *
     * Allowed formats: PDF, DOC, DOCX
     * Max file size: 10MB
     *
     * @param file Resume file to upload
     * @param userId User uploading the file
     * @return File upload response with fileId
     */
    @PostMapping(value = "/upload-resume", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "Upload resume",
            description = "Upload resume/CV for job application. Supports PDF, DOC, DOCX (max 10MB)"
    )
    public ApiResponse<FileUploadResponse> uploadResume(
            @RequestParam("file") MultipartFile file,
            @RequestHeader("X-User-Id") Long userId
    ) {
        FileUploadResponse response = fileUploadService.uploadResume(file, userId);
        return ApiResponse.success("Resume uploaded successfully", response);
    }

    /**
     * Upload cover letter document (optional)
     *
     * @param file Cover letter file
     * @param userId User uploading the file
     * @return File upload response with fileId
     */
    @PostMapping(value = "/upload-cover-letter", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "Upload cover letter",
            description = "Upload cover letter document. Supports PDF, DOC, DOCX (max 5MB)"
    )
    public ApiResponse<FileUploadResponse> uploadCoverLetter(
            @RequestParam("file") MultipartFile file,
            @RequestHeader("X-User-Id") Long userId
    ) {
        FileUploadResponse response = fileUploadService.uploadCoverLetter(file, userId);
        return ApiResponse.success("Cover letter uploaded successfully", response);
    }

    /**
     * Get file details by ID
     *
     * @param fileId File ID
     * @param userId User requesting file details
     * @return File metadata
     */
    @GetMapping("/{fileId}")
    @Operation(summary = "Get file details", description = "Get uploaded file metadata")
    public ApiResponse<FileUploadResponse> getFileDetails(
            @PathVariable Long fileId,
            @RequestHeader("X-User-Id") Long userId
    ) {
        FileUploadResponse response = fileUploadService.getFileDetails(fileId, userId);
        return ApiResponse.success(response);
    }

    /**
     * Delete uploaded file
     *
     * @param fileId File ID to delete
     * @param userId User requesting deletion
     */
    @DeleteMapping("/{fileId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete file", description = "Delete uploaded resume or cover letter")
    public void deleteFile(
            @PathVariable Long fileId,
            @RequestHeader("X-User-Id") Long userId
    ) {
        fileUploadService.deleteFile(fileId, userId);
    }
}