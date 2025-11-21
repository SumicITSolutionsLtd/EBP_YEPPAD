package com.youthconnect.ai.service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/*
 * @author Douglas Kings Kato
 * @version 1.0.0
 **/
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
