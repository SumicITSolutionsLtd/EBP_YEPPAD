package com.youthconnect.file.service.util;

import com.youthconnect.file.service.exception.InvalidFileException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;

/**
 * Utility class for comprehensive file validation
 * Validates file types, sizes, names, and content
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FileValidationUtil {

    // Allowed MIME types
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

    // Allowed file extensions
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
     * @throws InvalidFileException if validation fails
     */
    public void validateImageFile(MultipartFile file) {
        validateFileNotEmpty(file);
        validateFileSize(file, 10 * 1024 * 1024); // 10MB for images

        String contentType = file.getContentType();
        String extension = getFileExtension(file.getOriginalFilename());

        if (contentType == null || !ALLOWED_IMAGE_TYPES.contains(contentType.toLowerCase())) {
            throw new InvalidFileException(
                    "Invalid image file type. Allowed types: " + ALLOWED_IMAGE_EXTENSIONS
            );
        }

        if (!ALLOWED_IMAGE_EXTENSIONS.contains(extension.toLowerCase())) {
            throw new InvalidFileException(
                    "Invalid image file extension. Allowed: " + ALLOWED_IMAGE_EXTENSIONS
            );
        }

        log.debug("Image file validated: {} ({})", file.getOriginalFilename(), contentType);
    }

    /**
     * Validate document file
     * @throws InvalidFileException if validation fails
     */
    public void validateDocumentFile(MultipartFile file) {
        validateFileNotEmpty(file);
        validateFileSize(file, 50 * 1024 * 1024); // 50MB for documents

        String contentType = file.getContentType();
        String extension = getFileExtension(file.getOriginalFilename());

        if (contentType == null || !ALLOWED_DOCUMENT_TYPES.contains(contentType.toLowerCase())) {
            throw new InvalidFileException(
                    "Invalid document file type. Allowed types: " + ALLOWED_DOCUMENT_EXTENSIONS
            );
        }

        if (!ALLOWED_DOCUMENT_EXTENSIONS.contains(extension.toLowerCase())) {
            throw new InvalidFileException(
                    "Invalid document file extension. Allowed: " + ALLOWED_DOCUMENT_EXTENSIONS
            );
        }

        log.debug("Document file validated: {} ({})", file.getOriginalFilename(), contentType);
    }

    /**
     * Validate audio file
     * @throws InvalidFileException if validation fails
     */
    public void validateAudioFile(MultipartFile file) {
        validateFileNotEmpty(file);
        validateFileSize(file, 100 * 1024 * 1024); // 100MB for audio

        String contentType = file.getContentType();
        String extension = getFileExtension(file.getOriginalFilename());

        if (contentType == null || !ALLOWED_AUDIO_TYPES.contains(contentType.toLowerCase())) {
            throw new InvalidFileException(
                    "Invalid audio file type. Allowed types: " + ALLOWED_AUDIO_EXTENSIONS
            );
        }

        if (!ALLOWED_AUDIO_EXTENSIONS.contains(extension.toLowerCase())) {
            throw new InvalidFileException(
                    "Invalid audio file extension. Allowed: " + ALLOWED_AUDIO_EXTENSIONS
            );
        }

        log.debug("Audio file validated: {} ({})", file.getOriginalFilename(), contentType);
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
            throw new InvalidFileException("File is empty");
        }

        if (file.getOriginalFilename() == null || file.getOriginalFilename().trim().isEmpty()) {
            throw new InvalidFileException("File name is required");
        }
    }

    /**
     * Validate file size
     */
    private void validateFileSize(MultipartFile file, long maxSizeBytes) {
        if (file.getSize() > maxSizeBytes) {
            throw new InvalidFileException(
                    String.format("File size exceeds limit. Max allowed: %d MB, actual: %.2f MB",
                            maxSizeBytes / (1024 * 1024),
                            file.getSize() / (1024.0 * 1024.0))
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
     * Prevents path traversal and invalid characters
     */
    public void validateFilename(String filename) {
        if (filename == null || filename.trim().isEmpty()) {
            throw new InvalidFileException("Filename is required");
        }

        // Check for path traversal attempts
        if (filename.contains("..") || filename.contains("/") || filename.contains("\\")) {
            throw new InvalidFileException("Invalid filename: path traversal detected");
        }

        // Check for reserved names (Windows)
        String[] reservedNames = {
                "CON", "PRN", "AUX", "NUL",
                "COM1", "COM2", "COM3", "COM4", "COM5", "COM6", "COM7", "COM8", "COM9",
                "LPT1", "LPT2", "LPT3", "LPT4", "LPT5", "LPT6", "LPT7", "LPT8", "LPT9"
        };

        String nameWithoutExt = filename.contains(".")
                ? filename.substring(0, filename.lastIndexOf("."))
                : filename;

        for (String reserved : reservedNames) {
            if (reserved.equalsIgnoreCase(nameWithoutExt)) {
                throw new InvalidFileException("Invalid filename: reserved name");
            }
        }
    }
}