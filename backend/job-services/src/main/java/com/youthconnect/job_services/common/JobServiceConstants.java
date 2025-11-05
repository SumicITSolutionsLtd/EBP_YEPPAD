package com.youthconnect.job_services.common;

/**
 * Job Service Constants
 *
 * Centralized constants for the Job Service to avoid magic numbers
 * and ensure consistency across the application.
 *
 * @author Douglas Kings Kato
 * @since 1.0.0
 */
public final class JobServiceConstants {

    // ============================================================================
    // APPLICATION LIMITS
    // ============================================================================

    /**
     * Maximum number of job applications a user can submit per day
     * Prevents spam and abuse of the application system
     */
    public static final int MAX_APPLICATIONS_PER_DAY = 20;

    /**
     * Default maximum applications per job (0 = unlimited)
     */
    public static final int DEFAULT_MAX_APPLICATIONS_PER_JOB = 100;

    /**
     * Maximum concurrent applications a user can have in PENDING status
     */
    public static final int MAX_CONCURRENT_PENDING_APPLICATIONS = 50;

    // ============================================================================
    // VALIDATION CONSTRAINTS
    // ============================================================================

    // Job Title Constraints
    public static final int MIN_JOB_TITLE_LENGTH = 10;
    public static final int MAX_JOB_TITLE_LENGTH = 255;

    // Job Description Constraints
    public static final int MIN_JOB_DESCRIPTION_LENGTH = 100;
    public static final int MAX_JOB_DESCRIPTION_LENGTH = 10000;

    // Cover Letter Constraints
    public static final int MIN_COVER_LETTER_LENGTH = 50;
    public static final int MAX_COVER_LETTER_LENGTH = 5000;

    // Company Name Constraints
    public static final int MIN_COMPANY_NAME_LENGTH = 2;
    public static final int MAX_COMPANY_NAME_LENGTH = 200;

    // Requirements/Responsibilities Constraints
    public static final int MAX_REQUIREMENTS_LENGTH = 5000;
    public static final int MAX_RESPONSIBILITIES_LENGTH = 5000;

    // ============================================================================
    // CACHE CONFIGURATION
    // ============================================================================

    /**
     * Cache TTL (Time To Live) in seconds
     */
    public static final int CACHE_TTL_JOBS = 1800;              // 30 minutes
    public static final int CACHE_TTL_CATEGORIES = 3600;        // 1 hour
    public static final int CACHE_TTL_JOB_DETAILS = 900;        // 15 minutes
    public static final int CACHE_TTL_APPLICATIONS = 600;       // 10 minutes
    public static final int CACHE_TTL_SEARCH_RESULTS = 300;     // 5 minutes

    /**
     * Maximum cache sizes
     */
    public static final int MAX_CACHE_SIZE_JOBS = 1000;
    public static final int MAX_CACHE_SIZE_CATEGORIES = 100;
    public static final int MAX_CACHE_SIZE_APPLICATIONS = 5000;

    // ============================================================================
    // PAGINATION DEFAULTS
    // ============================================================================

    public static final int DEFAULT_PAGE_SIZE = 20;
    public static final int MAX_PAGE_SIZE = 100;
    public static final int DEFAULT_PAGE_NUMBER = 0;

    // ============================================================================
    // JOB POSTING LIMITS
    // ============================================================================

    /**
     * Maximum number of jobs a single organization can post per month
     */
    public static final int MAX_JOBS_PER_MONTH_NGO = 100;
    public static final int MAX_JOBS_PER_MONTH_COMPANY = 50;
    public static final int MAX_JOBS_PER_MONTH_RECRUITER = 200;
    public static final int MAX_JOBS_PER_MONTH_GOVERNMENT = 500;

    /**
     * Minimum days a job must remain open before it can be closed
     */
    public static final int MIN_JOB_OPEN_DAYS = 3;

    /**
     * Maximum days into the future a job deadline can be set
     */
    public static final int MAX_JOB_DEADLINE_DAYS = 365; // 1 year

