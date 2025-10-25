package com.youthconnect.user_service.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * ============================================================================
 * Standardized API Response Wrapper for Youth Connect Uganda Platform
 * ============================================================================
 *
 * Provides a unified response structure across ALL microservices and endpoints.
 * Every REST API endpoint MUST return an ApiResponse for consistency.
 *
 * ============================================================================
 * RESPONSE STRUCTURE
 * ============================================================================
 * {
 *   "success": true/false,           // Operation outcome indicator
 *   "message": "...",                // Human-readable description
 *   "data": {...},                   // Response payload (nullable)
 *   "timestamp": "2024-01-15T...",   // Response generation time
 *   "errorCode": "...",              // Optional error classification
 *   "path": "..."                    // Optional request path for debugging
 * }
 *
 * ============================================================================
 * FIELD DESCRIPTIONS
 * ============================================================================
 *
 * @field success (boolean, required)
 *   - true: Operation completed successfully
 *   - false: Operation failed (error occurred)
 *
 * @field message (String, required)
 *   - Success: "User registered successfully", "Profile updated"
 *   - Error: "User not found", "Validation failed"
 *
 * @field data (T, optional)
 *   - Success: Contains requested data (UserDTO, List<OpportunityDTO>, etc.)
 *   - Error: May contain error details (validation errors map)
 *   - null for simple responses
 *
 * @field timestamp (LocalDateTime, auto-generated)
 *   - When response was created
 *   - Used for: Request tracking, performance monitoring, debugging
 *
 * @field errorCode (String, optional)
 *   - Error classification for programmatic handling
 *   - Examples: "USER_NOT_FOUND", "VALIDATION_ERROR", "UNAUTHORIZED"
 *   - null for successful responses
 *
 * @field path (String, optional)
 *   - Request URI where error occurred
 *   - Useful for debugging and log correlation
 *   - Typically set by GlobalExceptionHandler
 *
 * ============================================================================
 * USAGE EXAMPLES
 * ============================================================================
 *
 * SUCCESS RESPONSES:
 * ------------------
 * // With data and custom message
 * return ResponseEntity.ok(
 *     ApiResponse.success("User found successfully", userDTO)
 * );
 *
 * // With data only (default message)
 * return ResponseEntity.ok(
 *     ApiResponse.success(userDTO)
 * );
 *
 * // Message only (no data)
 * return ResponseEntity.ok(
 *     ApiResponse.success("Profile updated successfully")
 * );
 *
 * ERROR RESPONSES:
 * ----------------
 * // Simple error with message
 * return ResponseEntity
 *     .status(HttpStatus.NOT_FOUND)
 *     .body(ApiResponse.error("User not found"));
 *
 * // Error with classification code
 * return ResponseEntity
 *     .status(HttpStatus.CONFLICT)
 *     .body(ApiResponse.error("User already exists", "USER_ALREADY_EXISTS"));
 *
 * // Error with validation details
 * Map<String, String> errors = new HashMap<>();
 * errors.put("email", "Invalid email format");
 * return ResponseEntity
 *     .badRequest()
 *     .body(ApiResponse.error("Validation failed", errors));
 *
 * ============================================================================
 * BENEFITS
 * ============================================================================
 * ✅ Consistent response contract across all services
 * ✅ Simplified client-side error handling
 * ✅ Better API documentation (Swagger/OpenAPI)
 * ✅ Structured logging and debugging
 * ✅ Frontend can parse all responses uniformly
 * ✅ Supports complex error scenarios (validation, business logic)
 *
 * ============================================================================
 * INTEGRATION WITH SPRING BOOT
 * ============================================================================
 * - Controllers return ResponseEntity<ApiResponse<T>>
 * - GlobalExceptionHandler wraps all exceptions in ApiResponse
 * - Feign clients can deserialize ApiResponse from other services
 * - Jackson automatically serializes/deserializes with @JsonInclude
 *
 * @param <T> Type of response payload (UserDTO, List<String>, Map, etc.)
 * @author Youth Connect Uganda Development Team
 * @version 3.0.0 (Merged Ultimate Edition)
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL) // Exclude null fields from JSON
public class ApiResponse<T> {

    // ========================================================================
    // CORE RESPONSE FIELDS
    // ========================================================================

    /**
     * Operation success indicator
     *
     * true = Operation completed successfully
     * false = Operation failed (error occurred)
     */
    private boolean success;

    /**
     * Human-readable message describing the outcome
     *
     * Success examples:
     * - "User registered successfully"
     * - "Profile updated"
     * - "Opportunity created"
     *
     * Error examples:
     * - "User not found"
     * - "Validation failed"
     * - "Invalid credentials"
     */
    private String message;

