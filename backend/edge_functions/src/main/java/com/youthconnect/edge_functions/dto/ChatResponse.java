package com.youthconnect.edge_functions.dto;

import lombok.Data;
import java.util.Map;

@Data
public class ChatResponse {
    private String response;
    private Map<String, Object> usage;
}
