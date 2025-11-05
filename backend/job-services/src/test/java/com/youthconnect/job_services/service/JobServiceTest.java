package com.youthconnect.job_services.service;

import com.youthconnect.job_services.client.*;
import com.youthconnect.job_services.dto.request.CreateJobRequest;
import com.youthconnect.job_services.dto.response.JobDetailResponse;
import com.youthconnect.job_services.entity.Job;
import com.youthconnect.job_services.entity.JobCategory;
import com.youthconnect.job_services.enums.*;
import com.youthconnect.job_services.exception.*;
import com.youthconnect.job_services.mapper.JobMapper;
import com.youthconnect.job_services.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Job Service Unit Tests - FULLY FIXED with UUID
 *
 * ✅ ALL ISSUES RESOLVED:
 * - All Long IDs changed to UUID
 * - Repository methods updated for UUID
 * - Test data uses UUID
 * - Mock configurations fixed
 *
 * Tests core business logic for job management.
 *
 * @author Douglas Kings Kato
 * @version 3.0.0 (UUID Migration - ALL ISSUES FIXED)
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Job Service Tests - UUID Version")
class JobServiceTest {

    @Mock
    private JobRepository jobRepository;

    @Mock
    private JobApplicationRepository applicationRepository;

    @Mock
    private JobCategoryRepository categoryRepository;

    @Mock
    private JobMapper jobMapper;

    @Mock
    private UserServiceClient userServiceClient;

    @Mock
    private NotificationServiceClient notificationClient;

    @Mock
    private AIRecommendationClient aiRecommendationClient;

    @InjectMocks
    private JobServiceImpl jobService;

    private CreateJobRequest validJobRequest;
    private Job testJob;
    private JobCategory testCategory;

    // ✅ FIXED: Test UUIDs
    private UUID testCategoryId;
    private UUID testJobId;
    private UUID testUserId;

    @BeforeEach
    void setUp() {
        // ✅ FIXED: Generate test UUIDs
        testCategoryId = UUID.randomUUID();
        testJobId = UUID.randomUUID();
        testUserId = UUID.randomUUID();

        // Setup test category with UUID
        testCategory = JobCategory.builder()
                .categoryId(testCategoryId)  // ✅ FIXED: UUID
                .categoryName("Technology & IT")
                .isActive(true)
                .build();

        // Setup test job request with UUID
        validJobRequest = new CreateJobRequest();
        validJobRequest.setJobTitle("Software Developer - Java");
        validJobRequest.setCompanyName("Tech Solutions Ltd");
        validJobRequest.setJobDescription(
                "We are looking for an experienced Java developer to join our team. " +
                        "The ideal candidate will have strong problem-solving skills and " +
                        "experience with Spring Boot."
        );
        validJobRequest.setJobType(JobType.FULL_TIME);
        validJobRequest.setWorkMode(WorkMode.REMOTE);
        validJobRequest.setCategoryId(testCategoryId);  // ✅ FIXED: UUID
        validJobRequest.setExpiresAt(LocalDateTime.now().plusDays(30));
        validJobRequest.setSalaryMin(new BigDecimal("3000000"));
        validJobRequest.setSalaryMax(new BigDecimal("5000000"));
        validJobRequest.setApplicationEmail("jobs@techsolutions.com");

        // Setup test job entity with UUID
        testJob = Job.builder()
                .jobId(testJobId)  // ✅ FIXED: UUID
                .jobTitle(validJobRequest.getJobTitle())
                .companyName(validJobRequest.getCompanyName())
                .postedByUserId(testUserId)  // ✅ FIXED: UUID
                .postedByRole(UserRole.COMPANY)
                .jobDescription(validJobRequest.getJobDescription())
                .jobType(validJobRequest.getJobType())
                .workMode(validJobRequest.getWorkMode())
                .category(testCategory)
                .status(JobStatus.DRAFT)
                .expiresAt(validJobRequest.getExpiresAt())
                .build();
    }

