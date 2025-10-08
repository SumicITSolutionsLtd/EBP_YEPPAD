package com.youthconnect.ai.service.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

/**
 * User behavior data model for AI processing
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserBehaviorData {
    private Long userId;
    private int sessionCount;
    private double averageSessionDuration;
    private List<Integer> mostActiveHours;
    private List<String> preferredContentTypes;
    private int totalInteractions;
}
