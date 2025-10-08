package com.youthconnect.file.service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileMetadata {
    private String fileName;
    private String filePath;
    private Long fileSize;
    private String contentType;
    private String fileType;
    private Long userId;
    private Instant uploadTime;
    private Instant lastModified;
}
