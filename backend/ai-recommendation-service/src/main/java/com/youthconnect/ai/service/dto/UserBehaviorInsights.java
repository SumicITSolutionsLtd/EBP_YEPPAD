package com.youthconnect.ai.service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserBehaviorInsights {
    private Long userId;
    private List<Integer> mostActiveHours;
    private List<String> preferredContent;
    private String engagementLevel;
    private List<String> topInterests;
    private double applicationSuccessRate;
    private List<String> recommendedActions;
}
