package com.youthconnect.user_service.exception;

/**
 * ============================================================================
 * UserNotFoundException - User Lookup Failure Exception
 * ============================================================================
 *
 * Custom runtime exception thrown when user lookup operations fail.
 *
 * ============================================================================
 * HTTP STATUS CODE MAPPING
 * ============================================================================
 * Mapped to: 404 NOT FOUND (GlobalExceptionHandler)
 *
 * ============================================================================
 * WHEN TO USE
 * ============================================================================
 * ✅ User lookup by ID fails
 * ✅ User lookup by email fails
 * ✅ User lookup by phone number fails
 * ✅ Profile retrieval for non-existent user
 * ✅ Internal service calls to user-service that fail
 * ✅ Admin operations on deleted/non-existent accounts
 *
 * ❌ DON'T USE FOR:
 * - Authentication failures (use InvalidCredentialsException)
 * - Authorization failures (use AccessDeniedException)
 * - Validation failures (use IllegalArgumentException or @Valid)
 *
 * ============================================================================
 * EXCEPTION HIERARCHY
 * ============================================================================
 * java.lang.RuntimeException (unchecked exception)
 *   └── UserNotFoundException
 *
 * WHY RUNTIMEEXCEPTION?
 * ✅ User not found is a recoverable error (not fatal)
 * ✅ No need to force try-catch everywhere
 * ✅ Spring's @Transactional works seamlessly
 * ✅ GlobalExceptionHandler catches it globally
 * ✅ Cleaner service layer code
 *
 * ============================================================================
 * USAGE EXAMPLES
 * ============================================================================
 *
 * Example 1: Lookup by ID
 * ------------------------
 * User user = userRepository.findById(userId)
 *     .orElseThrow(() -> new UserNotFoundException(userId));
 *
 * // Generated message: "User not found with ID: 123"
 *
 * Example 2: Lookup by Email
 * ---------------------------
 * User user = userRepository.findByEmail(email)
 *     .orElseThrow(() -> new UserNotFoundException("email", email));
 *
 * // Generated message: "User not found with email: john@example.com"
 *
 * Example 3: Lookup by Phone
 * ---------------------------
 * User user = userRepository.findByPhoneNumber(phone)
 *     .orElseThrow(() -> new UserNotFoundException("phone", phone));
 *
 * // Generated message: "User not found with phone: +256701234567"
 *
 * Example 4: Custom Message
 * --------------------------
 * if (user.isDeleted()) {
 *     throw new UserNotFoundException("User account has been deactivated");
 * }
 *
 * Example 5: With Root Cause
 * ---------------------------
 * try {
 *     User user = externalService.getUser(userId);
 * } catch (RemoteServiceException e) {
 *     throw new UserNotFoundException("Failed to retrieve user from external service", e);
 * }
 *
 * ============================================================================
 * GLOBAL EXCEPTION HANDLING
 * ============================================================================
 * Caught by GlobalExceptionHandler:
 *
 * @ExceptionHandler(UserNotFoundException.class)
 * public ResponseEntity<ApiResponse<Object>> handleUserNotFound(
 *         UserNotFoundException ex, WebRequest request) {
 *
 *     log.warn("User not found: {}", ex.getMessage());
 *
 *     return ResponseEntity
 *         .status(HttpStatus.NOT_FOUND)
 *         .body(ApiResponse.error(ex.getMessage()));
 * }
 *
 * Response format:
 * {
 *   "success": false,
 *   "message": "User not found with ID: 123",
 *   "data": null,
 *   "timestamp": "2024-01-15T10:30:00"
 * }
 *
 * ============================================================================
 * SECURITY CONSIDERATIONS
 * ============================================================================
 * ⚠️ Be careful with error messages in authentication contexts!
 *
 * BAD (enables username enumeration):
 * if (!userRepository.existsByEmail(email)) {
 *     throw new UserNotFoundException("email", email);
 * }
 * // Attacker can enumerate valid emails
 *
 * GOOD (prevents username enumeration):
 * User user = userRepository.findByEmail(email)
 *     .orElseThrow(() -> new InvalidCredentialsException("Invalid credentials"));
 * // Generic message doesn't reveal if email exists
 *
 * @author Youth Connect Uganda Development Team
 * @version 2.0.0 (Merged Ultimate Edition)
 * @since 1.0.0
 */
