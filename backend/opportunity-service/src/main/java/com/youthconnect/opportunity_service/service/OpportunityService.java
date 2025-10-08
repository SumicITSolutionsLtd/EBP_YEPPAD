package com.youthconnect.opportunity_service.service;

import com.youthconnect.opportunity_service.dto.OpportunityDTO;
import com.youthconnect.opportunity_service.dto.OpportunityRequest;
import com.youthconnect.opportunity_service.entity.Opportunity;
import com.youthconnect.opportunity_service.repository.OpportunityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OpportunityService {

    private final OpportunityRepository opportunityRepository;

    @Transactional
    public OpportunityDTO createOpportunity(OpportunityRequest request) {
        Opportunity opportunity = new Opportunity();
        opportunity.setPostedById(request.getPostedById());
        opportunity.setOpportunityType(request.getOpportunityType());
        opportunity.setTitle(request.getTitle());
        opportunity.setDescription(request.getDescription());
        opportunity.setApplicationDeadline(request.getApplicationDeadline());
        opportunity.setStatus(Opportunity.Status.OPEN);

        Opportunity savedOpportunity = opportunityRepository.save(opportunity);
        return convertToDto(savedOpportunity);
    }

    public List<OpportunityDTO> getAllOpportunities(Optional<Opportunity.OpportunityType> type) {
        List<Opportunity> opportunities = type.isPresent()
                ? opportunityRepository.findByOpportunityType(type.get())
                : opportunityRepository.findAll();
        return opportunities.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    // ==========================================================
    // == NEW METHODS FOR UPDATE AND DELETE ==
    // ==========================================================

    /**
     * Updates an existing opportunity.
     * @param id The ID of the opportunity to update.
     * @param request The DTO with the new data.
     * @return A DTO of the updated opportunity.
     * @throws RuntimeException if the opportunity with the given ID is not found.
     */
    @Transactional // Override to allow database writes
    public OpportunityDTO updateOpportunity(Long id, OpportunityRequest request) {
        // 1. Find the existing opportunity in the database.
        Opportunity opportunityToUpdate = opportunityRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Opportunity not found with id: " + id));

        // 2. Update its fields from the request.
        opportunityToUpdate.setTitle(request.getTitle());
        opportunityToUpdate.setDescription(request.getDescription());
        opportunityToUpdate.setOpportunityType(request.getOpportunityType());
        opportunityToUpdate.setApplicationDeadline(request.getApplicationDeadline());

        // 3. Save the updated entity back to the database.
        Opportunity updatedOpportunity = opportunityRepository.save(opportunityToUpdate);

        // 4. Return the result as a DTO.
        return convertToDto(updatedOpportunity);
    }

    /**
     * Deletes an opportunity by its ID.
     * @param id The ID of the opportunity to delete.
     * @throws RuntimeException if the opportunity with the given ID is not found.
     */
    @Transactional // Override to allow database writes
    public void deleteOpportunity(Long id) {
        // 1. Check if the opportunity exists before attempting to delete.
        if (!opportunityRepository.existsById(id)) {
            throw new RuntimeException("Opportunity not found with id: " + id);
        }
        // 2. Delete the opportunity.
        opportunityRepository.deleteById(id);
    }

    private OpportunityDTO convertToDto(Opportunity opportunity) {
        return OpportunityDTO.builder()
                // ... fields from previous implementation
                .build();
    }
}