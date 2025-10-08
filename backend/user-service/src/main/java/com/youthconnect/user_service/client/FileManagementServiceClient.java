package com.youthconnect.user_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Client for integrating with File Management Service
 * Handles file uploads and management
 */
@FeignClient(name = "file-management-service", fallback = FileManagementServiceFallback.class)
public interface FileManagementServiceClient {

    @PostMapping("/api/files/profile-picture/{userId}")
    CompletableFuture<ResponseEntity<Map<String, Object>>> uploadProfilePicture(
            @PathVariable Long userId,
            @RequestParam("file") MultipartFile file);

    @PostMapping("/api/files/document/{userId}")
    CompletableFuture<ResponseEntity<Map<String, Object>>> uploadDocument(
            @PathVariable Long userId,
            @RequestParam("documentType") String documentType,
            @RequestParam("file") MultipartFile file);

    @DeleteMapping("/api/files/{userId}/{fileName}")
    CompletableFuture<ResponseEntity<Map<String, Object>>> deleteFile(
            @PathVariable Long userId,
            @PathVariable String fileName,
            @RequestParam String category);
}