package com.youthconnect.edge_functions.client;

import com.youthconnect.edge_functions.dto.OpportunityDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "opportunity-service")
public interface OpportunityServiceClient {

    @GetMapping("/api/opportunities/recent")
    List<OpportunityDTO> getRecentOpportunities();

    @PostMapping("/api/opportunities")
    OpportunityDTO createOpportunity(@RequestBody OpportunityDTO opportunityDTO);

    @GetMapping("/api/opportunities/{id}")
    OpportunityDTO getOpportunityById(@PathVariable Long id);
}