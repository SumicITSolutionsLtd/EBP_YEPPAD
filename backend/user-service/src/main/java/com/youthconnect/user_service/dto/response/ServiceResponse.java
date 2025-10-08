package com.youthconnect.user_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Standard response wrapper for client communications
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceResponse<T> {
    private boolean success;
    private String message;
    private T data;
    private Map<String, Object> metadata;
    private LocalDateTime timestamp;
    private boolean fallback; // Indicates if response came from fallback
}
