package com.youthconnect.user_service.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entity class representing a Service Provider's profile information.
 * Linked one-to-one with a User entity that has the SERVICE_PROVIDER role.
 */
@Entity
@Table(name = "service_provider_profiles")
@Data
@NoArgsConstructor
public class ServiceProviderProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "provider_id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    // --- THIS IS THE FIX ---
    // Ensure the 'referencedColumnName' points to 'user_id' in the 'users' table.
    @JoinColumn(name = "user_id", referencedColumnName = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "provider_name", nullable = false, length = 150)
    private String providerName;

    @Column(length = 100)
    private String location;

    @Column(name = "area_of_expertise", nullable = false, columnDefinition = "TEXT")
    private String areaOfExpertise;

    @Column(name = "is_verified", nullable = false)
    private boolean isVerified = false;

    public ServiceProviderProfile(User user) {
        this.user = user;
    }
}