    @Test
    @DisplayName("Should create job successfully with valid data")
    void testCreateJob_Success() {
        // Given
        String userRole = "COMPANY";

        // ✅ FIXED: Mock with UUID
        when(userServiceClient.userExists(testUserId)).thenReturn(Boolean.TRUE);
        when(categoryRepository.findByCategoryIdAndIsDeletedFalse(testCategoryId))
                .thenReturn(Optional.of(testCategory));
        when(jobMapper.toEntity(any(), eq(testUserId), eq(userRole))).thenReturn(testJob);
        when(jobRepository.save(any(Job.class))).thenReturn(testJob);
        when(jobMapper.toDetailResponse(any(), any(), any(), any()))
                .thenReturn(JobDetailResponse.builder()
                        .jobId(testJobId)  // ✅ FIXED: UUID
                        .jobTitle("Software Developer - Java")
                        .status(JobStatus.DRAFT)
                        .build());

        // When
        JobDetailResponse response = jobService.createJob(validJobRequest, testUserId, userRole);

        // Then
        assertNotNull(response);
        assertEquals(testJobId, response.getJobId());  // ✅ FIXED: UUID comparison
        assertEquals("Software Developer - Java", response.getJobTitle());
        verify(jobRepository, times(1)).save(any(Job.class));
    }

    @Test
    @DisplayName("Should throw exception when category not found")
    void testCreateJob_CategoryNotFound() {
        // Given
        String userRole = "COMPANY";

        // ✅ FIXED: Mock with UUID
        when(userServiceClient.userExists(testUserId)).thenReturn(Boolean.TRUE);
        when(categoryRepository.findByCategoryIdAndIsDeletedFalse(testCategoryId))
                .thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> {
            jobService.createJob(validJobRequest, testUserId, userRole);
        });

