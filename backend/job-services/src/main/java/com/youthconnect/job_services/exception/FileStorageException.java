package com.youthconnect.job_services.exception;

/**
 * ═══════════════════════════════════════════════════════════════════════════
 * FILE STORAGE EXCEPTION
 * ═══════════════════════════════════════════════════════════════════════════
 *
 * Custom exception for file storage operations.
 * Thrown when file upload, download, or deletion operations fail.
 *
 * COMMON SCENARIOS:
 * - File too large
 * - Invalid file type
 * - Storage directory not accessible
 * - File not found
 * - I/O errors during read/write
 *
 * USAGE EXAMPLE:
 * try {
 *     uploadFile(file);
 * } catch (FileStorageException e) {
 *     log.error("Upload failed: {}", e.getMessage());
 *     return ResponseEntity.status(HttpStatus.BAD_REQUEST)
 *         .body(new ErrorResponse(e.getMessage()));
 * }
 *
 * @author Douglas Kings Kato
 * @version 1.0.0
 */
public class FileStorageException extends RuntimeException {

    /**
     * Constructs a new FileStorageException with the specified detail message.
     *
     * @param message Detailed error message
     */
    public FileStorageException(String message) {
        super(message);
    }

    /**
     * Constructs a new FileStorageException with the specified detail message
     * and cause.
     *
     * This is useful for wrapping lower-level exceptions (like IOException)
     * while preserving the stack trace.
     *
     * @param message Detailed error message
     * @param cause The cause of the exception (can be null)
     */
    public FileStorageException(String message, Throwable cause) {
        super(message, cause);
    }
}