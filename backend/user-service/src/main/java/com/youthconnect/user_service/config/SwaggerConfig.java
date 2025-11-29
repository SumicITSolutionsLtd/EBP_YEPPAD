package com.youthconnect.user_service.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.Components;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

/**
 * ═══════════════════════════════════════════════════════════════════════════
 * SWAGGER/OPENAPI CONFIGURATION - USER SERVICE
 * ═══════════════════════════════════════════════════════════════════════════
 *
 * Provides comprehensive API documentation using OpenAPI 3.0 specification
 *
 * Access Points:
 * - Swagger UI:    http://localhost:8181/swagger-ui/index.html
 * - OpenAPI JSON:  http://localhost:8181/v3/api-docs
 *
 * Key Features:
 * - JWT Bearer Authentication
 * - Multi-role User Management
 * - Interactive API Testing
 *
 * @author Douglas Kings Kato
 * @version 1.0.0
 * @since 2024-11-27
 * ═══════════════════════════════════════════════════════════════════════════
 */
@Configuration
public class SwaggerConfig {

    @Value("${server.port:8181}")
    private String serverPort;

    /**
     * Main OpenAPI configuration bean
     */
    @Bean
    public OpenAPI userServiceOpenAPI() {

        // ═══════════════════════════════════════════════════════════════
        // STEP 1: Create Security Scheme for JWT Bearer Authentication
        // ═══════════════════════════════════════════════════════════════
        SecurityScheme bearerAuthScheme = new SecurityScheme()
                .name("Bearer Authentication")
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .in(SecurityScheme.In.HEADER)
                .description("Enter JWT token obtained from /api/v1/auth/login endpoint. Format: Bearer {token}");

        // ═══════════════════════════════════════════════════════════════
        // STEP 2: Create Components with Security Schemes
        // ═══════════════════════════════════════════════════════════════
        Components components = new Components()
                .addSecuritySchemes("Bearer Authentication", bearerAuthScheme);

        // ═══════════════════════════════════════════════════════════════
        // STEP 3: Create Security Requirement (Apply to all endpoints)
        // ═══════════════════════════════════════════════════════════════
        SecurityRequirement securityRequirement = new SecurityRequirement()
                .addList("Bearer Authentication");

        // ═══════════════════════════════════════════════════════════════
        // STEP 4: Create API Information (MUST include version!)
        // ═══════════════════════════════════════════════════════════════
        Info apiInfo = new Info()
                .title("User Service API - Entrepreneurship Booster Platform Uganda")
                .version("1.0.0")  // ✅ CRITICAL: This is REQUIRED by OpenAPI spec
                .description(buildApiDescription())
                .termsOfService("https://entrepreneurshipboosterplatform.ug/terms")
                .contact(new Contact()
                        .name("douglaskings2@gmail.com - Douglas Kings Kato")
                        .email("dev@entrepreneurshipboosterplatform.ug")
                        .url("https://entrepreneurshipboosterplatform.ug/contact"))
                .license(new License()
                        .name("Proprietary License")
                        .url("https://entrepreneurshipboosterplatform.ug/license"));

        // ═══════════════════════════════════════════════════════════════
        // STEP 5: Define Server URLs
        // ═══════════════════════════════════════════════════════════════
        Server localServer = new Server()
                .url("http://localhost:" + serverPort)
                .description("Local Development Server - User Service");

        Server gatewayServer = new Server()
                .url("http://localhost:8088")
                .description("API Gateway - All Services");

        Server productionServer = new Server()
                .url("https://api.entrepreneurshipboosterplatform.ug")
                .description("Production Server");

        // ═══════════════════════════════════════════════════════════════
        // STEP 6: Build and Return OpenAPI Object
        // ═══════════════════════════════════════════════════════════════
        return new OpenAPI()
                // ✅ CRITICAL FIX: Explicitly set OpenAPI version
                .openapi("3.0.1")

                // Add API information
                .info(apiInfo)

                // Add servers
                .servers(Arrays.asList(localServer, gatewayServer, productionServer))

                // Add security requirement globally
                .addSecurityItem(securityRequirement)

                // Add components (security schemes)
                .components(components);
    }

    /**
     * Builds comprehensive API description with Markdown formatting
     *
     * @return Formatted API description string
     */
    private String buildApiDescription() {
        return """
                # User Service API Documentation
                
                ## 📋 Overview
                Comprehensive user management and authentication service for the Entrepreneurship Booster Platform Uganda.
                This service handles user registration, authentication, profile management, and role-based access control.
                
                ## 🔐 Authentication Flow
                
                Follow these steps to test protected endpoints:
                
                ### Step 1: Register a New User
                **Endpoint:** `POST /api/v1/auth/register`
                
                **Sample Request Body:**
```json            {
              "email": "testuser@example.com",
              "password": "TestPassword123!",
              "phoneNumber": "+256700123456",
              "role": "YOUTH",
              "firstName": "Test",
              "lastName": "User",
              "dateOfBirth": "2000-01-15",
              "gender": "MALE"
            }
                
                ### Step 2: Login to Get JWT Token
                **Endpoint:** `POST /api/v1/auth/login`
                
                **Sample Request Body:**
```json            {
              "email": "testuser@example.com",
              "password": "TestPassword123!"
            }
                
                **Response:** You'll receive a JWT token in the response.
                
                ### Step 3: Authorize Swagger UI
                1. Click the 🔒 **"Authorize"** button (top right of this page)
                2. Enter: `Bearer {your-jwt-token-here}`
                3. Click **"Authorize"** then **"Close"**
                4. Now you can test all protected endpoints!
                
                ## 👥 User Roles
                
                The platform supports multiple user types:
                
                | Role | Description | Access Level |
                |------|-------------|--------------|
                | **YOUTH** | Job seekers and young entrepreneurs | Standard User |
                | **MENTOR** | Career advisors and mentors | Mentor Features |
                | **NGO** | Non-governmental organizations | Organization Features |
                | **COMPANY** | Corporate employers and job posters | Employer Features |
                | **RECRUITER** | Recruitment agencies | Recruitment Features |
                | **FUNDER** | Funding organizations and investors | Funding Features |
                | **SERVICE_PROVIDER** | Service providers and consultants | Provider Features |
                | **GOVERNMENT** | Government entities and ministries | Government Features |
                | **ADMIN** | System administrators | Full System Access |
                
                ## 🚀 Key Features
                
                - ✅ Multi-role user registration and authentication
                - ✅ JWT-based secure authentication
                - ✅ Profile management (create, read, update)
                - ✅ Role-based access control (RBAC)
                - ✅ Document upload and management
                - ✅ Phone number verification (Uganda format)
                - ✅ Email verification
                - ✅ Password reset functionality
                - ✅ User activity tracking
                - ✅ Search and filtering
                
                ## 📞 Support & Contact
                
                **Email:** douglaskings2@gmail.com  
                **Website:** https://entrepreneurshipboosterplatform.ug  
                **Documentation:** https://docs.entrepreneurshipboosterplatform.ug
                
                ## 📌 API Version
                
                **Version:** 1.0.0  
                **Release Date:** November 2025  
                **Status:** Active Development
                
                ------
                **Copyright © 2025 Entrepreneurship Booster Platform Uganda - All rights reserved.**
                """;
    }
}