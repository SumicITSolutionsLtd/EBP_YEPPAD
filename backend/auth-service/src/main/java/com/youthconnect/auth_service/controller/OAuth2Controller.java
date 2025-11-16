package com.youthconnect.auth_service.controller;

import com.youthconnect.auth_service.dto.request.GoogleLoginRequest;
import com.youthconnect.auth_service.dto.response.ApiResponse;
import com.youthconnect.auth_service.dto.response.AuthResponse;
import com.youthconnect.auth_service.service.OAuth2Service;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/**
 * ═══════════════════════════════════════════════════════════════════════════
 * OAuth2 Authentication Controller
 * ═══════════════════════════════════════════════════════════════════════════
 *
 * Handles third-party authentication providers:
 * - Google Sign-In
 * - Facebook Login (future)
 * - Apple Sign-In (future)
 *
 * FLOW:
 * 1. Frontend gets ID token from Google
 * 2. Frontend sends token to this endpoint
 * 3. Backend verifies token with Google
 * 4. Backend creates/updates user in system
 * 5. Backend returns JWT tokens
 *
 * @author Douglas Kings Kato
 * @version 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/auth/oauth2")
@RequiredArgsConstructor
@Tag(name = "OAuth2 Authentication", description = "Third-party authentication endpoints")
public class OAuth2Controller {

    private final OAuth2Service oauth2Service;

    /**
     * Google Sign-In Endpoint
     *
     * FRONTEND INTEGRATION:
     * 1. Use Google Sign-In library to get ID token
     * 2. Send token to this endpoint
     * 3. Receive JWT tokens in response
     *
     * EXAMPLE (JavaScript):
     * ```javascript
     * const googleUser = await google.accounts.id.prompt();
     * const idToken = googleUser.credential;
     *
     * const response = await fetch('/api/auth/oauth2/google', {
     *   method: 'POST',
     *   headers: { 'Content-Type': 'application/json' },
     *   body: JSON.stringify({ idToken })
     * });
     * ```
     *
     * @param request Contains Google ID token
     * @return JWT access and refresh tokens
     */
    @PostMapping("/google")
    @ResponseStatus(HttpStatus.OK)
    @Operation(
            summary = "Google Sign-In",
            description = "Authenticate user with Google ID token. Creates new user if doesn't exist."
    )
    public ApiResponse<AuthResponse> googleLogin(@Valid @RequestBody GoogleLoginRequest request) {
        log.info("Google OAuth2 login request received");

        try {
            AuthResponse authResponse = oauth2Service.loginWithGoogle(request.getIdToken());
            return ApiResponse.success(authResponse, "Google login successful");

        } catch (Exception e) {
            log.error("Google login failed: {}", e.getMessage(), e);
            return ApiResponse.error("Google login failed: " + e.getMessage());
        }
    }

    /**
     * Facebook Login Endpoint (Placeholder for future)
     */
    @PostMapping("/facebook")
    @ResponseStatus(HttpStatus.NOT_IMPLEMENTED)
    @Operation(
            summary = "Facebook Login",
            description = "Coming soon - Facebook OAuth2 authentication"
    )
    public ApiResponse<Void> facebookLogin() {
        return ApiResponse.error("Facebook login not yet implemented");
    }

    /**
     * Apple Sign-In Endpoint (Placeholder for future)
     */
    @PostMapping("/apple")
    @ResponseStatus(HttpStatus.NOT_IMPLEMENTED)
    @Operation(
            summary = "Apple Sign-In",
            description = "Coming soon - Apple OAuth2 authentication"
    )
    public ApiResponse<Void> appleLogin() {
        return ApiResponse.error("Apple login not yet implemented");
    }
}