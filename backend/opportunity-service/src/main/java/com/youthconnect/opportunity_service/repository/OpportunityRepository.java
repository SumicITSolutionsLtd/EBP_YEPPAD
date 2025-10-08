package com.youthconnect.opportunity_service.repository;

import com.youthconnect.opportunity_service.entity.Opportunity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * Spring Data JPA repository for the Opportunity entity.
 * Inherits all standard CRUD operations and allows for custom query methods.
 */
@Repository
public interface OpportunityRepository extends JpaRepository<Opportunity, Long> {

    /**
     * Finds all opportunities of a specific type.
     * Spring Data JPA will automatically create the query from this method name.
     * Example Usage: GET /api/opportunities?type=JOB
     * @param type The OpportunityType to filter by.
     * @return A list of opportunities matching the type.
     */
    List<Opportunity> findByOpportunityType(Opportunity.OpportunityType type);
}
