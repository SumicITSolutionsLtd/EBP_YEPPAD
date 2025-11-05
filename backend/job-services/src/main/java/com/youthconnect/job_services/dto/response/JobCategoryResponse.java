package com.youthconnect.job_services.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Job Category Response DTO - Updated with UUID
 *
 * Response for job category information with job count.
 *
 * @author Douglas Kings Kato
 * @version 2.0.0
 */
@Data
@Builder
public class JobCategoryResponse {

    /**
     * Category identifier (UUID)
     */
    private UUID categoryId;
    private String categoryName;
    private String description;
    private String iconUrl;
    private Boolean isActive;
    private Integer displayOrder;

    // Additional metrics
    private Long jobCount;

    // Timestamps
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}