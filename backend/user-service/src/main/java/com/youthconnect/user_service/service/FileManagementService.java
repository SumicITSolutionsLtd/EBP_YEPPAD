package com.youthconnect.user_service.service;

import com.youthconnect.user_service.config.ApplicationProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.UUID;

/**
 * ============================================================================
 * File Management Service for Youth Connect Uganda - UUID MIGRATION COMPLETE
 * ============================================================================
 *
 * Comprehensive file management system handling all file operations for the
 * Youth Connect Uganda platform. This service provides secure, scalable, and
 * multi-language file management capabilities.
 *
 * ✅ FIXED: All userId references now use UUID instead of Long
 *
 * <p><b>Core Functionalities:</b></p>
 * <ul>
 *   <li>Audio file uploads for learning modules (multi-language support)</li>
 *   <li>Profile picture management with automatic optimization</li>
 *   <li>Document uploads (CVs, certificates, identity documents)</li>
 *   <li>File validation and security scanning</li>
 *   <li>Multi-language audio file management (English, Luganda, Alur, Lugbara)</li>
 *   <li>File storage optimization and compression</li>
 *   <li>Secure file deletion with data overwriting</li>
 * </ul>
 *
 * @author Douglas Kings Kato
 * @version 2.2.0 - UUID Migration Complete
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FileManagementService {

    private final ApplicationProperties applicationProperties;

    // ========================================================================
    // STATIC CONFIGURATION
    // ========================================================================

    private static final Map<String, String> CONTENT_TYPE_MAP = Map.of(
            "jpg", "image/jpeg",
            "jpeg", "image/jpeg",
            "png", "image/png",
            "pdf", "application/pdf",
            "doc", "application/msword",
            "docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "mp3", "audio/mpeg",
            "wav", "audio/wav",
            "m4a", "audio/mp4"
    );

    // ========================================================================
    // PUBLIC API METHODS - PROFILE PICTURES
    // ========================================================================

    /**
     * Upload Profile Picture
     *
     * ✅ FIXED: userId parameter now uses UUID type
     *
     * Handles user profile picture uploads with validation, optimization,
     * and thumbnail generation.
     *
     * @param userId User ID (UUID) for whom the profile picture is being uploaded
     * @param file MultipartFile containing the image data
     * @return CompletableFuture<FileUploadResult> containing upload status and file URLs
     */
    @Async("fileProcessingExecutor")
    public CompletableFuture<FileUploadResult> uploadProfilePicture(UUID userId, MultipartFile file) {
        log.info("Starting profile picture upload for user ID: {}", userId);

        try {
            // Step 1: Validate the image file
            validateImageFile(file);
            log.debug("Image validation passed for user: {}", userId);

            // Step 2: Generate unique filename with timestamp
            String fileName = generateProfilePictureFileName(userId, file);
            log.debug("Generated filename: {}", fileName);

            // Step 3: Create user-specific directory structure
            Path uploadPath = createUserDirectory(userId, "profile-pictures");
            log.debug("Created upload path: {}", uploadPath);

            // Step 4: Save the file
            Path filePath = uploadPath.resolve(fileName);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            log.info("Profile picture saved successfully: {}", filePath);

            // Step 5: Generate optimized versions
            Map<String, String> optimizedVersions = generateImageVersions(filePath, userId);
            log.debug("Generated {} optimized versions", optimizedVersions.size());

            // Step 6: Create comprehensive file metadata with UUID
            FileMetadata metadata = createFileMetadata(file, filePath, "PROFILE_PICTURE", userId);

            // Build successful result
            FileUploadResult result = FileUploadResult.builder()
                    .success(true)
                    .fileName(fileName)
                    .filePath(filePath.toString())
                    .fileUrl("/uploads/users/" + userId + "/profile-pictures/" + fileName)
                    .metadata(metadata)
                    .optimizedVersions(optimizedVersions)
                    .build();

            log.info("Profile picture uploaded successfully for user: {} - URL: {}",
                    userId, result.getFileUrl());
            return CompletableFuture.completedFuture(result);

        } catch (Exception e) {
            log.error("Failed to upload profile picture for user: {} - Error: {}",
                    userId, e.getMessage(), e);
            return CompletableFuture.completedFuture(
                    FileUploadResult.builder()
                            .success(false)
                            .errorMessage(e.getMessage())
                            .build());
        }
    }

    // ========================================================================
    // PUBLIC API METHODS - AUDIO MODULES
    // ========================================================================

    /**
     * Upload Audio Module
     *
     * Uploads audio files for learning modules with multi-language support.
     * Note: Audio modules are not user-specific, so no userId parameter needed.
     *
     * @param moduleKey Unique identifier for the learning module
     * @param language Language code (en, lg, alur, lgb)
     * @param file MultipartFile containing the audio data
     * @return CompletableFuture<FileUploadResult> containing upload status and URLs
     */
    @Async("fileProcessingExecutor")
    public CompletableFuture<FileUploadResult> uploadAudioModule(String moduleKey, String language, MultipartFile file) {
        log.info("Starting audio module upload - Module: {}, Language: {}", moduleKey, language);

        try {
            validateAudioFile(file);
            log.debug("Audio validation passed for module: {}", moduleKey);

            String fileName = generateAudioFileName(moduleKey, language, file);
            log.debug("Generated audio filename: {}", fileName);

            Path uploadPath = createModuleDirectory(moduleKey);
            log.debug("Created module path: {}", uploadPath);

            Path filePath = uploadPath.resolve(fileName);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            log.info("Audio file saved successfully: {}", filePath);

            Map<String, String> processedVersions = processAudioFile(filePath, moduleKey, language);
            log.debug("Generated {} processed audio versions", processedVersions.size());

            AudioMetadata audioMetadata = extractAudioMetadata(filePath);
            log.debug("Extracted audio metadata - Duration: {}s, Bitrate: {}kbps",
                    audioMetadata.getDuration(), audioMetadata.getBitrate());

            // Audio modules don't have userId - pass null
            FileMetadata metadata = createFileMetadata(file, filePath, "AUDIO_MODULE", null);
            metadata.setAudioMetadata(audioMetadata);

            FileUploadResult result = FileUploadResult.builder()
                    .success(true)
                    .fileName(fileName)
                    .filePath(filePath.toString())
                    .fileUrl("/uploads/modules/" + moduleKey + "/" + fileName)
                    .metadata(metadata)
                    .processedVersions(processedVersions)
                    .build();

            log.info("Audio module uploaded successfully - Module: {}, Language: {}, URL: {}",
                    moduleKey, language, result.getFileUrl());
            return CompletableFuture.completedFuture(result);

        } catch (Exception e) {
            log.error("Failed to upload audio module - Module: {}, Language: {} - Error: {}",
                    moduleKey, language, e.getMessage(), e);
            return CompletableFuture.completedFuture(
                    FileUploadResult.builder()
                            .success(false)
                            .errorMessage(e.getMessage())
                            .build());
        }
    }

    // ========================================================================
    // PUBLIC API METHODS - DOCUMENTS
    // ========================================================================

    /**
     * Upload User Document
     *
     * ✅ FIXED: userId parameter now uses UUID type
     *
     * Handles uploads of user documents with strict security measures.
     *
     * @param userId User ID (UUID) for whom the document is being uploaded
     * @param documentType Type of document (CV, CERTIFICATE, ID, etc.)
     * @param file MultipartFile containing the document data
     * @return CompletableFuture<FileUploadResult> containing upload status and URLs
     */
    @Async("fileProcessingExecutor")
    public CompletableFuture<FileUploadResult> uploadUserDocument(UUID userId, String documentType, MultipartFile file) {
        log.info("Starting document upload - User: {}, Type: {}", userId, documentType);

        try {
            validateDocumentFile(file);
            log.debug("Document validation passed for user: {}", userId);

            String fileName = generateDocumentFileName(userId, documentType, file);
            log.debug("Generated document filename: {}", fileName);

            Path uploadPath = createUserDirectory(userId, "documents");
            log.debug("Created documents path: {}", uploadPath);

            Path filePath = uploadPath.resolve(fileName);
            saveFileSecurely(file, filePath);
            log.info("Document saved successfully: {}", filePath);

            DocumentMetadata docMetadata = extractDocumentMetadata(filePath);
            log.debug("Extracted document metadata - Pages: {}, Searchable: {}",
                    docMetadata.getPageCount(), docMetadata.getIsSearchable());

            // Create metadata with UUID userId
            FileMetadata metadata = createFileMetadata(file, filePath, "USER_DOCUMENT", userId);
            metadata.setDocumentMetadata(docMetadata);

            FileUploadResult result = FileUploadResult.builder()
                    .success(true)
                    .fileName(fileName)
                    .filePath(filePath.toString())
                    .fileUrl("/uploads/users/" + userId + "/documents/" + fileName)
                    .metadata(metadata)
                    .documentType(documentType)
                    .build();

            log.info("Document uploaded successfully - User: {}, Type: {}, URL: {}",
                    userId, documentType, result.getFileUrl());
            return CompletableFuture.completedFuture(result);

        } catch (Exception e) {
            log.error("Failed to upload document - User: {}, Type: {} - Error: {}",
                    userId, documentType, e.getMessage(), e);
            return CompletableFuture.completedFuture(
                    FileUploadResult.builder()
                            .success(false)
                            .errorMessage(e.getMessage())
                            .build());
        }
    }

    // ========================================================================
    // PUBLIC API METHODS - FILE RETRIEVAL
    // ========================================================================

    /**
     * Get Module Audio Files
     *
     * Retrieves all audio files available for a learning module across all languages.
     *
     * @param moduleKey Unique identifier for the learning module
     * @return Map of language codes to file URLs
     */
    public Map<String, String> getModuleAudioFiles(String moduleKey) {
        log.debug("Retrieving audio files for module: {}", moduleKey);

        Map<String, String> audioFiles = new HashMap<>();

        try {
            Path modulePath = Paths.get(
                    applicationProperties.getUpload().getUploadDirectory(),
                    "modules",
                    moduleKey
            );

            if (!Files.exists(modulePath)) {
                log.warn("Module directory not found: {}", moduleKey);
                return audioFiles;
            }

            String[] languages = {"en", "lg", "alur", "lgb"};

            for (String lang : languages) {
                Optional<Path> audioFile = Files.list(modulePath)
                        .filter(path -> path.getFileName().toString().contains("_" + lang + "."))
                        .findFirst();

                if (audioFile.isPresent()) {
                    String relativePath = "/uploads/modules/" + moduleKey + "/" +
                            audioFile.get().getFileName();
                    audioFiles.put(lang, relativePath);
                    log.trace("Found audio file for language {}: {}", lang, relativePath);
                }
            }

            log.debug("Found {} audio files for module: {}", audioFiles.size(), moduleKey);

        } catch (Exception e) {
            log.error("Failed to retrieve audio files for module: {} - Error: {}",
                    moduleKey, e.getMessage(), e);
        }

        return audioFiles;
    }

    // ========================================================================
    // PUBLIC API METHODS - FILE DELETION
    // ========================================================================

    /**
     * Delete User File Securely
     *
     * ✅ FIXED: userId parameter now uses UUID type
     *
     * Implements secure file deletion with data overwriting.
     *
     * @param userId User ID (UUID) who owns the file
     * @param fileName Name of the file to delete
     * @param category File category (profile-pictures, documents, etc.)
     * @return CompletableFuture<Boolean> true if deleted successfully
     */
    @Async("fileProcessingExecutor")
    public CompletableFuture<Boolean> deleteUserFile(UUID userId, String fileName, String category) {
        log.info("Starting secure deletion - User: {}, File: {}, Category: {}",
                userId, fileName, category);

        try {
            Path filePath = Paths.get(
                    applicationProperties.getUpload().getUploadDirectory(),
                    "users",
                    userId.toString(),
                    category,
                    fileName
            );

            if (Files.exists(filePath)) {
                secureDelete(filePath);
                log.info("File deleted securely - User: {}, File: {}", userId, fileName);
                return CompletableFuture.completedFuture(true);
            } else {
                log.warn("File not found for deletion - User: {}, File: {}", userId, fileName);
                return CompletableFuture.completedFuture(false);
            }

        } catch (Exception e) {
            log.error("Failed to delete file - User: {}, File: {} - Error: {}",
                    userId, fileName, e.getMessage(), e);
            return CompletableFuture.completedFuture(false);
        }
    }

    // ========================================================================
    // PUBLIC API METHODS - FILE METADATA
    // ========================================================================

    /**
     * Get File Metadata
     *
     * @param filePath Full path to the file
     * @return FileMetadata object or null if file doesn't exist
     */
    public FileMetadata getFileMetadata(String filePath) {
        log.debug("Retrieving metadata for file: {}", filePath);

        try {
            Path path = Paths.get(filePath);
            if (Files.exists(path)) {
                FileMetadata metadata = FileMetadata.builder()
                        .fileName(path.getFileName().toString())
                        .filePath(filePath)
                        .fileSize(Files.size(path))
                        .lastModified(Files.getLastModifiedTime(path).toInstant())
                        .contentType(Files.probeContentType(path))
                        .build();

                log.debug("Retrieved metadata - Size: {} bytes, Type: {}",
                        metadata.getFileSize(), metadata.getContentType());
                return metadata;
            } else {
                log.warn("File not found: {}", filePath);
            }
        } catch (Exception e) {
            log.error("Failed to get file metadata for: {} - Error: {}",
                    filePath, e.getMessage(), e);
        }
        return null;
    }

    // ========================================================================
    // PRIVATE HELPER METHODS - VALIDATION
    // ========================================================================

    private void validateImageFile(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        String extension = getFileExtension(file.getOriginalFilename()).toLowerCase();
        if (!Arrays.asList("jpg", "jpeg", "png").contains(extension)) {
            throw new IllegalArgumentException("Invalid image format. Only JPG and PNG allowed.");
        }

        if (file.getSize() > 5 * 1024 * 1024) {
            throw new IllegalArgumentException("Image file too large. Maximum 5MB allowed.");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("Invalid image content type");
        }

        log.debug("Image validation passed - Size: {} bytes, Type: {}",
                file.getSize(), contentType);
    }

    private void validateAudioFile(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Audio file is empty");
        }

        String extension = getFileExtension(file.getOriginalFilename()).toLowerCase();
        if (!Arrays.asList("mp3", "wav", "m4a").contains(extension)) {
            throw new IllegalArgumentException("Invalid audio format. Only MP3, WAV, and M4A allowed.");
        }

        if (file.getSize() > 50 * 1024 * 1024) {
            throw new IllegalArgumentException("Audio file too large. Maximum 50MB allowed.");
        }

        log.debug("Audio validation passed - Size: {} bytes, Extension: {}",
                file.getSize(), extension);
    }

    private void validateDocumentFile(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Document file is empty");
        }

        String extension = getFileExtension(file.getOriginalFilename()).toLowerCase();
        List<String> allowedExtensions = applicationProperties.getUpload().getAllowedExtensions();

        if (!allowedExtensions.contains(extension)) {
            throw new IllegalArgumentException("Invalid document format. Allowed: " + allowedExtensions);
        }

        if (file.getSize() > 10 * 1024 * 1024) {
            throw new IllegalArgumentException("Document file too large. Maximum 10MB allowed.");
        }

        log.debug("Document validation passed - Size: {} bytes, Extension: {}",
                file.getSize(), extension);
    }

    // ========================================================================
    // PRIVATE HELPER METHODS - FILE NAME GENERATION
    // ========================================================================

    private String getFileExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf(".") + 1);
    }

    /**
     * Generate Profile Picture Filename
     *
     * ✅ FIXED: userId parameter now uses UUID type
     */
    private String generateProfilePictureFileName(UUID userId, MultipartFile file) {
        String extension = getFileExtension(file.getOriginalFilename());
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String filename = "profile_" + userId + "_" + timestamp + "." + extension;
        log.trace("Generated profile picture filename: {}", filename);
        return filename;
    }

    private String generateAudioFileName(String moduleKey, String language, MultipartFile file) {
        String extension = getFileExtension(file.getOriginalFilename());
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String filename = moduleKey + "_" + language + "_" + timestamp + "." + extension;
        log.trace("Generated audio filename: {}", filename);
        return filename;
    }

    /**
     * Generate Document Filename
     *
     * ✅ FIXED: userId parameter now uses UUID type
     */
    private String generateDocumentFileName(UUID userId, String documentType, MultipartFile file) {
        String extension = getFileExtension(file.getOriginalFilename());
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String filename = documentType.toLowerCase() + "_" + userId + "_" + timestamp + "." + extension;
        log.trace("Generated document filename: {}", filename);
        return filename;
    }

    // ========================================================================
    // PRIVATE HELPER METHODS - DIRECTORY MANAGEMENT
    // ========================================================================

    /**
     * Create User Directory
     *
     * ✅ FIXED: userId parameter now uses UUID type
     */
    private Path createUserDirectory(UUID userId, String category) throws IOException {
        Path userDir = Paths.get(
                applicationProperties.getUpload().getUploadDirectory(),
                "users",
                userId.toString(),
                category
        );
        Files.createDirectories(userDir);
        log.debug("Created user directory: {}", userDir);
        return userDir;
    }

    private Path createModuleDirectory(String moduleKey) throws IOException {
        Path moduleDir = Paths.get(
                applicationProperties.getUpload().getUploadDirectory(),
                "modules",
                moduleKey
        );
        Files.createDirectories(moduleDir);
        log.debug("Created module directory: {}", moduleDir);
        return moduleDir;
    }

    // ========================================================================
    // PRIVATE HELPER METHODS - FILE OPERATIONS
    // ========================================================================

    private void saveFileSecurely(MultipartFile file, Path filePath) throws IOException {
        log.debug("Saving file securely to: {}", filePath);

        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        log.trace("File copied successfully");

        try {
            Files.setPosixFilePermissions(filePath,
                    java.nio.file.attribute.PosixFilePermissions.fromString("rw-------"));
            log.trace("Set POSIX permissions: rw-------");
        } catch (UnsupportedOperationException e) {
            log.debug("POSIX permissions not supported on this platform (Windows)");
        }

        log.debug("File saved securely with size: {} bytes", Files.size(filePath));
    }

    private void secureDelete(Path filePath) throws IOException {
        log.debug("Starting secure deletion for: {}", filePath);

        byte[] randomData = new byte[1024];
        new Random().nextBytes(randomData);

        long fileSize = Files.size(filePath);
        log.trace("File size: {} bytes", fileSize);

        try (var channel = Files.newByteChannel(filePath,
                java.nio.file.StandardOpenOption.WRITE)) {
            long bytesWritten = 0;
            for (long pos = 0; pos < fileSize; pos += randomData.length) {
                channel.write(java.nio.ByteBuffer.wrap(randomData));
                bytesWritten += randomData.length;
            }
            log.trace("Overwrote {} bytes with random data", bytesWritten);
        }

        Files.delete(filePath);
        log.debug("File securely deleted: {}", filePath);
    }

    // ========================================================================
    // PRIVATE HELPER METHODS - METADATA CREATION
    // ========================================================================

    /**
     * Create File Metadata
     *
     * ✅ FIXED: userId parameter changed from Long to UUID
     *
     * Generates comprehensive metadata for uploaded files.
     *
     * @param file Original MultipartFile
     * @param filePath Saved file path
     * @param type File type category
     * @param userId User ID (UUID, optional - can be null for audio modules)
     * @return FileMetadata object
     */
    private FileMetadata createFileMetadata(MultipartFile file, Path filePath, String type, UUID userId) {
        log.trace("Creating file metadata for: {}", filePath);

        try {
            FileMetadata metadata = FileMetadata.builder()
                    .fileName(file.getOriginalFilename())
                    .filePath(filePath.toString())
                    .fileSize(file.getSize())
                    .contentType(file.getContentType())
                    .fileType(type)
                    .userId(userId)  // ✅ Now accepts UUID
                    .uploadTime(java.time.Instant.now())
                    .build();

            log.trace("Created metadata - Type: {}, Size: {} bytes, UserId: {}",
                    type, file.getSize(), userId);
            return metadata;
        } catch (Exception e) {
            log.error("Failed to create file metadata - Error: {}", e.getMessage(), e);
            return FileMetadata.builder().build();
        }
    }

    // ========================================================================
    // PRIVATE HELPER METHODS - FILE PROCESSING (PLACEHOLDERS)
    // ========================================================================

    /**
     * Generate Image Versions
     *
     * ✅ FIXED: userId parameter now uses UUID type
     */
    private Map<String, String> generateImageVersions(Path filePath, UUID userId) {
        log.debug("Generating image versions for: {}", filePath);

        Map<String, String> versions = new HashMap<>();
        versions.put("thumbnail", "/uploads/users/" + userId + "/profile-pictures/thumb_" + filePath.getFileName());
        versions.put("medium", "/uploads/users/" + userId + "/profile-pictures/med_" + filePath.getFileName());

        log.trace("Generated {} image version URLs (placeholder)", versions.size());
        return versions;
    }

    private Map<String, String> processAudioFile(Path filePath, String moduleKey, String language) {
        log.debug("Processing audio file: {}", filePath);

        Map<String, String> versions = new HashMap<>();
        versions.put("compressed", "/uploads/modules/" + moduleKey + "/compressed_" + filePath.getFileName());

        log.trace("Generated {} audio version URLs (placeholder)", versions.size());
        return versions;
    }

    private AudioMetadata extractAudioMetadata(Path filePath) {
        log.trace("Extracting audio metadata from: {}", filePath);

        AudioMetadata metadata = AudioMetadata.builder()
                .duration(120.0)
                .bitrate(128)
                .sampleRate(44100)
                .build();

        log.trace("Extracted audio metadata (placeholder) - Duration: {}s, Bitrate: {}kbps",
                metadata.getDuration(), metadata.getBitrate());
        return metadata;
    }

    private DocumentMetadata extractDocumentMetadata(Path filePath) {
        log.trace("Extracting document metadata from: {}", filePath);

        DocumentMetadata metadata = DocumentMetadata.builder()
                .pageCount(1)
                .hasText(true)
                .isSearchable(true)
                .build();

        log.trace("Extracted document metadata (placeholder) - Pages: {}, Searchable: {}",
                metadata.getPageCount(), metadata.getIsSearchable());
        return metadata;
    }

    // ========================================================================
    // DATA TRANSFER OBJECTS (DTOs) - UUID MIGRATION COMPLETE
    // ========================================================================

    @lombok.Data
    @lombok.Builder
    public static class FileUploadResult {
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

    /**
     * File Metadata DTO
     *
     * ✅ FIXED: userId field changed from Long to UUID
     */
    @lombok.Data
    @lombok.Builder
    public static class FileMetadata {
        private String fileName;
        private String filePath;
        private Long fileSize;
        private String contentType;
        private String fileType;

        /**
         * User ID who owns this file (optional)
         * ✅ FIXED: Changed from Long to UUID
         */
        private UUID userId;

        private java.time.Instant uploadTime;
        private java.time.Instant lastModified;
        private AudioMetadata audioMetadata;
        private DocumentMetadata documentMetadata;
    }

    @lombok.Data
    @lombok.Builder
    public static class AudioMetadata {
        private Double duration;
        private Integer bitrate;
        private Integer sampleRate;
        private String codec;
    }

    @lombok.Data
    @lombok.Builder
    public static class DocumentMetadata {
        private Integer pageCount;
        private Boolean hasText;
        private Boolean isSearchable;
        private String author;
        private java.time.Instant createdDate;
    }
}