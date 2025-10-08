package com.youthconnect.file.service.controller;

import com.youthconnect.file_service.dto.FileUploadResult;
import com.youthconnect.file_service.service.FileManagementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Slf4j
@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileManagementController {

    private final FileManagementService fileService;

    /**
     * Upload profile picture for a user
     */
    @PostMapping("/profile-picture/{userId}")
    public CompletableFuture<ResponseEntity<Map<String, Object>>> uploadProfilePicture(
            @PathVariable Long userId,
            @RequestParam("file") MultipartFile file) {

        log.info("Profile picture upload request for user: {}", userId);

        return fileService.uploadProfilePicture(userId, file)
                .thenApply(result -> {
                    if (result.isSuccess()) {
                        return ResponseEntity.ok(Map.of(
                                "success", true,
                                "message", "Profile picture uploaded successfully",
                                "fileName", result.getFileName(),
                                "fileUrl", result.getFileUrl(),
                                "optimizedVersions", result.getOptimizedVersions()
                        ));
                    } else {
                        return ResponseEntity.badRequest().body(Map.of(
                                "success", false,
                                "error", "Failed to upload profile picture",
                                "message", result.getErrorMessage()
                        ));
                    }
                });
    }

    /**
     * Upload audio file for learning modules
     */
    @PostMapping("/audio-module")
    public CompletableFuture<ResponseEntity<Map<String, Object>>> uploadAudioModule(
            @RequestParam("moduleKey") String moduleKey,
            @RequestParam("language") String language,
            @RequestParam("file") MultipartFile file) {

        log.info("Audio module upload request - Module: {}, Language: {}", moduleKey, language);

        return fileService.uploadAudioModule(moduleKey, language, file)
                .thenApply(result -> {
                    if (result.isSuccess()) {
                        return ResponseEntity.ok(Map.of(
                                "success", true,
                                "message", "Audio module uploaded successfully",
                                "fileName", result.getFileName(),
                                "fileUrl", result.getFileUrl(),
                                "processedVersions", result.getProcessedVersions()
                        ));
                    } else {
                        return ResponseEntity.badRequest().body(Map.of(
                                "success", false,
                                "error", "Failed to upload audio module",
                                "message", result.getErrorMessage()
                        ));
                    }
                });
    }

    /**
     * Upload document for user (CV, certificates, etc.)
     */
    @PostMapping("/document/{userId}")
    public CompletableFuture<ResponseEntity<Map<String, Object>>> uploadDocument(
            @PathVariable Long userId,
            @RequestParam("documentType") String documentType,
            @RequestParam("file") MultipartFile file) {

        log.info("Document upload request - User: {}, Type: {}", userId, documentType);

        return fileService.uploadUserDocument(userId, documentType, file)
                .thenApply(result -> {
                    if (result.isSuccess()) {
                        return ResponseEntity.ok(Map.of(
                                "success", true,
                                "message", "Document uploaded successfully",
                                "fileName", result.getFileName(),
                                "fileUrl", result.getFileUrl(),
                                "documentType", result.getDocumentType()
                        ));
                    } else {
                        return ResponseEntity.badRequest().body(Map.of(
                                "success", false,
                                "error", "Failed to upload document",
                                "message", result.getErrorMessage()
                        ));
                    }
                });
    }

    /**
     * Get audio files for a learning module in all languages
     */
    @GetMapping("/audio-module/{moduleKey}")
    public ResponseEntity<Map<String, Object>> getModuleAudioFiles(@PathVariable String moduleKey) {
        log.info("Retrieving audio files for module: {}", moduleKey);

        Map<String, String> audioFiles = fileService.getModuleAudioFiles(moduleKey);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "moduleKey", moduleKey,
                "audioFiles", audioFiles,
                "count", audioFiles.size()
        ));
    }

    /**
     * Download/serve file
     */
    @GetMapping("/download/{category}/{fileName}")
    public ResponseEntity<Resource> downloadFile(
            @PathVariable String category,
            @PathVariable String fileName,
            @RequestParam(required = false) Long userId) {

        try {
            Resource file = fileService.loadFileAsResource(category, fileName, userId);
            String contentType = fileService.getContentType(fileName);

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                    .body(file);

        } catch (Exception e) {
            log.error("Error downloading file {}/{}: {}", category, fileName, e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Delete user file
     */
    @DeleteMapping("/{userId}/{fileName}")
    public CompletableFuture<ResponseEntity<Map<String, Object>>> deleteFile(
            @PathVariable Long userId,
            @PathVariable String fileName,
            @RequestParam String category) {

        log.info("Delete file request - User: {}, File: {}, Category: {}", userId, fileName, category);

        return fileService.deleteUserFile(userId, fileName, category)
                .thenApply(success -> {
                    if (success) {
                        return ResponseEntity.ok(Map.of(
                                "success", true,
                                "message", "File deleted successfully",
                                "fileName", fileName
                        ));
                    } else {
                        return ResponseEntity.badRequest().body(Map.of(
                                "success", false,
                                "error", "Failed to delete file",
                                "fileName", fileName
                        ));
                    }
                });
    }

    /**
     * Get file metadata
     */
    @GetMapping("/metadata/{category}/{fileName}")
    public ResponseEntity<Map<String, Object>> getFileMetadata(
            @PathVariable String category,
            @PathVariable String fileName,
            @RequestParam(required = false) Long userId) {

        try {
            var metadata = fileService.getFileMetadata(category, fileName, userId);

            if (metadata != null) {
                return ResponseEntity.ok(Map.of(
                        "success", true,
                        "metadata", metadata
                ));
            } else {
                return ResponseEntity.notFound().build();
            }

        } catch (Exception e) {
            log.error("Error getting file metadata: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", "Failed to get file metadata"
            ));
        }
    }

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        boolean storageHealthy = fileService.checkStorageHealth();

        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "file-management-service",
                "checks", Map.of(
                        "storage", storageHealthy ? "UP" : "DOWN"
                )
        ));
    }
}
