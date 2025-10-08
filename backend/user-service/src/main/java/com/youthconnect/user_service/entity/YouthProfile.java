package com.youthconnect.user_service.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entity class representing a Youth's detailed profile information.
 * Each Youth profile is linked via a one-to-one relationship to a User entity with the YOUTH role.
 *
 * @author Youth Connect Uganda Development Team
 * @version 2.0.0
 */
@Entity
@Table(name = "youth_profiles")
@Data
@NoArgsConstructor
public class YouthProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "profile_id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "user_id", nullable = false, unique = true)
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

    @Column(name = "national_id", length = 50)
    private String nationalId;

    @Column(length = 100)
    private String profession;

    @Column(name = "academic_qualification", length = 100)
    private String academicQualification;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "area_of_interest", columnDefinition = "TEXT")
    private String areaOfInterest;

    @Column(name = "has_disability", nullable = false)
    private boolean hasDisability = false;

    @Column(name = "disability_details", columnDefinition = "TEXT")
    private String disabilityDetails;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Convenience constructor to create a new profile for a given user.
     * @param user The User entity this profile belongs to.
     */
    public YouthProfile(User user) {
        this.user = user;
    }
}