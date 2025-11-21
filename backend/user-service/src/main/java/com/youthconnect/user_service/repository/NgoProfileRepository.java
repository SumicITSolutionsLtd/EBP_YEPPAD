package com.youthconnect.user_service.repository;

import com.youthconnect.user_service.entity.NgoProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for managing NgoProfile entities.
 * Extends JpaRepository to provide standard CRUD operations
 * and powerful querying capabilities for NgoProfile objects.
 *
 * FIXED: Added findByUser_Id() method required by ProfileService
 *
 * @author Douglas Kings Kato
 * @version 1.0.1
 */
@Repository
public interface NgoProfileRepository extends JpaRepository<NgoProfile, Long> {

    // NgoProfileRepository
    Optional<NgoProfile> findByUser_Id(UUID userId);

    // Optional: Add custom query methods as needed
    // Optional<NgoProfile> findByOrganisationName(String organisationName);
    // List<NgoProfile> findByIsVerifiedTrue();
}