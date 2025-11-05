package com.youthconnect.job_services.enums;

/**
 * Job Status Enumeration
 * Tracks the lifecycle of a job posting
 */
public enum JobStatus {
    DRAFT,      // Created but not published
    PUBLISHED,  // Live and accepting applications
    CLOSED,     // Manually closed by poster
    EXPIRED,    // Automatically expired after deadline
    CANCELLED   // Cancelled by poster
}
