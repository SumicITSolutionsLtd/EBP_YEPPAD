package com.youthconnect.auth_service.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.beans.factory.annotation.Value;

/**
 * ═══════════════════════════════════════════════════════════════════════════
 * OAuth2 Configuration (OPTIONAL)
 * ═══════════════════════════════════════════════════════════════════════════
 *
 * ✅ FIXED: Made OAuth2 configuration optional and conditional
 *
 * This configuration class is only loaded when:
 * 1. app.oauth2.enabled=true in application.yml
 * 2. Valid Google OAuth2 credentials are provided
 *
 * DEFAULT BEHAVIOR:
 * - OAuth2 is DISABLED by default (app.oauth2.enabled=false)
 * - Service starts successfully even without Google credentials
 * - Users can still login using email/password authentication
 *
 * TO ENABLE OAUTH2:
 * 1. Get Google OAuth2 credentials from Google Cloud Console
 * 2. Set GOOGLE_CLIENT_ID and GOOGLE_CLIENT_SECRET in .env
 * 3. Set OAUTH2_ENABLED=true in .env or application.yml
 * 4. Restart the service
 *
 * SECURITY NOTES:
 * - Never commit real OAuth2 credentials to version control
 * - Use environment variables or secrets management in production
 * - Rotate credentials regularly
 * - Monitor OAuth2 login attempts for suspicious activity
 *
 * @author Douglas Kings Kato
 * @version 2.0.0 (Fixed - Optional Configuration)
 * @since 2025-11-16
 */
@Slf4j
@Configuration
@ConditionalOnProperty(
        name = "app.oauth2.enabled",
        havingValue = "true",
        matchIfMissing = false // ✅ Don't load if property is missing
)
public class OAuth2Config {

    /**
     * Google OAuth2 Client ID
     * Get from: https://console.cloud.google.com/apis/credentials
     *
     * Example: 123456789-abcdefg.apps.googleusercontent.com
     *
     * ✅ Default value prevents startup failure if not configured
     */
    @Value("${spring.security.oauth2.client.registration.google.client-id:not-configured}")
    private String googleClientId;

    /**
     * Google OAuth2 Client Secret
     * Get from: https://console.cloud.google.com/apis/credentials
     *
     * Example: GOCSPX-AbCdEfGhIjKlMnOpQrStUvWxYz
     *
     * ✅ Default value prevents startup failure if not configured
     */
    @Value("${spring.security.oauth2.client.registration.google.client-secret:not-configured}")
    private String googleClientSecret;

    /**
     * Configure OAuth2 Client Registrations
     *
     * This bean creates an in-memory repository of OAuth2 client registrations.
     * Currently supports Google OAuth2, but can be extended for:
     * - Facebook Login
     * - Apple Sign-In
     * - GitHub OAuth
     * - Microsoft Azure AD
     *
     * CONDITIONAL LOADING:
     * Only creates this bean if:
     * 1. app.oauth2.enabled=true
     * 2. This method validates credentials are not placeholder values
     *
     * @return ClientRegistrationRepository containing all configured OAuth2 providers
     */
    @Bean
    public ClientRegistrationRepository clientRegistrationRepository() {
        // ✅ Validate that credentials are actually configured
        if (googleClientId == null ||
                googleClientId.isBlank() ||
                googleClientId.equals("not-configured") ||
                googleClientId.contains("placeholder") ||
                googleClientSecret == null ||
                googleClientSecret.isBlank() ||
                googleClientSecret.equals("not-configured") ||
                googleClientSecret.contains("placeholder")) {

            log.warn("═══════════════════════════════════════════════════════════════");
            log.warn("  OAuth2 Configuration Warning");
            log.warn("═══════════════════════════════════════════════════════════════");
            log.warn("OAuth2 is enabled but credentials are not properly configured!");
            log.warn("Please set GOOGLE_CLIENT_ID and GOOGLE_CLIENT_SECRET environment variables.");
            log.warn("");
            log.warn("To get Google OAuth2 credentials:");
            log.warn("1. Go to https://console.cloud.google.com/apis/credentials");
            log.warn("2. Create OAuth 2.0 Client ID");
            log.warn("3. Add authorized redirect URI: http://localhost:8083/login/oauth2/code/google");
            log.warn("4. Set credentials in .env file");
            log.warn("═══════════════════════════════════════════════════════════════");

            // Return empty repository - OAuth2 won't work but service will start
            return new InMemoryClientRegistrationRepository();
        }

        log.info("═══════════════════════════════════════════════════════════════");
        log.info("  OAuth2 Configuration Loaded");
        log.info("═══════════════════════════════════════════════════════════════");
        log.info("✅ Google OAuth2 is ENABLED");
        log.info("   Client ID: {}...", maskClientId(googleClientId));
        log.info("   Redirect URI: http://localhost:8083/login/oauth2/code/google");
        log.info("═══════════════════════════════════════════════════════════════");

        // Create and return Google client registration
        return new InMemoryClientRegistrationRepository(googleClientRegistration());
    }

