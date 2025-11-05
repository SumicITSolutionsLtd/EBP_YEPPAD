package com.youthconnect.job_services.controller;

import com.youthconnect.job_services.common.ApiResponse;
import com.youthconnect.job_services.dto.response.JobCategoryResponse;
import com.youthconnect.job_services.entity.JobCategory;
import com.youthconnect.job_services.enums.JobStatus;
import com.youthconnect.job_services.exception.ResourceNotFoundException;
import com.youthconnect.job_services.mapper.JobMapper;
import com.youthconnect.job_services.repository.JobCategoryRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Job Category Controller - UUID Version
 *
 * UPDATED for Backend Guidelines:
 * - All IDs use UUID
 * - Fixed repository method calls
 *
 * @author Douglas Kings Kato
 * @version 2.0.0
 */
@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
@Tag(name = "Job Categories", description = "Job category management endpoints")
public class JobCategoryController {

    private final JobCategoryRepository categoryRepository;
    private final JobMapper jobMapper;

    /**
     * Get all active job categories
     */
    @GetMapping
    @Cacheable("categories")
    @Operation(summary = "List categories", description = "Get all active job categories with job counts")
    public ApiResponse<List<JobCategoryResponse>> getAllCategories() {
        List<JobCategory> categories = categoryRepository.findByIsActiveTrueAndIsDeletedFalseOrderByDisplayOrderAsc();

        List<JobCategoryResponse> responses = categories.stream()
                .map(category -> {
                    // Use custom query method to count jobs
                    Long jobCount = categoryRepository.countJobsInCategory(
                            category.getCategoryId(),
                            JobStatus.PUBLISHED
                    );
                    return jobMapper.toCategoryResponse(category, jobCount);
                })
                .collect(Collectors.toList());

        return ApiResponse.success(responses);
    }

    /**
     * Get category details by UUID
     */
    @GetMapping("/{categoryId}")
    @Cacheable(value = "categories", key = "#categoryId")
    @Operation(summary = "Get category", description = "Get category details by ID")
    public ApiResponse<JobCategoryResponse> getCategoryById(@PathVariable UUID categoryId) {
        JobCategory category = categoryRepository.findByCategoryIdAndIsDeletedFalse(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "categoryId", categoryId));

        Long jobCount = categoryRepository.countJobsInCategory(
                category.getCategoryId(),
                JobStatus.PUBLISHED
        );

        JobCategoryResponse response = jobMapper.toCategoryResponse(category, jobCount);
        return ApiResponse.success(response);
    }
}