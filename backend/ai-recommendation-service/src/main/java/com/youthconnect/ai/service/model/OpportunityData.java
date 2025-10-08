package com.youthconnect.ai.service.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Opportunity data model for AI processing
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OpportunityData {
    private Long id;
    private String title;
    private String type;
    private List<String> tags;
    private String location;
    private LocalDateTime deadline;
    private Long fundingAmount;
}
