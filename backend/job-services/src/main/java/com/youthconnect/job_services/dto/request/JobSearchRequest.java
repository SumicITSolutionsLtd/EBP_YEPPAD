package com.youthconnect.job_services.dto.request;

import com.youthconnect.job_services.enums.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Job Search Request DTO - Updated with UUID
 *
 * Supports advanced filtering and search for jobs.
 * Uses UUID for category filtering.
 *
 * @author Douglas Kings Kato
 * @version 2.0.0
 */
@Data
public class JobSearchRequest {

    // Keyword search across title, company, description
    private String keyword;

    // Job classification filters
    private JobType jobType;
    private WorkMode workMode;

    /**
     * Category ID using UUID
     */
    private UUID categoryId;

    // Location filters
    private String location;

    // Salary filters
    private BigDecimal minSalary;
    private BigDecimal maxSalary;

    // Experience and education filters
    private String experienceRequired;
    private EducationLevel educationLevel;

    // Date filters
    private Integer postedWithinDays; // Jobs posted within last N days
    private LocalDateTime deadlineAfter; // Jobs with deadline after this date

    // Special filters
    private Boolean featuredOnly = false;
    private Boolean urgentOnly = false;
    private Boolean remoteOnly = false;

    // Sorting
    private String sortBy = "publishedAt"; // Options: publishedAt, salary, deadline, relevance
    private String sortDirection = "DESC"; // ASC or DESC
}