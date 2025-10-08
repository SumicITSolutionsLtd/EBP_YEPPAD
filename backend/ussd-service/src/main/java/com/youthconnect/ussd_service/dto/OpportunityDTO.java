package com.youthconnect.ussd_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for opportunity responses from opportunity service
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OpportunityDTO {
    private Long id;
    private String title;
    private String description;
    private String type; // GRANT, TRAINING, JOB
    private String organizationName;
    private String location;
    private String applicationDeadline;
    private String eligibilityCriteria;
    private String contactInfo;
    private boolean isActive;
    private String createdDate;

    // Constructor with essential fields
    public OpportunityDTO(String title, String type, String organizationName) {
        this.title = title;
        this.type = type;
        this.organizationName = organizationName;
    }

    // Utility method to get shortened title for USSD display
    public String getShortTitle() {
        if (title != null && title.length() > 30) {
            return title.substring(0, 27) + "...";
        }
        return title;
    }
}