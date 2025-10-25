package com.youthconnect.mentor_service.controller;

import com.youthconnect.mentor_service.dto.request.AvailabilityRequest;
import com.youthconnect.mentor_service.dto.response.AvailabilityDto;
import com.youthconnect.mentor_service.entity.MentorAvailability;
import com.youthconnect.mentor_service.service.AvailabilityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * ============================================================================
 * AVAILABILITY CONTROLLER
 * ============================================================================
 *
 * REST controller for managing mentor availability schedules.
 *
 * @author Douglas Kings Kato
 * @version 1.0.0
 * @since 2025-01-22
 * ============================================================================
 */
@RestController
@RequestMapping("/api/mentors")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Mentor Availability", description = "Mentor availability management endpoints")
public class AvailabilityController {

    private final AvailabilityService availabilityService;

    /**
     * Get mentor's availability schedule
     */
    @GetMapping("/{mentorId}/availability")
    @Operation(summary = "Get mentor availability",
            description = "Retrieve weekly availability schedule for a mentor")
    public ResponseEntity<List<AvailabilityDto>> getMentorAvailability(
            @PathVariable Long mentorId
    ) {
        log.info("Fetching availability for mentor: {}", mentorId);
        List<AvailabilityDto> availability = availabilityService.getMentorAvailability(mentorId);
        return ResponseEntity.ok(availability);
    }

    /**
     * Set mentor availability (mentor only)
     */
    @PostMapping("/{mentorId}/availability")
    @PreAuthorize("hasRole('MENTOR') and #mentorId == authentication.principal.id")
    @Operation(summary = "Set mentor availability",
            description = "Set weekly availability schedule (mentor only)")
    public ResponseEntity<List<AvailabilityDto>> setAvailability(
            @PathVariable Long mentorId,
            @Valid @RequestBody List<AvailabilityRequest> availabilityRequests
    ) {
        log.info("Setting availability for mentor: {}", mentorId);
        List<AvailabilityDto> created = availabilityService.setAvailability(mentorId, availabilityRequests);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    /**
     * Update availability slot
     */
    @PutMapping("/availability/{availabilityId}")
    @PreAuthorize("hasRole('MENTOR')")
    @Operation(summary = "Update availability slot")
    public ResponseEntity<AvailabilityDto> updateAvailability(
            @PathVariable Long availabilityId,
            @Valid @RequestBody AvailabilityRequest request,
            @RequestHeader("X-User-Id") Long userId
    ) {
        log.info("Updating availability: {}", availabilityId);
        AvailabilityDto updated = availabilityService.updateAvailability(availabilityId, request, userId);
        return ResponseEntity.ok(updated);
    }

    /**
     * Delete availability slot
     */
    @DeleteMapping("/availability/{availabilityId}")
    @PreAuthorize("hasRole('MENTOR')")
    @Operation(summary = "Delete availability slot")
    public ResponseEntity<Void> deleteAvailability(
            @PathVariable Long availabilityId,
            @RequestHeader("X-User-Id") Long userId
    ) {
        log.info("Deleting availability: {}", availabilityId);
        availabilityService.deleteAvailability(availabilityId, userId);
        return ResponseEntity.noContent().build();
    }
}