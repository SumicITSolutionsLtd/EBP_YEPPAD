package com.youthconnect.opportunity_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * DTO for creating or updating an Application
 */
@Data
public class ApplicationRequest {

    @NotNull(message = "Opportunity ID is required")
    private Long opportunityId;

    @NotNull(message = "Applicant ID is required")
    private Long applicantId;

    @NotBlank(message = "Application content cannot be empty")
    @Size(min = 50, max = 5000, message = "Application content must be between 50 and 5000 characters")
    private String applicationContent;

    // Optional: For application review
    private Long reviewedById;
    private String reviewNotes;
}