package com.youthconnect.file.service.config;

// ✅ FIXED: Correct SpringDoc OpenAPI imports
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger/OpenAPI Documentation Configuration
 *
 * Accessible at: http://localhost:8089/swagger-ui.html
 * API Docs JSON: http://localhost:8089/v3/api-docs
 *
 * CRITICAL: This configuration follows the development guidelines:
 * ✅ Includes public health check endpoint documentation
 * ✅ Documents JWT authentication requirements
 * ✅ Shows which endpoints require authentication
 *
 * @author Douglas Kings Kato
 * @version 2.0.0 (With Authentication & Guidelines Compliance)
 */
@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "File Management Service API",
                version = "2.0.0",
                description = """
                        File Management Microservice for Youth Entrepreneurship Platform
                        
                        FEATURES:
                        - Profile picture uploads with optimization
                        - Audio file management for multi-language learning modules
                        - Document storage (CV, certificates, application attachments)
                        - Secure file access with JWT authentication
                        - Pagination support for file listings
                        
                        AUTHENTICATION:
                        - Most endpoints require JWT token from API Gateway
                        - Public endpoints: /health, /download/public/**, /download/modules/**
                        - Protected endpoints: All upload, delete, and private download operations
                        
                        GUIDELINES COMPLIANCE:
                        ✅ Uses DTOs instead of entities in responses
                        ✅ Uses UUIDs for all user identifiers
                        ✅ All list endpoints return paginated responses
                        ✅ Public health check endpoint available
                        ✅ JWT authentication through API Gateway
                        ✅ Docker-ready configuration
                        """,
                contact = @Contact(
                        name = "Douglas Kings Kato",
                        email = "support@youthconnect.ug",
                        url = "https://youthconnect.ug"
                ),
                license = @License(
                        name = "MIT License",
                        url = "https://opensource.org/licenses/MIT"
                )
        ),
        servers = {
                @Server(
                        url = "http://localhost:8089",
                        description = "Development Server (Direct Access)"
                ),
                @Server(
                        url = "http://localhost:8088",
                        description = "API Gateway (Development) - Recommended"
                ),
                @Server(
                        url = "https://api.youthconnect.ug",
                        description = "Production API Gateway"
                )
        }
)
@SecurityScheme(
        name = "Bearer Authentication",
        type = SecuritySchemeType.HTTP,
        bearerFormat = "JWT",
        scheme = "bearer",
        description = """
                JWT token obtained from authentication service via API Gateway.
                
                FORMAT: Bearer {token}
                
                EXAMPLE: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
                
                HOW TO OBTAIN:
                1. Login via API Gateway: POST /api/auth/login
                2. Use returned token in Authorization header
                3. Gateway validates token before routing to file service
                """
)
public class OpenApiConfig {
    // Configuration is done via annotations
    // No additional beans needed
}