    /**
     * Google OAuth2 Client Registration
     *
     * Configures Google Sign-In with Spring Security OAuth2.
     *
     * CONFIGURATION DETAILS:
     * - Registration ID: "google" (used in redirect URI)
     * - Grant Type: Authorization Code (most secure OAuth2 flow)
     * - Scopes: openid, profile, email (basic user information)
     * - Redirect URI: {baseUrl}/login/oauth2/code/google
     *
     * FLOW:
     * 1. User clicks "Sign in with Google"
     * 2. Redirected to Google's authorization page
     * 3. User grants permissions
     * 4. Google redirects back with authorization code
     * 5. Spring Security exchanges code for access token
     * 6. Fetches user info from Google's userinfo endpoint
     * 7. OAuth2SuccessHandler creates JWT tokens
     * 8. User is logged in to Youth Connect platform
     *
     * @return ClientRegistration for Google OAuth2
     */
    private ClientRegistration googleClientRegistration() {
        return ClientRegistration.withRegistrationId("google")
                // ═══════════════════════════════════════════════════════════════
                // CLIENT CREDENTIALS
                // ═══════════════════════════════════════════════════════════════
                .clientId(googleClientId)
                .clientSecret(googleClientSecret)

                // ═══════════════════════════════════════════════════════════════
                // SCOPES (Permissions requested from user)
                // ═══════════════════════════════════════════════════════════════
                .scope("openid", "profile", "email")

                // ═══════════════════════════════════════════════════════════════
                // GOOGLE OAUTH2 ENDPOINTS
                // ═══════════════════════════════════════════════════════════════
                .authorizationUri("https://accounts.google.com/o/oauth2/v2/auth")
                .tokenUri("https://oauth2.googleapis.com/token")
                .userInfoUri("https://www.googleapis.com/oauth2/v3/userinfo")
                .jwkSetUri("https://www.googleapis.com/oauth2/v3/certs")

                // ═══════════════════════════════════════════════════════════════
                // USER ATTRIBUTE MAPPING
                // ═══════════════════════════════════════════════════════════════
                .userNameAttributeName("sub") // Google's unique user ID

                // ═══════════════════════════════════════════════════════════════
                // OAUTH2 FLOW CONFIGURATION
                // ═══════════════════════════════════════════════════════════════
                .clientName("Google")
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)

                // ═══════════════════════════════════════════════════════════════
                // REDIRECT URI (Where Google sends the authorization code)
                // ═══════════════════════════════════════════════════════════════
                .redirectUri("{baseUrl}/login/oauth2/code/{registrationId}")
                // Actual URI will be: http://localhost:8083/login/oauth2/code/google

                .build();
    }

    /**
     * Mask Client ID for Security Logging
     *
     * Shows only first 10 and last 4 characters of client ID.
     * Prevents exposing full client ID in logs.
     *
     * Example:
     * Input:  123456789-abcdefghijklmnop.apps.googleusercontent.com
     * Output: 123456789-...com
     *
     * @param clientId Full client ID
     * @return Masked client ID for logging
     */
    private String maskClientId(String clientId) {
        if (clientId == null || clientId.length() < 15) {
            return "***";
        }
        return clientId.substring(0, 10) + "..." +
                clientId.substring(clientId.length() - 3);
    }

    /**
     * ═══════════════════════════════════════════════════════════════════════
     * FACEBOOK OAUTH2 (Placeholder for Future Implementation)
     * ═══════════════════════════════════════════════════════════════════════
     *
     * Uncomment and configure when Facebook credentials are available.
     *
     * @return ClientRegistration for Facebook OAuth2
     */
    // private ClientRegistration facebookClientRegistration() {
    //     return ClientRegistration.withRegistrationId("facebook")
    //             .clientId("FACEBOOK_CLIENT_ID")
    //             .clientSecret("FACEBOOK_CLIENT_SECRET")
    //             .scope("public_profile", "email")
    //             .authorizationUri("https://www.facebook.com/v12.0/dialog/oauth")
    //             .tokenUri("https://graph.facebook.com/v12.0/oauth/access_token")
    //             .userInfoUri("https://graph.facebook.com/me?fields=id,name,email")
    //             .userNameAttributeName("id")
    //             .clientName("Facebook")
    //             .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
    //             .redirectUri("{baseUrl}/login/oauth2/code/{registrationId}")
    //             .build();
    // }

    /**
     * ═══════════════════════════════════════════════════════════════════════
     * APPLE SIGN-IN (Placeholder for Future Implementation)
     * ═══════════════════════════════════════════════════════════════════════
     *
     * Uncomment and configure when Apple credentials are available.
     * Note: Apple Sign-In requires additional JWT token generation.
     *
     * @return ClientRegistration for Apple Sign-In
     */
    // private ClientRegistration appleClientRegistration() {
    //     return ClientRegistration.withRegistrationId("apple")
    //             .clientId("APPLE_SERVICE_ID")
    //             .clientSecret("APPLE_CLIENT_SECRET") // Generated JWT token
    //             .scope("name", "email")
    //             .authorizationUri("https://appleid.apple.com/auth/authorize")
    //             .tokenUri("https://appleid.apple.com/auth/token")
    //             .userInfoUri("https://appleid.apple.com/auth/userinfo")
    //             .userNameAttributeName("sub")
    //             .clientName("Apple")
    //             .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
    //             .redirectUri("{baseUrl}/login/oauth2/code/{registrationId}")
    //             .build();
    // }
}