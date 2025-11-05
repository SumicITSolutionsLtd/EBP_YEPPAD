package com.youthconnect.user_service.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.UUID;

/**
 * Entity class representing an NGO's profile information.
 * Linked one-to-one with a User entity that has the NGO role.
 */
@Entity
@Table(name = "ngo_profiles")
@Data
@NoArgsConstructor
public class NgoProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ngo_id")
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    // --- THIS IS THE FIX ---
    // Ensure the 'referencedColumnName' is 'user_id' to match the User entity's column.
    @JoinColumn(name = "user_id", referencedColumnName = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "organisation_name", nullable = false, length = 150)
    private String organisationName;

    @Column(length = 100)
    private String location;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "is_verified", nullable = false)
    private boolean isVerified = false;

    public NgoProfile(User user) {
        this.user = user;
    }
}