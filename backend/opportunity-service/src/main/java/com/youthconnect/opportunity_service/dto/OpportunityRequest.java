package com.youthconnect.opportunity_service.dto;

import com.youthconnect.opportunity_service.entity.Opportunity;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * DTO for creating a new Opportunity.
 */
@Data
public class OpportunityRequest {
    private Long postedById;
    private Opportunity.OpportunityType opportunityType;
    private String title;
    private String description;
    private LocalDateTime applicationDeadline;
}