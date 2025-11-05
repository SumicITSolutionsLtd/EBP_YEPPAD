package com.youthconnect.user_service.repository;

import com.youthconnect.user_service.entity.ServiceProviderProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for managing ServiceProviderProfile entities.
 * Provides standard CRUD operations and allows for custom query methods.
 *
 * FIXED: Added findByUser_Id() method required by ProfileService
 *
 * @author Douglas Kings Kato
 * @version 1.0.1
 */
@Repository
public interface ServiceProviderProfileRepository extends JpaRepository<ServiceProviderProfile, Long> {

    /**
     * Finds a ServiceProviderProfile by the associated user's ID.
     * Spring Data JPA automatically creates the query from the method name.
     *
     * Method name pattern: findBy + field name path
     * Path: ServiceProviderProfile -> user (field) -> id (field within User)
     * Therefore: findByUser_Id
     *
     * @param userId The ID of the user to search for
     * @return An Optional containing the ServiceProviderProfile if found
     */
    Optional<ServiceProviderProfile> findByUser_Id(UUID userId);

    // Optional: Add custom query methods as needed
    // Optional<ServiceProviderProfile> findByProviderName(String providerName);
    // List<ServiceProviderProfile> findByIsVerifiedTrue();
    // List<ServiceProviderProfile> findByAreaOfExpertiseContaining(String expertise);
}