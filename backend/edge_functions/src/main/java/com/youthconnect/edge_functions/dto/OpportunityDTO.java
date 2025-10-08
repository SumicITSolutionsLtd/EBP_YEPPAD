package com.youthconnect.edge_functions.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class OpportunityDTO {
    private Long opportunityId;
    private Long postedById;
    private String title;
    private String description;
    private String location;
    private BigDecimal salaryMin;
    private String opportunityType;
    private String status;
    private LocalDateTime applicationDeadline;
    private LocalDateTime createdAt;
    private String contactInfo;
    private String contactMethod;
}