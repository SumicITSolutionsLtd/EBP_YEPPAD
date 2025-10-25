package com.youthconnect.edge_functions.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * AI Service Response DTO
 * Generic wrapper for AI service responses
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AIServiceResponse<T> {
    private Boolean success;
    private T data;
    private String error;
    private Map<String, Object> metadata;
}
