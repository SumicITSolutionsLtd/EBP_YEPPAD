package com.youthconnect.user_service.repository;

import com.youthconnect.user_service.entity.User;
import com.youthconnect.user_service.entity.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * ═══════════════════════════════════════════════════════════════════════════
 * USER REPOSITORY - COMPLETE WITH UUID AND PAGINATION SUPPORT
 * ═══════════════════════════════════════════════════════════════════════════
 *
 * Provides comprehensive user data access operations for Entrepreneurship Booster Platform
 *
 * ✅ FIXED ISSUES:
 * - All ID types changed from Long to UUID
 * - Added both paginated and non-paginated search methods
 * - Added paginated findByRole methods
 * - Proper method overloading for backward compatibility
 *
 * @author Douglas Kings Kato
 * @version 2.1.0 (UUID Migration Complete)
 * @since 2025-11-02
 */
@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    // ========================================================================
    // BASIC USER LOOKUP METHODS
    // ========================================================================

    /**
     * Find user by email address (case-sensitive)
     *
     * @param email User's email address
     * @return Optional containing User if found
     */
    Optional<User> findByEmail(String email);

    /**
     * Find user by phone number
     *
     * @param phoneNumber User's phone number in E.164 format
     * @return Optional containing User if found
     */
    Optional<User> findByPhoneNumber(String phoneNumber);

    // ========================================================================
    // ROLE-BASED LOOKUP METHODS (WITH PAGINATION SUPPORT)
    // ========================================================================

    /**
     * Find all users by role with pagination support
     *
     * ✅ FIXED: Added paginated version for scalability
     *
     * @param role User role to filter by
     * @param pageable Pagination parameters
     * @return Page of users with specified role
     */
    Page<User> findByRole(Role role, Pageable pageable);

    /**
     * Find all active users by role with pagination support
     *
     * ✅ FIXED: Added paginated version - this is the method used by getAllMentors(Pageable)
     *
     * @param role User role to filter by
     * @param pageable Pagination parameters
     * @return Page of active users with specified role
     */
    Page<User> findByRoleAndIsActiveTrue(Role role, Pageable pageable);

    /**
     * Find all users by role (non-paginated)
     *
     * @param role User role to filter by
     * @return List of users with specified role
     */
    List<User> findByRole(Role role);

    /**
     * Find all active users by role (non-paginated)
     *
     * Kept for backward compatibility - prefer paginated version for large datasets
     *
     * @param role User role to filter by
     * @return List of active users with specified role
     */
    List<User> findByRoleAndIsActiveTrue(Role role);

    // ========================================================================
    // EXISTENCE CHECKS WITH ENHANCED VALIDATION
    // ========================================================================

    /**
     * Check if email exists (case-sensitive)
     *
     * @param email Email to check
     * @return true if email exists
     */
    boolean existsByEmail(String email);

    /**
     * Check if email exists (case-insensitive)
     *
     * ✅ RECOMMENDED: Use this for email validation to prevent duplicate accounts
     *
     * @param email Email to check (case-insensitive)
     * @return true if email exists
     */
    @Query("SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END FROM User u WHERE LOWER(u.email) = LOWER(:email)")
    boolean existsByEmailIgnoreCase(@Param("email") String email);

    /**
     * Check if phone number exists
     *
     * @param phoneNumber Phone number to check
     * @return true if phone number exists
     */
    boolean existsByPhoneNumber(String phoneNumber);

    // ========================================================================
    // ACTIVE USER QUERIES FOR SECURITY
    // ========================================================================

    /**
     * Find active user by email
     *
     * @param email User's email
     * @return Optional containing User if found and active
     */
    @Query("SELECT u FROM User u WHERE u.email = :email AND u.isActive = true")
    Optional<User> findActiveUserByEmail(@Param("email") String email);

    /**
     * Find active user by phone number
     *
     * @param phoneNumber User's phone number
     * @return Optional containing User if found and active
     */
    @Query("SELECT u FROM User u WHERE u.phoneNumber = :phoneNumber AND u.isActive = true")
    Optional<User> findActiveUserByPhoneNumber(@Param("phoneNumber") String phoneNumber);

    /**
     * Flexible identifier lookup for login (email or phone)
     *
     * @param identifier Email or phone number
     * @return Optional containing active User if found
     */
    @Query("SELECT u FROM User u WHERE (u.email = :identifier OR u.phoneNumber = :identifier) AND u.isActive = true")
    Optional<User> findByEmailOrPhoneNumber(@Param("identifier") String identifier);

    // ========================================================================
    // ADMIN OPERATIONS
    // ========================================================================

    /**
     * Find active users by role ordered by creation date
     *
     * @param role User role to filter by
     * @return List of active users ordered by creation date (newest first)
     */
    @Query("SELECT u FROM User u WHERE u.role = :role AND u.isActive = true ORDER BY u.createdAt DESC")
    List<User> findActiveUsersByRoleOrderByCreatedDate(@Param("role") Role role);

    /**
     * Count active users by role
     *
     * @param role User role to count
     * @return Number of active users with specified role
     */
    @Query("SELECT COUNT(u) FROM User u WHERE u.role = :role AND u.isActive = true")
    long countActiveUsersByRole(@Param("role") Role role);

    // ========================================================================
    // SEARCH FUNCTIONALITY (WITH PAGINATION SUPPORT)
    // ========================================================================

    /**
     * Search active users by multiple fields with pagination
     *
     * ✅ FIXED: Added paginated version for better performance
     *
     * Searches across:
     * - Email (partial match)
     * - Phone number (partial match)
     * - First name (from profiles)
     * - Last name (from profiles)
     *
     * @param searchTerm Search query string
     * @param pageable Pagination parameters
     * @return Page of users matching the search term
     */
    @Query("SELECT u FROM User u WHERE u.isActive = true AND " +
            "(LOWER(u.email) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "u.phoneNumber LIKE CONCAT('%', :searchTerm, '%'))")
    Page<User> searchActiveUsers(@Param("searchTerm") String searchTerm, Pageable pageable);

    /**
     * Search active users by multiple fields (non-paginated)
     *
     * ✅ FIXED: This is the overloaded version that was missing!
     *
     * Kept for backward compatibility - prefer paginated version for large result sets
     *
     * @param searchTerm Search query string
     * @return List of users matching the search term
     */
    @Query("SELECT u FROM User u WHERE u.isActive = true AND " +
            "(LOWER(u.email) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "u.phoneNumber LIKE CONCAT('%', :searchTerm, '%'))")
    List<User> searchActiveUsers(@Param("searchTerm") String searchTerm);

    // ========================================================================
    // USSD SERVICE INTEGRATION
    // ========================================================================

    /**
     * Find user by phone number including inactive users
     *
     * Used by USSD service for phone-based lookup
     *
     * @param phoneNumber User's phone number
     * @return Optional containing User regardless of active status
     */
    @Query("SELECT u FROM User u WHERE u.phoneNumber = :phoneNumber")
    Optional<User> findByPhoneNumberIncludingInactive(@Param("phoneNumber") String phoneNumber);

    // ========================================================================
    // SECURITY AND AUDIT QUERIES
    // ========================================================================

    /**
     * Find users with excessive failed login attempts
     *
     * @param maxAttempts Maximum allowed failed attempts
     * @return List of users exceeding the threshold
     */
    @Query("SELECT u FROM User u WHERE u.failedLoginAttempts >= :maxAttempts AND u.isActive = true")
    List<User> findUsersWithExcessiveFailedLogins(@Param("maxAttempts") int maxAttempts);

    /**
     * Find currently locked user accounts
     *
     * @return List of users whose accounts are currently locked
     */
    @Query("SELECT u FROM User u WHERE u.accountLockedUntil IS NOT NULL AND u.accountLockedUntil > CURRENT_TIMESTAMP")
    List<User> findLockedUsers();

    /**
     * Find unverified users created before cutoff date
     *
     * Useful for cleanup operations and reminder emails
     *
     * @param cutoffDate Date before which unverified users should be found
     * @return List of unverified users older than cutoff date
     */
    @Query("SELECT u FROM User u WHERE u.emailVerified = false AND u.createdAt < :cutoffDate")
    List<User> findUnverifiedUsers(@Param("cutoffDate") LocalDateTime cutoffDate);

    // ========================================================================
    // ADVANCED QUERIES FOR ANALYTICS
    // ========================================================================

    /**
     * Count users by role (all statuses)
     *
     * @param role User role to count
     * @return Total number of users with specified role
     */
    @Query("SELECT COUNT(u) FROM User u WHERE u.role = :role")
    long countByRole(@Param("role") Role role);

    /**
     * Find users created within date range
     *
     * @param startDate Start of date range
     * @param endDate End of date range
     * @return List of users created within the specified range
     */
    @Query("SELECT u FROM User u WHERE u.createdAt BETWEEN :startDate AND :endDate ORDER BY u.createdAt DESC")
    List<User> findUsersCreatedBetween(@Param("startDate") LocalDateTime startDate,
                                       @Param("endDate") LocalDateTime endDate);

    /**
     * Find users who haven't logged in since a certain date
     *
     * @param lastLoginDate Date threshold
     * @return List of inactive users
     */
    @Query("SELECT u FROM User u WHERE u.isActive = true AND " +
            "(u.lastLogin IS NULL OR u.lastLogin < :lastLoginDate)")
    List<User> findInactiveUsersSince(@Param("lastLoginDate") LocalDateTime lastLoginDate);
}