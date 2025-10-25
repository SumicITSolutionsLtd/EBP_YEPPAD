package com.youthconnect.user_service.repository;

import com.youthconnect.user_service.entity.FunderProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for managing FunderProfile entities.
 * Provides standard CRUD operations and allows for custom query methods.
 *
 * FIXED: Added findByUser_Id() method required by ProfileService
 *
 * @author Youth Connect Uganda Development Team
 * @version 1.0.1
 */
@Repository
public interface FunderProfileRepository extends JpaRepository<FunderProfile, Long> {

    /**
     * Finds a FunderProfile by the associated user's ID.
     * Spring Data JPA automatically creates the query from the method name.
     *
     * Method name pattern: findBy + field name path
     * Path: FunderProfile -> user (field) -> id (field within User)
     * Therefore: findByUser_Id
     *
     * @param userId The ID of the user to search for
     * @return An Optional containing the FunderProfile if found
     */
    Optional<FunderProfile> findByUser_Id(Long userId);

    // Optional: Add custom query methods as needed
    // Optional<FunderProfile> findByFunderName(String funderName);
    // List<FunderProfile> findByFundingFocusContaining(String focus);
}