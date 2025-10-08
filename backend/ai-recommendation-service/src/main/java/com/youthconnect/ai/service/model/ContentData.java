package com.youthconnect.ai.service.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

/**
 * Learning content data model for AI processing
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContentData {
    private Long id;
    private String title;
    private String type;
    private String language;
    private String difficulty;
    private Integer duration;
    private List<String> tags;
}