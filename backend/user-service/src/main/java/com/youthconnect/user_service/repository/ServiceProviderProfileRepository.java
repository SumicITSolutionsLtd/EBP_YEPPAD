package com.youthconnect.user_service.repository;

import com.youthconnect.user_service.entity.ServiceProviderProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for managing ServiceProviderProfile entities.
 * Provides standard CRUD operations and allows for custom query methods.
 */
@Repository
public interface ServiceProviderProfileRepository extends JpaRepository<ServiceProviderProfile, Long> {
    // Optional: Add custom query methods
    // Optional<ServiceProviderProfile> findByProviderName(String providerName);
}