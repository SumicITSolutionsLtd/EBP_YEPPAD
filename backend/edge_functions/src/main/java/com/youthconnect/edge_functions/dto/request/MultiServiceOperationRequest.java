package com.youthconnect.edge_functions.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * Multi-Service Operation Request DTO
 * For complex workflows spanning multiple services
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MultiServiceOperationRequest {
    private String operationType;
    private Long userId;
    private Map<String, Object> parameters;
    private List<String> services;
}