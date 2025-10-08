package com.youthconnect.file.service.repository;

import com.youthconnect.file.service.entity.FileRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for file management operations
 */
@Repository
public interface FileRecordRepository extends JpaRepository<FileRecord, Long> {

    /**
     * Find files by user ID and category
     */
    List<FileRecord> findByUserIdAndCategoryAndIsActiveTrue(Long userId, FileRecord.FileCategory category);

    /**
     * Find file by access token for secure downloads
     */
    Optional<FileRecord> findByAccessTokenAndIsActiveTrue(String accessToken);

    /**
     * Find files by filename pattern (for system files)
     */
    @Query("SELECT f FROM FileRecord f WHERE f.fileName LIKE :pattern AND f.isActive = true")
    List<FileRecord> findByFileNamePattern(@Param("pattern") String pattern);

    /**
     * Find old files for cleanup
     */
    @Query("SELECT f FROM FileRecord f WHERE f.uploadTime < :cutoffDate AND f.isActive = true")
    List<FileRecord> findFilesOlderThan(@Param("cutoffDate") LocalDateTime cutoffDate);

    /**
     * Calculate total storage used by user
     */
    @Query("SELECT COALESCE(SUM(f.fileSize), 0) FROM FileRecord f WHERE f.userId = :userId AND f.isActive = true")
    Long getTotalStorageByUser(@Param("userId") Long userId);

    /**
     * Find files not accessed recently
     */
    @Query("SELECT f FROM FileRecord f WHERE f.lastAccessed < :since OR f.lastAccessed IS NULL")
    List<FileRecord> findFilesNotAccessedSince(@Param("since") LocalDateTime since);
}