public class UserNotFoundException extends RuntimeException {

    /**
     * Create exception with custom message
     *
     * Use for specific, context-aware error messages.
     *
     * @param message Custom error message
     *
     * Example:
     * throw new UserNotFoundException("User account not found or has been deleted");
     * throw new UserNotFoundException("Requested user profile is unavailable");
     */
    public UserNotFoundException(String message) {
        super(message);
    }

    /**
     * Create exception with userId
     *
     * Generates standard formatted message with user ID.
     * Most common constructor for ID-based lookups.
     *
     * @param userId User's unique identifier
     *
     * Example:
     * User user = userRepository.findById(123L)
     *     .orElseThrow(() -> new UserNotFoundException(123L));
     *
     * Generated message: "User not found with ID: 123"
     */
    public UserNotFoundException(Long userId) {
        super("User not found with ID: " + userId);
    }

    /**
     * Create exception with field name and value
     *
     * Generates message showing which field was used for lookup.
     * Useful for email, phone, username lookups.
     *
     * @param field Lookup field name (email, phone, username, etc.)
     * @param value Value that was searched for
     *
     * Example:
     * User user = userRepository.findByEmail("john@example.com")
     *     .orElseThrow(() -> new UserNotFoundException("email", "john@example.com"));
     *
     * Generated message: "User not found with email: john@example.com"
     *
     * ⚠️ Security Note: In authentication contexts, use InvalidCredentialsException
     * instead to avoid revealing whether email/phone exists.
     */
    public UserNotFoundException(String field, String value) {
        super(String.format("User not found with %s: %s", field, value));
    }

    /**
     * Create exception with message and root cause
     *
     * Use for wrapping lower-level exceptions while preserving stack trace.
     *
     * @param message Custom error message
     * @param cause Root cause exception
     *
     * Example:
     * try {
     *     return databaseService.getUser(userId);
     * } catch (DataAccessException e) {
     *     throw new UserNotFoundException(
     *         "Database error while retrieving user",
     *         e
     *     );
     * }
     *
     * Benefits:
     * - Preserves original stack trace for debugging
     * - Allows upper layers to catch UserNotFoundException
     * - Provides context about what operation failed
     */
    public UserNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    // ========================================================================
    // STATIC FACTORY METHODS (Alternative API)
    // ========================================================================

    /**
     * Factory method: User not found by ID
     *
     * Alternative to constructor for better readability.
     *
     * @param userId User ID that wasn't found
     * @return UserNotFoundException instance
     *
     * Example:
     * throw UserNotFoundException.byId(userId);
     */
    public static UserNotFoundException byId(Long userId) {
        return new UserNotFoundException(userId);
    }

    /**
     * Factory method: User not found by email
     *
     * @param email Email address that wasn't found
     * @return UserNotFoundException instance
     *
     * Example:
     * throw UserNotFoundException.byEmail("john@example.com");
     *
     * ⚠️ Security: Use only in non-authentication contexts
     */
    public static UserNotFoundException byEmail(String email) {
        return new UserNotFoundException("email", email);
    }

    /**
     * Factory method: User not found by phone
     *
     * @param phoneNumber Phone number that wasn't found
     * @return UserNotFoundException instance
     *
     * Example:
     * throw UserNotFoundException.byPhone("+256701234567");
     *
     * ⚠️ Security: Use only in non-authentication contexts
     */
    public static UserNotFoundException byPhone(String phoneNumber) {
        return new UserNotFoundException("phone", phoneNumber);
    }

    /**
     * Factory method: Generic user not found
     *
     * @return UserNotFoundException with generic message
     *
     * Example:
     * throw UserNotFoundException.notFound();
     *
     * Message: "Requested user was not found"
     */
    public static UserNotFoundException notFound() {
        return new UserNotFoundException("Requested user was not found");
    }
}