package com.youthconnect.ai.service.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * User Profile Data Model for AI Recommendation Processing
 *
 * This model represents the complete user profile with all fields
 * required by the recommendation algorithms.
 *
 * @author Douglas Kings Kato
 * @version 1.1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileData {

    /**
     * Unique user identifier
     */
    private Long userId;

    /**
     * User role in the system
     * Values: YOUTH, NGO, FUNDER, SERVICE_PROVIDER, MENTOR, ADMIN
     */
    private String role;

    /**
     * User's areas of interest for opportunity matching
     * Example: ["Agriculture", "Technology", "Business Development"]
     */
    @Builder.Default
    private List<String> interests = new ArrayList<>();

    /**
     * User's current skill level
     * Values: "Beginner", "Intermediate", "Advanced", "Expert"
     */
    private String skillLevel;

    /**
     * User's geographic location (district in Uganda)
     * Example: "Nebbi", "Kampala", "Madi Okollo"
     */
    private String location;

    /**
     * Age group classification
     * Example: "16-20", "21-25", "26-30", "31-35"
     */
    private String ageGroup;

    /**
     * Business stage for youth entrepreneurs
     * Values: "Idea Phase", "Early Stage", "Growth Stage", "Established"
     */
    private String businessStage;

    /**
     * Profile completion percentage (0.0 - 1.0)
     * Used to assess data quality for recommendations
     */
    @Builder.Default
    private double profileCompleteness = 0.0;

    /**
     * User's preferred language for content
     * Values: "EN" (English), "LG" (Luganda), "LUR" (Alur), "LGB" (Lugbara)
     *
     * FIXED: Added missing field used in ContentRecommendation scoring
     */
    @Builder.Default
    private String preferredLanguage = "EN";

    /**
     * User's profession or occupation
     * Example: "Student", "Farmer", "Entrepreneur", "Unemployed"
     */
    private String profession;

    /**
     * Academic qualifications
     * Example: "Secondary Education", "University Degree", "Vocational Training"
     */
    private String academicQualification;

    /**
     * Whether user has any disability
     * Used for ensuring inclusive recommendations
     */
    @Builder.Default
    private boolean hasDisability = false;

    /**
     * User's gender
     * Values: "MALE", "FEMALE", "OTHER"
     */
    private String gender;

    /**
     * Employment status
     * Values: "EMPLOYED", "UNEMPLOYED", "STUDENT", "ENTREPRENEUR"
     */
    private String employmentStatus;

    /**
     * Get interest count for validation
     *
     * @return number of interests defined
     */
    public int getInterestCount() {
        return interests != null ? interests.size() : 0;
    }

    /**
     * Check if profile has minimum data for recommendations
     *
     * @return true if profile is usable for AI processing
     */
    public boolean hasMinimumData() {
        return userId != null
                && role != null
                && getInterestCount() > 0
                && location != null;
    }

    /**
     * Get profile quality score for weighting recommendations
     * Higher scores indicate more reliable matching
     *
     * @return quality score (0.0 - 1.0)
     */
    public double getProfileQualityScore() {
        double score = 0.0;

        // Basic fields (40% weight)
        if (interests != null && !interests.isEmpty()) score += 0.15;
        if (skillLevel != null) score += 0.10;
        if (location != null) score += 0.10;
        if (businessStage != null) score += 0.05;

        // Extended fields (30% weight)
        if (profession != null) score += 0.10;
        if (ageGroup != null) score += 0.10;
        if (academicQualification != null) score += 0.10;

        // Profile completeness (30% weight)
        score += profileCompleteness * 0.3;

        return Math.min(score, 1.0);
    }
}