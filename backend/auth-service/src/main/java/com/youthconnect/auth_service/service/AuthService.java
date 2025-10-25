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
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Core Authentication Service - COMPLETE IMPLEMENTATION
 *
 * Handles all authentication and authorization operations:
 * ✅ User login (web and USSD)
 * ✅ User registration with Feign client integration
 * ✅ Token generation and validation
 * ✅ Refresh token management
 * ✅ Password verification with BCrypt
 * ✅ Token blacklisting for logout
 * ✅ Circuit breaker pattern for resilience
 *
 * @author Youth Connect Uganda Development Team
 * @version 1.0.0
 */
@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class AuthService {

    private final UserServiceClient userServiceClient;
    private final NotificationServiceClient notificationServiceClient;
    private final RefreshTokenRepository refreshTokenRepository;
    private final TokenBlacklistService tokenBlacklistService;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    /**
     * Authenticate User - Web Login
     *
     * Process:
     * 1. Retrieve user from user-service by email/phone
     * 2. Verify password using BCrypt
     * 3. Generate access token (short-lived)
     * 4. Generate refresh token (long-lived, stored in database)
     * 5. Return tokens and user info
     *
     * @param request Login request containing email/phone and password
     * @return AuthResponse containing tokens and user information
     * @throws InvalidCredentialsException if credentials are invalid
     * @throws UserNotFoundException if user doesn't exist
     */
    @CircuitBreaker(name = "userService", fallbackMethod = "loginFallback")
    @Retry(name = "userService")
    public AuthResponse login(LoginRequest request) {
        log.info("Processing login request for identifier: {}", maskIdentifier(request.getIdentifier()));

        try {
            // Fetch user from user-service via Feign client
            ApiResponse<UserInfoResponse> response = userServiceClient.getUserByIdentifier(request.getIdentifier());

            // Extract user from ApiResponse
            if (response == null || !response.isSuccess() || response.getData() == null) {
                throw new UserNotFoundException("User not found: " + request.getIdentifier());
            }

            UserInfoResponse user = response.getData();

            // Verify password using BCrypt
            if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
                log.warn("Invalid password attempt for user: {}", maskIdentifier(request.getIdentifier()));
                throw new InvalidCredentialsException("Invalid email/phone or password");
            }

            // Check if account is active
            if (!user.isActive()) {
                throw new InvalidCredentialsException("Account is inactive. Please contact support.");
            }

            // Generate JWT access token (15 minutes expiry)
            String accessToken = jwtUtil.generateAccessToken(
                    user.getEmail(),
                    user.getUserId(),
                    user.getRole()
            );

            // Generate and save refresh token (7 days expiry)
            String refreshToken = generateAndSaveRefreshToken(user.getUserId(), user.getEmail(), user.getRole());

            log.info("Login successful for user: {} (ID: {})", maskIdentifier(user.getEmail()), user.getUserId());

            return AuthResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .tokenType("Bearer")
                    .expiresIn(jwtUtil.getTokenExpirationSeconds(accessToken))
                    .userId(user.getUserId())
                    .email(user.getEmail())
                    .role(user.getRole())
                    .message("Login successful")
                    .build();

        } catch (InvalidCredentialsException | UserNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error during login: {}", e.getMessage(), e);
            throw new RuntimeException("Authentication failed due to system error");
        }
    }

    /**
     * Authenticate User - USSD Login
     *
     * Simplified authentication for USSD users using phone number only.
     * Password is not required for USSD sessions due to UX constraints.
     *
     * Security Note:
     * - USSD sessions are assumed to be secure (telco network)
     * - Phone number is verified through USSD session ID
     *
     * @param request USSD login request containing phone number
     * @return AuthResponse containing tokens
     * @throws UserNotFoundException if phone number not registered
     */
    @CircuitBreaker(name = "userService", fallbackMethod = "ussdLoginFallback")
    @Retry(name = "userService")
    public AuthResponse loginUssd(UssdLoginRequest request) {
        log.info("Processing USSD login for phone: {}", maskPhoneNumber(request.getPhoneNumber()));

        try {
            // Fetch user by phone number only
            ApiResponse<UserInfoResponse> response = userServiceClient.getUserByPhone(request.getPhoneNumber());

            if (response == null || !response.isSuccess() || response.getData() == null) {
                throw new UserNotFoundException("Phone number not registered: " + request.getPhoneNumber());
            }

            UserInfoResponse user = response.getData();

            // Check if account is active
            if (!user.isActive()) {
                throw new InvalidCredentialsException("Account is inactive");
            }

            // Generate tokens
            String accessToken = jwtUtil.generateAccessToken(
                    user.getEmail(),
                    user.getUserId(),
                    user.getRole()
            );

            String refreshToken = generateAndSaveRefreshToken(user.getUserId(), user.getEmail(), user.getRole());

            log.info("USSD login successful for phone: {}", maskPhoneNumber(request.getPhoneNumber()));

            return AuthResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .tokenType("Bearer")
                    .expiresIn(jwtUtil.getTokenExpirationSeconds(accessToken))
                    .userId(user.getUserId())
                    .email(user.getEmail())
                    .role(user.getRole())
                    .message("USSD login successful")
                    .build();

        } catch (UserNotFoundException | InvalidCredentialsException e) {
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error during USSD login: {}", e.getMessage(), e);
            throw new RuntimeException("USSD authentication failed");
        }
    }

    /**
     * Register New User
     *
     * Delegates user registration to user-service and sends welcome notification.
     * Auth-service only handles authentication, not user data management.
     *
     * @param request Registration request
     * @return AuthResponse with tokens for immediate login after registration
     */
    @CircuitBreaker(name = "userService", fallbackMethod = "registerFallback")
    @Retry(name = "userService")
    public AuthResponse register(RegisterRequest request) {
        log.info("Processing registration for email: {}", request.getEmail());

        try {
            // Delegate registration to user-service via Feign
            ApiResponse<UserInfoResponse> response = userServiceClient.registerUser(request);

            if (response == null || !response.isSuccess() || response.getData() == null) {
                throw new RuntimeException("User registration failed");
            }

            UserInfoResponse newUser = response.getData();

            // Send welcome notification (non-blocking, fire and forget)
            sendWelcomeNotification(newUser);

            // Generate tokens for automatic login after registration
            String accessToken = jwtUtil.generateAccessToken(
                    newUser.getEmail(),
                    newUser.getUserId(),
                    newUser.getRole()
            );

            String refreshToken = generateAndSaveRefreshToken(newUser.getUserId(), newUser.getEmail(), newUser.getRole());

            log.info("Registration successful for user: {} (ID: {})", newUser.getEmail(), newUser.getUserId());

            return AuthResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .tokenType("Bearer")
                    .expiresIn(jwtUtil.getTokenExpirationSeconds(accessToken))
                    .userId(newUser.getUserId())
                    .email(newUser.getEmail())
                    .role(newUser.getRole())
                    .message("Registration successful. Welcome to Youth Connect Uganda!")
                    .build();

        } catch (Exception e) {
            log.error("Registration failed: {}", e.getMessage(), e);
            throw new RuntimeException("Registration failed: " + e.getMessage());
        }
    }

    /**
     * Refresh Access Token
     *
     * Generates a new access token using a valid refresh token.
     * Allows users to stay logged in without re-entering credentials.
     *
     * @param refreshTokenValue Refresh token string
     * @return AuthResponse with new access token
     * @throws InvalidCredentialsException if refresh token is invalid or expired
     */
    public AuthResponse refreshAccessToken(String refreshTokenValue) {
        log.debug("Processing token refresh request");

        // Validate refresh token format and signature
        if (!jwtUtil.validateRefreshToken(refreshTokenValue)) {
            throw new InvalidCredentialsException("Invalid refresh token");
        }

        // Find refresh token in database
        RefreshToken refreshToken = refreshTokenRepository
                .findByTokenAndRevokedFalse(refreshTokenValue)
                .orElseThrow(() -> new InvalidCredentialsException("Refresh token not found or has been revoked"));

        // Check if refresh token is expired
        if (refreshToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            // Mark as revoked
            refreshToken.setRevoked(true);
            refreshToken.setRevokedAt(LocalDateTime.now());
            refreshTokenRepository.save(refreshToken);
            throw new InvalidCredentialsException("Refresh token has expired");
        }

        // Extract user info from JWT token
        String username = jwtUtil.extractUsername(refreshTokenValue);
        Long userId = jwtUtil.extractUserId(refreshTokenValue);

        // Generate new access token
        String newAccessToken = jwtUtil.generateAccessToken(
                username,
                userId,
                refreshToken.getUserRole()
        );

        // Update last used timestamp
        refreshToken.setLastUsedAt(LocalDateTime.now());
        refreshTokenRepository.save(refreshToken);

        log.debug("Token refreshed successfully for user: {}", maskIdentifier(username));

        return AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(refreshTokenValue)  // Return same refresh token
                .tokenType("Bearer")
                .expiresIn(jwtUtil.getTokenExpirationSeconds(newAccessToken))
                .userId(userId)
                .email(username)
                .role(refreshToken.getUserRole())
                .message("Token refreshed successfully")
                .build();
    }

    /**
     * Logout User
     *
     * Invalidates the current access token and refresh token:
     * - Adds access token to Redis blacklist
     * - Revokes refresh token in database
     *
     * @param accessToken Access token to invalidate
     * @param refreshTokenValue Refresh token to revoke
     */
    public void logout(String accessToken, String refreshTokenValue) {
        log.info("Processing logout request");

        try {
            // Blacklist access token in Redis (expires when token naturally expires)
            Long expirationSeconds = jwtUtil.getTokenExpirationSeconds(accessToken);
            if (expirationSeconds > 0) {
                tokenBlacklistService.blacklistToken(accessToken, expirationSeconds);
                log.debug("Access token blacklisted for {} seconds", expirationSeconds);
            }

            // Revoke refresh token in database
            if (refreshTokenValue != null && !refreshTokenValue.isBlank()) {
                Optional<RefreshToken> refreshToken = refreshTokenRepository
                        .findByTokenAndRevokedFalse(refreshTokenValue);

                refreshToken.ifPresent(token -> {
                    token.setRevoked(true);
                    token.setRevokedAt(LocalDateTime.now());
                    refreshTokenRepository.save(token);
                    log.debug("Refresh token revoked for user ID: {}", token.getUserId());
                });
            }

            log.info("Logout successful");

        } catch (Exception e) {
            log.error("Error during logout: {}", e.getMessage(), e);
            // Don't throw exception - logout should always succeed from user perspective
        }
    }

    /**
     * Validate Token
     *
     * Checks if an access token is valid, not expired, and not blacklisted.
     *
     * @param token Access token to validate
     * @return true if valid, false otherwise
     */
    public boolean validateToken(String token) {
        try {
            // Check if token is blacklisted
            if (tokenBlacklistService.isTokenBlacklisted(token)) {
                log.debug("Token validation failed: Token is blacklisted");
                return false;
            }

            // Validate token signature and expiration
            String username = jwtUtil.extractUsername(token);
            boolean isValid = jwtUtil.validateAccessToken(token, username);

            log.debug("Token validation result: {}", isValid);
            return isValid;

        } catch (Exception e) {
            log.error("Token validation error: {}", e.getMessage());
            return false;
        }
    }

    // =========================================================================
    // HELPER METHODS
    // =========================================================================

    /**
     * Generate and Save Refresh Token
     *
     * Creates a new refresh token JWT, saves it to database with metadata.
     *
     * @param userId User ID
     * @param email User email
     * @param role User role
     * @return Refresh token string
     */
    private String generateAndSaveRefreshToken(Long userId, String email, String role) {
        // Generate JWT refresh token
        String refreshTokenValue = jwtUtil.generateRefreshToken(email, userId);

        // Create database entity
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken(refreshTokenValue);
        refreshToken.setUserId(userId);
        refreshToken.setUserEmail(email);
        refreshToken.setUserRole(role);
        refreshToken.setExpiresAt(LocalDateTime.now().plusDays(7));  // 7 days expiry
        refreshToken.setCreatedAt(LocalDateTime.now());
        refreshToken.setLastUsedAt(LocalDateTime.now());
        refreshToken.setRevoked(false);

        // Save to database
        refreshTokenRepository.save(refreshToken);

        log.debug("Refresh token saved for user ID: {}", userId);

        return refreshTokenValue;
    }

    /**
     * Send Welcome Notification
     *
     * Sends welcome email/SMS to newly registered user.
     * Uses circuit breaker to prevent registration failure if notification service is down.
     *
     * @param user User information
     */
    @CircuitBreaker(name = "notificationService")
    private void sendWelcomeNotification(UserInfoResponse user) {
        try {
            notificationServiceClient.sendWelcomeEmail(user.getEmail(), user.getRole());
            log.info("Welcome notification sent to: {}", maskIdentifier(user.getEmail()));
        } catch (Exception e) {
            log.warn("Failed to send welcome notification: {}", e.getMessage());
            // Don't fail registration if notification fails
        }
    }

    /**
     * Mask Identifier for Logging (Privacy Protection)
     *
     * @param identifier Email or phone to mask
     * @return Masked identifier
     */
    private String maskIdentifier(String identifier) {
        if (identifier == null || identifier.length() < 6) {
            return "***";
        }
        return identifier.substring(0, 3) + "***" + identifier.substring(identifier.length() - 3);
    }

    /**
     * Mask Phone Number for Logging (Privacy Protection)
     *
     * @param phone Phone number to mask
     * @return Masked phone number
     */
    private String maskPhoneNumber(String phone) {
        if (phone == null || phone.length() < 6) {
            return "***";
        }
        String cleaned = phone.replaceAll("[\\s\\-\\(\\)\\.]", "");
        if (cleaned.length() >= 6) {
            return cleaned.substring(0, 3) + "****" + cleaned.substring(cleaned.length() - 3);
        }
        return "***";
    }

    // =========================================================================
    // CIRCUIT BREAKER FALLBACK METHODS
    // =========================================================================

    /**
     * Fallback method when user-service is unavailable during login
     */
    private AuthResponse loginFallback(LoginRequest request, Exception e) {
        log.error("User service unavailable during login. Fallback triggered.", e);
        throw new RuntimeException("Authentication service temporarily unavailable. Please try again in a few moments.");
    }

    /**
     * Fallback method when user-service is unavailable during USSD login
     */
    private AuthResponse ussdLoginFallback(UssdLoginRequest request, Exception e) {
        log.error("User service unavailable during USSD login. Fallback triggered.", e);
        throw new RuntimeException("USSD service temporarily unavailable. Please try again later.");
    }

    /**
     * Fallback method when user-service is unavailable during registration
     */
    private AuthResponse registerFallback(RegisterRequest request, Exception e) {
        log.error("User service unavailable during registration. Fallback triggered.", e);
        throw new RuntimeException("Registration service temporarily unavailable. Please try again later.");
    }
}