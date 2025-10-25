Youth Connect Uganda – Auth Service
Authentication & Authorization Microservice

A robust, secure, and scalable JWT-based authentication service for the Youth Connect Uganda Platform, supporting web, mobile, and USSD interfaces.

OVERVIEW

The Auth Service is a standalone microservice that handles authentication, authorization, and token management for all Youth Connect platform users. It integrates with the User Service and Notification Service via Feign clients and supports resilient inter-service communication using Resilience4j and Eureka for service discovery.

KEY FEATURES

Implemented:

Multi-Channel Authentication

Web login (email/phone + password)

USSD login (phone-only authentication)

JWT-based session management

Refresh token mechanism (7 days expiry)

User Registration

Delegated registration to User Service

Role-based profiles (Youth, NGO, Mentor, Funder, Service Provider)

Email and phone validation (Uganda format)

Password hashing (BCrypt strength 12)

Security & Token Management

Access tokens (1-hour expiry)

Refresh tokens (7-day expiry)

Token blacklisting via Redis

Stateless JWT-based authentication

Password reset workflow with secure tokens

Circuit breaker and retry patterns for resilience

Observability & Monitoring

Actuator health and metrics

Prometheus endpoint for monitoring

Centralized structured logging

In Progress (25% Remaining):

Account lockout mechanism (5 failed attempts)

Device fingerprinting for login sessions

Token rotation on refresh

OAuth2 (Google, Facebook)

Advanced integration tests and security audits

SYSTEM ARCHITECTURE:
┌─────────────────────────────────────────────────────────────┐
│                    API Gateway (Port 8080)                  │
│               (Single entry point for all clients)          │
└────────────────────────────┬────────────────────────────────┘
                             │
                             ▼
┌─────────────────────────────────────────────────────────────┐
│                 AUTH SERVICE (Port 8082)                    │
│─────────────────────────────────────────────────────────────│
│  Controllers:                                                │
│    • AuthController (Login, Register, Logout)               │
│    • TokenController (Refresh, Validate)                    │
│    • PasswordResetController                                │
│                                                             │
│  Services:                                                  │
│    • AuthService (Core logic)                               │
│    • JwtService (Token generation/validation)               │
│    • RefreshTokenService (Persistence)                      │
│    • TokenBlacklistService (Redis)                          │
│    • PasswordResetService                                   │
│                                                             │
│  Feign Clients:                                             │
│    • UserServiceClient (User data retrieval)                │
│    • NotificationServiceClient (Email/SMS)                  │
└─────────────────────────────────────────────────────────────┘
         │                    │                    │
         ▼                    ▼                    ▼
┌──────────────┐    ┌──────────────┐    ┌──────────────┐
│ User Service │    │ Notification │    │ Redis Cache  │
│ (Port 8081)  │    │ Service      │    │ (Port 6379)  │
└──────────────┘    └──────────────┘    └──────────────┘

TECHNOLOGY STACK

Framework: Spring Boot 3.1.5
Language: Java 17
Security: Spring Security + JWT (JJWT 0.11.5)
Database: MySQL 8.0
Cache: Redis 7.0
Discovery: Netflix Eureka
Resilience: Resilience4j (Circuit Breaker, Retry)
Docs: SpringDoc OpenAPI 3
Monitoring: Actuator + Prometheus
Migration: Flyway
Build Tool: Maven 3.9+

QUICK START

Prerequisites:

Java 17+

Maven 3.9+

MySQL 8.0+

Redis 7.0+

Eureka Service Registry (Port 8761)

Clone & Build:
git clone https://github.com/youthconnect/auth-service.git

cd auth-service
mvn clean install

Run Locally:
mvn spring-boot:run

Run with Docker:
docker build -t youthconnect/auth-service:1.0.0 .
docker run -d -p 8082:8082
-e DB_HOST=mysql
-e REDIS_HOST=redis
-e JWT_SECRET=your-secret-key
--network youthconnect-network
youthconnect/auth-service:1.0.0

ENVIRONMENT CONFIGURATION

DB_HOST=localhost
DB_PORT=3307
DB_NAME=youth_connect_db
DB_USER=root
DB_PASSWORD=your_password

REDIS_HOST=localhost
REDIS_PORT=6379

JWT_SECRET=YourSecure256BitSecretKey
JWT_EXPIRATION=3600000
JWT_REFRESH_EXPIRATION=604800000

EUREKA_URL=http://localhost:8761/eureka/

SPRING_PROFILES_ACTIVE=dev
SERVER_PORT=8082

API ENDPOINTS

Authentication:
POST /api/auth/register - Register new user
POST /api/auth/login - Login with email/phone
POST /api/auth/ussd/login - USSD login (phone only)
POST /api/auth/refresh - Refresh JWT token
POST /api/auth/logout - Logout user
GET /api/auth/validate - Validate token

Password Reset:
POST /api/auth/password/forgot - Request password reset
GET /api/auth/password/validate-reset-token - Validate reset token
POST /api/auth/password/reset - Reset password

Health & Docs:
GET /api/auth/health
GET /actuator/prometheus
GET /swagger-ui.html

SAMPLE REQUEST

Login Example:

curl -X POST http://localhost:8082/api/auth/login

-H "Content-Type: application/json"
-d '{
"identifier": "john@example.com
",
"password": "SecurePass@123"
}'

Response:
{
"success": true,
"message": "Login successful",
"data": {
"accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
"refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
"expiresIn": 3600,
"userId": 1,
"email": "john@example.com
",
"role": "YOUTH"
}
}

APPLICATION PROPERTIES

app.security.password.min-length=8
app.security.password.require-uppercase=true
app.security.password.require-lowercase=true
app.security.password.require-digit=true
app.security.password.require-special-char=true
app.security.password.max-attempts=5
app.security.password.lockout-duration-minutes=30

app.token.blacklist-cleanup-cron=0 0 2 * * ?
app.token.max-refresh-count=10

app.password-reset.token-expiry-minutes=15
app.password-reset.max-attempts=3

TESTING

mvn test
mvn verify
mvn jacoco:report

MONITORING

Health: http://localhost:8082/actuator/health

Metrics: http://localhost:8082/actuator/metrics

Prometheus: http://localhost:8082/actuator/prometheus

Swagger UI: http://localhost:8082/swagger-ui.html

SECURITY BEST PRACTICES

Use a strong JWT secret (at least 256 bits)

Deploy only over HTTPS

Rotate JWT secrets periodically

Monitor failed login attempts

Keep dependencies updated

Store tokens securely in Redis (with TTL)

Enforce BCrypt password hashing (strength 12)

DEVELOPMENT TEAM

Youth Connect Uganda Development Team

Backend Lead: Douglas Kings Kato
Security & Integration: [Team Member]
DevOps & Infrastructure: [Team Member]

LICENSE

MIT License – Youth Connect Uganda Platform
© 2025 Youth Connect Uganda. All rights reserved.

SUPPORT

Email: tech@youthconnect.ug

Slack: #backend-support
Docs: https://docs.youthconnect.ug

Issues: https://github.com/youthconnect/auth-service/issues

Version: 1.0.0
Status: Production Ready (75% Complete)
Last Updated: October 2025