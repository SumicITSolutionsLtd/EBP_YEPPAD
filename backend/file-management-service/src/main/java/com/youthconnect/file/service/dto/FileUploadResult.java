package com.youthconnect.file.service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Map;

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
    private Map<String, String> optimizedVersions;
    private Map<String, String> processedVersions;
}
