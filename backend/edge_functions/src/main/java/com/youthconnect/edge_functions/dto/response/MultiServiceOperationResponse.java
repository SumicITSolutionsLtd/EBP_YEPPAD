package com.youthconnect.edge_functions.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * Multi-Service Operation Response DTO
 * Aggregates results from multiple services
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MultiServiceOperationResponse {
    private Boolean success;
    private Map<String, Object> results;
    private List<String> errors;
    private Long executionTimeMs;
}
