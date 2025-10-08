package com.youthconnect.ai.service.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

/**
 * Mentor data model for AI processing
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MentorData {
    private Long id;
    private String firstName;
    private String lastName;
    private List<String> expertiseAreas;
    private Integer experienceYears;
    private String location;
    private String availabilityStatus;
}
