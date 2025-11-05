package com.youthconnect.job_services.enums;

/**
 * User Role Enumeration
 * Defines who can post jobs
 */
public enum UserRole {
    YOUTH,          // Cannot post jobs, can only apply
    NGO,            // Can post jobs
    COMPANY,        // Can post jobs
    RECRUITER,      // Can post jobs
    GOVERNMENT,     // Can post jobs
    ADMIN           // Platform administrator
}
