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
public class MentorRecommendation {
    private Long mentorId;
    private String mentorName;
    private double compatibilityScore;
    private List<String> expertise;
    private Integer experience;
    private List<String> matchingReasons;
    private String location;
    private String availabilityStatus;
}
