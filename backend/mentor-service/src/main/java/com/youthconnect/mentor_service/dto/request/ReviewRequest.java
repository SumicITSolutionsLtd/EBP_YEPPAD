package com.youthconnect.mentor_service.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ============================================================================
 * REVIEW REQUEST DTO
 * ============================================================================
 *
 * Request DTO for submitting session reviews.
 *
 * @author Douglas Kings Kato
 * @version 1.0.0
 * @since 2025-01-22
 * ============================================================================
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewRequest {

    /**
     * Session ID being reviewed
     */
    @NotNull(message = "Session ID is required")
    private Long sessionId;

    /**
     * Rating (1-5 stars)
     */
    @NotNull(message = "Rating is required")
    @Min(value = 1, message = "Rating must be at least 1")
    @Max(value = 5, message = "Rating cannot exceed 5")
    private Integer rating;

    /**
     * Review comment (optional but recommended)
     */
    @Size(min = 10, max = 500, message = "Comment must be between 10 and 500 characters")
    private String comment;
}