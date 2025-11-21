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
 * OAuth2 Configuration
 * ═══════════════════════════════════════════════════════════════════════════
 *
 * Made OAuth2 configuration optional and conditional
 *
 * This configuration is only loaded when:
 * 1. app.oauth2.enabled=true in application.yml
 * 2. Valid Google OAuth2 credentials are provided
 *
 * DEFAULT BEHAVIOR:
 * - OAuth2 is DISABLED by default (app.oauth2.enabled=false)
 * - Service starts successfully even without Google credentials
 * - Users can still login using email/password authentication
 *
 * @author Douglas Kings Kato
 * @version 2.0.0 (Fixed)
 */
@Slf4j
@Configuration
@ConditionalOnProperty(
        name = "app.oauth2.enabled",
        havingValue = "true",
        matchIfMissing = false  // ✅ Don't load if property is missing
)
public class OAuth2Config {

    @Value("${spring.security.oauth2.client.registration.google.client-id:not-configured}")
    private String googleClientId;

    @Value("${spring.security.oauth2.client.registration.google.client-secret:not-configured}")
    private String googleClientSecret;

    /**
     * Configure OAuth2 Client Registrations
     *
     * Only creates this bean if app.oauth2.enabled=true
     */
    @Bean
    public ClientRegistrationRepository clientRegistrationRepository() {
        // ✅ Validate credentials
        if (isInvalidCredential(googleClientId) || isInvalidCredential(googleClientSecret)) {
            log.warn("═══════════════════════════════════════════════════════════════");
            log.warn("  OAuth2 Credentials Not Configured");
            log.warn("═══════════════════════════════════════════════════════════════");
            log.warn("OAuth2 is enabled but credentials are missing.");
            log.warn("Please set GOOGLE_CLIENT_ID and GOOGLE_CLIENT_SECRET.");
            log.warn("═══════════════════════════════════════════════════════════════");

            return new InMemoryClientRegistrationRepository();
        }

        log.info("═══════════════════════════════════════════════════════════════");
        log.info("  OAuth2 Configuration Loaded");
        log.info("═══════════════════════════════════════════════════════════════");
        log.info("✅ Google OAuth2 is ENABLED");
        log.info("   Client ID: {}...", maskClientId(googleClientId));
        log.info("═══════════════════════════════════════════════════════════════");

        return new InMemoryClientRegistrationRepository(googleClientRegistration());
    }

    /**
     * Google OAuth2 Client Registration
     */
    private ClientRegistration googleClientRegistration() {
        return ClientRegistration.withRegistrationId("google")
                .clientId(googleClientId)
                .clientSecret(googleClientSecret)
                .scope("openid", "profile", "email")
                .authorizationUri("https://accounts.google.com/o/oauth2/v2/auth")
                .tokenUri("https://oauth2.googleapis.com/token")
                .userInfoUri("https://www.googleapis.com/oauth2/v3/userinfo")
                .userNameAttributeName("sub")
                .clientName("Google")
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri("{baseUrl}/login/oauth2/code/{registrationId}")
                .build();
    }

    /**
     * Check if credential is invalid
     */
    private boolean isInvalidCredential(String credential) {
        return credential == null ||
                credential.isBlank() ||
                credential.equals("not-configured") ||
                credential.equals("placeholder-not-configured") ||
                credential.contains("placeholder");
    }

    /**
     * Mask Client ID for logging
     */
    private String maskClientId(String clientId) {
        if (clientId == null || clientId.length() < 15) {
            return "***";
        }
        return clientId.substring(0, 10) + "..." + clientId.substring(clientId.length() - 3);
    }
}