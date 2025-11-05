package com.youthconnect.job_services.dto.response;

import com.youthconnect.job_services.enums.ApplicationStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Application Response DTO - Updated with UUID
 *
 * Response for job application details.
 * All identifiers updated to UUID.
 *
 * @author Douglas Kings Kato
 * @version 2.0.0
 */
@Data
@Builder
public class ApplicationResponse {

    /**
     * Application identifier (UUID)
     */
    private UUID applicationId;

    // Job information (UUID)
    private UUID jobId;
    private String jobTitle;
    private String companyName;

    // Applicant information (UUID)
    private UUID applicantUserId;

    // Application content
    private String coverLetter;
    private UUID resumeFileId;  // UUID reference to file

    // Status tracking
    private ApplicationStatus status;

    // Review information (UUID)
    private UUID reviewedByUserId;
    private LocalDateTime reviewedAt;
    private String reviewNotes;

    // Interview details
    private LocalDateTime interviewDate;
    private String interviewLocation;
    private String interviewNotes;

    // Timestamps
    private LocalDateTime submittedAt;
    private LocalDateTime updatedAt;
}