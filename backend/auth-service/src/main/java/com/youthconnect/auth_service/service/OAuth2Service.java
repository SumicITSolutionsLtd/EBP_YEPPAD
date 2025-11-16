package com.youthconnect.auth_service.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.youthconnect.auth_service.client.UserServiceClient;
import com.youthconnect.auth_service.dto.request.RegisterRequest;
import com.youthconnect.auth_service.dto.response.ApiResponse;
import com.youthconnect.auth_service.dto.response.AuthResponse;
import com.youthconnect.auth_service.dto.response.UserInfoResponse;
import com.youthconnect.auth_service.entity.User;
import com.youthconnect.auth_service.exception.InvalidCredentialsException;
import com.youthconnect.auth_service.repository.UserRepository;
import com.youthconnect.auth_service.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

/**
 * ═══════════════════════════════════════════════════════════════════════════
 * OAuth2 Authentication Service
 * ═══════════════════════════════════════════════════════════════════════════
 *
 * Handles third-party authentication providers:
 * - Google Sign-In (Implemented)
 * - Facebook Login (Placeholder)
 * - Apple Sign-In (Placeholder)
 *
 * FLOW:
 * 1. Frontend obtains ID token from provider (Google)
 * 2. Frontend sends token to /api/auth/oauth2/google
 * 3. Backend verifies token with Google's public keys
 * 4. Backend creates/updates user in database
 * 5. Backend generates JWT tokens (access + refresh)
 * 6. Returns tokens to frontend
 *
 * SECURITY:
 * - ID token verified against Google's public keys
 * - Token signature validation (RSA)
 * - Token expiration check
 * - Audience validation (must match our client ID)
 *
 * DATABASE INTEGRATION:
 * - Stores oauth2_provider and oauth2_user_id in users table
 * - Links Google account to existing email if found
 * - Creates new user if first-time OAuth2 login
 *
 * @author Douglas Kings Kato
 * @version 2.0.0 (Complete Implementation)
 * @since 2025-11-16
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class OAuth2Service {

    private final UserServiceClient userServiceClient;
    private final UserRepository userRepository; // ✅ Added for OAuth2 user lookup
    private final JwtUtil jwtUtil;
    private final AuthService authService;
    private final PasswordEncoder passwordEncoder;

    @Value("${spring.security.oauth2.client.registration.google.client-id:not-configured}")
    private String googleClientId;

    // ═══════════════════════════════════════════════════════════════════════
    // GOOGLE SIGN-IN (Primary OAuth2 Provider)
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Authenticate user with Google ID token
     *
     * PREREQUISITES:
     * - User must have obtained ID token from Google via frontend
     * - Token must be valid and not expired
     * - Client ID in token must match our application's client ID
     *
     * BUSINESS LOGIC:
     * - If user exists (by email): Update oauth2_provider and oauth2_user_id
     * - If user is new: Create account with role=YOUTH
     * - Always generate new JWT tokens
     * - Log OAuth2 login event for audit
     *
     * @param idToken Google ID token (JWT format)
     * @return AuthResponse with JWT tokens
     * @throws InvalidCredentialsException if token is invalid
     * @throws GeneralSecurityException if signature verification fails
     * @throws IOException if network error during verification
     */
    public AuthResponse loginWithGoogle(String idToken)
            throws GeneralSecurityException, IOException, InvalidCredentialsException {

        log.info("═══════════════════════════════════════════════════════════════");
        log.info(" GOOGLE OAUTH2 LOGIN STARTED");
        log.info("═══════════════════════════════════════════════════════════════");

        // ─────────────────────────────────────────────────────────────────────
        // STEP 1: Verify ID Token with Google's Public Keys
        // ─────────────────────────────────────────────────────────────────────
        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                new NetHttpTransport(),
                new GsonFactory()
        )
                .setAudience(Collections.singletonList(googleClientId))
                .build();

        GoogleIdToken googleIdToken = verifier.verify(idToken);

        if (googleIdToken == null) {
            log.error("❌ Invalid Google ID token - verification failed");
            throw new InvalidCredentialsException("Invalid Google ID token");
        }

        // ─────────────────────────────────────────────────────────────────────
        // STEP 2: Extract User Information from Token Payload
        // ─────────────────────────────────────────────────────────────────────
        GoogleIdToken.Payload payload = googleIdToken.getPayload();

        String googleUserId = payload.getSubject(); // Google's unique user ID
        String email = payload.getEmail();
        boolean emailVerified = payload.getEmailVerified();
        String name = (String) payload.get("name");
        String pictureUrl = (String) payload.get("picture");
        String givenName = (String) payload.get("given_name");
        String familyName = (String) payload.get("family_name");

        log.info("✅ Token verified | Google ID: {} | Email: {} | Verified: {}",
                maskId(googleUserId), maskEmail(email), emailVerified);

        // Security check: Ensure email is verified by Google
        if (!emailVerified) {
            log.warn("⚠️ Unverified email from Google: {}", maskEmail(email));
            throw new InvalidCredentialsException(
                    "Email not verified by Google. Please verify your email first."
            );
        }

        // ─────────────────────────────────────────────────────────────────────
        // STEP 3: Check if User Exists (by email OR oauth2_user_id)
        // ─────────────────────────────────────────────────────────────────────
        Optional<User> existingUser = userRepository.findByEmail(email)
                .or(() -> userRepository.findByOauth2ProviderAndOauth2UserId("google", googleUserId));

        User user;

        if (existingUser.isPresent()) {
            // ─────────────────────────────────────────────────────────────────
            // STEP 3A: Update Existing User's OAuth2 Info
            // ─────────────────────────────────────────────────────────────────
            user = existingUser.get();

            log.info("✅ Existing user found | User ID: {} | Email: {}",
                    user.getUserId(), maskEmail(user.getEmail()));

            // Update OAuth2 fields if not already set
            if (user.getOauth2Provider() == null) {
                user.setOauth2Provider("google");
                user.setOauth2UserId(googleUserId);
                user.setEmailVerified(true); // Google confirmed email
                userRepository.save(user);
                log.info("✅ Updated user with Google OAuth2 credentials");
            }

        } else {
            // ─────────────────────────────────────────────────────────────────
            // STEP 3B: Create New User from Google Account
            // ─────────────────────────────────────────────────────────────────
            log.info("➕ Creating new user from Google account: {}", maskEmail(email));

            user = new User();
            user.setUserId(UUID.randomUUID());
            user.setEmail(email);
            user.setFirstName(givenName != null ? givenName : extractFirstName(name));
            user.setLastName(familyName != null ? familyName : extractLastName(name));
            user.setRole(User.UserRole.YOUTH); // Default role for OAuth2 users
            user.setActive(true);
            user.setEmailVerified(true); // Google confirmed email
            user.setOauth2Provider("google");
            user.setOauth2UserId(googleUserId);

            // Generate random password (user won't use it, but database requires it)
            user.setPasswordHash(passwordEncoder.encode(generateSecureRandomPassword()));

            // Save to database
            user = userRepository.save(user);

            log.info("✅ New user created | User ID: {} | Email: {}",
                    user.getUserId(), maskEmail(user.getEmail()));
        }

        // ─────────────────────────────────────────────────────────────────────
        // STEP 4: Generate JWT Tokens (Access + Refresh)
        // ─────────────────────────────────────────────────────────────────────
        String accessToken = jwtUtil.generateAccessToken(
                user.getEmail(),
                user.getUserId(),
                user.getRole().name()
        );

        String refreshToken = authService.generateAndSaveRefreshToken(
                user.getUserId(),
                user.getEmail(),
                user.getRole().name()
        );

        log.info("✅ JWT tokens generated | User: {}", maskEmail(user.getEmail()));

        // ─────────────────────────────────────────────────────────────────────
        // STEP 5: Return Authentication Response
        // ─────────────────────────────────────────────────────────────────────
        log.info("═══════════════════════════════════════════════════════════════");
        log.info(" GOOGLE OAUTH2 LOGIN SUCCESSFUL");
        log.info("═══════════════════════════════════════════════════════════════");

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtUtil.getTokenExpirationSeconds(accessToken))
                .userId(user.getUserId())
                .email(user.getEmail())
                .role(user.getRole().name())
                .message("Google login successful")
                .build();
    }

    // ═══════════════════════════════════════════════════════════════════════
    // FACEBOOK LOGIN (Placeholder - Not Implemented)
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Facebook OAuth2 Login (Future Implementation)
     *
     * REQUIREMENTS:
     * - Facebook App ID and App Secret
     * - Facebook SDK integration
     * - Token verification with Facebook API
     *
     * @param accessToken Facebook access token
     * @return AuthResponse with JWT tokens
     * @throws UnsupportedOperationException until implemented
     */
    public AuthResponse loginWithFacebook(String accessToken) {
        log.warn("⚠️ Facebook login not yet implemented");
        throw new UnsupportedOperationException("Facebook login coming soon");
    }

    // ═══════════════════════════════════════════════════════════════════════
    // APPLE SIGN-IN (Placeholder - Not Implemented)
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Apple Sign-In (Future Implementation)
     *
     * REQUIREMENTS:
     * - Apple Developer Account
     * - Apple Services ID
     * - Client secret generation (JWT signed with Apple private key)
     *
     * @param identityToken Apple identity token
     * @return AuthResponse with JWT tokens
     * @throws UnsupportedOperationException until implemented
     */
    public AuthResponse loginWithApple(String identityToken) {
        log.warn("⚠️ Apple Sign-In not yet implemented");
        throw new UnsupportedOperationException("Apple Sign-In coming soon");
    }

    // ═══════════════════════════════════════════════════════════════════════
    // UTILITY METHODS (Private)
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Extract first name from full name
     * Example: "John Doe" → "John"
     */
    private String extractFirstName(String fullName) {
        if (fullName == null || fullName.trim().isEmpty()) {
            return "User";
        }
        String[] parts = fullName.trim().split("\\s+");
        return parts[0];
    }

    /**
     * Extract last name from full name
     * Example: "John Doe Smith" → "Smith"
     */
    private String extractLastName(String fullName) {
        if (fullName == null || fullName.trim().isEmpty()) {
            return "Account";
        }
        String[] parts = fullName.trim().split("\\s+");
        return parts.length > 1 ? parts[parts.length - 1] : parts[0];
    }

    /**
     * Generate cryptographically secure random password
     * Used for OAuth2 users (they won't use password login)
     */
    private String generateSecureRandomPassword() {
        return UUID.randomUUID().toString() +
                UUID.randomUUID().toString() +
                "!@#"; // Meets password requirements
    }

    /**
     * Mask Google ID for logging (privacy)
     * Example: "1234567890" → "123***890"
     */
    private String maskId(String id) {
        if (id == null || id.length() < 6) return "***";
        return id.substring(0, 3) + "***" + id.substring(id.length() - 3);
    }

    /**
     * Mask email for logging (privacy)
     * Example: "john.doe@gmail.com" → "joh***@gmail.com"
     */
    private String maskEmail(String email) {
        if (email == null || !email.contains("@")) return "***";
        String[] parts = email.split("@");
        if (parts[0].length() <= 3) return "***@" + parts[1];
        return parts[0].substring(0, 3) + "***@" + parts[1];
    }
}