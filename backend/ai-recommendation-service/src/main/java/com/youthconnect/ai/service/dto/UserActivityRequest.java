package com.youthconnect.ai.service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Request DTO for recording user activities
 * Used by other services to log user behavior for AI learning
 *
 * @author Douglas Kings Kato
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserActivityRequest {

    /**
     * ID of the user performing the activity
     */
    @NotNull(message = "User ID is required")
    private Long userId;

    /**
     * Type of activity performed
     * Examples: VIEW_OPPORTUNITY, APPLY_JOB, LISTEN_AUDIO, COMPLETE_MODULE
     */
    @NotBlank(message = "Activity type is required")
    private String activityType;

    /**
     * ID of the target entity (optional)
     * e.g., Opportunity ID, Job ID, Module ID
     */
    private Long targetId;

    /**
     * Additional context metadata (optional)
     * Can include: sessionId, duration, source, deviceType, etc.
     */
    private Map<String, Object> metadata;
}