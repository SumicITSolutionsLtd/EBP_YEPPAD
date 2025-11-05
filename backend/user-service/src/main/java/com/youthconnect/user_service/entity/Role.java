package com.youthconnect.user_service.entity;

/**
 * ═══════════════════════════════════════════════════════════════════════════
 * ROLE ENUM - COMPLETE USER ROLE DEFINITIONS
 * ═══════════════════════════════════════════════════════════════════════════
 *
 * Defines all user roles in the Youth Connect Uganda platform.
 *
 * ROLE CATEGORIES:
 * ===============
 * 1. Youth & Mentors: YOUTH, MENTOR
 * 2. Organizations: NGO, COMPANY
 * 3. Service Providers: SERVICE_PROVIDER, RECRUITER
 * 4. Funders: FUNDER
 * 5. Government: GOVERNMENT
 * 6. Administration: ADMIN
 *
 * JOB POSTING PERMISSIONS:
 * =======================
 * Can Post Jobs: NGO, COMPANY, RECRUITER, GOVERNMENT
 * Cannot Post Jobs: YOUTH, MENTOR, FUNDER, SERVICE_PROVIDER, ADMIN
 *
 * @author Douglas Kings Kato
 * @version 1.1.0 (Complete)
 * @since 2025-10-31
 */
public enum Role {

    /**
     * Young people seeking opportunities, mentorship, and employment
     * - Primary beneficiaries of the platform
     * - Can apply for jobs and opportunities
     * - Can request mentorship
     * - Cannot post jobs
     */
    YOUTH,

    /**
     * Experienced professionals providing guidance and mentorship
     * - Can mentor youth
     * - Can share expertise and resources
     * - Cannot post jobs
     */
    MENTOR,

    /**
     * Non-Governmental Organizations
     * - Can post job opportunities
     * - Can offer training and programs
     * - Can provide funding opportunities
     * - Subject to verification
     */
    NGO,

    /**
     * Private companies and businesses
     * - Can post job opportunities
     * - Can hire youth and professionals
     * - Can offer internships and training
     */
    COMPANY,

    /**
     * Professional recruiters and HR agencies
     * - Can post jobs on behalf of companies
     * - Can manage application processes
     * - Can conduct interviews and assessments
     */
    RECRUITER,

    /**
     * Government agencies and departments
     * - Can post government job opportunities
     * - Can offer public sector positions
     * - Can provide government programs
     */
    GOVERNMENT,

    /**
     * Funding organizations and donors
     * - Can provide financial support
     * - Can offer grants and scholarships
     * - Cannot post jobs (funding only)
     */
    FUNDER,

    /**
     * Verified service providers
     * - Can offer specialized services
     * - Can provide training and consultancy
     * - Subject to verification
     * - Cannot post jobs
     */
    SERVICE_PROVIDER,

    /**
     * Platform administrators
     * - Full system access
     * - Can manage all users and content
     * - Can verify organizations
     * - Cannot post jobs
     */
    ADMIN
}