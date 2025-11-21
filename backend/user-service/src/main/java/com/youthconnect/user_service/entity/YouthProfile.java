





package com.youthconnect.user_service.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * ═══════════════════════════════════════════════════════════════════════════
 * YOUTH PROFILE ENTITY - FIXED
 * ═══════════════════════════════════════════════════════════════════════════
 *
 * FIXES APPLIED:
 * 1. Correct @JoinColumn configuration
 * 2. Proper UUID handling
 * 3. Correct referenced column name
 */
@Entity
@Table(name = "youth_profiles")
@Data
@NoArgsConstructor
public class YouthProfile {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "profile_id", columnDefinition = "UUID")
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "user_id",                    // Column in youth_profiles table
            referencedColumnName = "user_id",
            nullable = false,
            unique = true
    )
    private User user;

    @Column(name = "first_name", nullable = false, length = 50)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 50)
    private String lastName;

    @Column(length = 10)
    private String gender;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(name = "district", length = 50)
    private String district;

    @Column(length = 100)
    private String profession;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "has_disability", nullable = false)
    private boolean hasDisability = false;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Constructor for creating profile with associated user
     */
    public YouthProfile(User user) {
        this.user = user;
    }
}