    /**
     * Response data payload (generic type)
     *
     * Can be:
     * - Single object: UserDTO, OpportunityDTO, ProfileDTO
     * - Collection: List<UserDTO>, Set<String>
     * - Map: Map<String, String> for validation errors
     * - Primitive: Long (user ID), Integer (count)
     * - null: For simple success/error messages
     */
    private T data;

    /**
     * Response creation timestamp
     *
     * Automatically set to current time when response is built.
     *
     * Use cases:
     * - Request tracking and correlation
     * - Performance monitoring (compare with request timestamp)
     * - Debugging timeout issues
     * - Log aggregation and analysis
     */
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();

    // ========================================================================
    // OPTIONAL ERROR CLASSIFICATION FIELDS
    // ========================================================================

    /**
     * Error code for programmatic error handling
     *
     * Enables clients to handle errors programmatically without parsing messages.
     *
     * Common error codes:
     * - "USER_NOT_FOUND"           → 404
     * - "USER_ALREADY_EXISTS"      → 409
     * - "VALIDATION_ERROR"         → 400
     * - "UNAUTHORIZED"             → 401
     * - "FORBIDDEN"                → 403
     * - "INTERNAL_SERVER_ERROR"    → 500
     *
     * null for successful responses.
     */
    private String errorCode;

    /**
     * Request path where error occurred
     *
     * Primarily useful in error responses for:
     * - Debugging which endpoint failed
     * - Correlating errors with API documentation
     * - Frontend error routing and display
     *
     * Example: "/api/v1/users/123"
     *
     * null for successful responses or when not relevant.
     */
    private String path;

    // ========================================================================
    // SUCCESS RESPONSE FACTORY METHODS
    // ========================================================================

    /**
     * Create success response with data and custom message
     *
     * Use when you want both data and a specific success message.
     *
     * @param data Response payload
     * @param message Custom success message
     * @param <T> Type of data
     * @return ApiResponse with success=true
     *
     * Example:
     * ApiResponse.success(userDTO, "User profile retrieved successfully")
     */
    public static <T> ApiResponse<T> success(T data, String message) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Create success response with data only (default message)
     *
     * Use when data is self-explanatory and custom message isn't needed.
     * Uses generic "Operation successful" message.
     *
     * @param data Response payload
     * @param <T> Type of data
     * @return ApiResponse with success=true and generic message
     *
     * Example:
     * ApiResponse.success(opportunityList)
     */
    public static <T> ApiResponse<T> success(T data) {
        return success(data, "Operation completed successfully");
    }

