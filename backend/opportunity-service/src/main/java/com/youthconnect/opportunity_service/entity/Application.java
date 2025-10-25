package com.youthconnect.opportunity_service.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;

/**
 * JPA Entity representing the 'applications' table.
 * Tracks youth applications to opportunities with full review workflow.
 */
@Entity
@Table(name = "applications")
@Data
@NoArgsConstructor
public class Application {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long applicationId;

    /**
     * Foreign key to opportunities table
     */
    @Column(nullable = false)
    private Long opportunityId;

    /**
     * Foreign key to users table (youth who applied)
     */
    @Column(nullable = false)
    private Long applicantId;

    /**
     * Application status tracking
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "VARCHAR(20) DEFAULT 'PENDING'")
    private Status status = Status.PENDING;

    /**
     * User who reviewed the application (NGO/Funder)
     */
    private Long reviewedById;

    /**
     * Reviewer's feedback or notes
     */
    @Column(columnDefinition = "TEXT")
    private String reviewNotes;

    /**
     * When the application was reviewed
     */
    private LocalDateTime reviewedAt;

    /**
     * Application content (cover letter, answers to questions, etc.)
     */
    @Column(columnDefinition = "TEXT")
    private String applicationContent;

    /**
     * Submission timestamp
     */
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime submittedAt;

    /**
     * Last update timestamp
     */
    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Application status enum matching database schema
     */
    public enum Status {
        PENDING,
        UNDER_REVIEW,
        APPROVED,
        REJECTED,
        WITHDRAWN
    }

    /**
     * Business logic: Check if application can be edited
     */
    public boolean isEditable() {
        return status == Status.PENDING;
    }

    /**
     * Business logic: Check if application can be withdrawn
     */
    public boolean isWithdrawable() {
        return status == Status.PENDING || status == Status.UNDER_REVIEW;
    }

    /**
     * Business logic: Check if application has been reviewed
     */
    public boolean isReviewed() {
        return reviewedAt != null && reviewedById != null;
    }
}