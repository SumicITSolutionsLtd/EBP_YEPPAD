package com.youthconnect.mentor_service.service;

import com.youthconnect.mentor_service.dto.request.AvailabilityRequest;
import com.youthconnect.mentor_service.dto.response.AvailabilityDto;
import com.youthconnect.mentor_service.entity.MentorAvailability;
import com.youthconnect.mentor_service.exception.ValidationException;
import com.youthconnect.mentor_service.repository.MentorAvailabilityRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * ============================================================================
 * AVAILABILITY SERVICE
 * ============================================================================
 *
 * Business logic for mentor availability management.
 *
 * @author Douglas Kings Kato
 * @version 1.0.0
 * @since 2025-01-22
 * ============================================================================
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AvailabilityService {

    private final MentorAvailabilityRepository availabilityRepository;

    /**
     * Get mentor's availability schedule
     */
    @Cacheable(value = "availability", key = "#mentorId")
    @Transactional(readOnly = true)
    public List<AvailabilityDto> getMentorAvailability(Long mentorId) {
        log.debug("Fetching availability for mentor: {}", mentorId);

        List<MentorAvailability> availability =
                availabilityRepository.findByMentorIdAndIsActiveTrue(mentorId);

        return availability.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    /**
     * Set mentor availability schedule
     */
    @CacheEvict(value = "availability", key = "#mentorId")
    public List<AvailabilityDto> setAvailability(
            Long mentorId,
            List<AvailabilityRequest> requests
    ) {
        log.info("Setting availability for mentor: {}", mentorId);

        // Validate requests
        validateAvailabilityRequests(requests);

        // Delete existing availability
        availabilityRepository.deleteByMentorId(mentorId);

        // Create new availability slots
        List<MentorAvailability> availability = requests.stream()
                .map(request -> buildAvailability(mentorId, request))
                .collect(Collectors.toList());

        List<MentorAvailability> saved = availabilityRepository.saveAll(availability);

        return saved.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    /**
     * Update availability slot
     */
    @CacheEvict(value = "availability", allEntries = true)
    public AvailabilityDto updateAvailability(
            Long availabilityId,
            AvailabilityRequest request,
            Long userId
    ) {
        log.info("Updating availability: {}", availabilityId);

        MentorAvailability availability = availabilityRepository
                .findById(availabilityId)
                .orElseThrow(() -> new ValidationException("Availability slot not found"));

        // Verify ownership
        if (!availability.getMentorId().equals(userId)) {
            throw new SecurityException("Not authorized to update this availability");
        }

        // Validate times
        if (request.getStartTime().isAfter(request.getEndTime()) ||
                request.getStartTime().equals(request.getEndTime())) {
            throw new ValidationException("Start time must be before end time");
        }

        // Update fields
        availability.setDayOfWeek(request.getDayOfWeek());
        availability.setStartTime(request.getStartTime());
        availability.setEndTime(request.getEndTime());
        availability.setIsActive(request.getIsActive());

        MentorAvailability updated = availabilityRepository.save(availability);

        return mapToDto(updated);
    }

    /**
     * Delete availability slot
     */
    @CacheEvict(value = "availability", allEntries = true)
    public void deleteAvailability(Long availabilityId, Long userId) {
        log.info("Deleting availability: {}", availabilityId);

        MentorAvailability availability = availabilityRepository
                .findById(availabilityId)
                .orElseThrow(() -> new ValidationException("Availability slot not found"));

        // Verify ownership
        if (!availability.getMentorId().equals(userId)) {
            throw new SecurityException("Not authorized to delete this availability");
        }

        availabilityRepository.delete(availability);
    }

    /**
     * Validate availability requests
     */
    private void validateAvailabilityRequests(List<AvailabilityRequest> requests) {
        for (AvailabilityRequest request : requests) {
            // Validate start < end
            if (request.getStartTime().isAfter(request.getEndTime()) ||
                    request.getStartTime().equals(request.getEndTime())) {
                throw new ValidationException("Start time must be before end time");
            }

            // Validate reasonable hours (6 AM - 11 PM)
            if (request.getStartTime().getHour() < 6 || request.getEndTime().getHour() > 23) {
                throw new ValidationException("Availability must be between 6 AM and 11 PM");
            }
        }
    }

    /**
     * Build availability entity from request
     */
    private MentorAvailability buildAvailability(Long mentorId, AvailabilityRequest request) {
        return MentorAvailability.builder()
                .mentorId(mentorId)
                .dayOfWeek(request.getDayOfWeek())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .build();
    }

    /**
     * Map entity to DTO
     */
    private AvailabilityDto mapToDto(MentorAvailability availability) {
        return AvailabilityDto.builder()
                .availabilityId(availability.getAvailabilityId())
                .mentorId(availability.getMentorId())
                .dayOfWeek(availability.getDayOfWeek())
                .startTime(availability.getStartTime())
                .endTime(availability.getEndTime())
                .isActive(availability.getIsActive())
                .build();
    }
}