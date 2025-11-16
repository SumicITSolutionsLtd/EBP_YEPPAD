package com.youthconnect.notification.service.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * ═══════════════════════════════════════════════════════════════════════════
 * SWAGGER/OPENAPI CONFIGURATION - API Documentation
 * ═══════════════════════════════════════════════════════════════════════════
 *
 * Provides interactive API documentation accessible at:
 * - Swagger UI: http://localhost:7077/swagger-ui.html
 * - OpenAPI JSON: http://localhost:7077/v3/api-docs
 *
 * @author Douglas Kings Kato
 * @version 1.0.0
 */
@Configuration
public class SwaggerConfig {

    @Value("${spring.application.name:notification-service}")
    private String applicationName;

    @Value("${app.version:1.0.0}")
    private String applicationVersion;

    @Value("${app.web-url:http://localhost:7077}")
    private String serverUrl;

    @Bean
    public OpenAPI notificationServiceOpenAPI() {
        return new OpenAPI()
                .info(buildApiInfo())
                .servers(buildServerList());
    }

    private Info buildApiInfo() {
        return new Info()
                .title("Entrepreneurship Booster Platform Notification Service API")
                .version(applicationVersion)
                .description("""
                        Multi-channel notification delivery service for Entrepreneurship Booster Platform Uganda Platform.
                        
                        **Capabilities:**
                        - SMS delivery via Africa's Talking API (Ugandan mobile networks)
                        - Email delivery via SMTP with HTML templates
                        - Push notifications via Firebase Cloud Messaging
                        - Multi-language support (English, Luganda, Lugbara, Alur)
                        - Delivery tracking with retry mechanism
                        - Template management for common notifications
                        - Comprehensive delivery analytics
                        
                        **API Guidelines Compliance:**
                        ✅ Uses PostgreSQL with UUID primary keys
                        ✅ All list endpoints support pagination
                        ✅ Public health check endpoint available
                        ✅ Flyway database migrations
                        ✅ Docker support with multi-stage builds
                        ✅ Returns DTOs only (no ResponseEntity)
                        
                        **Integration Notes:**
                        - Internal service (accessed via API Gateway)
                        - Authentication handled by API Gateway
                        - Rate limited to 100 requests/minute per user
                        - Async processing ensures non-blocking operations
                        - Automatic retries for failed deliveries (max 3 attempts)
                        """)
                .contact(buildContactInfo())
                .license(buildLicenseInfo())
                .termsOfService("https://ebp.ug/terms");
    }

    private Contact buildContactInfo() {
        return new Contact()
                .name("EBP Support Team")
                .email("support@ebp.ug")
                .url("https://ebp.ug/support");
    }

    private License buildLicenseInfo() {
        return new License()
                .name("Proprietary")
                .url("https://ebp.ug/license");
    }

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
}