package com.youthconnect.auth_service.service;

import com.youthconnect.auth_service.client.NotificationServiceClient;
import com.youthconnect.auth_service.client.UserServiceClient;
import com.youthconnect.auth_service.dto.request.LoginRequest;
import com.youthconnect.auth_service.dto.request.RegisterRequest;
import com.youthconnect.auth_service.dto.request.UssdLoginRequest;
import com.youthconnect.auth_service.dto.response.ApiResponse;
import com.youthconnect.auth_service.dto.response.AuthResponse;
import com.youthconnect.auth_service.dto.response.UserInfoResponse;
import com.youthconnect.auth_service.entity.RefreshToken;
import com.youthconnect.auth_service.exception.InvalidCredentialsException;
import com.youthconnect.auth_service.exception.UserNotFoundException;
import com.youthconnect.auth_service.repository.RefreshTokenRepository;
import com.youthconnect.auth_service.util.JwtUtil;
import feign.FeignException; // ✅ Added Import
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * ============================================================================
 *  Youth Connect Uganda - Authentication Service
 * ============================================================================
 *
 *  Core authentication service handling:
 *  ✅ User login (web & USSD)
 *  ✅ User registration with welcome notifications
 *  ✅ JWT access token generation and validation
 *  ✅ Refresh token management
 *  ✅ Token blacklisting and logout
 *  ✅ Resilience patterns (circuit breaker, retry)
 *
 *  @author DOUGLAS KINGS
 *  @version 2.2.0 (Updated Error Handling)
 */
