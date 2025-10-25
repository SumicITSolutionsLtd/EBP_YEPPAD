package com.youthconnect.edge_functions.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Speech-to-Text Request DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class STTRequest {
    private String audioBase64;  // Base64 encoded audio
    private String audioFormat;  // mp3, wav, webm, etc.
    private String language;     // Optional language hint
}
