package com.youthconnect.user_service.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * ═══════════════════════════════════════════════════════════════════════════
 * USER ROLE ENUM - COMPLETE DEFINITION
 * ═══════════════════════════════════════════════════════════════════════════
 *
 * Defines all user roles in the Youth Connect Uganda platform.
 *
 * CRITICAL: Enum values MUST match PostgreSQL enum definition exactly:
 * CREATE TYPE user_role AS ENUM (
 *   'YOUTH', 'MENTOR', 'NGO', 'COMPANY', 'RECRUITER',
 *   'GOVERNMENT', 'FUNDER', 'SERVICE_PROVIDER', 'ADMIN'
 * );
 *
 * ROLE CATEGORIES:
 * ===============
 * 1. Beneficiaries: YOUTH
 * 2. Guidance: MENTOR
 * 3. Job Creators: NGO, COMPANY, RECRUITER, GOVERNMENT
 * 4. Support/Funding: FUNDER, SERVICE_PROVIDER
 * 5. Administration: ADMIN
 *
 * @author Douglas Kings Kato
 * @version 2.1.0
 */
@Getter
public enum Role {

    /**
     * Young people seeking opportunities (18-35 years old)
     * - Can create profiles and apply for jobs/funding
     * - Can request mentorship
     * - Cannot post jobs
     */
    YOUTH("Youth", "Young person seeking opportunities, mentorship, and employment"),

    /**
     * Experienced professionals providing guidance
     * - Can mentor youth and review plans
     * - Cannot post jobs
     */
    MENTOR("Mentor", "Experienced professional offering guidance"),

    /**
     * Non-Governmental Organizations
     * - Can post jobs, training, and funding
     * - Subject to verification
     */
    NGO("NGO", "Non-Governmental Organization"),

    /**
     * Private companies and businesses
     * - Can post jobs and offer internships
     * - Can hire youth
     */
    COMPANY("Company", "Private entity offering employment"),

    /**
     * Professional recruiters and HR agencies
     * - Can post jobs on behalf of companies
     * - Manage application processes
     */
    RECRUITER("Recruiter", "Professional recruiter or HR agency"),

    /**
     * Government agencies and departments
     * - Can post government jobs and public sector positions
     * - Can offer government programs
     */
    GOVERNMENT("Government", "Government agency or department"),

    /**
     * Funding organizations and donors
     * - Provide capital and grants
     * - Cannot post jobs (funding only)
     */
    FUNDER("Funder", "Investor or funding organization"),

    /**
     * Verified service providers
     * - Offer specialized business services (Legal, IT, etc.)
     * - Cannot post jobs
     */
    SERVICE_PROVIDER("Service Provider", "Business service provider"),

    /**
     * Platform Administrators
     * - Full system access
     * - Verify organizations and manage disputes
     */
    ADMIN("Administrator", "Platform administrator");

    // Human-readable display name
    private final String displayName;

    // Detailed description
    private final String description;

    /**
     * Constructor
     */
    Role(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    /**
     * Get the enum constant name (matches PostgreSQL enum value)
     * Used for JSON serialization to database/frontend
     */
    @JsonValue
    public String getValue() {
        return this.name();
    }

    /**
     * Check if role is privileged (ADMIN)
     */
    public boolean isAdmin() {
        return this == ADMIN;
    }

    /**
     * Check if the role allows posting Job Opportunities.
     * Based on business rules: NGO, COMPANY, RECRUITER, GOVERNMENT.
     */
    public boolean canPostJobs() {
        return this == NGO ||
                this == COMPANY ||
                this == RECRUITER ||
                this == GOVERNMENT;
    }

    /**
     * Check if role is a beneficiary (receives services primary)
     */
    public boolean isBeneficiary() {
        return this == YOUTH;
    }

    /**
     * Check if role is an institutional entity (Organization/Company/Gov)
     * Often used for profile verification logic.
     */
    public boolean isOrganization() {
        return this == NGO ||
                this == COMPANY ||
                this == GOVERNMENT ||
                this == FUNDER ||
                this == RECRUITER;
    }

    /**
     * Parse role from string (case-insensitive)
     * Annotated with @JsonCreator to handle incoming JSON strings safely
     *
     * @param value String value to parse
     * @return Role enum constant
     * @throws IllegalArgumentException if value doesn't match any role
     */
    @JsonCreator
    public static Role fromString(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Role cannot be null or empty");
        }

        try {
            return Role.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "Invalid role: '" + value + "'. Valid roles are: " + getAllRoleNames()
            );
        }
    }

    /**
     * Get all role names as a comma-separated string
     * Useful for validation messages and API documentation
     */
    public static String getAllRoleNames() {
        return Arrays.stream(Role.values())
                .map(Role::name)
                .collect(Collectors.joining(", "));
    }

    /**
     * String representation for logging/debugging
     */
    @Override
    public String toString() {
        return this.displayName + " (" + this.name() + ")";
    }
}