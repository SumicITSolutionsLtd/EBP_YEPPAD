package com.youthconnect.ai.service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OpportunityRecommendation {
    private Long opportunityId;
    private String title;
    private String type;
    private double score;
    private String reason;
    private List<String> tags;
    private LocalDateTime deadline;
    private Long fundingAmount;
}
