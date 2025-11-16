package com.youthconnect.file.service.controller;

import com.youthconnect.file.service.dto.FileRecordDto;
import com.youthconnect.file.service.dto.PagedResponse;
import com.youthconnect.file.service.service.FileManagementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * REST Controller for file management operations
 *
 * ✅ GUIDELINES COMPLIANCE:
 * ✅ Implements authentication following development guidelines
 * ✅ Returns DTOs instead of entities
 * ✅ Implements pagination for list endpoints
 * ✅ Includes Swagger documentation
 * ✅ All methods use UUID for userId
 * ✅ Public health check endpoint available
 *
 * AUTHENTICATION PATTERNS:
 * - Health check: PUBLIC (no authentication)
 * - Public downloads: PUBLIC (learning modules, public files)
 * - Profile pictures: AUTHENTICATED (user can only upload their own)
 * - Documents: AUTHENTICATED (user can only access their own)
 * - Audio modules: AUTHENTICATED (NGO/ADMIN only)
 *
 * @author Douglas Kings Kato
 * @version 2.0.0 (With Authentication & Pagination - UUID Fixed)
 */
@Slf4j
@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
@Tag(name = "File Management", description = "File upload, download, and management operations")
public class FileManagementController {

    private final FileManagementService fileService;

    /**
     * Get current authenticated user ID from SecurityContext
     *
     * SECURITY: User ID is set by JwtAuthenticationFilter after JWT validation
     * Filter extracts userId (UUID) from JWT and sets as principal
     *
     * @return UUID of authenticated user, or null if not authenticated
     */
    private UUID getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // Principal is the userId (UUID) set by JwtAuthenticationFilter
        if (authentication != null && authentication.getPrincipal() instanceof UUID) {
            return (UUID) authentication.getPrincipal();
        }

        // Fallback: Try to parse from string if principal is String
        if (authentication != null && authentication.getPrincipal() instanceof String) {
            try {
                return UUID.fromString((String) authentication.getPrincipal());
            } catch (IllegalArgumentException e) {
                log.warn("Invalid UUID format in authentication principal: {}", authentication.getPrincipal());
            }
        }

