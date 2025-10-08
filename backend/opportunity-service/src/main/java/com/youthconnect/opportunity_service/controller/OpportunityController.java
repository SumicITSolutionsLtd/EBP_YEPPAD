package com.youthconnect.opportunity_service.controller;

import com.youthconnect.opportunity_service.dto.OpportunityDTO;
import com.youthconnect.opportunity_service.dto.OpportunityRequest;
import com.youthconnect.opportunity_service.entity.Opportunity; // <-- THIS IS THE FIX: Import the Opportunity entity.
import com.youthconnect.opportunity_service.service.OpportunityService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;

/**
 * REST Controller for managing all Opportunity-related endpoints.
 * This class handles incoming HTTP requests and delegates the business logic
 * to the OpportunityService. It is the public API for this microservice.
 */
@RestController
@RequestMapping("/api/opportunities")
@RequiredArgsConstructor
public class OpportunityController {

    private final OpportunityService opportunityService;

    /**
     * Endpoint for an authorized user (e.g., NGO) to create a new opportunity.
     * @param request The request body containing the new opportunity's details.
     * @return The created opportunity as a DTO with a 201 Created status.
     */
    @PostMapping
    public ResponseEntity<OpportunityDTO> createOpportunity(@RequestBody OpportunityRequest request) {
        OpportunityDTO createdOpportunity = opportunityService.createOpportunity(request);
        return new ResponseEntity<>(createdOpportunity, HttpStatus.CREATED);
    }

    /**
     * Endpoint to get a list of all opportunities. Can be filtered by type.
     * Example: GET /api/opportunities?type=JOB
     * @param type An optional request parameter to filter by opportunity type.
     * @return A list of OpportunityDTOs.
     */
    @GetMapping
    public ResponseEntity<List<OpportunityDTO>> getAllOpportunities(
            // The Opportunity class is now correctly imported, so this parameter will work.
            @RequestParam(required = false) Optional<Opportunity.OpportunityType> type) {
        List<OpportunityDTO> opportunities = opportunityService.getAllOpportunities(type);
        return ResponseEntity.ok(opportunities);
    }

    /**
     * Endpoint to update an existing opportunity.
     * @param id The ID of the opportunity to update.
     * @param request The request body with the updated details.
     * @return The updated opportunity DTO.
     */
    @PutMapping("/{id}")
    public ResponseEntity<OpportunityDTO> updateOpportunity(
            @PathVariable Long id, @RequestBody OpportunityRequest request) {
        OpportunityDTO updatedOpportunity = opportunityService.updateOpportunity(id, request);
        return ResponseEntity.ok(updatedOpportunity);
    }

    /**
     * Endpoint to delete an opportunity.
     * @param id The ID of the opportunity to delete.
     * @return A No Content (204) response on successful deletion.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOpportunity(@PathVariable Long id) {
        opportunityService.deleteOpportunity(id);
        return ResponseEntity.noContent().build();
    }
}