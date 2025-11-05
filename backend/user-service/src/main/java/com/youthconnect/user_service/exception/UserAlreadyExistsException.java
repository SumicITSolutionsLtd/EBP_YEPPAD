package com.youthconnect.user_service.exception;

/**
 * ============================================================================
 * UserAlreadyExistsException - Duplicate User Registration Exception
 * ============================================================================
 *
 * Exception thrown when attempting to register a user with
 * email or phone number that already exists in the system.
 *
 * ============================================================================
 * HTTP STATUS CODE MAPPING
 * ============================================================================
 * Mapped to: 409 CONFLICT (GlobalExceptionHandler)
 *
 * ============================================================================
 * WHEN TO USE
 * ============================================================================
 * ✅ Email already registered
 * ✅ Phone number already registered
 * ✅ Username already taken (if implemented)
 * ✅ Duplicate user creation attempt
 * ✅ Account reactivation conflicts
 *
 * ❌ DON'T USE FOR:
 * - Login failures (use InvalidCredentialsException)
 * - Duplicate non-user entities (create specific exceptions)
 * - Validation failures (use @Valid annotations)
 *
 * ============================================================================
 * USAGE EXAMPLES
 * ============================================================================
 *
 * Example 1: Check email uniqueness
 * ----------------------------------
 * if (userRepository.existsByEmail(email)) {
 *     throw UserAlreadyExistsException.forEmail(email);
 * }
 *
 * Example 2: Check phone uniqueness
 * ----------------------------------
 * if (userRepository.existsByPhoneNumber(phone)) {
 *     throw UserAlreadyExistsException.forPhone(phone);
 * }
 *
 * Example 3: Generic duplicate check
 * -----------------------------------
 * Optional<User> existing = userRepository.findByEmail(email);
 * if (existing.isPresent()) {
 *     throw new UserAlreadyExistsException(
 *         "User with this email already exists"
 *     );
 * }
 *
 * Example 4: Handle database integrity violations
 * ------------------------------------------------
 * try {
 *     userRepository.save(user);
 * } catch (DataIntegrityViolationException e) {
 *     throw new UserAlreadyExistsException("Duplicate user detected", e);
 * }
 *
 * ============================================================================
 * GLOBAL EXCEPTION HANDLING
 * ============================================================================
 * Response format:
 * {
 *   "success": false,
 *   "message": "User already exists with email: john@example.com",
 *   "data": null,
 *   "timestamp": "2024-01-15T10:30:00"
 * }
 *
 * @author Douglas Kings Kato
 * @version 2.0.0 (Merged Ultimate Edition)
 * @since 1.0.0
 */
public class UserAlreadyExistsException extends RuntimeException {

    /**
     * Create exception with custom message
     *
     * @param message Custom error message
     *
     * Example:
     * throw new UserAlreadyExistsException("User account already exists");
     */
    public UserAlreadyExistsException(String message) {
        super(message);
    }

    /**
     * Create exception with message and cause
     *
     * @param message Error message
     * @param cause Root cause exception
     *
     * Example:
     * try {
     *     userRepository.save(user);
     * } catch (DataIntegrityViolationException e) {
     *     throw new UserAlreadyExistsException("Duplicate user detected", e);
     * }
     */
    public UserAlreadyExistsException(String message, Throwable cause) {
        super(message, cause);
    }

    // ========================================================================
    // STATIC FACTORY METHODS (Recommended API)
    // ========================================================================

    /**
     * Factory method: User already exists with this email
     *
     * @param email Duplicate email address
     * @return UserAlreadyExistsException instance
     *
     * Example:
     * if (userRepository.existsByEmail(email)) {
     *     throw UserAlreadyExistsException.forEmail(email);
     * }
     */
    public static UserAlreadyExistsException forEmail(String email) {
        return new UserAlreadyExistsException(
                "User already exists with email: " + email
        );
    }

    /**
     * Factory method: User already exists with this phone number
     *
     * @param phoneNumber Duplicate phone number
     * @return UserAlreadyExistsException instance
     *
     * Example:
     * if (userRepository.existsByPhoneNumber(phone)) {
     *     throw UserAlreadyExistsException.forPhone(phone);
     * }
     */
    public static UserAlreadyExistsException forPhone(String phoneNumber) {
        return new UserAlreadyExistsException(
                "User already exists with phone number: " + phoneNumber
        );
    }

    /**
     * Factory method: User already exists with this username
     *
     * @param username Duplicate username
     * @return UserAlreadyExistsException instance
     *
     * Example:
     * if (userRepository.existsByUsername(username)) {
     *     throw UserAlreadyExistsException.forUsername(username);
     * }
     */
    public static UserAlreadyExistsException forUsername(String username) {
        return new UserAlreadyExistsException(
                "User already exists with username: " + username
        );
    }

    /**
     * Factory method: Generic user already exists
     *
     * @return UserAlreadyExistsException instance
     *
     * Example:
     * throw UserAlreadyExistsException.generic();
     */
    public static UserAlreadyExistsException generic() {
        return new UserAlreadyExistsException(
                "A user with the provided credentials already exists"
        );
    }
}