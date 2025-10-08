package com.youthconnect.user_service.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entity class representing a Mentor's profile information.
 * Linked one-to-one with a User entity that has the MENTOR role.
 */
@Entity
@Table(name = "mentor_profiles")
@Data
@NoArgsConstructor
public class MentorProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "mentor_profile_id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    // --- THIS IS THE FIX ---
    // Ensure the 'referencedColumnName' points to 'user_id' in the 'users' table.
    @JoinColumn(name = "user_id", referencedColumnName = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "first_name", nullable = false, length = 50)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 50)
    private String lastName;

    @Column(columnDefinition = "TEXT")
    private String bio;

    @Column(name = "area_of_expertise", nullable = false, columnDefinition = "TEXT")
    private String areaOfExpertise;

    private Integer experienceYears;

    @Enumerated(EnumType.STRING)
    @Column(name = "availability_status", length = 20)
    private AvailabilityStatus availabilityStatus = AvailabilityStatus.AVAILABLE;

    public enum AvailabilityStatus {
        AVAILABLE, BUSY, ON_LEAVE
    }

    public MentorProfile(User user) {
        this.user = user;
    }
}