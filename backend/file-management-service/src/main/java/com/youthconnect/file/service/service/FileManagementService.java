package com.youthconnect.file.service.service;

import com.youthconnect.file_service.config.FileStorageProperties;
import com.youthconnect.file_service.dto.FileUploadResult;
import com.youthconnect.file_service.dto.FileMetadata;
import com.youthconnect.file_service.util.FileValidationUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileManagementService {

    private final FileStorageProperties storageProperties;
    private final FileValidationUtil validationUtil;
    private final Tika tika = new Tika();

    /**
     * Upload profile picture for a user
     */
    @Async
    public CompletableFuture<FileUploadResult> uploadProfilePicture(Long userId, MultipartFile file) {
        log.info("Uploading profile picture for user: {}", userId);

        try {
            // Validate image file
            validationUtil.validateImageFile(file);

            // Generate unique filename
            String fileName = generateProfilePictureFileName(userId, file);

            // Create user directory
            Path uploadPath = createUserDirectory(userId, "profile-pictures");

            // Save file
            Path filePath = uploadPath.resolve(fileName);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // Generate optimized versions (placeholder - implement actual image processing)
            Map<String, String> optimizedVersions = generateImageVersions(filePath, userId);

            // Create file metadata
            FileMetadata metadata = createFileMetadata(file, filePath, "PROFILE_PICTURE", userId);

            FileUploadResult result = FileUploadResult.builder()
                    .success(true)
                    .fileName(fileName)
                    .filePath(filePath.toString())
                    .fileUrl(buildFileUrl("users/" + userId + "/profile-pictures", fileName))
                    .metadata(metadata)
                    .optimizedVersions(optimizedVersions)
                    .build();

            log.info("Profile picture uploaded successfully for user: {}", userId);
            return CompletableFuture.completedFuture(result);

        } catch (Exception e) {
            log.error("Failed to upload profile picture for user: {}", userId, e);
            return CompletableFuture.completedFuture(
                    FileUploadResult.builder()
                            .success(false)
                            .errorMessage(e.getMessage())
                            .build());
        }
    }

    /**
     * Upload audio file for learning modules
     */
    @Async
    public CompletableFuture<FileUploadResult> uploadAudioModule(String moduleKey, String language, MultipartFile file) {
        log.info("Uploading audio module: {} for language: {}", moduleKey, language);

        try {
            // Validate audio file
            validationUtil.validateAudioFile(file);

            // Generate filename with language code
            String fileName = generateAudioFileName(moduleKey, language, file);

            // Create module directory
            Path uploadPath = createModuleDirectory(moduleKey);

            // Save original file
            Path filePath = uploadPath.resolve(fileName);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // Process audio (placeholder - implement actual audio processing)
            Map<String, String> processedVersions = processAudioFile(filePath, moduleKey, language);

            FileMetadata metadata = createFileMetadata(file, filePath, "AUDIO_MODULE", null);

            FileUploadResult result = FileUploadResult.builder()
                    .success(true)
                    .fileName(fileName)
                    .filePath(filePath.toString())
                    .fileUrl(buildFileUrl("modules/" + moduleKey, fileName))
                    .metadata(metadata)
                    .processedVersions(processedVersions)
                    .build();

            log.info("Audio module uploaded successfully: {}", moduleKey);
            return CompletableFuture.completedFuture(result);

        } catch (Exception e) {
            log.error("Failed to upload audio module: {}", moduleKey, e);
            return CompletableFuture.completedFuture(
                    FileUploadResult.builder()
                            .success(false)
                            .errorMessage(e.getMessage())
                            .build());
        }
    }

    /**
     * Upload document for user
     */
    @Async
    public CompletableFuture<FileUploadResult> uploadUserDocument(Long userId, String documentType, MultipartFile file) {
        log.info("Uploading {} document for user: {}", documentType, userId);

        try {
            // Validate document file
            validationUtil.validateDocumentFile(file);

            // Generate secure filename
            String fileName = generateDocumentFileName(userId, documentType, file);

            // Create user documents directory
            Path uploadPath = createUserDirectory(userId, "documents");

            // Save file
            Path filePath = uploadPath.resolve(fileName);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            FileMetadata metadata = createFileMetadata(file, filePath, "USER_DOCUMENT", userId);

            FileUploadResult result = FileUploadResult.builder()
                    .success(true)
                    .fileName(fileName)
                    .filePath(filePath.toString())
                    .fileUrl(buildFileUrl("users/" + userId + "/documents", fileName))
                    .metadata(metadata)
                    .documentType(documentType)
                    .build();

            log.info("Document uploaded successfully for user: {}", userId);
            return CompletableFuture.completedFuture(result);

        } catch (Exception e) {
            log.error("Failed to upload document for user: {}", userId, e);
            return CompletableFuture.completedFuture(
                    FileUploadResult.builder()
                            .success(false)
                            .errorMessage(e.getMessage())
                            .build());
        }
    }

    /**
     * Get audio files for a learning module in all available languages
     */
    public Map<String, String> getModuleAudioFiles(String moduleKey) {
        log.debug("Retrieving audio files for module: {}", moduleKey);

        Map<String, String> audioFiles = new HashMap<>();

        try {
            Path modulePath = Paths.get(storageProperties.getStoragePath(), "modules", moduleKey);

            if (!Files.exists(modulePath)) {
                log.warn("Module directory not found: {}", moduleKey);
                return audioFiles;
            }

            // Scan for audio files by language
            String[] languages = {"en", "lg", "alur", "lgb"}; // English, Luganda, Alur, Lugbara

            for (String lang : languages) {
                try {
                    Files.list(modulePath)
                            .filter(path -> path.getFileName().toString().contains("_" + lang + "."))
                            .filter(path -> validationUtil.isAudioFile(path.getFileName().toString()))
                            .findFirst()
                            .ifPresent(audioFile -> {
                                String relativePath = buildFileUrl("modules/" + moduleKey, audioFile.getFileName().toString());
                                audioFiles.put(lang, relativePath);
                            });
                } catch (IOException e) {
                    log.warn("Error scanning for language {} in module {}: {}", lang, moduleKey, e.getMessage());
                }
            }

            log.debug("Found {} audio files for module: {}", audioFiles.size(), moduleKey);

        } catch (Exception e) {
            log.error("Failed to retrieve audio files for module: {}", moduleKey, e);
        }

        return audioFiles;
    }

    /**
     * Delete user file securely
     */
    @Async
    public CompletableFuture<Boolean> deleteUserFile(Long userId, String fileName, String category) {
        log.info("Deleting file: {} for user: {} in category: {}", fileName, userId, category);

        try {
            Path filePath = Paths.get(storageProperties.getStoragePath(), "users", userId.toString(), category, fileName);

            if (Files.exists(filePath)) {
                Files.delete(filePath);
                log.info("File deleted successfully: {}", fileName);
                return CompletableFuture.completedFuture(true);
            } else {
                log.warn("File not found for deletion: {}", fileName);
                return CompletableFuture.completedFuture(false);
            }

        } catch (Exception e) {
            log.error("Failed to delete file: {}", fileName, e);
            return CompletableFuture.completedFuture(false);
        }
    }

    /**
     * Load file as resource for download
     */
    public Resource loadFileAsResource(String category, String fileName, Long userId) {
        try {
            Path filePath;
            if (userId != null) {
                filePath = Paths.get(storageProperties.getStoragePath(), "users", userId.toString(), category, fileName);
            } else {
                filePath = Paths.get(storageProperties.getStoragePath(), category, fileName);
            }

            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                throw new RuntimeException("File not found or not readable: " + fileName);
            }
        } catch (Exception e) {
            throw new RuntimeException("File not found: " + fileName, e);
        }
    }

    /**
     * Get file metadata
     */
    public FileMetadata getFileMetadata(String category, String fileName, Long userId) {
        try {
            Path filePath;
            if (userId != null) {
                filePath = Paths.get(storageProperties.getStoragePath(), "users", userId.toString(), category, fileName);
            } else {
                filePath = Paths.get(storageProperties.getStoragePath(), category, fileName);
            }

            if (Files.exists(filePath)) {
                return FileMetadata.builder()
                        .fileName(fileName)
                        .filePath(filePath.toString())
                        .fileSize(Files.size(filePath))
                        .lastModified(Files.getLastModifiedTime(filePath).toInstant())
                        .contentType(getContentType(fileName))
                        .build();
            }
        } catch (Exception e) {
            log.error("Failed to get file metadata for: {}", fileName, e);
        }
        return null;
    }

    /**
     * Get content type for file
     */
    public String getContentType(String fileName) {
        try {
            String extension = getFileExtension(fileName).toLowerCase();
            return switch (extension) {
                case "jpg", "jpeg" -> "image/jpeg";
                case "png" -> "image/png";
                case "pdf" -> "application/pdf";
                case "doc" -> "application/msword";
                case "docx" -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
                case "mp3" -> "audio/mpeg";
                case "wav" -> "audio/wav";
                case "m4a" -> "audio/mp4";
                default -> "application/octet-stream";
            };
        } catch (Exception e) {
            return "application/octet-stream";
        }
    }

    /**
     * Check storage health
     */
    public boolean checkStorageHealth() {
        try {
            Path storagePath = Paths.get(storageProperties.getStoragePath());
            return Files.exists(storagePath) && Files.isWritable(storagePath);
        } catch (Exception e) {
            log.error("Storage health check failed: {}", e.getMessage());
            return false;
        }
    }

    // Private helper methods
    private String getFileExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf(".") + 1);
    }

    private String generateProfilePictureFileName(Long userId, MultipartFile file) {
        String extension = getFileExtension(file.getOriginalFilename());
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        return "profile_" + userId + "_" + timestamp + "." + extension;
    }

    private String generateAudioFileName(String moduleKey, String language, MultipartFile file) {
        String extension = getFileExtension(file.getOriginalFilename());
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        return moduleKey + "_" + language + "_" + timestamp + "." + extension;
    }

    private String generateDocumentFileName(Long userId, String documentType, MultipartFile file) {
        String extension = getFileExtension(file.getOriginalFilename());
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        return documentType.toLowerCase() + "_" + userId + "_" + timestamp + "." + extension;
    }

    private Path createUserDirectory(Long userId, String category) throws IOException {
        Path userDir = Paths.get(storageProperties.getStoragePath(), "users", userId.toString(), category);
        Files.createDirectories(userDir);
        return userDir;
    }

    private Path createModuleDirectory(String moduleKey) throws IOException {
        Path moduleDir = Paths.get(storageProperties.getStoragePath(), "modules", moduleKey);
        Files.createDirectories(moduleDir);
        return moduleDir;
    }

    private String buildFileUrl(String path, String fileName) {
        return storageProperties.getBaseUrl() + "/api/files/download/" + path + "/" + fileName;
    }

    private FileMetadata createFileMetadata(MultipartFile file, Path filePath, String type, Long userId) {
        try {
            return FileMetadata.builder()
                    .fileName(file.getOriginalFilename())
                    .filePath(filePath.toString())
                    .fileSize(file.getSize())
                    .contentType(file.getContentType())
                    .fileType(type)
                    .userId(userId)
                    .uploadTime(java.time.Instant.now())
                    .build();
        } catch (Exception e) {
            log.error("Failed to create file metadata", e);
            return FileMetadata.builder().build();
        }
    }

    // Placeholder methods for advanced features
    private Map<String, String> generateImageVersions(Path filePath, Long userId) {
        // Implement actual image processing
        Map<String, String> versions = new HashMap<>();
        versions.put("thumbnail", buildFileUrl("users/" + userId + "/profile-pictures", "thumb_" + filePath.getFileName()));
        versions.put("medium", buildFileUrl("users/" + userId + "/profile-pictures", "med_" + filePath.getFileName()));
        return versions;
    }

    private Map<String, String> processAudioFile(Path filePath, String moduleKey, String language) {
        // Implement actual audio processing
        Map<String, String> versions = new HashMap<>();
        versions.put("compressed", buildFileUrl("modules/" + moduleKey, "compressed_" + filePath.getFileName()));
        return versions;
    }
}