        verify(jobRepository, never()).save(any(Job.class));
    }

    @Test
    @DisplayName("Should throw exception when user is YOUTH role")
    void testCreateJob_UnauthorizedRole() {
        // Given
        String userRole = "YOUTH";

        // When & Then
        assertThrows(UnauthorizedAccessException.class, () -> {
            jobService.createJob(validJobRequest, testUserId, userRole);
        });

        verify(jobRepository, never()).save(any(Job.class));
    }

    @Test
    @DisplayName("Should publish job successfully")
    void testPublishJob_Success() {
        // Given
        testJob.setPostedByUserId(testUserId);

        // ✅ FIXED: Mock with UUID
        when(jobRepository.findByJobIdAndIsDeletedFalse(testJobId))
                .thenReturn(Optional.of(testJob));
        when(jobRepository.save(any(Job.class))).thenReturn(testJob);
        when(jobMapper.toDetailResponse(any(), any(), any(), any()))
                .thenReturn(JobDetailResponse.builder()
                        .jobId(testJobId)  // ✅ FIXED: UUID
                        .status(JobStatus.PUBLISHED)
                        .build());

        // When
        JobDetailResponse response = jobService.publishJob(testJobId, testUserId);

        // Then
        assertNotNull(response);
        assertEquals(JobStatus.PUBLISHED, testJob.getStatus());
        assertNotNull(testJob.getPublishedAt());
        verify(jobRepository, times(1)).save(testJob);
    }

    @Test
    @DisplayName("Should throw exception when non-owner tries to publish")
    void testPublishJob_Unauthorized() {
        // Given
        UUID differentUserId = UUID.randomUUID();  // ✅ FIXED: Different UUID
        testJob.setPostedByUserId(testUserId);

        // ✅ FIXED: Mock with UUID
        when(jobRepository.findByJobIdAndIsDeletedFalse(testJobId))
                .thenReturn(Optional.of(testJob));

        // When & Then
        assertThrows(UnauthorizedAccessException.class, () -> {
            jobService.publishJob(testJobId, differentUserId);
        });

        verify(jobRepository, never()).save(any(Job.class));
    }

    @Test
    @DisplayName("Should delete draft job successfully")
    void testDeleteJob_Success() {
        // Given
        testJob.setPostedByUserId(testUserId);
        testJob.setStatus(JobStatus.DRAFT);

        // ✅ FIXED: Mock with UUID
        when(jobRepository.findByJobIdAndIsDeletedFalse(testJobId))
                .thenReturn(Optional.of(testJob));

        // When
        jobService.deleteJob(testJobId, testUserId);

        // Then
        verify(jobRepository, times(1)).delete(testJob);
    }

    @Test
    @DisplayName("Should get job by ID successfully")
    void testGetJobById_Success() {
        // Given
        // ✅ FIXED: Mock with UUID
        when(jobRepository.findByJobIdAndIsDeletedFalse(testJobId))
                .thenReturn(Optional.of(testJob));
        when(applicationRepository.findByJob_JobIdAndApplicantUserIdAndIsDeletedFalse(testJobId, testUserId))
                .thenReturn(Optional.empty());
        when(jobMapper.toDetailResponse(any(), eq(false), any(), any()))
                .thenReturn(JobDetailResponse.builder()
                        .jobId(testJobId)  // ✅ FIXED: UUID
                        .jobTitle("Software Developer - Java")
                        .hasApplied(false)
                        .build());

        // When
        JobDetailResponse response = jobService.getJobById(testJobId, testUserId);

        // Then
        assertNotNull(response);
        assertEquals(testJobId, response.getJobId());  // ✅ FIXED: UUID comparison
        assertEquals(Boolean.FALSE, response.getHasApplied());
    }

    @Test
    @DisplayName("Should throw exception when job not found")
    void testGetJobById_NotFound() {
        // Given
        // ✅ FIXED: Mock with UUID
        when(jobRepository.findByJobIdAndIsDeletedFalse(testJobId))
                .thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> {
            jobService.getJobById(testJobId, testUserId);
        });
    }

    @Test
    @DisplayName("Should close published job successfully")
    void testCloseJob_Success() {
        // Given
        testJob.setPostedByUserId(testUserId);
        testJob.setStatus(JobStatus.PUBLISHED);

        // ✅ FIXED: Mock with UUID
        when(jobRepository.findByJobIdAndIsDeletedFalse(testJobId))
                .thenReturn(Optional.of(testJob));
        when(jobRepository.save(any(Job.class))).thenReturn(testJob);
        when(jobMapper.toDetailResponse(any(), any(), any(), any()))
                .thenReturn(JobDetailResponse.builder()
                        .jobId(testJobId)  // ✅ FIXED: UUID
                        .status(JobStatus.CLOSED)
                        .build());

        // When
        JobDetailResponse response = jobService.closeJob(testJobId, testUserId);

        // Then
        assertNotNull(response);
        assertEquals(JobStatus.CLOSED, testJob.getStatus());
        assertNotNull(testJob.getClosedAt());
        verify(jobRepository, times(1)).save(testJob);
    }

    @Test
    @DisplayName("Should throw exception when trying to close non-published job")
    void testCloseJob_InvalidStatus() {
        // Given
        testJob.setPostedByUserId(testUserId);
        testJob.setStatus(JobStatus.DRAFT);

        // ✅ FIXED: Mock with UUID
        when(jobRepository.findByJobIdAndIsDeletedFalse(testJobId))
                .thenReturn(Optional.of(testJob));

        // When & Then
        assertThrows(IllegalStateException.class, () -> {
            jobService.closeJob(testJobId, testUserId);
        });

        verify(jobRepository, never()).save(any(Job.class));
    }

    @Test
    @DisplayName("Should throw exception when trying to delete published job")
    void testDeleteJob_InvalidStatus() {
        // Given
        testJob.setPostedByUserId(testUserId);
        testJob.setStatus(JobStatus.PUBLISHED);

        // ✅ FIXED: Mock with UUID
        when(jobRepository.findByJobIdAndIsDeletedFalse(testJobId))
                .thenReturn(Optional.of(testJob));

        // When & Then
        assertThrows(IllegalStateException.class, () -> {
            jobService.deleteJob(testJobId, testUserId);
        });

        verify(jobRepository, never()).delete(any(Job.class));
    }
}