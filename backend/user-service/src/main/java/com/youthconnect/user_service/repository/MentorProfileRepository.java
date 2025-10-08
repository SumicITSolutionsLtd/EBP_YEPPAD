package com.youthconnect.user_service.repository;

import com.youthconnect.user_service.entity.MentorProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

/**
 * Repository interface for managing MentorProfile entities.
 * Provides standard CRUD operations and allows for custom query methods.
 */
@Repository
public interface MentorProfileRepository extends JpaRepository<MentorProfile, Long> {
    // Optional: Add custom query methods
    // List<MentorProfile> findByAreaOfExpertiseContaining(String expertise);

    /**
     * Finds a MentorProfile by the associated user's ID.
     * Spring Data JPA automatically creates the query from the method name.
     *
     * @param userId The ID of the user to search for.
     * @return An Optional containing the MentorProfile if found.
     */
    Optional<MentorProfile> findByUser_Id(Long userId);
}