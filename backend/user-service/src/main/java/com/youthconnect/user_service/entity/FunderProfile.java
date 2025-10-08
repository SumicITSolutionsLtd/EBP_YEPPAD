package com.youthconnect.user_service.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entity class representing a Funder's profile information.
 * Linked one-to-one with a User entity that has the FUNDER role.
 */
@Entity
@Table(name = "funder_profiles")
@Data
@NoArgsConstructor
public class FunderProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "funder_id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    // --- THIS IS THE FIX ---
    // Ensure the 'referencedColumnName' points to 'user_id' in the 'users' table.
    @JoinColumn(name = "user_id", referencedColumnName = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "funder_name", nullable = false, length = 150)
    private String funderName;

    @Column(name = "funding_focus", columnDefinition = "TEXT")
    private String fundingFocus;

    public FunderProfile(User user) {
        this.user = user;
    }
}