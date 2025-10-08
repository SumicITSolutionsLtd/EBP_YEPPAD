package com.youthconnect.ai.service.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

/**
 * User profile data model for AI processing
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileData {
    private Long userId;
    private String role;
    private List<String> interests;
    private String skillLevel;
    private String location;
    private String ageGroup;
    private String businessStage;
    private double profileCompleteness;
}
