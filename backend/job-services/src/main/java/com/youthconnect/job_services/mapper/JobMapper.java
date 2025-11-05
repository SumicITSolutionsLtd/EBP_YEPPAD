package com.youthconnect.job_services.mapper;

import com.youthconnect.job_services.dto.request.CreateJobRequest;
import com.youthconnect.job_services.dto.request.UpdateJobRequest;
import com.youthconnect.job_services.dto.response.*;
import com.youthconnect.job_services.entity.*;
import com.youthconnect.job_services.enums.JobStatus;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Job Mapper - FIXED with UUID Support
 *
 * Handles conversions between entities and DTOs.
 * All ID fields updated to UUID.
 *
 * @author Douglas Kings Kato
 * @version 2.0.0
 */
@Component
public class JobMapper {

    /**
     * Convert CreateJobRequest to Job entity
     * FIXED: All IDs are now UUID
     */
    public Job toEntity(CreateJobRequest request, UUID postedByUserId, String postedByRole) {
        return Job.builder()
                .jobTitle(request.getJobTitle())
                .companyName(request.getCompanyName())
                .postedByUserId(postedByUserId)  // UUID
                .postedByRole(com.youthconnect.job_services.enums.UserRole.valueOf(postedByRole))
                .jobDescription(request.getJobDescription())
                .responsibilities(request.getResponsibilities())
                .requirements(request.getRequirements())
                .jobType(request.getJobType())
                .workMode(request.getWorkMode())
                .location(request.getLocation())
                .salaryMin(request.getSalaryMin())
                .salaryMax(request.getSalaryMax())
                .salaryCurrency(request.getSalaryCurrency())
                .salaryPeriod(request.getSalaryPeriod())
                .showSalary(request.getShowSalary())
                .experienceRequired(request.getExperienceRequired())
                .educationLevel(request.getEducationLevel())
                .applicationEmail(request.getApplicationEmail())
                .applicationPhone(request.getApplicationPhone())
                .applicationUrl(request.getApplicationUrl())
                .howToApply(request.getHowToApply())
                .status(JobStatus.DRAFT)
                .expiresAt(request.getExpiresAt())
                .maxApplications(request.getMaxApplications())
                .isFeatured(request.getIsFeatured())
                .isUrgent(request.getIsUrgent())
                .applicationCount(0)
                .viewCount(0)
                .build();
    }

    /**
     * Update Job entity from UpdateJobRequest
     * FIXED: Handles UUID for categoryId
     */
    public void updateEntity(Job job, UpdateJobRequest request) {
        if (request.getJobTitle() != null) {
            job.setJobTitle(request.getJobTitle());
        }
        if (request.getCompanyName() != null) {
            job.setCompanyName(request.getCompanyName());
        }
        if (request.getJobDescription() != null) {
            job.setJobDescription(request.getJobDescription());
        }
        if (request.getResponsibilities() != null) {
            job.setResponsibilities(request.getResponsibilities());
        }
        if (request.getRequirements() != null) {
            job.setRequirements(request.getRequirements());
        }
        if (request.getJobType() != null) {
            job.setJobType(request.getJobType());
        }
        if (request.getWorkMode() != null) {
            job.setWorkMode(request.getWorkMode());
        }
        if (request.getLocation() != null) {
            job.setLocation(request.getLocation());
        }
        if (request.getSalaryMin() != null) {
            job.setSalaryMin(request.getSalaryMin());
        }
        if (request.getSalaryMax() != null) {
            job.setSalaryMax(request.getSalaryMax());
        }
        if (request.getSalaryCurrency() != null) {
            job.setSalaryCurrency(request.getSalaryCurrency());
        }
        if (request.getSalaryPeriod() != null) {
            job.setSalaryPeriod(request.getSalaryPeriod());
        }
        if (request.getShowSalary() != null) {
            job.setShowSalary(request.getShowSalary());
        }
        if (request.getExperienceRequired() != null) {
            job.setExperienceRequired(request.getExperienceRequired());
        }
        if (request.getEducationLevel() != null) {
            job.setEducationLevel(request.getEducationLevel());
        }
        if (request.getApplicationEmail() != null) {
            job.setApplicationEmail(request.getApplicationEmail());
        }
        if (request.getApplicationPhone() != null) {
            job.setApplicationPhone(request.getApplicationPhone());
        }
        if (request.getApplicationUrl() != null) {
            job.setApplicationUrl(request.getApplicationUrl());
        }
        if (request.getHowToApply() != null) {
            job.setHowToApply(request.getHowToApply());
        }
        if (request.getExpiresAt() != null) {
            job.setExpiresAt(request.getExpiresAt());
        }
        if (request.getMaxApplications() != null) {
            job.setMaxApplications(request.getMaxApplications());
        }
        if (request.getIsFeatured() != null) {
            job.setIsFeatured(request.getIsFeatured());
        }
        if (request.getIsUrgent() != null) {
            job.setIsUrgent(request.getIsUrgent());
        }
        if (request.getStatus() != null) {
            job.setStatus(request.getStatus());
        }
    }

