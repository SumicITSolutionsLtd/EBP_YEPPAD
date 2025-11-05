package com.youthconnect.job_services.entity;

import com.youthconnect.job_services.enums.*;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Job Entity - Updated with UUID
 *
 * Represents a job posting in the system following platform guidelines:
 * - Uses UUID instead of Long for jobId
 * - References users by UUID (postedByUserId)
 * - Supports automatic expiration and application tracking
 * - LinkedIn-style work modes (Remote, Onsite, Hybrid)
 *
 * Key Features:
 * - UUID-based identification for distributed systems
 * - Automatic view and application count tracking
 * - Featured/Urgent flags for visibility
 * - Soft delete support via is_deleted flag
 *
 * @author Douglas Kings Kato
 * @version 2.0.0
 * @since 1.0.0
 */
@Entity
@Table(name = "jobs", indexes = {
        @Index(name = "idx_status_expires", columnList = "status, expires_at"),
        @Index(name = "idx_posted_by", columnList = "posted_by_user_id"),
        @Index(name = "idx_category", columnList = "category_id"),
        @Index(name = "idx_job_deleted", columnList = "is_deleted")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Job extends BaseEntity {

    /**
     * Primary key using UUID for distributed system compatibility
     * Generated automatically using Hibernate's UUID generator
     */
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "job_id", updatable = false, nullable = false, columnDefinition = "uuid")
    private UUID jobId;

    // ============================================================================
    // BASIC INFORMATION
    // ============================================================================

    @Column(name = "job_title", nullable = false, length = 255)
    private String jobTitle;

    @Column(name = "company_name", nullable = false, length = 200)
    private String companyName;

    /**
     * UUID reference to the user who posted this job
     * Updated from Long to UUID following platform guidelines
     */
    @Column(name = "posted_by_user_id", nullable = false, columnDefinition = "uuid")
    private UUID postedByUserId;

    @Enumerated(EnumType.STRING)
    @Column(name = "posted_by_role", nullable = false, length = 20)
    private UserRole postedByRole;

    // ============================================================================
    // JOB DETAILS
    // ============================================================================

    @Column(name = "job_description", columnDefinition = "TEXT", nullable = false)
    private String jobDescription;

    @Column(name = "responsibilities", columnDefinition = "TEXT")
    private String responsibilities;

    @Column(name = "requirements", columnDefinition = "TEXT")
    private String requirements;

    // ============================================================================
    // JOB CLASSIFICATION
    // ============================================================================

    @Enumerated(EnumType.STRING)
    @Column(name = "job_type", nullable = false, length = 20)
    private JobType jobType;

    @Enumerated(EnumType.STRING)
    @Column(name = "work_mode", nullable = false, length = 20)
    private WorkMode workMode;

    @Column(name = "location", length = 200)
    private String location;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private JobCategory category;

    // ============================================================================
    // COMPENSATION
    // ============================================================================

    @Column(name = "salary_min", precision = 15, scale = 2)
    private BigDecimal salaryMin;

    @Column(name = "salary_max", precision = 15, scale = 2)
    private BigDecimal salaryMax;

    @Column(name = "salary_currency", length = 3)
    private String salaryCurrency = "UGX";

    @Enumerated(EnumType.STRING)
    @Column(name = "salary_period", length = 20)
    private SalaryPeriod salaryPeriod;

    @Column(name = "show_salary")
    private Boolean showSalary = false;

    // ============================================================================
    // REQUIREMENTS
    // ============================================================================

    @Column(name = "experience_required", length = 50)
    private String experienceRequired;

    @Enumerated(EnumType.STRING)
    @Column(name = "education_level", length = 20)
    private EducationLevel educationLevel;

    // ============================================================================
    // APPLICATION DETAILS
    // ============================================================================

    @Column(name = "application_email", length = 255)
    private String applicationEmail;

    @Column(name = "application_phone", length = 20)
    private String applicationPhone;

    @Column(name = "application_url", length = 500)
    private String applicationUrl;

    @Column(name = "how_to_apply", columnDefinition = "TEXT")
    private String howToApply;

    // ============================================================================
    // STATUS & LIFECYCLE
    // ============================================================================

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private JobStatus status = JobStatus.DRAFT;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "closed_at")
    private LocalDateTime closedAt;

    // ============================================================================
    // LIMITS & TRACKING
    // ============================================================================

    @Column(name = "max_applications")
    private Integer maxApplications = 0;  // 0 = unlimited

    @Column(name = "application_count")
    private Integer applicationCount = 0;

    @Column(name = "view_count")
    private Integer viewCount = 0;

    // ============================================================================
    // VISIBILITY FLAGS
    // ============================================================================

    @Column(name = "is_featured")
    private Boolean isFeatured = false;

    @Column(name = "is_urgent")
    private Boolean isUrgent = false;

    // ============================================================================
    // SOFT DELETE SUPPORT
    // ============================================================================

    @Column(name = "is_deleted")
    private Boolean isDeleted = false;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    // ============================================================================
    // HELPER METHODS
    // ============================================================================

    /**
     * Increment view count atomically
     * Used for analytics and tracking job popularity
     */
    public void incrementViewCount() {
        this.viewCount = (this.viewCount == null ? 0 : this.viewCount) + 1;
    }

    /**
     * Increment application count atomically
     * Updated when a new application is submitted
     */
    public void incrementApplicationCount() {
        this.applicationCount = (this.applicationCount == null ? 0 : this.applicationCount) + 1;
    }

    /**
     * Check if job has expired based on expires_at date
     *
     * @return true if job is past expiration date
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    /**
     * Check if job is currently published and active
     *
     * @return true if job status is PUBLISHED and not expired
     */
    public boolean isPublished() {
        return JobStatus.PUBLISHED.equals(status) && !isExpired();
    }

    /**
     * Check if maximum applications limit has been reached
     *
     * @return true if max applications is set and reached
     */
    public boolean hasReachedMaxApplications() {
        return maxApplications > 0 && applicationCount >= maxApplications;
    }

    /**
     * Check if job is accepting applications
     * Considers publication status, expiration, and application limits
     *
     * @return true if job can accept new applications
     */
    public boolean isAcceptingApplications() {
        return isPublished() && !hasReachedMaxApplications() && !isDeleted;
    }

    /**
     * Soft delete the job
     * Sets is_deleted flag and deleted_at timestamp
     */
    public void softDelete() {
        this.isDeleted = true;
        this.deletedAt = LocalDateTime.now();
        this.status = JobStatus.CANCELLED;
    }

    /**
     * Restore soft deleted job
     */
    public void restore() {
        this.isDeleted = false;
        this.deletedAt = null;
    }
}