package com.youthconnect.user_service.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
public class FileManagementServiceFallback implements FileManagementServiceClient {

    @Override
    public CompletableFuture<ResponseEntity<Map<String, Object>>> uploadProfilePicture(
            Long userId, MultipartFile file) {
        log.warn("File management service unavailable - profile picture upload fallback");

        return CompletableFuture.completedFuture(ResponseEntity.ok(Map.of(
                "success", false,
                "message", "File management service unavailable",
                "fallback", true
        )));
    }

    @Override
    public CompletableFuture<ResponseEntity<Map<String, Object>>> uploadDocument(
            Long userId, String documentType, MultipartFile file) {
        log.warn("File management service unavailable - document upload fallback");

        return CompletableFuture.completedFuture(ResponseEntity.ok(Map.of(
                "success", false,
                "message", "File management service unavailable",
                "fallback", true
        )));
    }

    @Override
    public CompletableFuture<ResponseEntity<Map<String, Object>>> deleteFile(
            Long userId, String fileName, String category) {
        log.warn("File management service unavailable - file deletion fallback");

        return CompletableFuture.completedFuture(ResponseEntity.ok(Map.of(
                "success", false,
                "message", "File management service unavailable",
                "fallback", true
        )));
    }
}