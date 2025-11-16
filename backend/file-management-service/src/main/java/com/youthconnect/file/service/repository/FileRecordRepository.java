package com.youthconnect.file.service.repository;

import com.youthconnect.file.service.entity.FileRecord;
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
 * Repository for file record database operations
 *
 * ✅ FIXED: All methods now use UUID for userId parameter
 * ✅ Includes pagination support for all list operations
 * ✅ Optimized queries with proper indexing
 *
 * NAMING CONVENTIONS:
 * - findBy*: Returns collection (List or Page)
 * - findOneBy*: Returns Optional<FileRecord>
 * - existsBy*: Returns boolean
 * - countBy*: Returns long
 *
 * @author Douglas Kings Kato
 * @version 2.0.0 (UUID Fixed)
 */
@Repository
public interface FileRecordRepository extends JpaRepository<FileRecord, Long> {

    // ============================================================
    // BASIC QUERIES - Find by userId and category
    // ============================================================

    /**
     * Find files by user ID and category (without pagination)
     *
     * ⚠️ USE WITH CAUTION: Can return large result sets
     * Prefer paginated version for user-facing queries
     *
     * USAGE: Admin tools, batch jobs, export operations
     *
     * @param userId User's UUID from auth-service
     * @param category File category enum
     * @return List of active file records
     */
    List<FileRecord> findByUserIdAndCategoryAndIsActiveTrue(
            UUID userId,
            FileRecord.FileCategory category
    );

    /**
     * ⭐ Find files by user ID and category WITH PAGINATION
     *
     * PREFERRED METHOD for user file listings
     * Prevents performance issues with large file collections
     *
     * USAGE:
     * ```
     * PageRequest pageRequest = PageRequest.of(0, 20, Sort.by("uploadTime").descending());
     * Page<FileRecord> files = repository.findByUserIdAndCategoryAndIsActiveTrue(
     *     userId, FileCategory.DOCUMENT, pageRequest
     * );
     * ```
     *
     * @param userId User's UUID
     * @param category File category
     * @param pageable Pagination parameters (page, size, sort)
     * @return Page of file records with pagination metadata
     */
    Page<FileRecord> findByUserIdAndCategoryAndIsActiveTrue(
            UUID userId,
            FileRecord.FileCategory category,
            Pageable pageable
    );

    /**
     * ⭐ Find all files by user ID WITH PAGINATION
     *
     * Returns all categories for a user
     * Useful for "My Files" dashboard views
     *
     * @param userId User's UUID
     * @param pageable Pagination parameters
     * @return Page of file records across all categories
     */
    Page<FileRecord> findByUserIdAndIsActiveTrue(UUID userId, Pageable pageable);

    // ============================================================
    // FILE LOOKUP QUERIES
    // ============================================================

    /**
     * Find file by exact filename and user
     *
     * USAGE:
     * - Check for duplicate filenames before upload
     * - Retrieve specific file by known filename
     *
     * @param fileName Exact filename (case-sensitive)
     * @param userId User's UUID
     * @return Optional file record (empty if not found)
     */
    Optional<FileRecord> findByFileNameAndUserIdAndIsActiveTrue(String fileName, UUID userId);

    /**
     * Find files by filename pattern (SQL LIKE wildcards)
     *
     * USAGE:
     * - Find all profile pictures: "profile_%"
     * - Find files by date: "%_20250110_%"
     * - Find by extension: "%.pdf"
     *
     * PERFORMANCE: Indexed search on fileName column
     *
     * @param pattern SQL LIKE pattern (% = any chars, _ = single char)
     * @return List of matching files
     */
    @Query("SELECT f FROM FileRecord f WHERE f.fileName LIKE :pattern AND f.isActive = true")
    List<FileRecord> findByFileNamePattern(@Param("pattern") String pattern);

    // ============================================================
    // STORAGE MANAGEMENT QUERIES
    // ============================================================

    /**
     * Find old files for cleanup/archival
     *
     * USAGE: Scheduled cleanup jobs to:
     * - Archive old files to cold storage
     * - Delete temporary files
     * - Enforce retention policies
     *
     * EXAMPLE:
     * ```
     * LocalDateTime cutoff = LocalDateTime.now().minusDays(90);
     * List<FileRecord> oldFiles = repository.findFilesOlderThan(cutoff);
     * ```
     *
     * @param cutoffDate Files uploaded before this date
     * @return List of old files
     */
    @Query("SELECT f FROM FileRecord f WHERE f.uploadTime < :cutoffDate AND f.isActive = true")
    List<FileRecord> findFilesOlderThan(@Param("cutoffDate") LocalDateTime cutoffDate);

    /**
     * Calculate total storage used by user (in bytes)
     *
     * USAGE:
     * - Enforce storage quotas
     * - Display storage usage in dashboard
     * - Generate billing reports
     *
     * PERFORMANCE: Uses indexed SUM aggregation
     *
     * @param userId User's UUID
     * @return Total bytes used (0 if no files)
     */
    @Query("SELECT COALESCE(SUM(f.fileSize), 0) FROM FileRecord f WHERE f.userId = :userId AND f.isActive = true")
    Long getTotalStorageByUser(@Param("userId") UUID userId);

    /**
     * Find files not accessed recently
     *
     * USAGE: Identify candidates for:
     * - Cold storage migration (AWS S3 Glacier)
     * - Automatic deletion after retention period
     * - User notifications (unused files)
     *
     * BUSINESS RULE: Files not accessed in 6 months → cold storage
     *
     * @param since Date threshold (e.g., 6 months ago)
     * @return List of inactive files
     */
    @Query("SELECT f FROM FileRecord f WHERE (f.lastAccessed < :since OR f.lastAccessed IS NULL) AND f.isActive = true")
    List<FileRecord> findFilesNotAccessedSince(@Param("since") LocalDateTime since);

    // ============================================================
    // EXISTENCE CHECKS
    // ============================================================

    /**
     * Check if file exists by name and user
     *
     * FASTER than loading full entity
     * Uses EXISTS query - returns as soon as match found
     *
     * USAGE: Pre-upload duplicate check
     *
     * @param fileName Filename to check
     * @param userId User's UUID
     * @return true if file exists and is active
     */
    boolean existsByFileNameAndUserIdAndIsActiveTrue(String fileName, UUID userId);

    /**
     * Count active files for user
     *
     * USAGE:
     * - Dashboard metrics ("You have 15 files")
     * - Quota enforcement
     * - Analytics
     *
     * @param userId User's UUID
     * @return Count of active files
     */
    long countByUserIdAndIsActiveTrue(UUID userId);

    /**
     * Count files by category for user
     *
     * USAGE: Category breakdown in dashboard
     * Example: "Documents: 5, Images: 12, Audio: 3"
     *
     * @param userId User's UUID
     * @param category File category
     * @return Count of files in category
     */
    long countByUserIdAndCategoryAndIsActiveTrue(UUID userId, FileRecord.FileCategory category);

    // ============================================================
    // PUBLIC FILE QUERIES
    // ============================================================

    /**
     * Find all public files by category
     *
     * USAGE:
     * - Public file galleries
     * - Learning module listings
     * - Public profile pictures
     *
     * @param category File category
     * @param pageable Pagination
     * @return Page of public files
     */
    Page<FileRecord> findByCategoryAndIsPublicTrueAndIsActiveTrue(
            FileRecord.FileCategory category,
            Pageable pageable
    );

    /**
     * Find public file by filename
     * No userId required for public files
     *
     * @param fileName Filename
     * @return Optional file record
     */
    Optional<FileRecord> findByFileNameAndIsPublicTrueAndIsActiveTrue(String fileName);
}