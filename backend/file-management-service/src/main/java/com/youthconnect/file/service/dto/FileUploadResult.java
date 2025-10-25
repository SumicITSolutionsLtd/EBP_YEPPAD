package com.youthconnect.file.service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Result object returned after file upload operations
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileUploadResult {
    private boolean success;
    private String fileName;
    private String filePath;
    private String fileUrl;
    private String errorMessage;
    private FileMetadata metadata;
    private String documentType;

    /**
     * For image files: thumbnail, medium, large versions
     */
    private Map<String, String> optimizedVersions;

    /**
     * For audio files: compressed, normalized versions
     */
    private Map<String, String> processedVersions;
}