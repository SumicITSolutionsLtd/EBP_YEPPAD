package com.youthconnect.content_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for user basic information from User Service
 * Used to enrich content with author details
 *
 * @author Douglas Kings Kato
 * @version 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserBasicInfoDto {
    /**
     * User's unique identifier
     */
    private Long userId;

    /**
     * Display name shown with content
     * Usually first name + last name for Youth users
     * Organization name for NGO/Funder users
     */
    private String displayName;

    /**
     * URL to user's profile picture
     * Null if user hasn't uploaded a profile picture
     */
    private String profilePictureUrl;

    /**
     * User's role in the platform
     * Used to display role badges (e.g., "NGO", "Mentor")
     */
    private String role;

    /**
     * User reputation score (optional)
     * Calculated based on helpful contributions
     * Higher reputation indicates trusted community member
     */
    private Integer reputationScore;
}