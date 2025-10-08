package com.youthconnect.edge_functions.dto;

import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class ChatRequest {
    private String message;
    private String systemPrompt;
    private List<Map<String, String>> conversationHistory;
}
