package com.youthconnect.job_services.enums;

/**
 * Job Type Enumeration
 * Defines the employment type for job postings
 */
public enum JobType {
    FULL_TIME("Full Time"),
    PART_TIME("Part Time"),
    CONTRACT("Contract"),
    INTERNSHIP("Internship"),
    VOLUNTEER("Volunteer");

    private final String displayName;

    JobType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}