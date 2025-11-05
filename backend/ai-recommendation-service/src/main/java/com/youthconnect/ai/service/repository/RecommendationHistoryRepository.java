package com.youthconnect.ai.service.repository;

import com.youthconnect.ai.service.entity.RecommendationHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for recommendation history tracking
 * Supports analytics and ML model improvement
 *
 * @author Douglas Kings Kato
 * @version 1.0.0
 */
@Repository
public interface RecommendationHistoryRepository extends JpaRepository<RecommendationHistory, Long> {

    /**
     * Find all recommendations for a specific user
     */
    List<RecommendationHistory> findByUserId(Long userId);

    /**
     * Find recommendations by type and user
     */
    List<RecommendationHistory> findByUserIdAndRecommendationType(
            Long userId,
            String recommendationType
    );

    /**
     * Find recommendations that were clicked but not applied
     * Useful for understanding drop-off points
     */
    @Query("SELECT r FROM RecommendationHistory r " +
            "WHERE r.userId = :userId " +
            "AND r.wasClicked = true " +
            "AND r.wasApplied = false")
    List<RecommendationHistory> findClickedButNotApplied(@Param("userId") Long userId);

    /**
     * Calculate click-through rate for a specific algorithm
     */
    @Query("SELECT " +
            "COUNT(CASE WHEN r.wasClicked = true THEN 1 END) * 1.0 / COUNT(r) " +
            "FROM RecommendationHistory r " +
            "WHERE r.algorithmName = :algorithmName " +
            "AND r.createdAt >= :since")
    Double calculateClickThroughRate(
            @Param("algorithmName") String algorithmName,
            @Param("since") LocalDateTime since
    );

    /**
     * Calculate conversion rate (application rate) for an algorithm
     */
    @Query("SELECT " +
            "COUNT(CASE WHEN r.wasApplied = true THEN 1 END) * 1.0 / COUNT(r) " +
            "FROM RecommendationHistory r " +
            "WHERE r.algorithmName = :algorithmName " +
            "AND r.createdAt >= :since")
    Double calculateConversionRate(
            @Param("algorithmName") String algorithmName,
            @Param("since") LocalDateTime since
    );

    /**
     * Find recent recommendations for a user (for avoiding duplicates)
     */
    @Query("SELECT r FROM RecommendationHistory r " +
            "WHERE r.userId = :userId " +
            "AND r.recommendationType = :type " +
            "AND r.createdAt >= :since " +
            "ORDER BY r.createdAt DESC")
    List<RecommendationHistory> findRecentRecommendations(
            @Param("userId") Long userId,
            @Param("type") String type,
            @Param("since") LocalDateTime since
    );

    /**
     * Get average user engagement time per recommendation type
     */
    @Query("SELECT r.recommendationType, AVG(r.timeSpentSeconds) " +
            "FROM RecommendationHistory r " +
            "WHERE r.timeSpentSeconds IS NOT NULL " +
            "GROUP BY r.recommendationType")
    List<Object[]> getAverageEngagementTime();
}