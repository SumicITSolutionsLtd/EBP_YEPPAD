package com.youthconnect.user_service.repository;

import com.youthconnect.user_service.entity.NgoProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for managing NgoProfile entities.
 * Extends JpaRepository to provide standard CRUD operations
 * and powerful querying capabilities for NgoProfile objects.
 *
 * FIXED: Added findByUser_Id() method required by ProfileService
 *
 * @author Youth Connect Uganda Development Team
 * @version 1.0.1
 */
@Repository
public interface NgoProfileRepository extends JpaRepository<NgoProfile, Long> {

    /**
     * Finds an NgoProfile by the associated user's ID.
     * Spring Data JPA automatically creates the query from the method name.
     *
     * Method name pattern: findBy + field name path
     * Path: NgoProfile -> user (field) -> id (field within User)
     * Therefore: findByUser_Id
     *
     * @param userId The ID of the user to search for
     * @return An Optional containing the NgoProfile if found
     */
    Optional<NgoProfile> findByUser_Id(Long userId);

    // Optional: Add custom query methods as needed
    // Optional<NgoProfile> findByOrganisationName(String organisationName);
    // List<NgoProfile> findByIsVerifiedTrue();
}