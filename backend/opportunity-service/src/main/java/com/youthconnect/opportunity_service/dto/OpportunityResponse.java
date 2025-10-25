package com.youthconnect.opportunity_service.dto;

import lombok.Builder;
import lombok.Data;
import java.util.List;

/**
 * Paginated response wrapper for opportunity listings
 */
@Data
@Builder
public class OpportunityResponse {
    private List<OpportunityDTO> opportunities;
    private int totalCount;
    private int pageNumber;
    private int pageSize;
    private int totalPages;
    private boolean hasNext;
    private boolean hasPrevious;
}