    /**
     * Create success response with message only (no data)
     *
     * Use for operations that don't return data (updates, deletes).
     *
     * @param message Success message
     * @param <T> Type parameter (will be null)
     * @return ApiResponse with success=true, data=null
     *
     * Example:
     * ApiResponse.success("Profile updated successfully")
     * ApiResponse.success("User deleted successfully")
     */
    public static <T> ApiResponse<T> success(String message) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .data(null)
                .timestamp(LocalDateTime.now())
                .build();
    }

    // ========================================================================
    // ERROR RESPONSE FACTORY METHODS
    // ========================================================================

    /**
     * Create simple error response with message only
     *
     * Use for straightforward errors without additional details.
     *
     * @param message Error description
     * @param <T> Type parameter (will be null)
     * @return ApiResponse with success=false, data=null
     *
     * Example:
     * ApiResponse.error("User not found")
     * ApiResponse.error("Invalid credentials")
     *
     * Used by:
     * - UserNotFoundException
     * - InvalidCredentialsException
     * - Simple error scenarios
     */
    public static <T> ApiResponse<T> error(String message) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .data(null)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Create error response with message and error code
     *
     * Use when clients need to handle errors programmatically.
     * Error code enables conditional logic on frontend.
     *
     * @param message Human-readable error description
     * @param errorCode Machine-readable error classification
     * @param <T> Type parameter (will be null)
     * @return ApiResponse with success=false and error code
     *
     * Example:
     * ApiResponse.error("User already exists", "USER_ALREADY_EXISTS")
     * ApiResponse.error("Access denied", "FORBIDDEN")
     *
     * Frontend handling:
     * if (response.errorCode === "USER_ALREADY_EXISTS") {
     *     showLoginInstead();
     * }
     */
    public static <T> ApiResponse<T> error(String message, String errorCode) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .errorCode(errorCode)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Create error response with message and detailed error data
     *
     * ⭐ CRITICAL METHOD FOR VALIDATION ERRORS ⭐
     *
     * Use for complex errors requiring additional details:
     * - Validation errors with field-level messages
     * - Business rule violations with context
     * - Multi-faceted error scenarios
     *
     * @param message Error summary message
     * @param data Detailed error information (usually Map<String, String>)
     * @param <T> Type of error data
     * @return ApiResponse with success=false and error details
     *
     * Example:
     * Map<String, String> validationErrors = new HashMap<>();
     * validationErrors.put("email", "Email format is invalid");
     * validationErrors.put("phone", "Phone must be Ugandan format (+256...)");
     * validationErrors.put("firstName", "First name is required");
     *
     * ApiResponse.error("Validation failed", validationErrors)
     *
     * Response:
     * {
     *   "success": false,
     *   "message": "Validation failed",
     *   "data": {
     *     "email": "Email format is invalid",
     *     "phone": "Phone must be Ugandan format (+256...)",
     *     "firstName": "First name is required"
     *   }
     * }
     *
     * Used by:
     * - MethodArgumentNotValidException in GlobalExceptionHandler
     * - Custom validation logic in services
     * - Complex error scenarios
     */
    public static <T> ApiResponse<T> error(String message, T data) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Create error response from exception
     *
     * Extracts error information from exception.
     * Error code set to exception class name.
     *
     * ⚠️ SECURITY WARNING ⚠️
     * Use ONLY in development environments!
     * Never expose exception details in production.
     *
     * @param message Custom error message (don't use exception message directly)
     * @param exception Exception that occurred
     * @param <T> Type parameter (will be null)
     * @return ApiResponse with success=false
     *
     * Example (DEVELOPMENT ONLY):
     * try {
     *     // operation
     * } catch (DatabaseException e) {
     *     return ApiResponse.error("Database error occurred", e);
     * }
     *
     * Production alternative:
     * Log exception server-side, return generic message:
     * log.error("Database error", e);
     * return ApiResponse.error("An error occurred. Please try again.");
     */
    public static <T> ApiResponse<T> error(String message, Exception exception) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .errorCode(exception.getClass().getSimpleName())
                .data(null)
                .timestamp(LocalDateTime.now())
                .build();
    }

    // ========================================================================
    // UTILITY METHODS
    // ========================================================================

    /**
     * Check if response represents successful operation
     *
     * Convenience method for null-safe success check.
     *
     * @return true if operation was successful, false otherwise
     *
     * Usage:
     * if (response.isSuccess()) {
     *     processData(response.getData());
     * }
     */
    public boolean isSuccess() {
        return success;
    }

    /**
     * Check if response contains data payload
     *
     * @return true if data is not null, false if data is null
     *
     * Usage:
     * if (response.hasData()) {
     *     UserDTO user = response.getData();
     * }
     */
    public boolean hasData() {
        return data != null;
    }

    /**
     * Get data or throw exception if null
     *
     * Use when data is expected and null would be a programming error.
     *
     * @return Data payload
     * @throws IllegalStateException if data is null
     *
     * Usage:
     * UserDTO user = response.getDataOrThrow();
     * // Throws if response has no data
     */
    public T getDataOrThrow() {
        if (data == null) {
            throw new IllegalStateException(
                    "No data available in response. " +
                            "Check hasData() before accessing data, or use getData() with null check."
            );
        }
        return data;
    }

    /**
     * Check if response represents an error
     *
     * Convenience method (inverse of isSuccess).
     *
     * @return true if operation failed, false if successful
     *
     * Usage:
     * if (response.isError()) {
     *     handleError(response.getMessage());
     * }
     */
    public boolean isError() {
        return !success;
    }

    /**
     * Check if response has an error code
     *
     * @return true if errorCode is not null, false otherwise
     *
     * Usage:
     * if (response.hasErrorCode()) {
     *     switch (response.getErrorCode()) {
     *         case "USER_NOT_FOUND": ...
     *         case "VALIDATION_ERROR": ...
     *     }
     * }
     */
    public boolean hasErrorCode() {
        return errorCode != null && !errorCode.isEmpty();
    }

    // ========================================================================
    // BUILDER CUSTOMIZATION (Advanced Usage)
    // ========================================================================

    /**
     * Create builder for custom response construction
     *
     * Use when you need full control over all fields.
     * Most use cases should use factory methods instead.
     *
     * Example:
     * ApiResponse.<UserDTO>builder()
     *     .success(true)
     *     .message("Custom message")
     *     .data(userDTO)
     *     .errorCode(null)
     *     .path("/api/users/123")
     *     .timestamp(LocalDateTime.now())
     *     .build()
     */
    // Lombok @Builder provides this automatically

    /**
     * Create error response with path for debugging
     *
     * Typically called by GlobalExceptionHandler.
     *
     * @param message Error message
     * @param path Request path where error occurred
     * @param <T> Type parameter
     * @return ApiResponse with path field set
     */
    public static <T> ApiResponse<T> errorWithPath(String message, String path) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .path(path)
                .timestamp(LocalDateTime.now())
                .build();
    }
}