package com.youthconnect.user_service.repository;

import com.youthconnect.user_service.entity.User;
import com.youthconnect.user_service.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * FIXED: Complete UserRepository with all required methods
 * Provides comprehensive user data access operations for Youth Connect Uganda
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Basic user lookup methods
    Optional<User> findByEmail(String email);

    Optional<User> findByPhoneNumber(String phoneNumber);

    // FIXED: Added missing method required by AIRecommendationService and UserService
    List<User> findByRole(Role role);

    List<User> findByRoleAndIsActiveTrue(Role role);

    // Existence checks with enhanced validation
    @Query("SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END FROM User u WHERE LOWER(u.email) = LOWER(:email)")
    boolean existsByEmailIgnoreCase(@Param("email") String email);

    boolean existsByPhoneNumber(String phoneNumber);

    boolean existsByEmail(String email);

    // Active user queries for security
    @Query("SELECT u FROM User u WHERE u.email = :email AND u.isActive = true")
    Optional<User> findActiveUserByEmail(@Param("email") String email);

    @Query("SELECT u FROM User u WHERE u.phoneNumber = :phoneNumber AND u.isActive = true")
    Optional<User> findActiveUserByPhoneNumber(@Param("phoneNumber") String phoneNumber);

    // Flexible identifier lookup for login (email or phone)
    @Query("SELECT u FROM User u WHERE (u.email = :identifier OR u.phoneNumber = :identifier) AND u.isActive = true")
    Optional<User> findByEmailOrPhoneNumber(@Param("identifier") String identifier);

    // Additional useful queries for admin operations
    @Query("SELECT u FROM User u WHERE u.role = :role AND u.isActive = true ORDER BY u.createdAt DESC")
    List<User> findActiveUsersByRoleOrderByCreatedDate(@Param("role") Role role);

    @Query("SELECT COUNT(u) FROM User u WHERE u.role = :role AND u.isActive = true")
    long countActiveUsersByRole(@Param("role") Role role);

    // Search functionality
    @Query("SELECT u FROM User u WHERE u.isActive = true AND " +
            "(LOWER(u.email) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "u.phoneNumber LIKE CONCAT('%', :searchTerm, '%'))")
    List<User> searchActiveUsers(@Param("searchTerm") String searchTerm);

    // For USSD service integration
    @Query("SELECT u FROM User u WHERE u.phoneNumber = :phoneNumber")
    Optional<User> findByPhoneNumberIncludingInactive(@Param("phoneNumber") String phoneNumber);

    // Security and audit queries
    @Query("SELECT u FROM User u WHERE u.failedLoginAttempts >= :maxAttempts AND u.isActive = true")
    List<User> findUsersWithExcessiveFailedLogins(@Param("maxAttempts") int maxAttempts);

    @Query("SELECT u FROM User u WHERE u.accountLockedUntil IS NOT NULL AND u.accountLockedUntil > CURRENT_TIMESTAMP")
    List<User> findLockedUsers();

    @Query("SELECT u FROM User u WHERE u.emailVerified = false AND u.createdAt < :cutoffDate")
    List<User> findUnverifiedUsers(@Param("cutoffDate") java.time.LocalDateTime cutoffDate);
}