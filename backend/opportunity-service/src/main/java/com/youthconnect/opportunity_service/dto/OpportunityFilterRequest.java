package com.youthconnect.opportunity_service.dto;

import com.youthconnect.opportunity_service.entity.Opportunity;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * DTO for filtering and searching opportunities
 * Supports advanced search with multiple criteria
 */
@Data
public class OpportunityFilterRequest {
    private Opportunity.OpportunityType type;
    private Opportunity.Status status;
    private String searchTerm; // Search in title and description
    private Long postedById;
    private LocalDateTime deadlineAfter;
    private LocalDateTime deadlineBefore;
    private Integer page = 0;
    private Integer size = 20;
    private String sortBy = "createdAt";
    private String sortDirection = "DESC";
}