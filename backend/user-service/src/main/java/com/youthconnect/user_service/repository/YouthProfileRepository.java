package com.youthconnect.user_service.repository;

import com.youthconnect.user_service.entity.YouthProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Spring Data JPA repository for the YouthProfile entity.
 *
 * This interface provides all the standard CRUD (Create, Read, Update, Delete)
 * operations for the youth_profiles table, inherited from JpaRepository.
 *
 * We also define a custom method to find a profile based on the associated User's ID.
 */
@Repository
public interface YouthProfileRepository extends JpaRepository<YouthProfile, Long> {

    /**
     * Finds a YouthProfile by the primary key ('id') of the associated User entity.
     * --- THIS IS THE FIX ---
     * The method name must match the actual path to the property.
     * Path: YouthProfile -> user (field) -> id (field within User)
     * Therefore, the correct method name is findByUser_Id.
     *
     * @param userId The primary key of the User entity.
     * @return An Optional containing the YouthProfile if found.
     */
    Optional<YouthProfile> findByUser_Id(Long userId);
}
