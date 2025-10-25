package com.youthconnect.edge_functions.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Request DTO for AI chat interactions
 * Supports conversation history for context-aware responses
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatRequest {
    private String message;
    private String systemPrompt;
    private List<Map<String, String>> conversationHistory;
}