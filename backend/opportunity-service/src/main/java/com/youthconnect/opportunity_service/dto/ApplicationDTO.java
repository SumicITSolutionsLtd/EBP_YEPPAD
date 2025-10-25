package com.youthconnect.opportunity_service.dto;

import com.youthconnect.opportunity_service.entity.Application;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * DTO for representing an Application in API responses
 */
@Data
@Builder
public class ApplicationDTO {
    private Long applicationId;
    private Long opportunityId;
    private Long applicantId;
    private String applicantName; // Populated from User Service
    private String applicantEmail; // Populated from User Service
    private Application.Status status;
    private String applicationContent;
    private Long reviewedById;
    private String reviewerName; // Populated from User Service
    private String reviewNotes;
    private LocalDateTime reviewedAt;
    private LocalDateTime submittedAt;
    private LocalDateTime updatedAt;

    // Computed fields
    private boolean isEditable;
    private boolean isWithdrawable;
    private int daysSinceSubmission;
}