    /**
     * Convert Job entity to JobResponse (summary)
     * FIXED: All IDs are UUID
     */
    public JobResponse toResponse(Job job) {
        return JobResponse.builder()
                .jobId(job.getJobId())  // UUID
                .jobTitle(job.getJobTitle())
                .companyName(job.getCompanyName())
                .jobType(job.getJobType())
                .workMode(job.getWorkMode())
                .location(job.getLocation())
                .categoryId(job.getCategory() != null ? job.getCategory().getCategoryId() : null)  // UUID
                .categoryName(job.getCategory() != null ? job.getCategory().getCategoryName() : null)
                .salaryMin(job.getSalaryMin())
                .salaryMax(job.getSalaryMax())
                .salaryCurrency(job.getSalaryCurrency())
                .salaryPeriod(job.getSalaryPeriod())
                .showSalary(job.getShowSalary())
                .status(job.getStatus())
                .publishedAt(job.getPublishedAt())
                .expiresAt(job.getExpiresAt())
                .applicationCount(job.getApplicationCount())
                .viewCount(job.getViewCount())
                .isFeatured(job.getIsFeatured())
                .isUrgent(job.getIsUrgent())
                .createdAt(job.getCreatedAt())
                .updatedAt(job.getUpdatedAt())
                .build();
    }

    /**
     * Convert Job entity to JobDetailResponse (full details)
     * FIXED: All IDs are UUID
     */
    public JobDetailResponse toDetailResponse(Job job, Boolean hasApplied,
                                              UUID userApplicationId, String applicantStatus) {
        return JobDetailResponse.builder()
                .jobId(job.getJobId())  // UUID
                .jobTitle(job.getJobTitle())
                .companyName(job.getCompanyName())
                .postedByUserId(job.getPostedByUserId())  // UUID
                .postedByRole(job.getPostedByRole())
                .jobDescription(job.getJobDescription())
                .responsibilities(job.getResponsibilities())
                .requirements(job.getRequirements())
                .jobType(job.getJobType())
                .workMode(job.getWorkMode())
                .location(job.getLocation())
                .categoryId(job.getCategory() != null ? job.getCategory().getCategoryId() : null)  // UUID
                .categoryName(job.getCategory() != null ? job.getCategory().getCategoryName() : null)
                .categoryIconUrl(job.getCategory() != null ? job.getCategory().getIconUrl() : null)
                .salaryMin(job.getSalaryMin())
                .salaryMax(job.getSalaryMax())
                .salaryCurrency(job.getSalaryCurrency())
                .salaryPeriod(job.getSalaryPeriod())
                .showSalary(job.getShowSalary())
                .experienceRequired(job.getExperienceRequired())
                .educationLevel(job.getEducationLevel())
                .applicationEmail(job.getApplicationEmail())
                .applicationPhone(job.getApplicationPhone())
                .applicationUrl(job.getApplicationUrl())
                .howToApply(job.getHowToApply())
                .status(job.getStatus())
                .publishedAt(job.getPublishedAt())
                .expiresAt(job.getExpiresAt())
                .closedAt(job.getClosedAt())
                .maxApplications(job.getMaxApplications())
                .applicationCount(job.getApplicationCount())
                .viewCount(job.getViewCount())
                .hasApplied(hasApplied)
                .userApplicationId(userApplicationId)  // UUID
                .userApplicationStatus(applicantStatus != null ?
                        com.youthconnect.job_services.enums.ApplicationStatus.valueOf(applicantStatus) : null)
                .isFeatured(job.getIsFeatured())
                .isUrgent(job.getIsUrgent())
                .createdAt(job.getCreatedAt())
                .updatedAt(job.getUpdatedAt())
                .build();
    }

    /**
     * Convert JobApplication entity to ApplicationResponse
     * FIXED: All IDs are UUID
     */
    public ApplicationResponse toApplicationResponse(JobApplication application) {
        return ApplicationResponse.builder()
                .applicationId(application.getApplicationId())  // UUID
                .jobId(application.getJob().getJobId())  // UUID
                .jobTitle(application.getJob().getJobTitle())
                .companyName(application.getJob().getCompanyName())
                .applicantUserId(application.getApplicantUserId())  // UUID
                .coverLetter(application.getCoverLetter())
                .resumeFileId(application.getResumeFileId())  // UUID
                .status(application.getStatus())
                .reviewedByUserId(application.getReviewedByUserId())  // UUID
                .reviewedAt(application.getReviewedAt())
                .reviewNotes(application.getReviewNotes())
                .interviewDate(application.getInterviewDate())
                .interviewLocation(application.getInterviewLocation())
                .interviewNotes(application.getInterviewNotes())
                .submittedAt(application.getSubmittedAt())
                .updatedAt(application.getUpdatedAt())
                .build();
    }

    /**
     * Convert JobCategory entity to CategoryResponse
     * FIXED: categoryId is UUID
     */
    public JobCategoryResponse toCategoryResponse(JobCategory category, Long jobCount) {
        return JobCategoryResponse.builder()
                .categoryId(category.getCategoryId())  // UUID
                .categoryName(category.getCategoryName())
                .description(category.getDescription())
                .iconUrl(category.getIconUrl())
                .isActive(category.getIsActive())
                .displayOrder(category.getDisplayOrder())
                .jobCount(jobCount)
                .createdAt(category.getCreatedAt())
                .updatedAt(category.getUpdatedAt())
                .build();
    }

    /**
     * Batch convert jobs to responses
     */
    public List<JobResponse> toResponseList(List<Job> jobs) {
        return jobs.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
}