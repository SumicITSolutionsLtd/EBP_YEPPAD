package com.youthconnect.file.service.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;

/**
 * Utility class for file validation
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FileValidationUtil {

    private static final List<String> ALLOWED_IMAGE_TYPES = Arrays.asList(
            "image/jpeg", "image/jpg", "image/png", "image/gif"
    );

    private static final List<String> ALLOWED_DOCUMENT_TYPES = Arrays.asList(
            "application/pdf",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "text/plain"
    );

    private static final List<String> ALLOWED_AUDIO_TYPES = Arrays.asList(
            "audio/mpeg", "audio/mp3", "audio/wav", "audio/m4a", "audio/mp4"
    );

    private static final List<String> ALLOWED_IMAGE_EXTENSIONS = Arrays.asList(
            "jpg", "jpeg", "png", "gif"
    );

    private static final List<String> ALLOWED_DOCUMENT_EXTENSIONS = Arrays.asList(
            "pdf", "doc", "docx", "txt"
    );

    private static final List<String> ALLOWED_AUDIO_EXTENSIONS = Arrays.asList(
            "mp3", "wav", "m4a", "mp4"
    );

    /**
     * Validate image file
     */
    public void validateImageFile(MultipartFile file) {
        validateFileNotEmpty(file);
        validateFileSize(file, 10 * 1024 * 1024); // 10MB for images

        String contentType = file.getContentType();
        String extension = getFileExtension(file.getOriginalFilename());

        if (!ALLOWED_IMAGE_TYPES.contains(contentType) ||
                !ALLOWED_IMAGE_EXTENSIONS.contains(extension.toLowerCase())) {
            throw new IllegalArgumentException("Invalid image file type. Allowed: " + ALLOWED_IMAGE_EXTENSIONS);
        }
    }

    /**
     * Validate document file
     */
    public void validateDocumentFile(MultipartFile file) {
        validateFileNotEmpty(file);
        validateFileSize(file, 50 * 1024 * 1024); // 50MB for documents

        String contentType = file.getContentType();
        String extension = getFileExtension(file.getOriginalFilename());

        if (!ALLOWED_DOCUMENT_TYPES.contains(contentType) ||
                !ALLOWED_DOCUMENT_EXTENSIONS.contains(extension.toLowerCase())) {
            throw new IllegalArgumentException("Invalid document file type. Allowed: " + ALLOWED_DOCUMENT_EXTENSIONS);
        }
    }

    /**
     * Validate audio file
     */
    public void validateAudioFile(MultipartFile file) {
        validateFileNotEmpty(file);
        validateFileSize(file, 100 * 1024 * 1024); // 100MB for audio

        String contentType = file.getContentType();
        String extension = getFileExtension(file.getOriginalFilename());

        if (!ALLOWED_AUDIO_TYPES.contains(contentType) ||
                !ALLOWED_AUDIO_EXTENSIONS.contains(extension.toLowerCase())) {
            throw new IllegalArgumentException("Invalid audio file type. Allowed: " + ALLOWED_AUDIO_EXTENSIONS);
        }
    }

    /**
     * Check if file is an audio file by extension
     */
    public boolean isAudioFile(String filename) {
        String extension = getFileExtension(filename);
        return ALLOWED_AUDIO_EXTENSIONS.contains(extension.toLowerCase());
    }

    /**
     * Validate file is not empty
     */
    private void validateFileNotEmpty(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        if (file.getOriginalFilename() == null || file.getOriginalFilename().trim().isEmpty()) {
            throw new IllegalArgumentException("File name is required");
        }
    }

    /**
     * Validate file size
     */
    private void validateFileSize(MultipartFile file, long maxSizeBytes) {
        if (file.getSize() > maxSizeBytes) {
            throw new IllegalArgumentException(
                    String.format("File size exceeds limit. Max allowed: %d bytes, actual: %d bytes",
                            maxSizeBytes, file.getSize())
            );
        }
    }

    /**
     * Get file extension from filename
     */
    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf(".") + 1);
    }

    /**
     * Validate filename for security
     */
    public void validateFilename(String filename) {
        if (filename == null || filename.trim().isEmpty()) {
            throw new IllegalArgumentException("Filename is required");
        }

        // Check for path traversal attempts
        if (filename.contains("..") || filename.contains("/") || filename.contains("\\")) {
            throw new IllegalArgumentException("Invalid filename: path traversal detected");
        }

        // Check for reserved names (Windows)
        String[] reservedNames = {"CON", "PRN", "AUX", "NUL", "COM1", "COM2", "COM3", "COM4",
                "COM5", "COM6", "COM7", "COM8", "COM9", "LPT1", "LPT2",
                "LPT3", "LPT4", "LPT5", "LPT6", "LPT7", "LPT8", "LPT9"};

        String nameWithoutExt = filename.contains(".") ?
                filename.substring(0, filename.lastIndexOf(".")) : filename;

        for (String reserved : reservedNames) {
            if (reserved.equalsIgnoreCase(nameWithoutExt)) {
                throw new IllegalArgumentException("Invalid filename: reserved name");
            }
        }
    }
}
