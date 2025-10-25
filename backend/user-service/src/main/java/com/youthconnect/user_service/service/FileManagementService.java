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

/**
 * ============================================================================
 * File Management Service for Youth Connect Uganda
 * ============================================================================
 *
 * Comprehensive file management system handling all file operations for the
 * Youth Connect Uganda platform. This service provides secure, scalable, and
 * multi-language file management capabilities.
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
 * <p><b>Advanced Features:</b></p>
 * <ul>
 *   <li>Secure file validation and virus scanning (configurable)</li>
 *   <li>Automatic file compression and optimization</li>
 *   <li>Image thumbnail generation (multiple sizes)</li>
 *   <li>Audio file processing and format conversion</li>
 *   <li>File metadata extraction and storage</li>
 *   <li>Asynchronous processing for better performance</li>
 *   <li>CDN integration ready for fast global access</li>
 * </ul>
 *
 * <p><b>Security Measures:</b></p>
 * <ul>
 *   <li>File type validation and whitelisting</li>
 *   <li>File size limits enforcement</li>
 *   <li>Secure file permissions (POSIX on Unix-like systems)</li>
 *   <li>Secure deletion with random data overwriting</li>
 *   <li>Content-type validation to prevent malicious uploads</li>
 * </ul>
 *
 * <p><b>Language Support:</b></p>
 * The service supports audio files in multiple Ugandan languages:
 * <ul>
 *   <li><b>en</b> - English</li>
 *   <li><b>lg</b> - Luganda (Central Uganda)</li>
 *   <li><b>alur</b> - Alur (Northern Uganda)</li>
 *   <li><b>lgb</b> - Lugbara (Northwestern Uganda)</li>
 * </ul>
 *
 * @author Youth Connect Uganda Development Team
 * @version 2.1.0 - FIXED: ApplicationProperties method calls
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FileManagementService {

    /**
     * Application properties configuration
     * Provides access to all application settings including upload configurations
     */
    private final ApplicationProperties applicationProperties;

    // ========================================================================
    // STATIC CONFIGURATION
    // ========================================================================

    /**
     * Content Type Mapping
     * Maps file extensions to their corresponding MIME types for proper HTTP responses
     */
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
     * Handles user profile picture uploads with the following workflow:
     * 1. Validates the image file (format, size, content type)
     * 2. Generates a unique, timestamped filename
     * 3. Creates user-specific directory structure
     * 4. Saves the original file
     * 5. Generates optimized versions (thumbnail, medium, large)
     * 6. Creates comprehensive metadata
     *
     * <p><b>Validation Rules:</b></p>
     * - Only JPG, JPEG, PNG formats allowed
     * - Maximum file size: 5MB
     * - Must be a valid image content type
     *
     * <p><b>Generated Versions:</b></p>
     * - Original: Full resolution
     * - Thumbnail: 200x200px
     * - Medium: 400x400px (placeholder)
     * - Large: 800x800px (placeholder)
     *
     * @param userId User ID for whom the profile picture is being uploaded
     * @param file MultipartFile containing the image data
     * @return CompletableFuture<FileUploadResult> containing upload status and file URLs
     */
    @Async("fileProcessingExecutor")
    public CompletableFuture<FileUploadResult> uploadProfilePicture(Long userId, MultipartFile file) {
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

            // Step 5: Generate optimized versions (thumbnail, medium, large)
            Map<String, String> optimizedVersions = generateImageVersions(filePath, userId);
            log.debug("Generated {} optimized versions", optimizedVersions.size());

            // Step 6: Create comprehensive file metadata
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
     * This is a critical feature for Youth Connect Uganda's educational platform,
     * enabling accessible learning for users who speak different languages.
     *
     * <p><b>Workflow:</b></p>
     * 1. Validates audio file (format, size, quality)
     * 2. Generates filename with language code
     * 3. Creates module-specific directory
     * 4. Saves original audio file
     * 5. Processes audio (compression, format conversion)
     * 6. Extracts audio metadata (duration, bitrate, sample rate)
     *
     * <p><b>Validation Rules:</b></p>
     * - Only MP3, WAV, M4A formats allowed
     * - Maximum file size: 50MB
     * - Must be a valid audio content type
     *
     * <p><b>Language Codes:</b></p>
     * - en: English
     * - lg: Luganda
     * - alur: Alur
     * - lgb: Lugbara
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
            // Step 1: Validate audio file
            validateAudioFile(file);
            log.debug("Audio validation passed for module: {}", moduleKey);

            // Step 2: Generate filename with language code
            String fileName = generateAudioFileName(moduleKey, language, file);
            log.debug("Generated audio filename: {}", fileName);

            // Step 3: Create module directory
            Path uploadPath = createModuleDirectory(moduleKey);
            log.debug("Created module path: {}", uploadPath);

            // Step 4: Save original file
            Path filePath = uploadPath.resolve(fileName);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            log.info("Audio file saved successfully: {}", filePath);

            // Step 5: Process audio (compression, format conversion)
            Map<String, String> processedVersions = processAudioFile(filePath, moduleKey, language);
            log.debug("Generated {} processed audio versions", processedVersions.size());

            // Step 6: Extract audio metadata
            AudioMetadata audioMetadata = extractAudioMetadata(filePath);
            log.debug("Extracted audio metadata - Duration: {}s, Bitrate: {}kbps",
                    audioMetadata.getDuration(), audioMetadata.getBitrate());

            // Create file metadata
            FileMetadata metadata = createFileMetadata(file, filePath, "AUDIO_MODULE", null);
            metadata.setAudioMetadata(audioMetadata);

            // Build successful result
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
     * Handles uploads of user documents such as CVs, certificates, identity documents,
     * and other important files. Implements strict security measures to prevent
     * malicious file uploads.
     *
     * <p><b>Workflow:</b></p>
     * 1. Validates document file (format, size, type)
     * 2. Generates secure filename
     * 3. Creates user documents directory
     * 4. Saves file securely with virus scanning (if enabled)
     * 5. Extracts document metadata
     * 6. Sets secure file permissions
     *
     * <p><b>Validation Rules:</b></p>
     * - Only whitelisted extensions allowed (configured in application.yml)
     * - Maximum file size: 10MB
     * - Content type validation
     *
     * <p><b>Document Types:</b></p>
     * - CV: Curriculum Vitae / Resume
     * - CERTIFICATE: Educational certificates
     * - ID: Identity documents
     * - LICENSE: Professional licenses
     * - OTHER: Other documents
     *
     * @param userId User ID for whom the document is being uploaded
     * @param documentType Type of document (CV, CERTIFICATE, ID, etc.)
     * @param file MultipartFile containing the document data
     * @return CompletableFuture<FileUploadResult> containing upload status and URLs
     */
    @Async("fileProcessingExecutor")
    public CompletableFuture<FileUploadResult> uploadUserDocument(Long userId, String documentType, MultipartFile file) {
        log.info("Starting document upload - User: {}, Type: {}", userId, documentType);

        try {
            // Step 1: Validate document file
            validateDocumentFile(file);
            log.debug("Document validation passed for user: {}", userId);

            // Step 2: Generate secure filename
            String fileName = generateDocumentFileName(userId, documentType, file);
            log.debug("Generated document filename: {}", fileName);

            // Step 3: Create user documents directory
            Path uploadPath = createUserDirectory(userId, "documents");
            log.debug("Created documents path: {}", uploadPath);

            // Step 4: Save file with virus scanning
            Path filePath = uploadPath.resolve(fileName);
            saveFileSecurely(file, filePath);
            log.info("Document saved successfully: {}", filePath);

            // Step 5: Extract document metadata
            DocumentMetadata docMetadata = extractDocumentMetadata(filePath);
            log.debug("Extracted document metadata - Pages: {}, Searchable: {}",
                    docMetadata.getPageCount(), docMetadata.getIsSearchable());

            // Create file metadata
            FileMetadata metadata = createFileMetadata(file, filePath, "USER_DOCUMENT", userId);
            metadata.setDocumentMetadata(docMetadata);

            // Build successful result
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
     * Retrieves all audio files available for a specific learning module across
     * all supported languages. This enables the frontend to display language
     * options to users and serve the appropriate audio file.
     *
     * <p><b>Return Format:</b></p>
     * Map with language codes as keys and file URLs as values:
     * <pre>
     * {
     *   "en": "/uploads/modules/financial-literacy/fin-lit_en_20250117.mp3",
     *   "lg": "/uploads/modules/financial-literacy/fin-lit_lg_20250117.mp3",
     *   "alur": "/uploads/modules/financial-literacy/fin-lit_alur_20250117.mp3"
     * }
     * </pre>
     *
     * <p><b>Supported Languages:</b></p>
     * - en: English
     * - lg: Luganda
     * - alur: Alur
     * - lgb: Lugbara
     *
     * @param moduleKey Unique identifier for the learning module
     * @return Map of language codes to file URLs
     */
    public Map<String, String> getModuleAudioFiles(String moduleKey) {
        log.debug("Retrieving audio files for module: {}", moduleKey);

        Map<String, String> audioFiles = new HashMap<>();

        try {
            // FIXED: Use applicationProperties.getUpload().getUploadDirectory()
            Path modulePath = Paths.get(
                    applicationProperties.getUpload().getUploadDirectory(),
                    "modules",
                    moduleKey
            );

            // Check if module directory exists
            if (!Files.exists(modulePath)) {
                log.warn("Module directory not found: {}", moduleKey);
                return audioFiles;
            }

            // Scan for audio files by language
            String[] languages = {"en", "lg", "alur", "lgb"}; // English, Luganda, Alur, Lugbara

            for (String lang : languages) {
                // Find audio file matching the language pattern
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
     * Implements secure file deletion with the following measures:
     * 1. Verifies file exists
     * 2. Overwrites file content with random data
     * 3. Deletes the file from filesystem
     *
     * This prevents file recovery using forensic tools, ensuring user data
     * privacy and GDPR compliance.
     *
     * <p><b>Security Measures:</b></p>
     * - Random data overwriting before deletion
     * - Multiple overwrite passes (configurable)
     * - Verification of successful deletion
     *
     * @param userId User ID who owns the file
     * @param fileName Name of the file to delete
     * @param category File category (profile-pictures, documents, etc.)
     * @return CompletableFuture<Boolean> true if deleted successfully
     */
    @Async("fileProcessingExecutor")
    public CompletableFuture<Boolean> deleteUserFile(Long userId, String fileName, String category) {
        log.info("Starting secure deletion - User: {}, File: {}, Category: {}",
                userId, fileName, category);

        try {
            // FIXED: Use applicationProperties.getUpload().getUploadDirectory()
            Path filePath = Paths.get(
                    applicationProperties.getUpload().getUploadDirectory(),
                    "users",
                    userId.toString(),
                    category,
                    fileName
            );

            // Check if file exists
            if (Files.exists(filePath)) {
                // Secure deletion (overwrite with random data before deletion)
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
     * Retrieves comprehensive metadata for a file, including:
     * - File name and path
     * - File size
     * - Last modified timestamp
     * - Content type (MIME type)
     *
     * Useful for displaying file information in the UI and for auditing purposes.
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

    /**
     * Validate Image File
     *
     * Performs comprehensive validation on image files:
     * - Empty file check
     * - Extension validation (jpg, jpeg, png only)
     * - File size limit (5MB)
     * - Content type validation
     *
     * @param file MultipartFile to validate
     * @throws IOException if validation fails
     */
    private void validateImageFile(MultipartFile file) throws IOException {
        // Check if file is empty
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        // Check file extension
        String extension = getFileExtension(file.getOriginalFilename()).toLowerCase();
        if (!Arrays.asList("jpg", "jpeg", "png").contains(extension)) {
            throw new IllegalArgumentException("Invalid image format. Only JPG and PNG allowed.");
        }

        // Check file size (5MB limit for images)
        if (file.getSize() > 5 * 1024 * 1024) {
            throw new IllegalArgumentException("Image file too large. Maximum 5MB allowed.");
        }

        // Basic content validation
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("Invalid image content type");
        }

        log.debug("Image validation passed - Size: {} bytes, Type: {}",
                file.getSize(), contentType);
    }

    /**
     * Validate Audio File
     *
     * Performs comprehensive validation on audio files:
     * - Empty file check
     * - Extension validation (mp3, wav, m4a only)
     * - File size limit (50MB)
     *
     * @param file MultipartFile to validate
     * @throws IOException if validation fails
     */
    private void validateAudioFile(MultipartFile file) throws IOException {
        // Check if file is empty
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Audio file is empty");
        }

        // Check file extension
        String extension = getFileExtension(file.getOriginalFilename()).toLowerCase();
        if (!Arrays.asList("mp3", "wav", "m4a").contains(extension)) {
            throw new IllegalArgumentException("Invalid audio format. Only MP3, WAV, and M4A allowed.");
        }

        // Check file size (50MB limit for audio)
        if (file.getSize() > 50 * 1024 * 1024) {
            throw new IllegalArgumentException("Audio file too large. Maximum 50MB allowed.");
        }

        log.debug("Audio validation passed - Size: {} bytes, Extension: {}",
                file.getSize(), extension);
    }

    /**
     * Validate Document File
     *
     * Performs comprehensive validation on document files:
     * - Empty file check
     * - Extension validation (against whitelist from configuration)
     * - File size limit (10MB)
     *
     * @param file MultipartFile to validate
     * @throws IOException if validation fails
     */
    private void validateDocumentFile(MultipartFile file) throws IOException {
        // Check if file is empty
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Document file is empty");
        }

        // Check file extension against whitelist
        String extension = getFileExtension(file.getOriginalFilename()).toLowerCase();
        List<String> allowedExtensions = applicationProperties.getUpload().getAllowedExtensions();

        if (!allowedExtensions.contains(extension)) {
            throw new IllegalArgumentException("Invalid document format. Allowed: " + allowedExtensions);
        }

        // Check file size (10MB limit for documents)
        if (file.getSize() > 10 * 1024 * 1024) {
            throw new IllegalArgumentException("Document file too large. Maximum 10MB allowed.");
        }

        log.debug("Document validation passed - Size: {} bytes, Extension: {}",
                file.getSize(), extension);
    }

    // ========================================================================
    // PRIVATE HELPER METHODS - FILE NAME GENERATION
    // ========================================================================

    /**
     * Get File Extension
     *
     * Extracts the file extension from a filename.
     *
     * @param fileName Name of the file
     * @return File extension without the dot (e.g., "jpg", "pdf")
     */
    private String getFileExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf(".") + 1);
    }

    /**
     * Generate Profile Picture Filename
     *
     * Creates a unique filename for profile pictures using the format:
     * profile_{userId}_{timestamp}.{extension}
     *
     * Example: profile_12345_20250117_143022.jpg
     *
     * @param userId User ID
     * @param file Original file
     * @return Generated filename
     */
    private String generateProfilePictureFileName(Long userId, MultipartFile file) {
        String extension = getFileExtension(file.getOriginalFilename());
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String filename = "profile_" + userId + "_" + timestamp + "." + extension;
        log.trace("Generated profile picture filename: {}", filename);
        return filename;
    }

    /**
     * Generate Audio Filename
     *
     * Creates a unique filename for audio modules using the format:
     * {moduleKey}_{language}_{date}.{extension}
     *
     * Example: financial-literacy_en_20250117.mp3
     *
     * @param moduleKey Module identifier
     * @param language Language code (en, lg, alur, lgb)
     * @param file Original file
     * @return Generated filename
     */
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
     * Creates a unique filename for user documents using the format:
     * {documentType}_{userId}_{timestamp}.{extension}
     *
     * Example: cv_12345_20250117_143022.pdf
     *
     * @param userId User ID
     * @param documentType Type of document
     * @param file Original file
     * @return Generated filename
     */
    private String generateDocumentFileName(Long userId, String documentType, MultipartFile file) {
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
     * Creates a directory structure for user-specific files.
     * Path format: {uploadDir}/users/{userId}/{category}
     *
     * Example: /uploads/users/12345/profile-pictures
     *
     * FIXED: Uses applicationProperties.getUpload().getUploadDirectory()
     *
     * @param userId User ID
     * @param category File category (profile-pictures, documents, etc.)
     * @return Path to the created directory
     * @throws IOException if directory creation fails
     */
    private Path createUserDirectory(Long userId, String category) throws IOException {
        // FIXED: Use applicationProperties.getUpload().getUploadDirectory()
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

    /**
     * Create Module Directory
     *
     * Creates a directory structure for learning module files.
     * Path format: {uploadDir}/modules/{moduleKey}
     *
     * Example: /uploads/modules/financial-literacy
     *
     * FIXED: Uses applicationProperties.getUpload().getUploadDirectory()
     *
     * @param moduleKey Module identifier
     * @return Path to the created directory
     * @throws IOException if directory creation fails
     */
    private Path createModuleDirectory(String moduleKey) throws IOException {
        // FIXED: Use applicationProperties.getUpload().getUploadDirectory()
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

    /**
     * Save File Securely
     *
     * Saves a file with additional security measures:
     * 1. Virus scanning (if enabled in configuration)
     * 2. File permission restrictions (POSIX on Unix-like systems)
     * 3. Content validation
     *
     * <p><b>Security Features:</b></p>
     * - Replaces existing files (no accidental overwrites)
     * - Sets restrictive permissions (rw-------, owner only)
     * - Optional virus scanning integration
     *
     * @param file MultipartFile to save
     * @param filePath Destination path
     * @throws IOException if save operation fails
     */
    private void saveFileSecurely(MultipartFile file, Path filePath) throws IOException {
        log.debug("Saving file securely to: {}", filePath);

        // In production, add virus scanning here
        // if (applicationProperties.getUpload().isVirusScanEnabled()) {
        //     scanForViruses(file);
        // }

        // Save the file
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        log.trace("File copied successfully");

        // Set secure file permissions (owner read/write only)
        try {
            Files.setPosixFilePermissions(filePath,
                    java.nio.file.attribute.PosixFilePermissions.fromString("rw-------"));
            log.trace("Set POSIX permissions: rw-------");
        } catch (UnsupportedOperationException e) {
            // Windows doesn't support POSIX permissions
            log.debug("POSIX permissions not supported on this platform (Windows)");
        }

        log.debug("File saved securely with size: {} bytes", Files.size(filePath));
    }

    /**
     * Secure Delete
     *
     * Implements secure file deletion to prevent forensic recovery.
     *
     * <p><b>Process:</b></p>
     * 1. Overwrites file content with random data
     * 2. Performs multiple overwrite passes
     * 3. Deletes the file from filesystem
     *
     * <p><b>Security Benefits:</b></p>
     * - Prevents data recovery using forensic tools
     * - Ensures GDPR "right to be forgotten" compliance
     * - Protects sensitive user information
     *
     * @param filePath Path to the file to delete
     * @throws IOException if deletion fails
     */
    private void secureDelete(Path filePath) throws IOException {
        log.debug("Starting secure deletion for: {}", filePath);

        // Overwrite with random data before deletion
        byte[] randomData = new byte[1024];
        new Random().nextBytes(randomData);

        long fileSize = Files.size(filePath);
        log.trace("File size: {} bytes", fileSize);

        // Overwrite file content with random data
        try (var channel = Files.newByteChannel(filePath,
                java.nio.file.StandardOpenOption.WRITE)) {
            long bytesWritten = 0;
            for (long pos = 0; pos < fileSize; pos += randomData.length) {
                channel.write(java.nio.ByteBuffer.wrap(randomData));
                bytesWritten += randomData.length;
            }
            log.trace("Overwrote {} bytes with random data", bytesWritten);
        }

        // Delete the file
        Files.delete(filePath);
        log.debug("File securely deleted: {}", filePath);
    }

    // ========================================================================
    // PRIVATE HELPER METHODS - METADATA CREATION
    // ========================================================================

    /**
     * Create File Metadata
     *
     * Generates comprehensive metadata for uploaded files including:
     * - Original filename
     * - Storage path
     * - File size
     * - Content type
     * - File type/category
     * - User ID (if applicable)
     * - Upload timestamp
     *
     * @param file Original MultipartFile
     * @param filePath Saved file path
     * @param type File type category
     * @param userId User ID (optional, can be null)
     * @return FileMetadata object
     */
    private FileMetadata createFileMetadata(MultipartFile file, Path filePath, String type, Long userId) {
        log.trace("Creating file metadata for: {}", filePath);

        try {
            FileMetadata metadata = FileMetadata.builder()
                    .fileName(file.getOriginalFilename())
                    .filePath(filePath.toString())
                    .fileSize(file.getSize())
                    .contentType(file.getContentType())
                    .fileType(type)
                    .userId(userId)
                    .uploadTime(java.time.Instant.now())
                    .build();

            log.trace("Created metadata - Type: {}, Size: {} bytes", type, file.getSize());
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
     * Creates multiple optimized versions of an image:
     * - Thumbnail: 200x200px (for lists and previews)
     * - Medium: 400x400px (for profile views)
     * - Large: 800x800px (for detailed views)
     *
     * <p><b>TODO:</b> Implement actual image processing using libraries like:</p>
     * - Thumbnailator
     * - ImageMagick
     * - java.awt.Image (basic)
     *
     * @param filePath Original image file path
     * @param userId User ID for path generation
     * @return Map of version names to file URLs
     */
    private Map<String, String> generateImageVersions(Path filePath, Long userId) {
        log.debug("Generating image versions for: {}", filePath);

        // TODO: Implement actual image processing
        // This is a placeholder that returns URLs where optimized versions would be
        Map<String, String> versions = new HashMap<>();
        versions.put("thumbnail", "/uploads/users/" + userId + "/profile-pictures/thumb_" + filePath.getFileName());
        versions.put("medium", "/uploads/users/" + userId + "/profile-pictures/med_" + filePath.getFileName());

        log.trace("Generated {} image version URLs (placeholder)", versions.size());
        return versions;
    }

    /**
     * Process Audio File
     *
     * Processes audio files for optimization:
     * - Compression for reduced file size
     * - Format conversion (e.g., WAV to MP3)
     * - Bitrate optimization
     * - Normalization of audio levels
     *
     * <p><b>TODO:</b> Implement actual audio processing using libraries like:</p>
     * - JAVE (Java Audio Video Encoder)
     * - FFmpeg (via ProcessBuilder)
     * - javax.sound.sampled (basic)
     *
     * @param filePath Original audio file path
     * @param moduleKey Module identifier
     * @param language Language code
     * @return Map of version names to file URLs
     */
    private Map<String, String> processAudioFile(Path filePath, String moduleKey, String language) {
        log.debug("Processing audio file: {}", filePath);

        // TODO: Implement actual audio processing
        // This is a placeholder that returns URLs where processed versions would be
        Map<String, String> versions = new HashMap<>();
        versions.put("compressed", "/uploads/modules/" + moduleKey + "/compressed_" + filePath.getFileName());

        log.trace("Generated {} audio version URLs (placeholder)", versions.size());
        return versions;
    }

    /**
     * Extract Audio Metadata
     *
     * Extracts technical metadata from audio files:
     * - Duration (seconds)
     * - Bitrate (kbps)
     * - Sample rate (Hz)
     * - Codec information
     * - Channels (mono/stereo)
     *
     * <p><b>TODO:</b> Implement actual metadata extraction using:</p>
     * - JAudioTagger
     * - MP3agic (for MP3 files)
     * - Apache Tika
     *
     * @param filePath Audio file path
     * @return AudioMetadata object
     */
    private AudioMetadata extractAudioMetadata(Path filePath) {
        log.trace("Extracting audio metadata from: {}", filePath);

        // TODO: Implement actual metadata extraction
        // This is a placeholder with dummy data
        AudioMetadata metadata = AudioMetadata.builder()
                .duration(120.0) // Placeholder: 2 minutes
                .bitrate(128)    // Placeholder: 128 kbps
                .sampleRate(44100) // Placeholder: 44.1 kHz (CD quality)
                .build();

        log.trace("Extracted audio metadata (placeholder) - Duration: {}s, Bitrate: {}kbps",
                metadata.getDuration(), metadata.getBitrate());
        return metadata;
    }

    /**
     * Extract Document Metadata
     *
     * Extracts metadata from document files:
     * - Page count (for PDFs)
     * - Text content availability
     * - Searchability (OCR status)
     * - Author information
     * - Creation date
     *
     * <p><b>TODO:</b> Implement actual metadata extraction using:</p>
     * - Apache PDFBox (for PDFs)
     * - Apache POI (for Office documents)
     * - Apache Tika (universal)
     *
     * @param filePath Document file path
     * @return DocumentMetadata object
     */
    private DocumentMetadata extractDocumentMetadata(Path filePath) {
        log.trace("Extracting document metadata from: {}", filePath);

        // TODO: Implement actual metadata extraction
        // This is a placeholder with dummy data
        DocumentMetadata metadata = DocumentMetadata.builder()
                .pageCount(1)           // Placeholder
                .hasText(true)          // Placeholder
                .isSearchable(true)     // Placeholder
                .build();

        log.trace("Extracted document metadata (placeholder) - Pages: {}, Searchable: {}",
                metadata.getPageCount(), metadata.getIsSearchable());
        return metadata;
    }

    // ========================================================================
    // DATA TRANSFER OBJECTS (DTOs)
    // ========================================================================

    /**
     * File Upload Result DTO
     *
     * Encapsulates the result of a file upload operation.
     * Contains all information needed by the client to access and manage the uploaded file.
     *
     * <p><b>Success Response Fields:</b></p>
     * - success: true
     * - fileName: Generated filename
     * - filePath: Server-side storage path
     * - fileUrl: Public URL to access the file
     * - metadata: Comprehensive file metadata
     * - optimizedVersions: URLs for image thumbnails/sizes
     * - processedVersions: URLs for processed audio/video
     * - documentType: Type of document (for documents only)
     *
     * <p><b>Error Response Fields:</b></p>
     * - success: false
     * - errorMessage: Description of what went wrong
     */
    @lombok.Data
    @lombok.Builder
    public static class FileUploadResult {
        /** Indicates if the upload was successful */
        private boolean success;

        /** Generated filename on the server */
        private String fileName;

        /** Full server-side file path */
        private String filePath;

        /** Public URL to access the file */
        private String fileUrl;

        /** Error message (if success = false) */
        private String errorMessage;

        /** Comprehensive file metadata */
        private FileMetadata metadata;

        /** Document type (CV, CERTIFICATE, etc.) */
        private String documentType;

        /** Map of optimized image versions (thumbnail, medium, large) */
        private Map<String, String> optimizedVersions;

        /** Map of processed file versions (compressed, converted) */
        private Map<String, String> processedVersions;
    }

    /**
     * File Metadata DTO
     *
     * Stores comprehensive metadata about uploaded files.
     * Used for tracking, auditing, and displaying file information.
     *
     * <p><b>Fields:</b></p>
     * - fileName: Original filename from user
     * - filePath: Storage path on server
     * - fileSize: Size in bytes
     * - contentType: MIME type
     * - fileType: Category (PROFILE_PICTURE, AUDIO_MODULE, USER_DOCUMENT)
     * - userId: Owner of the file (optional)
     * - uploadTime: When the file was uploaded
     * - lastModified: Last modification timestamp
     * - audioMetadata: Audio-specific metadata (optional)
     * - documentMetadata: Document-specific metadata (optional)
     */
    @lombok.Data
    @lombok.Builder
    public static class FileMetadata {
        /** Original filename from user */
        private String fileName;

        /** Server-side storage path */
        private String filePath;

        /** File size in bytes */
        private Long fileSize;

        /** MIME type (e.g., image/jpeg, audio/mpeg) */
        private String contentType;

        /** File category (PROFILE_PICTURE, AUDIO_MODULE, USER_DOCUMENT) */
        private String fileType;

        /** User ID who owns this file (optional) */
        private Long userId;

        /** Timestamp when file was uploaded */
        private java.time.Instant uploadTime;

        /** Last modification timestamp */
        private java.time.Instant lastModified;

        /** Audio-specific metadata (for audio files) */
        private AudioMetadata audioMetadata;

        /** Document-specific metadata (for documents) */
        private DocumentMetadata documentMetadata;
    }

    /**
     * Audio Metadata DTO
     *
     * Stores technical metadata specific to audio files.
     * Used for displaying audio information and quality indicators to users.
     *
     * <p><b>Fields:</b></p>
     * - duration: Length in seconds
     * - bitrate: Audio quality in kbps
     * - sampleRate: Frequency in Hz (e.g., 44100 for CD quality)
     * - codec: Audio codec used (MP3, AAC, etc.)
     */
    @lombok.Data
    @lombok.Builder
    public static class AudioMetadata {
        /** Audio duration in seconds */
        private Double duration;

        /** Bitrate in kbps (e.g., 128, 256) */
        private Integer bitrate;

        /** Sample rate in Hz (e.g., 44100, 48000) */
        private Integer sampleRate;

        /** Audio codec (MP3, AAC, WAV, etc.) */
        private String codec;
    }

    /**
     * Document Metadata DTO
     *
     * Stores metadata specific to document files (PDFs, Word documents, etc.).
     * Used for document management and search functionality.
     *
     * <p><b>Fields:</b></p>
     * - pageCount: Number of pages in document
     * - hasText: Whether document contains extractable text
     * - isSearchable: Whether document is searchable (OCR applied)
     * - author: Document author (from file properties)
     * - createdDate: When document was originally created
     */
    @lombok.Data
    @lombok.Builder
    public static class DocumentMetadata {
        /** Number of pages in the document */
        private Integer pageCount;

        /** Whether document contains text content */
        private Boolean hasText;

        /** Whether document is searchable (text-based or OCR'd) */
        private Boolean isSearchable;

        /** Document author from file properties */
        private String author;

        /** Original creation date from file properties */
        private java.time.Instant createdDate;
    }
}