@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class AuthService {

    // =========================================================================
    // DEPENDENCIES
    // =========================================================================
    private final UserServiceClient userServiceClient;
    private final NotificationServiceClient notificationServiceClient;
    private final RefreshTokenRepository refreshTokenRepository;
    private final TokenBlacklistService tokenBlacklistService;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    // =========================================================================
    // LOGIN (Web) - Email/Phone + Password Authentication
    // =========================================================================
    /**
     * Authenticates a user using email or phone number with password.
     *
     * Process:
     * 1. Fetch user from User Service by identifier
     * 2. Validate password hash
     * 3. Check account active status
     * 4. Generate access and refresh tokens
     *
     * @param request LoginRequest containing identifier (email/phone) and password
     * @return AuthResponse with tokens and user details
     * @throws UserNotFoundException if user doesn't exist
     * @throws InvalidCredentialsException if password incorrect or account inactive
     */
    @CircuitBreaker(name = "userService", fallbackMethod = "loginFallback")
    @Retry(name = "userService")
    public AuthResponse login(LoginRequest request) {
        log.info("Processing login for identifier: {}", maskIdentifier(request.getIdentifier()));

        // Fetch user from User Service
        ApiResponse<UserInfoResponse> response =
                userServiceClient.getUserByIdentifier(request.getIdentifier());

        if (response == null || !response.isSuccess() || response.getData() == null) {
            throw new UserNotFoundException("User not found: " + request.getIdentifier());
        }

        UserInfoResponse user = response.getData();

        // Validate password
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new InvalidCredentialsException("Invalid email/phone or password");
        }

        // Check account status
        if (!user.isActive()) {
            throw new InvalidCredentialsException("Account is inactive. Please contact support.");
        }

        // Generate authentication tokens
        String accessToken = jwtUtil.generateAccessToken(
                user.getEmail(), user.getUserId(), user.getRole());
        String refreshToken = generateAndSaveRefreshToken(
                user.getUserId(), user.getEmail(), user.getRole());

        log.info("Login successful for {}", maskIdentifier(user.getEmail()));

        return buildAuthResponse(accessToken, refreshToken, user, "Login successful");
    }

    // =========================================================================
    // LOGIN (USSD) - Phone Number Only Authentication
    // =========================================================================
    /**
     * Simplified authentication for USSD channel using phone number only.
     * No password required - relies on telecom provider authentication.
     *
     * @param request UssdLoginRequest containing phone number
     * @return AuthResponse with tokens and user details
     * @throws UserNotFoundException if phone not registered
     * @throws InvalidCredentialsException if account inactive
     */
    @CircuitBreaker(name = "userService", fallbackMethod = "ussdLoginFallback")
    @Retry(name = "userService")
    public AuthResponse loginUssd(UssdLoginRequest request) {
        log.info("Processing USSD login for phone: {}", maskPhoneNumber(request.getPhoneNumber()));

        // Fetch user by phone number
        ApiResponse<UserInfoResponse> response =
                userServiceClient.getUserByPhone(request.getPhoneNumber());

        if (response == null || !response.isSuccess() || response.getData() == null) {
            throw new UserNotFoundException("Phone number not registered: " + request.getPhoneNumber());
        }

        UserInfoResponse user = response.getData();

        // Check account status
        if (!user.isActive()) {
            throw new InvalidCredentialsException("Account is inactive");
        }

        // Generate authentication tokens
        String accessToken = jwtUtil.generateAccessToken(
                user.getEmail(), user.getUserId(), user.getRole());
        String refreshToken = generateAndSaveRefreshToken(
                user.getUserId(), user.getEmail(), user.getRole());

        log.info("USSD login successful for {}", maskPhoneNumber(request.getPhoneNumber()));

        return buildAuthResponse(accessToken, refreshToken, user, "USSD login successful");
    }

    // =========================================================================
    // REGISTER - New User Registration
    // =========================================================================
    /**
     * Registers a new user and automatically authenticates them.
     *
     * Process:
     * 1. Call User Service to create user profile
     * 2. Send welcome notification (async, non-blocking)
     * 3. Generate authentication tokens
     * 4. Return credentials for immediate login
     *
     * @param request RegisterRequest with user details
     * @return AuthResponse with tokens for newly registered user
     * @throws RuntimeException if registration fails at User Service
     */
    @CircuitBreaker(name = "userService", fallbackMethod = "registerFallback")
    @Retry(name = "userService")
    public AuthResponse register(RegisterRequest request) {
        log.info("Processing registration for email: {}", request.getEmail());

        UserInfoResponse newUser;

        try {
            // 1. Call User Service to create user profile
            // Wrapped in try-catch to handle Feign errors explicitly
            ApiResponse<UserInfoResponse> response = userServiceClient.registerUser(request);

            if (response == null || !response.isSuccess() || response.getData() == null) {
                String errorMsg = (response != null) ? response.getMessage() : "No response from user service";
                throw new RuntimeException("User registration failed: " + errorMsg);
            }

            newUser = response.getData();

        } catch (FeignException.Forbidden e) {
            // Handle 403 Forbidden - Likely API Key mismatch
            log.error("Access denied calling User Service: {}", e.getMessage());
            throw new RuntimeException("Access to User Service denied. Check Internal API Key configuration.");

        } catch (FeignException.Conflict e) {
            // Handle 409 Conflict - User already exists
            log.warn("Registration conflict for email {}: {}", request.getEmail(), e.getMessage());
            throw new RuntimeException("User with this email or phone number already exists.");

        } catch (FeignException.BadRequest e) {
            // Handle 400 Bad Request - Validation errors
            log.warn("Invalid registration data: {}", e.getMessage());
            throw new RuntimeException("Invalid registration data provided.");

        } catch (Exception e) {
            // Handle other crashes
            log.error("Unexpected error during registration: {}", e.getMessage());
            throw new RuntimeException("Registration failed: " + e.getMessage());
        }

        // 2. Send welcome notification (non-blocking)
        sendWelcomeNotification(newUser);

        // 3. Generate authentication tokens
        String accessToken = jwtUtil.generateAccessToken(
                newUser.getEmail(), newUser.getUserId(), newUser.getRole());
        String refreshToken = generateAndSaveRefreshToken(
                newUser.getUserId(), newUser.getEmail(), newUser.getRole());

        log.info("Registration successful for user {}", newUser.getEmail());

        return buildAuthResponse(accessToken, refreshToken, newUser,
                "Registration successful. Welcome to Youth Connect Uganda!");
    }

    // =========================================================================
    // REFRESH TOKEN - Generate New Access Token
    // =========================================================================
    /**
     * Issues a new access token using a valid refresh token.
     * Updates the refresh token's last used timestamp.
     *
     * @param refreshTokenValue The refresh token string
     * @return AuthResponse with new access token
     * @throws InvalidCredentialsException if token invalid, revoked, or expired
     */
    public AuthResponse refreshAccessToken(String refreshTokenValue) {
        log.debug("Processing token refresh");

        // Validate refresh token JWT structure
        if (!jwtUtil.validateRefreshToken(refreshTokenValue)) {
            throw new InvalidCredentialsException("Invalid refresh token");
        }

        // Fetch refresh token from database
        RefreshToken refreshToken = refreshTokenRepository
                .findByTokenAndRevokedFalse(refreshTokenValue)
                .orElseThrow(() -> new InvalidCredentialsException("Refresh token not found or revoked"));

        // Check expiration
        if (refreshToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            refreshToken.setRevoked(true);
            refreshToken.setRevokedAt(LocalDateTime.now());
            refreshTokenRepository.save(refreshToken);
            throw new InvalidCredentialsException("Refresh token expired");
        }

        // Extract user info from token
        String username = jwtUtil.extractUsername(refreshTokenValue);
        UUID userId = jwtUtil.extractUserId(refreshTokenValue);

        // Generate new access token
        String newAccessToken = jwtUtil.generateAccessToken(
                username, userId, refreshToken.getUserRole());

        // Update last used timestamp
        refreshToken.setLastUsedAt(LocalDateTime.now());
        refreshTokenRepository.save(refreshToken);

        log.info("Access token refreshed for user {}", maskIdentifier(username));

        return AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(refreshTokenValue)
                .tokenType("Bearer")
                .expiresIn(jwtUtil.getTokenExpirationSeconds(newAccessToken))
                .userId(userId)
                .email(username)
                .role(refreshToken.getUserRole())
                .message("Token refreshed successfully")
                .build();
    }

    /**
     * Compatibility wrapper for legacy code using refreshToken() method name.
     * Delegates to refreshAccessToken().
     */
    public AuthResponse refreshToken(String refreshTokenValue) {
        return refreshAccessToken(refreshTokenValue);
    }

    // =========================================================================
    // LOGOUT - Invalidate User Session
    // =========================================================================
    /**
     * Logs out a user by invalidating both access and refresh tokens.
     *
     * Process:
     * 1. Blacklist access token (prevents reuse until natural expiration)
     * 2. Revoke refresh token in database
     *
     * @param accessToken The access token to blacklist
     * @param refreshTokenValue The refresh token to revoke
     */
    public void logout(String accessToken, String refreshTokenValue) {
        log.info("Processing logout request");

        try {
            // Blacklist access token for remaining lifetime
            Long expirationSeconds = jwtUtil.getTokenExpirationSeconds(accessToken);
            if (expirationSeconds > 0) {
                tokenBlacklistService.blacklistToken(accessToken, expirationSeconds);
            }

            // Revoke refresh token if provided
            if (refreshTokenValue != null && !refreshTokenValue.isBlank()) {
                refreshTokenRepository.findByTokenAndRevokedFalse(refreshTokenValue)
                        .ifPresent(token -> {
                            token.setRevoked(true);
                            token.setRevokedAt(LocalDateTime.now());
                            refreshTokenRepository.save(token);
                        });
            }

            log.info("Logout completed successfully");

        } catch (Exception e) {
            log.error("Logout process encountered an error: {}", e.getMessage(), e);
        }
    }

    // =========================================================================
    // TOKEN VALIDATION - Verify Access Token
    // =========================================================================
    /**
     * Validates an access token's authenticity and status.
     *
     * Checks:
     * - Token not blacklisted
     * - Token signature valid
     * - Token not expired
     * - Username matches token claims
     *
     * @param token The access token to validate
     * @return true if valid, false otherwise
     */
    public boolean validateToken(String token) {
        try {
            // Check blacklist first (fast reject)
            if (tokenBlacklistService.isTokenBlacklisted(token)) {
                return false;
            }

            // Validate JWT structure and expiration
            String username = jwtUtil.extractUsername(token);
            return jwtUtil.validateAccessToken(token, username);

        } catch (Exception e) {
            log.warn("Token validation failed: {}", e.getMessage());
            return false;
        }
    }

    // =========================================================================
    // HELPER METHODS
    // =========================================================================

    /**
     * Generates a refresh token and persists it to the database.
     *
     * Made public to allow OAuth2Service to generate tokens after
     * successful OAuth2 authentication.
     *
     * @param userId User's unique identifier
     * @param email User's email address
     * @param role User's role (YOUTH, EMPLOYER, etc.)
     * @return The generated refresh token string
     */
    public String generateAndSaveRefreshToken(UUID userId, String email, String role) {
        String tokenValue = jwtUtil.generateRefreshToken(email, userId);

        RefreshToken token = new RefreshToken();
        token.setToken(tokenValue);
        token.setUserId(userId);
        token.setUserEmail(email);
        token.setUserRole(role);
        token.setExpiresAt(LocalDateTime.now().plusDays(7)); // 7-day expiration
        token.setCreatedAt(LocalDateTime.now());
        token.setLastUsedAt(LocalDateTime.now());
        token.setRevoked(false);

        refreshTokenRepository.save(token);
        return tokenValue;
    }

    /**
     * Sends welcome email notification to newly registered user.
     * Failures are logged but don't block registration flow.
     */
    @CircuitBreaker(name = "notificationService")
    private void sendWelcomeNotification(UserInfoResponse user) {
        try {
            notificationServiceClient.sendWelcomeEmail(user.getEmail(), user.getRole());
        } catch (Exception e) {
            log.warn("Failed to send welcome email: {}", e.getMessage());
        }
    }

    /**
     * Builds standardized AuthResponse object.
     */
    private AuthResponse buildAuthResponse(String accessToken, String refreshToken,
                                           UserInfoResponse user, String message) {
        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtUtil.getTokenExpirationSeconds(accessToken))
                .userId(user.getUserId())
                .email(user.getEmail())
                .role(user.getRole())
                .message(message)
                .build();
    }

    /**
     * Masks sensitive identifier for logging (shows first/last 3 chars).
     */
    private String maskIdentifier(String id) {
        if (id == null || id.length() < 6) return "***";
        return id.substring(0, 3) + "***" + id.substring(id.length() - 3);
    }

    /**
     * Masks phone number for logging (shows first/last 3 digits).
     */
    private String maskPhoneNumber(String phone) {
        if (phone == null) return "***";
        String cleaned = phone.replaceAll("[\\s\\-\\(\\)\\.]", "");
        if (cleaned.length() < 6) return "***";
        return cleaned.substring(0, 3) + "****" + cleaned.substring(cleaned.length() - 3);
    }

    // =========================================================================
    // CIRCUIT BREAKER FALLBACK METHODS
    // =========================================================================

    /**
     * Fallback when User Service is unavailable during login.
     */
    private AuthResponse loginFallback(LoginRequest request, Exception e) {
        log.error("Login fallback triggered for: {}", maskIdentifier(request.getIdentifier()));
        log.error("Cause: {}", e.getMessage(), e);
        throw new RuntimeException("Authentication service temporarily unavailable. Please try again later.");
    }

    /**
     * Fallback when User Service is unavailable during USSD login.
     */
    private AuthResponse ussdLoginFallback(UssdLoginRequest request, Exception e) {
        log.error("USSD login fallback triggered for: {}", maskPhoneNumber(request.getPhoneNumber()));
        log.error("Cause: {}", e.getMessage(), e);
        throw new RuntimeException("USSD authentication service temporarily unavailable. Please try again later.");
    }

    /**
     * Fallback when User Service is unavailable during registration.
     * Provides specific error messages for common HTTP status codes.
     */
    private AuthResponse registerFallback(RegisterRequest request, Exception e) {
        log.error("Registration fallback triggered for email: {}", request.getEmail());
        log.error("Cause: {}", e.getMessage(), e);

        // Note: With the new try-catch block in the main method, specific
        // FeignExceptions (403, 409) will be handled there first.
        // This fallback will primarily catch connectivity issues (Connection Refused, Timeouts).

        throw new RuntimeException("Registration service temporarily unavailable. Please try again later.");
    }
}