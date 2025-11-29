package com.youthconnect.job_services.controller;

import com.youthconnect.job_services.common.ApiResponse;
import com.youthconnect.job_services.dto.response.FileUploadResponse;
import com.youthconnect.job_services.service.FileUploadService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

/**
 * ═══════════════════════════════════════════════════════════════════════════
 * FILE UPLOAD CONTROLLER
 * ═══════════════════════════════════════════════════════════════════════════
 *
 * Handles file uploads for job applications with the following endpoints:
 *
 * 📤 UPLOAD ENDPOINTS:
 *   POST /api/v1/files/upload-resume        - Upload resume/CV
 *   POST /api/v1/files/upload-cover-letter  - Upload cover letter
 *
 * 📥 RETRIEVAL ENDPOINTS:
 *   GET  /api/v1/files/{fileId}             - Get file metadata
 *   GET  /api/v1/files/download/{fileId}    - Download file
 *
 * 🗑️ DELETION ENDPOINTS:
 *   DELETE /api/v1/files/{fileId}           - Delete file
 *
 * 🔒 SECURITY:
 *   - All endpoints require authentication (JWT token)
 *   - User ID extracted from JWT via X-User-Id header
 *   - Users can only access/delete their own files
 *
 * 📦 ACCEPTED FILE FORMATS:
 *   - PDF (.pdf)
 *   - Microsoft Word (.doc, .docx)
 *   - Maximum size: 10MB
 *
 * @author Douglas Kings Kato
 * @version 3.0.0
 * @since 2025-01-29
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/files")
@RequiredArgsConstructor
@Tag(
        name = "File Upload",
        description = "Resume and document upload management endpoints"
)
public class FileUploadController {

    private final FileUploadService fileUploadService;

    // ═══════════════════════════════════════════════════════════════════════
    // UPLOAD ENDPOINTS
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Upload resume/CV for job application
     *
     * 📝 REQUEST:
     *   - Method: POST
     *   - Content-Type: multipart/form-data
     *   - Headers: X-User-Id (UUID from JWT token)
     *   - Body: file (resume file)
     *
     * ✅ SUCCESS RESPONSE (201 Created):
     *   {
     *     "success": true,
     *     "message": "Resume uploaded successfully",
     *     "data": {
     *       "fileId": "123e4567-e89b-12d3-a456-426614174000",
     *       "fileName": "john_doe_resume.pdf",
     *       "contentType": "application/pdf",
     *       "fileSize": 1048576,
     *       "category": "RESUME",
     *       "downloadUrl": "http://localhost:8000/api/v1/files/download/123e4567...",
     *       "uploadedAt": "2025-01-29T10:30:00",
     *       "status": "UPLOADED",
     *       "scanResult": "PENDING"
     *     }
     *   }
     *
     * ❌ ERROR RESPONSES:
     *   - 400 Bad Request: Invalid file (wrong type, too large, etc.)
     *   - 401 Unauthorized: Missing or invalid JWT token
     *   - 413 Payload Too Large: File exceeds 10MB
     *   - 500 Internal Server Error: File I/O error
     *
     * @param file Resume file (PDF, DOC, DOCX)
     * @param userIdHeader User ID from JWT token (X-User-Id header)
     * @return FileUploadResponse with file metadata
     */
    @PostMapping(
            value = "/upload-resume",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "Upload resume/CV",
            description = "Upload resume or CV document for job application. " +
                    "Accepts PDF, DOC, DOCX formats up to 10MB. " +
                    "Returns file ID for use in job applications."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "Resume uploaded successfully",
                    content = @Content(schema = @Schema(implementation = FileUploadResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Invalid file (wrong type, too large, empty, etc.)"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - Missing or invalid JWT token"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "413",
                    description = "File size exceeds maximum allowed size (10MB)"
            )
    })
    public ApiResponse<FileUploadResponse> uploadResume(
            @Parameter(
                    description = "Resume file (PDF, DOC, DOCX - max 10MB)",
                    required = true,
                    content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE)
            )
            @RequestParam("file") MultipartFile file,

            @Parameter(
                    description = "User ID from JWT token",
                    required = true,
                    example = "123e4567-e89b-12d3-a456-426614174000"
            )
            @RequestHeader("X-User-Id") String userIdHeader
    ) {
        log.info("Resume upload request - File: {}, User: {}",
                file.getOriginalFilename(), userIdHeader);

        // Convert userId header to UUID
        UUID userId = UUID.fromString(userIdHeader);

        // Upload file
        FileUploadResponse response = fileUploadService.uploadResume(file, userId);

        log.info("Resume uploaded successfully - FileId: {}, User: {}",
                response.getFileId(), userId);

        return ApiResponse.success("Resume uploaded successfully", response);
    }

    /**
     * Upload cover letter document (optional)
     *
     * Same functionality as uploadResume but stores in different category
     *
     * @param file Cover letter file (PDF, DOC, DOCX)
     * @param userIdHeader User ID from JWT token
     * @return FileUploadResponse with file metadata
     */
    @PostMapping(
            value = "/upload-cover-letter",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "Upload cover letter",
            description = "Upload cover letter document. " +
                    "Accepts PDF, DOC, DOCX formats up to 5MB. " +
                    "Optional for job applications."
    )
    public ApiResponse<FileUploadResponse> uploadCoverLetter(
            @Parameter(
                    description = "Cover letter file (PDF, DOC, DOCX)",
                    required = true
            )
            @RequestParam("file") MultipartFile file,

            @Parameter(
                    description = "User ID from JWT token",
                    required = true
            )
            @RequestHeader("X-User-Id") String userIdHeader
    ) {
        log.info("Cover letter upload request - File: {}, User: {}",
                file.getOriginalFilename(), userIdHeader);

        UUID userId = UUID.fromString(userIdHeader);
        FileUploadResponse response = fileUploadService.uploadCoverLetter(file, userId);

        log.info("Cover letter uploaded successfully - FileId: {}", response.getFileId());

        return ApiResponse.success("Cover letter uploaded successfully", response);
    }

    // ═══════════════════════════════════════════════════════════════════════
    // RETRIEVAL ENDPOINTS
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Get file metadata by ID
     *
     * 📝 REQUEST:
     *   - Method: GET
     *   - Path: /api/v1/files/{fileId}
     *   - Headers: X-User-Id
     *
     * ✅ SUCCESS RESPONSE (200 OK):
     *   Returns file metadata (same structure as upload response)
     *
     * ❌ ERROR RESPONSES:
     *   - 403 Forbidden: User doesn't own this file
     *   - 404 Not Found: File doesn't exist or has been deleted
     *
     * @param fileId UUID of uploaded file
     * @param userIdHeader User ID from JWT token
     * @return FileUploadResponse with file metadata
     */
    @GetMapping(
            value = "/{fileId}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @Operation(
            summary = "Get file details",
            description = "Get uploaded file metadata including download URL. " +
                    "User must own the file to access it."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "File details retrieved successfully"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "Forbidden - User doesn't own this file"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "File not found or has been deleted"
            )
    })
    public ApiResponse<FileUploadResponse> getFileDetails(
            @Parameter(
                    description = "File UUID",
                    required = true,
                    example = "123e4567-e89b-12d3-a456-426614174000"
            )
            @PathVariable UUID fileId,

            @Parameter(
                    description = "User ID from JWT token",
                    required = true
            )
            @RequestHeader("X-User-Id") String userIdHeader
    ) {
        log.info("File details request - FileId: {}, User: {}", fileId, userIdHeader);

        UUID userId = UUID.fromString(userIdHeader);
        FileUploadResponse response = fileUploadService.getFileDetails(fileId, userId);

        return ApiResponse.success(response);
    }

    /**
     * Download file
     *
     * ⚠️ TODO: Implement file streaming/download endpoint
     *
     * This endpoint should:
     * 1. Validate user owns file
     * 2. Read file from disk
     * 3. Stream file to client with proper Content-Type
     * 4. Set Content-Disposition header for download
     *
     * @param fileId UUID of file to download
     * @param userIdHeader User ID from JWT token
     */
    @GetMapping("/download/{fileId}")
    @Operation(
            summary = "Download file",
            description = "Download uploaded file. Returns file stream with proper Content-Type."
    )
    public void downloadFile(
            @PathVariable UUID fileId,
            @RequestHeader("X-User-Id") String userIdHeader
    ) {
        // TODO: Implement file download
        log.warn("File download endpoint not yet implemented - FileId: {}", fileId);
        throw new UnsupportedOperationException("File download not yet implemented");
    }

    // ═══════════════════════════════════════════════════════════════════════
    // DELETION ENDPOINTS
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Delete uploaded file (soft delete)
     *
     * 📝 REQUEST:
     *   - Method: DELETE
     *   - Path: /api/v1/files/{fileId}
     *   - Headers: X-User-Id
     *
     * ✅ SUCCESS RESPONSE (204 No Content):
     *   File marked as deleted successfully
     *
     * ❌ ERROR RESPONSES:
     *   - 403 Forbidden: User doesn't own this file
     *   - 404 Not Found: File doesn't exist
     *
     * @param fileId UUID of file to delete
     * @param userIdHeader User ID from JWT token
     */
    @DeleteMapping("/{fileId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(
            summary = "Delete file",
            description = "Soft delete uploaded file. File is marked as deleted but not immediately removed from storage."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "204",
                    description = "File deleted successfully"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "Forbidden - User doesn't own this file"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "File not found"
            )
    })
    public void deleteFile(
            @Parameter(
                    description = "File UUID to delete",
                    required = true
            )
            @PathVariable UUID fileId,

            @Parameter(
                    description = "User ID from JWT token",
                    required = true
            )
            @RequestHeader("X-User-Id") String userIdHeader
    ) {
        log.info("File deletion request - FileId: {}, User: {}", fileId, userIdHeader);

        UUID userId = UUID.fromString(userIdHeader);
        fileUploadService.deleteFile(fileId, userId);

        log.info("File deleted successfully - FileId: {}", fileId);
    }
}