package com.youthconnect.ai.service.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents an opportunity (grant, loan, job, training, or skill market)
 * used for AI recommendation and matching.
 *
 * <p>This model aggregates data from various sources such as
 * grants, loans, jobs, and training programs for use in AI-based
 * opportunity matching and recommendation.</p>
 *
 * <p>Examples of opportunity types include:
 * <ul>
 *     <li>GRANT</li>
 *     <li>LOAN</li>
 *     <li>JOB</li>
 *     <li>TRAINING</li>
 *     <li>SKILL_MARKET</li>
 * </ul></p>
 *
 * @author
 *     Douglas Kings Kato
 * @version
 *     1.1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OpportunityData {

    /** Unique identifier for the opportunity */
    private Long id;

    /** Opportunity title */
    private String title;

    /** Type of opportunity: GRANT, LOAN, JOB, TRAINING, SKILL_MARKET */
    private String type;

    /** Detailed description of the opportunity */
    private String description;

    /**
     * Tags or keywords associated with this opportunity.
     * <p>Examples: ["Agriculture", "Youth", "Funding"]</p>
     */
    @Builder.Default
    private List<String> tags = new ArrayList<>();

    /** Location or district for the opportunity */
    private String location;

    /** Deadline for applications */
    private LocalDateTime deadline;

    /** Funding amount (for grants or loans) in UGX */
    private Long fundingAmount;

    /** Name of the organization or entity posting the opportunity */
    private String postedBy;

    /** Number of applications received for this opportunity */
    @Builder.Default
    private Integer applicationCount = 0;

    /** Number of views recorded for this opportunity */
    @Builder.Default
    private Integer viewCount = 0;

    /** Indicates if the opportunity is featured or highlighted */
    @Builder.Default
    private Boolean isFeatured = false;

    /** Required skills (for jobs or training opportunities) */
    @Builder.Default
    private List<String> requiredSkills = new ArrayList<>();

    /** Target age group for applicants (e.g., "18-35") */
    private String targetAgeGroup;

    /** Experience level required (e.g., "Beginner", "Intermediate", "Advanced", "Expert") */
    private String experienceLevel;

    /** Date the opportunity was published */
    private LocalDateTime publishedAt;

    /** Current status of the opportunity (e.g., "OPEN", "CLOSED", "PENDING") */
    private String status;
}
