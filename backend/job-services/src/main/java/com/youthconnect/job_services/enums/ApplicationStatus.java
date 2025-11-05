package com.youthconnect.job_services.enums;

/**
 * Application Status Enumeration
 * Tracks the status of job applications
 */
public enum ApplicationStatus {
    SUBMITTED,            // Initial submission
    UNDER_REVIEW,        // Being reviewed by employer
    SHORTLISTED,         // Selected for next round
    INTERVIEW_SCHEDULED, // Interview scheduled
    REJECTED,            // Application rejected
    ACCEPTED,            // Application accepted/hired
    WITHDRAWN            // Applicant withdrew
}