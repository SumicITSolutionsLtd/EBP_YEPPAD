package com.youthconnect.user_service.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * ═══════════════════════════════════════════════════════════════════════════
 * MENTOR PROFILE ENTITY - UUID IMPLEMENTATION
 * ═══════════════════════════════════════════════════════════════════════════
 *
 * Represents mentor-specific profile information in the platform.
 *
 * Key Features:
 * - UUID-based primary key for distributed system compatibility
 * - One-to-one relationship with User entity
 * - Availability status tracking
 * - Area of expertise and experience management
 * - Audit timestamps for tracking changes
 *
 * @author Douglas Kings Kato & Youth Connect Uganda Team
 * @version 2.0.0 (UUID Migration)
 * @since 2025-11-02
 */
@Entity
@Table(name = "mentor_profiles")
@Data
@NoArgsConstructor
public class MentorProfile {

    /**
     * Primary key using UUID for global uniqueness
     * Automatically generated using Hibernate's UUID generator
     */
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(
            name = "UUID",
            strategy = "org.hibernate.id.UUIDGenerator"
    )
    @Column(name = "mentor_profile_id", updatable = false, nullable = false, columnDefinition = "UUID")
    private UUID id;

    /**
     * One-to-one relationship with User entity
     * Each mentor profile is associated with exactly one user account
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "user_id", nullable = false, unique = true)
    private User user;

    /**
     * Mentor's first name
     */
    @Column(name = "first_name", nullable = false, length = 50)
    private String firstName;

    /**
     * Mentor's last name
     */
    @Column(name = "last_name", nullable = false, length = 50)
    private String lastName;

    /**
     * Professional biography describing mentor's background and expertise
     */
    @Column(name = "bio", columnDefinition = "TEXT")
    private String bio;

    /**
     * Primary area of expertise (e.g., "Technology", "Business", "Agriculture")
     */
    @Column(name = "area_of_expertise", length = 100)
    private String areaOfExpertise;

    /**
     * Number of years of professional experience
     */
    @Column(name = "experience_years")
    private Integer experienceYears;

    /**
     * Current availability status for mentorship engagements
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "availability_status", nullable = false, length = 20)
    private AvailabilityStatus availabilityStatus = AvailabilityStatus.AVAILABLE;

    /**
     * Maximum number of mentees the mentor can handle concurrently
     */
    @Column(name = "max_mentees")
    private Integer maxMentees = 5;

    /**
     * Current number of active mentees
     */
    @Column(name = "current_mentees")
    private Integer currentMentees = 0;

    /**
     * Timestamp when the profile was created
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Timestamp when the profile was last updated
     */
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Constructor for creating mentor profile with associated user
     *
     * @param user The user entity this profile belongs to
     */
    public MentorProfile(User user) {
        this.user = user;
        this.availabilityStatus = AvailabilityStatus.AVAILABLE;
        this.maxMentees = 5;
        this.currentMentees = 0;
    }

    /**
     * Enum representing mentor availability status
     */
    public enum AvailabilityStatus {
        AVAILABLE,      // Accepting new mentees
        BUSY,          // Temporarily unavailable
        FULL,          // At maximum capacity
        UNAVAILABLE    // Not accepting mentees
    }

    /**
     * Check if mentor can accept new mentees
     *
     * @return true if available and under capacity
     */
    public boolean canAcceptNewMentee() {
        return availabilityStatus == AvailabilityStatus.AVAILABLE
                && currentMentees < maxMentees;
    }

    /**
     * Increment current mentee count
     */
    public void addMentee() {
        if (currentMentees < maxMentees) {
            currentMentees++;
            if (currentMentees >= maxMentees) {
                availabilityStatus = AvailabilityStatus.FULL;
            }
        }
    }

    /**
     * Decrement current mentee count
     */
    public void removeMentee() {
        if (currentMentees > 0) {
            currentMentees--;
            if (availabilityStatus == AvailabilityStatus.FULL) {
                availabilityStatus = AvailabilityStatus.AVAILABLE;
            }
        }
    }
}