package com.youthconnect.user_service.repository;

import com.youthconnect.user_service.entity.YouthProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * ═══════════════════════════════════════════════════════════════════════════
 * YOUTH PROFILE REPOSITORY - FIXED
 * ═══════════════════════════════════════════════════════════════════════════
 *
 * FIXES APPLIED:
 * 1. Added findByUser_Id method (was missing)
 * 2. Proper UUID type for user_id
 * 3. Correct method naming convention
 */
@Repository
public interface YouthProfileRepository extends JpaRepository<YouthProfile, UUID> {

    /**
     * CRITICAL FIX: Added missing method
     *
     * Finds a YouthProfile by the associated user's ID.
     * Spring Data JPA automatically creates the query from the method name.
     *
     * Method name pattern: findBy + field name path
     * Path: YouthProfile -> user (field) -> id (field within User)
     * Therefore: findByUser_Id
     *
     * @param userId The ID of the user to search for
     * @return An Optional containing the YouthProfile if found
     */
    Optional<YouthProfile> findByUser_Id(UUID userId);
}