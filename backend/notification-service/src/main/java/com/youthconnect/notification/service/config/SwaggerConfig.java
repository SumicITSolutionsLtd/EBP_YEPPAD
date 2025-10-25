package com.youthconnect.notification.service.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * ═══════════════════════════════════════════════════════════════════════════
 * SWAGGER/OPENAPI CONFIGURATION - API DOCUMENTATION
 * ═══════════════════════════════════════════════════════════════════════════
 *
 * Provides interactive API documentation accessible at:
 * - Swagger UI: http://localhost:7077/swagger-ui.html
 * - OpenAPI JSON: http://localhost:7077/v3/api-docs
 *
 * Features:
 * - Complete endpoint documentation
 * - Request/response schemas with examples
 * - Authentication configuration (JWT)
 * - Try-it-out functionality for testing
 * - Server environment selection
 * - Comprehensive error response documentation
 *
 * Benefits:
 * - Reduces onboarding time for new developers
 * - Living documentation (always up-to-date with code)
 * - Client SDK generation capability
 * - API testing without Postman
 *
 * @author Douglas Kings Kato
 * @version 1.0
 * @since 2025-01-20
 */
@Configuration
public class SwaggerConfig {

    @Value("${spring.application.name:notification-service}")
    private String applicationName;

    @Value("${app.version:1.0.0}")
    private String applicationVersion;

    @Value("${app.web-url:http://localhost:7077}")
    private String serverUrl;

    /**
     * Configure OpenAPI 3.0 specification for notification service.
     *
     * Documentation includes:
     * - API metadata (title, version, description)
     * - Contact information for API support
     * - License information
     * - Server configurations (dev, staging, production)
     * - Security schemes (JWT authentication)
     * - Global security requirements
     *
     * @return OpenAPI configuration bean
     */
    @Bean
    public OpenAPI notificationServiceOpenAPI() {
        return new OpenAPI()
                .info(buildApiInfo())
                .servers(buildServerList())
                .addSecurityItem(buildSecurityRequirement())
                .components(new io.swagger.v3.oas.models.Components()
                        .addSecuritySchemes("Bearer Authentication", buildSecurityScheme()));
    }

    /**
     * Build API metadata information.
     *
     * Includes:
     * - Service name and version
     * - Detailed description of capabilities
     * - Contact information for support
     * - License details
     * - Terms of service URL
     *
     * @return API info object
     */
    private Info buildApiInfo() {
        return new Info()
                .title("Youth Connect Notification Service API")
                .version(applicationVersion)
                .description("""
                        Multi-channel notification delivery service for Kwetu-Hub Uganda Platform.
                        
                        **Capabilities:**
                        - SMS delivery via Africa's Talking API (Ugandan mobile networks)
                        - Email delivery via SMTP with HTML templates
                        - Push notifications via Firebase Cloud Messaging
                        - Multi-language support (English, Luganda, Lugbara, Alur)
                        - Delivery tracking with retry mechanism
                        - Template management for common notifications
                        - User preference management
                        - Comprehensive delivery analytics
                        
                        **Integration Notes:**
                        - All endpoints require JWT authentication (except health check)
                        - Rate limited to 100 requests/minute per user
                        - Async processing ensures non-blocking operations
                        - Automatic retries for failed deliveries (max 3 attempts)
                        - Delivery status tracked in real-time
                        
                        **Notification Types:**
                        - Welcome notifications (SMS + Email)
                        - Application status updates
                        - Opportunity alerts
                        - Mentorship reminders
                        - Learning module updates
                        - USSD registration confirmations
                        """)
                .contact(buildContactInfo())
                .license(buildLicenseInfo())
                .termsOfService("https://kwetuhub.ug/terms");
    }

    /**
     * Build contact information for API support.
     *
     * @return Contact object with support details
     */
    private Contact buildContactInfo() {
        return new Contact()
                .name("Kwetu-Hub Support Team")
                .email("support@kwetuhub.ug")
                .url("https://kwetuhub.ug/support");
    }

    /**
     * Build license information.
     *
     * @return License object
     */
    private License buildLicenseInfo() {
        return new License()
                .name("Proprietary")
                .url("https://kwetuhub.ug/license");
    }

    /**
     * Build server list for different environments.
     *
     * Allows API testing against:
     * - Local development server
     * - Staging environment
     * - Production environment
     *
     * @return List of server configurations
     */
    private List<Server> buildServerList() {
        return List.of(
                new Server()
                        .url("http://localhost:7077")
                        .description("Local Development Server"),
                new Server()
                        .url("https://staging-api.kwetuhub.ug")
                        .description("Staging Environment"),
                new Server()
                        .url("https://api.kwetuhub.ug")
                        .description("Production Environment")
        );
    }

    /**
     * Build JWT security scheme configuration.
     *
     * Defines how authentication works:
     * - Type: HTTP Bearer Token
     * - Scheme: Bearer (JWT)
     * - Format: JWT
     * - Header: Authorization
     *
     * Usage in requests:
     * Authorization: Bearer <your_jwt_token>
     *
     * @return Security scheme configuration
     */
    private SecurityScheme buildSecurityScheme() {
        return new SecurityScheme()
                .name("Bearer Authentication")
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .in(SecurityScheme.In.HEADER)
                .description("""
                        JWT Authorization header using Bearer scheme.
                        
                        **How to authenticate:**
                        1. Obtain JWT token from auth-service (/api/auth/login)
                        2. Include token in Authorization header: `Bearer <token>`
                        3. Token expires after 15 minutes (refresh with refresh token)
                        
                        **Example:**
                        ```
                        Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
                        ```
                        """);
    }

    /**
     * Build global security requirement.
     *
     * Applies Bearer authentication to all endpoints by default.
     * Individual endpoints can override with @SecurityRequirement annotation.
     *
     * @return Security requirement configuration
     */
    private SecurityRequirement buildSecurityRequirement() {
        return new SecurityRequirement()
                .addList("Bearer Authentication");
    }
}