package com.youthconnect.mentor_service.dto.response;

import com.youthconnect.mentor_service.dto.request.ReviewRequest;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * ============================================================================
 * MENTOR PROFILE DTO
 * ============================================================================
 *
 * Data Transfer Object for mentor profile information.
 * Contains mentor details, expertise, and availability summary.
 *
 * USAGE:
 * - Display mentor profiles in search results
 * - Show mentor details on profile page
 * - Mentor matching algorithm input
 *
 * FIELDS:
 * - Basic Info: ID, name, bio
 * - Professional: Expertise areas, experience years
 * - Availability: Status and schedule
 * - Performance: Ratings, session count
 *
 * @author Douglas Kings Kato
 * @version 1.0.0
 * @since 2025-01-21
 * ============================================================================
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MentorProfileDto {

    /**
     * Mentor user ID
     */
    private Long userId;

    /**
     * Full name
     */
    private String fullName;

    /**
     * Professional biography
     */
    private String bio;

    /**
     * Areas of expertise (comma-separated or list)
     */
    private String areasOfExpertise;

    /**
     * Years of professional experience
     */
    private Integer experienceYears;

    /**
     * Availability status (AVAILABLE, BUSY, ON_LEAVE)
     */
    private String availabilityStatus;

    /**
     * Average rating (1-5 stars)
     */
    private Double averageRating;

    /**
     * Total number of sessions conducted
     */
    private Integer totalSessions;

    /**
     * Total number of reviews received
     */
    private Integer totalReviews;

    /**
     * Profile picture URL
     */
    private String profilePictureUrl;

    /**
     * Whether mentor is verified
     */
    private Boolean isVerified;

    /**
     * Weekly availability schedule (optional)
     */
    private List<AvailabilityDto> weeklySchedule;

    /**
     * Recent reviews (optional, limited to top 5)
     */
    private List<ReviewRequest> recentReviews;
}