package com.youthconnect.auth_service.security;

import com.youthconnect.auth_service.service.AuthService;
import com.youthconnect.auth_service.util.JwtUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.UUID;

/**
 * ═══════════════════════════════════════════════════════════════════════════
 * OAuth2 Authentication Success Handler
 * ═══════════════════════════════════════════════════════════════════════════
 *
 * Handles successful OAuth2 authentication by:
 * 1. Extracting user information from OAuth2 provider (Google, Facebook, etc.)
 * 2. Creating or updating user in the system
 * 3. Generating JWT tokens
 * 4. Redirecting to frontend with tokens in URL
 *
 * SECURITY CONSIDERATIONS:
 * - Tokens are passed via URL for SPA authentication
 * - Frontend should extract and store tokens in memory/HttpOnly cookies
 * - URL tokens should be cleared from browser history
 *
 * @author Douglas Kings Kato
 * @version 1.0.0
 * @since 2025-01-15
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtUtil jwtUtil;
    private final AuthService authService;

    /**
     * Frontend redirect URL after successful OAuth2 login
     * Default: http://localhost:3000/auth/callback
     */
    @Value("${app.oauth2.redirect-uri:http://localhost:3000/auth/callback}")
    private String frontendRedirectUri;

    /**
     * Handle Successful OAuth2 Authentication
     *
     * Flow:
     * 1. Extract OAuth2 user details (email, name, picture)
     * 2. Check if user exists in system
     * 3. If not, create new user account
     * 4. Generate JWT access and refresh tokens
     * 5. Redirect to frontend with tokens
     *
     * @param request HTTP request
     * @param response HTTP response
     * @param authentication Successful authentication object
     * @throws IOException if redirect fails
     * @throws ServletException if request handling fails
     */
    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException, ServletException {

        log.info("OAuth2 authentication successful");

        try {
            // Extract OAuth2 user information
            OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
            String email = oAuth2User.getAttribute("email");
            String name = oAuth2User.getAttribute("name");
            String provider = extractProvider(request);

            log.info("OAuth2 user authenticated: email={}, provider={}", email, provider);

            // TODO: Implement user creation/retrieval logic
            // This should call UserServiceClient to get or create user
            UUID userId = UUID.randomUUID(); // Placeholder
            String role = "YOUTH"; // Default role

            // Generate JWT tokens
            String accessToken = jwtUtil.generateAccessToken(email, userId, role);
            String refreshToken = authService.generateAndSaveRefreshToken(userId, email, role);

            // Build redirect URL with tokens
            String redirectUrl = UriComponentsBuilder
                    .fromUriString(frontendRedirectUri)
                    .queryParam("access_token", accessToken)
                    .queryParam("refresh_token", refreshToken)
                    .queryParam("token_type", "Bearer")
                    .queryParam("provider", provider)
                    .build()
                    .toUriString();

            log.info("Redirecting to frontend: {}", frontendRedirectUri);

            // Redirect to frontend
            getRedirectStrategy().sendRedirect(request, response, redirectUrl);

        } catch (Exception e) {
            log.error("OAuth2 authentication post-processing failed: {}", e.getMessage(), e);

            // Redirect to error page
            String errorUrl = UriComponentsBuilder
                    .fromUriString(frontendRedirectUri)
                    .queryParam("error", "authentication_failed")
                    .queryParam("message", e.getMessage())
                    .build()
                    .toUriString();

            getRedirectStrategy().sendRedirect(request, response, errorUrl);
        }
    }

    /**
     * Extract OAuth2 Provider Name from Request
     *
     * Parses the request path to determine which OAuth2 provider was used.
     * Example: /login/oauth2/code/google → "google"
     *
     * @param request HTTP request
     * @return Provider name (google, facebook, apple, etc.)
     */
    private String extractProvider(HttpServletRequest request) {
        String requestPath = request.getRequestURI();
        if (requestPath.contains("/oauth2/code/")) {
            String[] parts = requestPath.split("/oauth2/code/");
            return parts.length > 1 ? parts[1] : "unknown";
        }
        return "unknown";
    }
}