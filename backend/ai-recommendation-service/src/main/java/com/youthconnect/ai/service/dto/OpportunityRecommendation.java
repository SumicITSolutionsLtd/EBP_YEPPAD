package com.youthconnect.ai.service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO representing an AI-recommended opportunity for a user
 * Includes match score and reasoning for transparency
 *
 * @author Douglas Kings Kato
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OpportunityRecommendation {

    /**
     * Unique identifier of the opportunity
     */
    private Long opportunityId;

    /**
     * Title of the opportunity
     */
    private String title;

    /**
     * Company or organization offering the opportunity (for jobs)
     */
    private String companyName;

    /**
     * Type: GRANT, LOAN, JOB, TRAINING, SKILL_MARKET
     */
    private String type;

    /**
     * AI-computed match score (0.0 - 1.0)
     * Higher scores indicate better fit for user's profile
     */
    private Double matchScore;

    /**
     * Human-readable explanation for the recommendation
     * Example: "Matches your interest in agriculture and location in Nebbi"
     */
    private String reason;

    /**
     * Tags/keywords associated with the opportunity
     */
    private List<String> tags;

    /**
     * Application deadline (if applicable)
     */
    private LocalDateTime deadline;

    /**
     * Funding amount (for grants/loans) in UGX
     */
    private Long fundingAmount;

    /**
     * Currency code (default: UGX)
     */
    @Builder.Default
    private String currency = "UGX";
}