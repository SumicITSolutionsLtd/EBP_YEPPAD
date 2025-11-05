package com.youthconnect.ai.service.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Job data model for AI recommendation processing
 * Represents job opportunities from job-service
 *
 * @author Douglas Kings Kato
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobRecommendationModel {

    /**
     * Unique job identifier
     */
    private Long jobId;

    /**
     * Job title
     */
    private String jobTitle;

    /**
     * Company name
     */
    private String companyName;

    /**
     * Job type: FULL_TIME, PART_TIME, CONTRACT, INTERNSHIP
     */
    private String jobType;

    /**
     * Work mode: REMOTE, ON_SITE, HYBRID
     */
    private String workMode;

    /**
     * Job location
     */
    private String location;

    /**
     * Category ID
     */
    private Long categoryId;

    /**
     * Category name
     */
    private String categoryName;

    /**
     * Number of applications received
     */
    private Integer applicationCount;

    /**
     * Number of views
     */
    private Integer viewCount;

    /**
     * Featured job flag
     */
    private Boolean isFeatured;

    /**
     * Publication date
     */
    private LocalDateTime publishedAt;

    /**
     * Expiration date
     */
    private LocalDateTime expiresAt;

    /**
     * Required skills (extracted for matching)
     */
    private List<String> requiredSkills;

    /**
     * Experience level required: ENTRY, MID, SENIOR
     */
    private String experienceLevel;

    /**
     * Education requirement
     */
    private String educationRequirement;

    /**
     * Salary range (if provided)
     */
    private String salaryRange;
}