    // ============================================================================
    // SALARY CONSTRAINTS
    // ============================================================================

    public static final double MIN_SALARY_AMOUNT = 0.0;
    public static final double MAX_SALARY_AMOUNT = 100_000_000.0; // 100M UGX
    public static final String DEFAULT_CURRENCY = "UGX";

    // ============================================================================
    // NOTIFICATION THRESHOLDS
    // ============================================================================

    /**
     * Days before deadline to send reminder notifications
     */
    public static final int DEADLINE_REMINDER_DAYS_BEFORE = 7;
    public static final int DEADLINE_URGENT_REMINDER_DAYS_BEFORE = 3;
    public static final int DEADLINE_FINAL_REMINDER_DAYS_BEFORE = 1;

    /**
     * Application count threshold to notify job poster
     */
    public static final int APPLICATION_MILESTONE_THRESHOLD = 10;

    // ============================================================================
    // SEARCH & FILTER DEFAULTS
    // ============================================================================

    public static final String DEFAULT_SORT_BY = "publishedAt";
    public static final String DEFAULT_SORT_DIRECTION = "DESC";
    public static final int MAX_SEARCH_KEYWORD_LENGTH = 200;

    // ============================================================================
    // FILE UPLOAD LIMITS
    // ============================================================================

    /**
     * Maximum resume/CV file size in bytes (10 MB)
     */
    public static final long MAX_RESUME_FILE_SIZE = 10 * 1024 * 1024;

    /**
     * Allowed file types for resume uploads
     */
    public static final String[] ALLOWED_RESUME_TYPES = {"pdf", "doc", "docx"};

    // ============================================================================
    // RATE LIMITING
    // ============================================================================

    /**
     * Maximum API requests per minute per user
     */
    public static final int MAX_REQUESTS_PER_MINUTE = 100;
    public static final int MAX_JOB_SEARCH_REQUESTS_PER_MINUTE = 20;
    public static final int MAX_APPLICATION_SUBMISSIONS_PER_MINUTE = 5;

    // ============================================================================
    // ANALYTICS & METRICS
    // ============================================================================

    /**
     * Days of data to include in trending calculations
     */
    public static final int TRENDING_CALCULATION_DAYS = 7;

    /**
     * Minimum views required for a job to be considered "popular"
     */
    public static final int MIN_VIEWS_FOR_POPULAR = 100;

    // ============================================================================
    // ERROR MESSAGES
    // ============================================================================

    public static final String ERR_JOB_NOT_FOUND = "Job not found with ID: ";
    public static final String ERR_APPLICATION_NOT_FOUND = "Application not found with ID: ";
    public static final String ERR_CATEGORY_NOT_FOUND = "Category not found with ID: ";
    public static final String ERR_UNAUTHORIZED = "You are not authorized to perform this action";
    public static final String ERR_JOB_EXPIRED = "This job posting has expired";
    public static final String ERR_MAX_APPLICATIONS_REACHED = "Maximum number of applications reached";
    public static final String ERR_DUPLICATE_APPLICATION = "You have already applied to this job";
    public static final String ERR_INVALID_STATUS = "Invalid job status for this operation";

    // ============================================================================
    // SUCCESS MESSAGES
    // ============================================================================

    public static final String SUCCESS_JOB_CREATED = "Job created successfully";
    public static final String SUCCESS_JOB_UPDATED = "Job updated successfully";
    public static final String SUCCESS_JOB_PUBLISHED = "Job published successfully";
    public static final String SUCCESS_JOB_CLOSED = "Job closed successfully";
    public static final String SUCCESS_JOB_DELETED = "Job deleted successfully";
    public static final String SUCCESS_APPLICATION_SUBMITTED = "Application submitted successfully";
    public static final String SUCCESS_APPLICATION_WITHDRAWN = "Application withdrawn successfully";

    // ============================================================================
    // CONSTRUCTOR
    // ============================================================================

    /**
     * Private constructor to prevent instantiation of this constants class
     */
    private JobServiceConstants() {
        throw new IllegalStateException("Constants class cannot be instantiated");
    }
}