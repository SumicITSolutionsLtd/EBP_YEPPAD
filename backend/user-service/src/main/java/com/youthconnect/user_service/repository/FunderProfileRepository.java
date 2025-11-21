package com.youthconnect.user_service.repository;

import com.youthconnect.user_service.entity.FunderProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for managing FunderProfile entities.
 * Provides standard CRUD operations and allows for custom query methods.
 *
 * FIXED: Added findByUser_Id() method required by ProfileService
 *
 * @author Douglas Kings Kato
 * @version 1.0.1
 */
@Repository
public interface FunderProfileRepository extends JpaRepository<FunderProfile, Long> {

    // FunderProfileRepository
    Optional<FunderProfile> findByUser_Id(UUID userId);

    // Optional: Add custom query methods as needed
    // Optional<FunderProfile> findByFunderName(String funderName);
    // List<FunderProfile> findByFundingFocusContaining(String focus);
}