package com.youthconnect.auth_service.service;

import com.youthconnect.auth_service.client.NotificationServiceClient;
import com.youthconnect.auth_service.client.UserServiceClient;
import com.youthconnect.auth_service.dto.request.LoginRequest;
import com.youthconnect.auth_service.dto.request.RegisterRequest;
import com.youthconnect.auth_service.dto.request.UssdLoginRequest; // Ensure this import is correct
import com.youthconnect.auth_service.dto.response.ApiResponse;
import com.youthconnect.auth_service.dto.response.AuthResponse;
import com.youthconnect.auth_service.dto.response.UserInfoResponse;
import com.youthconnect.auth_service.entity.RefreshToken;
import com.youthconnect.auth_service.exception.InvalidCredentialsException;
import com.youthconnect.auth_service.exception.UserNotFoundException;
import com.youthconnect.auth_service.repository.RefreshTokenRepository; // Ensure this import is correct
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
import java.util.UUID;

/**
 * ============================================================================
 *  Youth Connect - Authentication Service
 * ============================================================================
 *
 *  ✅ Handles login (web & USSD)
 *  ✅ Handles registration with notification
 *  ✅ Manages JWT and refresh tokens
 *  ✅ Supports token validation, refresh, and logout
 *  ✅ Uses Resilience4j for circuit-breaking and retries
 *
 *  @author DOUGLAS KINGS
 *  @version 2.1.0 (Refined Edition)
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

    // =========================================================================
    // LOGIN (Web)
    // =========================================================================
    /**
     * Authenticate a user via email or phone and password.
     * Generates both access and refresh tokens upon successful login.
     */
    @CircuitBreaker(name = "userService", fallbackMethod = "loginFallback")
    @Retry(name = "userService")
    public AuthResponse login(LoginRequest request) {
        log.info("Processing login for identifier: {}", maskIdentifier(request.getIdentifier()));

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

        if (!user.isActive()) {
            throw new InvalidCredentialsException("Account is inactive. Please contact support.");
        }

        // Generate tokens
        String accessToken = jwtUtil.generateAccessToken(
                user.getEmail(), user.getUserId(), user.getRole());
        String refreshToken = generateAndSaveRefreshToken(user.getUserId(), user.getEmail(), user.getRole());

        log.info("Login successful for {}", maskIdentifier(user.getEmail()));

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
    }

    // =========================================================================
    // LOGIN (USSD)
    // =========================================================================
    /**
     * Simplified login for USSD users (phone number only).
     */
    @CircuitBreaker(name = "userService", fallbackMethod = "ussdLoginFallback")
    @Retry(name = "userService")
    public AuthResponse loginUssd(UssdLoginRequest request) {
        log.info("Processing USSD login for phone: {}", maskPhoneNumber(request.getPhoneNumber()));

        ApiResponse<UserInfoResponse> response = userServiceClient.getUserByPhone(request.getPhoneNumber());

        if (response == null || !response.isSuccess() || response.getData() == null) {
            throw new UserNotFoundException("Phone number not registered: " + request.getPhoneNumber());
        }

        UserInfoResponse user = response.getData();

        if (!user.isActive()) {
            throw new InvalidCredentialsException("Account is inactive");
        }

        String accessToken = jwtUtil.generateAccessToken(
                user.getEmail(), user.getUserId(), user.getRole());
        String refreshToken = generateAndSaveRefreshToken(user.getUserId(), user.getEmail(), user.getRole());

        log.info("USSD login successful for {}", maskPhoneNumber(request.getPhoneNumber()));

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
    }

    // =========================================================================
    // REGISTER
    // =========================================================================
    /**
     * Registers a new user and automatically logs them in with generated tokens.
     */
    @CircuitBreaker(name = "userService", fallbackMethod = "registerFallback")
    @Retry(name = "userService")
    public AuthResponse register(RegisterRequest request) {
        log.info("Processing registration for email: {}", request.getEmail());

        ApiResponse<UserInfoResponse> response = userServiceClient.registerUser(request);

        if (response == null || !response.isSuccess() || response.getData() == null) {
            throw new RuntimeException("User registration failed");
        }

        UserInfoResponse newUser = response.getData();

        sendWelcomeNotification(newUser);

        String accessToken = jwtUtil.generateAccessToken(
                newUser.getEmail(), newUser.getUserId(), newUser.getRole());
        String refreshToken = generateAndSaveRefreshToken(newUser.getUserId(), newUser.getEmail(), newUser.getRole());

        log.info("Registration successful for user {}", newUser.getEmail());

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
    }

    // =========================================================================
    // REFRESH TOKEN
    // =========================================================================
    /**
     * Generates a new access token using a valid refresh token.
     */
    public AuthResponse refreshAccessToken(String refreshTokenValue) {
        log.debug("Processing token refresh");

        if (!jwtUtil.validateRefreshToken(refreshTokenValue)) {
            throw new InvalidCredentialsException("Invalid refresh token");
        }

        RefreshToken refreshToken = refreshTokenRepository
                .findByTokenAndRevokedFalse(refreshTokenValue)
                .orElseThrow(() -> new InvalidCredentialsException("Refresh token not found or revoked"));

        if (refreshToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            refreshToken.setRevoked(true);
            refreshToken.setRevokedAt(LocalDateTime.now());
            refreshTokenRepository.save(refreshToken);
            throw new InvalidCredentialsException("Refresh token expired");
        }

        String username = jwtUtil.extractUsername(refreshTokenValue);
        UUID userId = jwtUtil.extractUserId(refreshTokenValue);

        String newAccessToken = jwtUtil.generateAccessToken(
                username, userId, refreshToken.getUserRole());

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
     * ⚙️ Compatibility wrapper for tests expecting `refreshToken()` instead of `refreshAccessToken()`.
     */
    public AuthResponse refreshToken(String refreshTokenValue) {
        return refreshAccessToken(refreshTokenValue);
    }

    // =========================================================================
    // LOGOUT
    // =========================================================================
    /**
     * Invalidates both access and refresh tokens.
     */
    public void logout(String accessToken, String refreshTokenValue) {
        log.info("Processing logout request");

        try {
            Long expirationSeconds = jwtUtil.getTokenExpirationSeconds(accessToken);
            if (expirationSeconds > 0) {
                tokenBlacklistService.blacklistToken(accessToken, expirationSeconds);
            }

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
    // TOKEN VALIDATION
    // =========================================================================
    /**
     * Verifies whether a given access token is valid, unexpired, and not blacklisted.
     */
    public boolean validateToken(String token) {
        try {
            if (tokenBlacklistService.isTokenBlacklisted(token)) {
                return false;
            }
            String username = jwtUtil.extractUsername(token);
            return jwtUtil.validateAccessToken(token, username);
        } catch (Exception e) {
            log.warn("Token validation failed: {}", e.getMessage());
            return false;
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // HELPER METHODS
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Generate and Save Refresh Token
     *
     * ✅ FIXED: Changed from private to public for OAuth2Service access
     *
     * Creates a new refresh token and persists it in the database.
     * This method is now public to allow OAuth2Service to generate tokens
     * after successful OAuth2 authentication.
     *
     * @param userId User UUID
     * @param email User email
     * @param role User role
     * @return Refresh token string
     */
    public String generateAndSaveRefreshToken(UUID userId, String email, String role) {
        String tokenValue = jwtUtil.generateRefreshToken(email, userId);

        RefreshToken token = new RefreshToken();
        token.setToken(tokenValue);
        token.setUserId(userId);
        token.setUserEmail(email);
        token.setUserRole(role);
        token.setExpiresAt(LocalDateTime.now().plusDays(7));
        token.setCreatedAt(LocalDateTime.now());
        token.setLastUsedAt(LocalDateTime.now());
        token.setRevoked(false);

        refreshTokenRepository.save(token);
        return tokenValue;
    }

    @CircuitBreaker(name = "notificationService")
    private void sendWelcomeNotification(UserInfoResponse user) {
        try {
            notificationServiceClient.sendWelcomeEmail(user.getEmail(), user.getRole());
        } catch (Exception e) {
            log.warn("Failed to send welcome email: {}", e.getMessage());
        }
    }

    private String maskIdentifier(String id) {
        if (id == null || id.length() < 6) return "***";
        return id.substring(0, 3) + "***" + id.substring(id.length() - 3);
    }

    private String maskPhoneNumber(String phone) {
        if (phone == null) return "***";
        String cleaned = phone.replaceAll("[\\s\\-\\(\\)\\.]", "");
        if (cleaned.length() < 6) return "***";
        return cleaned.substring(0, 3) + "****" + cleaned.substring(cleaned.length() - 3);
    }

    // =========================================================================
    // CIRCUIT BREAKER FALLBACKS
    // =========================================================================
    private AuthResponse loginFallback(LoginRequest request, Exception e) {
        throw new RuntimeException("Authentication service temporarily unavailable.");
    }

    private AuthResponse ussdLoginFallback(UssdLoginRequest request, Exception e) {
        throw new RuntimeException("USSD authentication service temporarily unavailable.");
    }

    private AuthResponse registerFallback(RegisterRequest request, Exception e) {
        throw new RuntimeException("Registration service temporarily unavailable.");
    }
}