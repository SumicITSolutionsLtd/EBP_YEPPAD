package com.youthconnect.ai.service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContentRecommendation {
    private Long contentId;
    private String title;
    private String type;
    private double score;
    private String reason;
    private String language;
    private Integer duration;
    private String difficulty;
}
