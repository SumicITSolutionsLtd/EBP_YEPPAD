package com.youthconnect.content_service.controller;

import com.youthconnect.content_service.dto.request.UpdateProgressRequest;
import com.youthconnect.content_service.dto.response.ApiResponse;
import com.youthconnect.content_service.dto.response.LearningModuleDTO;
import com.youthconnect.content_service.entity.LearningModule;
import com.youthconnect.content_service.service.ContentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for Learning Modules
 * Handles module retrieval, creation, and progress tracking
 */
@RestController
@RequestMapping("/api/content/modules")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Learning Modules", description = "Educational content management")
public class LearningModuleController {

    private final ContentService contentService;

    /**
     * Get all learning modules with language-specific audio
     */
    @GetMapping
    @Operation(summary = "Get modules", description = "Retrieves all modules with audio in specified language")
    public ResponseEntity<ApiResponse<List<LearningModuleDTO>>> getAllModules(
            @Parameter(description = "Language code: en, lg, lur, lgb")
            @RequestParam(name = "lang", defaultValue = "en") String languageCode) {

        log.debug("GET /api/content/modules?lang={}", languageCode);

        List<LearningModuleDTO> modules = contentService.getModulesByLanguage(languageCode);
        return ResponseEntity.ok(ApiResponse.success(modules));
    }

    /**
     * Create new learning module (ADMIN/NGO only)
     */
    @PostMapping
    @Operation(summary = "Create module", description = "Creates a new learning module (admin only)")
    public ResponseEntity<ApiResponse<LearningModule>> createModule(
            @Valid @RequestBody LearningModule module) {

        log.info("POST /api/content/modules - Title: {}", module.getTitleKey());

        LearningModule savedModule = contentService.createModule(module);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Module created successfully", savedModule));
    }

    /**
     * Update user progress on a module
     */
    @PutMapping("/{moduleId}/progress")
    @Operation(summary = "Update progress", description = "Updates user's progress on a learning module")
    public ResponseEntity<ApiResponse<String>> updateProgress(
            @Parameter(description = "Module ID")
            @PathVariable Long moduleId,
            @Valid @RequestBody UpdateProgressRequest request,
            @Parameter(description = "User ID (from JWT in production)")
            @RequestHeader(value = "X-User-Id", required = false, defaultValue = "1") Long userId) {

        log.debug("PUT /api/content/modules/{}/progress - User: {}", moduleId, userId);

        contentService.updateModuleProgress(userId, moduleId, request);
        return ResponseEntity.ok(ApiResponse.success("Progress updated successfully", null));
    }
}