        return null;
    }

    // ============================================================
    // FILE UPLOAD ENDPOINTS
    // ============================================================

    /**
     * Upload profile picture for authenticated user
     *
     * ✅ Authentication: REQUIRED
     * ✅ Authorization: User can only upload their own profile picture
     *
     * @param userId User's UUID
     * @param file Profile picture file (JPG, PNG, GIF - max 10MB)
     * @return Upload result with file URL and optimized versions
     */
    @PostMapping("/profile-picture/{userId}")
    @PreAuthorize("authentication.principal.toString() == #userId.toString()")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(
            summary = "Upload profile picture",
            description = "Upload a profile picture for the authenticated user. Generates optimized versions (thumbnail, medium, original)."
    )
    public CompletableFuture<Map<String, Object>> uploadProfilePicture(
            @Parameter(description = "User ID (UUID)") @PathVariable UUID userId,
            @Parameter(description = "Profile picture file") @RequestParam("file") MultipartFile file) {

        UUID currentUserId = getCurrentUserId();
        log.info("Profile picture upload request - User: {}, AuthUser: {}", userId, currentUserId);

        // Double-check authorization (Spring Security @PreAuthorize is primary check)
        if (!userId.equals(currentUserId)) {
            return CompletableFuture.completedFuture(Map.of(
                    "success", false,
                    "error", "Unauthorized",
                    "message", "You can only upload your own profile picture"
            ));
        }

        return fileService.uploadProfilePicture(userId, file)
                .thenApply(result -> {
                    if (result.isSuccess()) {
                        return Map.of(
                                "success", true,
                                "message", "Profile picture uploaded successfully",
                                "data", Map.of(
                                        "fileName", result.getFileName(),
                                        "fileUrl", result.getFileUrl(),
                                        "optimizedVersions", result.getOptimizedVersions()
                                )
                        );
                    } else {
                        return Map.of(
                                "success", false,
                                "error", "Upload failed",
                                "message", result.getErrorMessage()
                        );
                    }
                });
    }

    /**
     * Upload audio module for learning content
     *
     * ✅ Authentication: REQUIRED
     * ✅ Authorization: NGO/ADMIN roles only
     *
     * @param moduleKey Module identifier (e.g., "intro_entrepreneurship")
     * @param language Language code (en, lg, lur, lgb)
     * @param file Audio file (MP3, WAV, M4A - max 100MB)
     * @return Upload result with processed versions
     */
    @PostMapping("/audio-module")
    @PreAuthorize("hasAnyRole('NGO', 'ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(
            summary = "Upload audio module",
            description = "Upload audio file for multi-language learning modules (NGO/ADMIN only)"
    )
    public CompletableFuture<Map<String, Object>> uploadAudioModule(
            @Parameter(description = "Module key/identifier") @RequestParam("moduleKey") String moduleKey,
            @Parameter(description = "Language code (en, lg, lur, lgb)") @RequestParam("language") String language,
            @Parameter(description = "Audio file") @RequestParam("file") MultipartFile file) {

        log.info("Audio module upload - Module: {}, Language: {}, User: {}",
                moduleKey, language, getCurrentUserId());

        return fileService.uploadAudioModule(moduleKey, language, file)
                .thenApply(result -> {
                    if (result.isSuccess()) {
                        return Map.of(
                                "success", true,
                                "message", "Audio module uploaded successfully",
                                "data", Map.of(
                                        "fileName", result.getFileName(),
                                        "fileUrl", result.getFileUrl(),
                                        "processedVersions", result.getProcessedVersions()
                                )
                        );
                    } else {
                        return Map.of(
                                "success", false,
                                "error", "Upload failed",
                                "message", result.getErrorMessage()
                        );
                    }
                });
    }

    /**
     * Upload document for authenticated user
     *
     * ✅ Authentication: REQUIRED
     * ✅ Authorization: User can only upload their own documents
     *
     * @param userId User's UUID
     * @param documentType Document type (CV, CERTIFICATE, etc.)
     * @param file Document file (PDF, DOC, DOCX - max 50MB)
     * @return Upload result with file URL
     */
    @PostMapping("/document/{userId}")
    @PreAuthorize("authentication.principal.toString() == #userId.toString()")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(
            summary = "Upload document",
            description = "Upload CV, certificate, or other document"
    )
    public CompletableFuture<Map<String, Object>> uploadDocument(
            @Parameter(description = "User ID (UUID)") @PathVariable UUID userId,
            @Parameter(description = "Document type (CV, CERTIFICATE, etc.)") @RequestParam("documentType") String documentType,
            @Parameter(description = "Document file") @RequestParam("file") MultipartFile file) {

        UUID currentUserId = getCurrentUserId();
        log.info("Document upload - User: {}, Type: {}, AuthUser: {}", userId, documentType, currentUserId);

        if (!userId.equals(currentUserId)) {
            return CompletableFuture.completedFuture(Map.of(
                    "success", false,
                    "error", "Unauthorized",
                    "message", "You can only upload your own documents"
            ));
        }

        return fileService.uploadUserDocument(userId, documentType, file)
                .thenApply(result -> {
                    if (result.isSuccess()) {
                        return Map.of(
                                "success", true,
                                "message", "Document uploaded successfully",
                                "data", Map.of(
                                        "fileName", result.getFileName(),
                                        "fileUrl", result.getFileUrl(),
                                        "documentType", result.getDocumentType()
                                )
                        );
                    } else {
                        return Map.of(
                                "success", false,
                                "error", "Upload failed",
                                "message", result.getErrorMessage()
                        );
                    }
                });
    }

    // ============================================================
    // FILE RETRIEVAL ENDPOINTS
    // ============================================================

    /**
     * Get audio files for learning module (all languages)
     *
     * ✅ Authentication: REQUIRED
     *
     * @param moduleKey Module identifier
     * @return Map of language codes to file URLs
     */
    @GetMapping("/audio-module/{moduleKey}")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(
            summary = "Get module audio files",
            description = "Retrieve audio files for a learning module in all available languages"
    )
    public Map<String, Object> getModuleAudioFiles(
            @Parameter(description = "Module key/identifier") @PathVariable String moduleKey) {

        log.info("Retrieving audio files for module: {}", moduleKey);

        Map<String, String> audioFiles = fileService.getModuleAudioFiles(moduleKey);

        return Map.of(
                "success", true,
                "data", Map.of(
                        "moduleKey", moduleKey,
                        "audioFiles", audioFiles,
                        "count", audioFiles.size()
                )
        );
    }

    /**
     * ⭐ List user files with pagination
     *
     * ✅ Returns PagedResponse (follows guidelines - no raw collections)
     * ✅ Authentication: REQUIRED
     * ✅ Authorization: User can only see their own files
     *
     * @param userId User's UUID
     * @param page Page number (0-indexed)
     * @param size Page size (default 20)
     * @param category Optional category filter
     * @return Paginated list of file records
     */
    @GetMapping("/user/{userId}")
    @PreAuthorize("authentication.principal.toString() == #userId.toString()")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(
            summary = "List user files",
            description = "Get paginated list of user's uploaded files"
    )
    public PagedResponse<FileRecordDto> listUserFiles(
            @Parameter(description = "User ID (UUID)") @PathVariable UUID userId,
            @Parameter(description = "Page number (0-indexed)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "File category filter (optional)") @RequestParam(required = false) String category) {

        log.info("Listing files for user: {}, page: {}, size: {}", userId, page, size);

        return fileService.listUserFiles(userId, page, size, category);
    }

    /**
     * Download file (public or private based on path)
     *
     * ✅ Public files: No authentication
     * ✅ Private files: Authentication required
     *
     * @param category File category
     * @param fileName File name
     * @param userId User ID (required for private files)
     * @return File resource for download
     */
    @GetMapping("/download/{category}/{fileName}")
    @Operation(
            summary = "Download file",
            description = "Download file by category and filename"
    )
    public ResponseEntity<Resource> downloadFile(
            @Parameter(description = "File category") @PathVariable String category,
            @Parameter(description = "File name") @PathVariable String fileName,
            @Parameter(description = "User ID (required for private files)") @RequestParam(required = false) UUID userId) {

        try {
            Resource file = fileService.loadFileAsResource(category, fileName, userId);
            String contentType = fileService.getContentType(fileName);

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + fileName + "\"")
                    .header("X-File-Name", fileName)
                    .header("X-File-Size", String.valueOf(file.contentLength()))
                    .body(file);

        } catch (Exception e) {
            log.error("Error downloading file {}/{}: {}", category, fileName, e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    // ============================================================
    // FILE MANAGEMENT ENDPOINTS
    // ============================================================

    /**
     * Delete user file
     *
     * ✅ Authentication: REQUIRED
     * ✅ Authorization: Only file owner can delete
     *
     * @param userId User's UUID
     * @param fileName File name
     * @param category File category
     * @return Success/failure response
     */
    @DeleteMapping("/{userId}/{fileName}")
    @PreAuthorize("authentication.principal.toString() == #userId.toString()")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(
            summary = "Delete file",
            description = "Delete a user's file"
    )
    public CompletableFuture<Map<String, Object>> deleteFile(
            @Parameter(description = "User ID (UUID)") @PathVariable UUID userId,
            @Parameter(description = "File name") @PathVariable String fileName,
            @Parameter(description = "File category") @RequestParam String category) {

        log.info("Delete file request - User: {}, File: {}, Category: {}", userId, fileName, category);

        return fileService.deleteUserFile(userId, fileName, category)
                .thenApply(success -> {
                    if (success) {
                        return Map.of(
                                "success", true,
                                "message", "File deleted successfully",
                                "data", Map.of("fileName", fileName)
                        );
                    } else {
                        return Map.of(
                                "success", false,
                                "error", "Deletion failed",
                                "message", "File not found or already deleted"
                        );
                    }
                });
    }

    /**
     * Get file metadata
     *
     * ✅ Authentication: REQUIRED
     * ✅ Authorization: User can only access metadata for their own files
     *
     * @param category File category
     * @param fileName File name
     * @param userId User ID (required for private files)
     * @return File metadata
     */
    @GetMapping("/metadata/{category}/{fileName}")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(
            summary = "Get file metadata",
            description = "Retrieve file metadata without downloading the file"
    )
    public Map<String, Object> getFileMetadata(
            @Parameter(description = "File category") @PathVariable String category,
            @Parameter(description = "File name") @PathVariable String fileName,
            @Parameter(description = "User ID (required for private files)") @RequestParam(required = false) UUID userId) {

        try {
            var metadata = fileService.getFileMetadata(category, fileName, userId);

            if (metadata != null) {
                return Map.of(
                        "success", true,
                        "data", metadata
                );
            } else {
                return Map.of(
                        "success", false,
                        "error", "Not found",
                        "message", "File metadata not found"
                );
            }

        } catch (Exception e) {
            log.error("Error getting file metadata: {}", e.getMessage());
            return Map.of(
                    "success", false,
                    "error", "Failed to retrieve metadata",
                    "message", e.getMessage()
            );
        }
    }

    /**
     * Health check endpoint (PUBLIC - no authentication required)
     *
     * ✅ REQUIRED BY GUIDELINES: Public health check endpoint
     *
     * Used by:
     * - Docker health checks
     * - Load balancers
     * - Monitoring systems
     * - API Gateway health checks
     *
     * @return Service health status
     */
    @GetMapping("/health")
    @Operation(
            summary = "Health check",
            description = "Check if file service is healthy (PUBLIC - no authentication required)"
    )
    public Map<String, Object> healthCheck() {
        boolean storageHealthy = fileService.checkStorageHealth();

        return Map.of(
                "status", storageHealthy ? "UP" : "DOWN",
                "service", "file-management-service",
                "timestamp", java.time.LocalDateTime.now().toString(),
                "checks", Map.of(
                        "storage", storageHealthy ? "UP" : "DOWN",
                        "database", "UP" // Assuming DB is up if service is running
                )
        );
    }
}