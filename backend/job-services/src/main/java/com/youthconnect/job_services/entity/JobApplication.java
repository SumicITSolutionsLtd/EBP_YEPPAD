package com.youthconnect.job_services.entity;

import jakarta.persistence.*;
import lombok.*;
import com.youthconnect.job_services.enums.ApplicationStatus;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Job Application Entity - FIXED with UUID
 *
 * Represents a user's application to a job posting.
 * Unique constraint ensures one application per user per job.
 *
 * UPDATED: All IDs changed to UUID
 *
 * @author Douglas Kings Kato
 * @version 2.0.0
 */
@Entity
@Table(name = "job_applications",
        uniqueConstraints = @UniqueConstraint(
                name = "unique_applicant_job",
                columnNames = {"job_id", "applicant_user_id"}
        ),
        indexes = {
                @Index(name = "idx_job_status", columnList = "job_id, status"),
                @Index(name = "idx_applicant", columnList = "applicant_user_id"),
                @Index(name = "idx_app_deleted", columnList = "is_deleted")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobApplication extends BaseEntity {

    /**
     * Primary key using UUID
     */
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "application_id", updatable = false, nullable = false, columnDefinition = "uuid")
    private UUID applicationId;

    // References
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_id", nullable = false)
    private Job job;

    /**
     * UUID of the applicant user
     */
    @Column(name = "applicant_user_id", nullable = false, columnDefinition = "uuid")
    private UUID applicantUserId;

    // Application Content
    @Column(name = "cover_letter", columnDefinition = "TEXT")
    private String coverLetter;

    /**
     * UUID reference to file in file-management-service
     */
    @Column(name = "resume_file_id", columnDefinition = "uuid")
    private UUID resumeFileId;

    // Status Tracking
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private ApplicationStatus status = ApplicationStatus.SUBMITTED;

    // Review Information (UUID)
    @Column(name = "reviewed_by_user_id", columnDefinition = "uuid")
    private UUID reviewedByUserId;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    @Column(name = "review_notes", columnDefinition = "TEXT")
    private String reviewNotes;

    // Interview Details
    @Column(name = "interview_date")
    private LocalDateTime interviewDate;

    @Column(name = "interview_location", length = 255)
    private String interviewLocation;

    @Column(name = "interview_notes", columnDefinition = "TEXT")
    private String interviewNotes;

    // Submission Tracking
    @Column(name = "submitted_at", nullable = false, updatable = false)
    private LocalDateTime submittedAt;

    // Soft delete support
    @Column(name = "is_deleted")
    private Boolean isDeleted = false;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    /**
     * Set submission time on creation
     */
    @PrePersist
    protected void onCreate() {
        if (submittedAt == null) {
            submittedAt = LocalDateTime.now();
        }
    }
}