package com.youthconnect.content_service.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for updating learning module progress
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateProgressRequest {

    @NotNull(message = "Progress percentage is required")
    @Min(value = 0, message = "Progress cannot be negative")
    @Max(value = 100, message = "Progress cannot exceed 100%")
    private Integer progressPercentage;

    @NotNull(message = "Last position is required")
    @Min(value = 0, message = "Position cannot be negative")
    private Integer lastPositionSeconds;

    // Optional: Time spent in this session
    private Integer timeSpentSeconds;
}