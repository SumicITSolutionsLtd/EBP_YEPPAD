# Auth Service - Youth Connect Uganda Platform

## Overview

Standalone authentication and authorization microservice for the Youth Connect Uganda Platform. Handles all authentication flows including web-based login, USSD authentication, JWT token management, and password reset workflows.

## Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                      Auth Service                            │
├─────────────────────────────────────────────────────────────┤
│  Controllers                                                 │
│    ├── AuthController (Login, Register, Logout)            │
│    ├── TokenController (Refresh, Validate)                 │
│    └── PasswordResetController                              │
├─────────────────────────────────────────────────────────────┤
│  Services                                                    │
│    ├── AuthService (Core authentication logic)              │
│    ├── JwtService (Token generation/validation)             │
│    ├── RefreshTokenService (Token management)               │
│    ├── TokenBlacklistService (Redis-based)                  │
│    └── PasswordResetService                                 │
├─────────────────────────────────────────────────────────────┤
│  Feign Clients (Inter-service communication)                │
│    ├── UserServiceClient (User data retrieval)              │
│    └── NotificationServiceClient (Email/SMS)                │
├─────────────────────────────────────────────────────────────┤
│  Data Layer                                                  │
│    ├── MySQL (Refresh tokens, Password reset tokens)        │
│    └── Redis (Token blacklist, Session cache)               │
└─────────────────────────────────────────────────────────────┘
```

## Technology Stack

- **Framework**: Spring Boot 3.1.5
- **Language**: Java 17
- **Security**: Spring Security + JWT (JJWT 0.11.5)
- **Database**: MySQL 8.0
- **Cache**: Redis 7.0
- **Service Discovery**: Netflix Eureka
- **API Communication**: OpenFeign
- **Resilience**: Resilience4j (Circuit Breaker, Retry)
- **Database Migration**: Flyway
- **API Documentation**: SpringDoc OpenAPI 3
- **Monitoring**: Spring Boot Actuator + Prometheus

## Features

### ✅ Implemented Features

1. **Web Authentication**
   - Email/Phone + Password login
   - User registration (delegates to user-service)
   - JWT access token generation (1 hour expiry)
   - JWT refresh token generation (7 days expiry)

2. **USSD Authentication**
   - Phone-only authentication
   - Simplified login flow for USSD users
   - Session management

3. **Token Management**
   - Access token validation
   - Refresh token rotation
   - Token blacklisting on logout
   - Redis-based blacklist with TTL

4. **Security**
   - BCrypt password hashing (strength 12)
   - Stateless JWT authentication
   - CORS configuration
   - Rate limiting (via API Gateway)
   - Circuit breaker pattern for resilience

5. **Password Management**
   - Password reset request
   - Token-based password reset
   - Secure token generation

6. **Monitoring & Health**
   - Spring Boot Actuator endpoints
   - Prometheus metrics
   - Health checks for dependencies
   - Structured logging

## Prerequisites

- Java 17 or higher
- Maven 3.9+
- MySQL 8.0
- Redis 7.0
- Running Eureka Service Registry (port 8761)

## Environment Variables

```bash
# Database Configuration
DB_HOST=localhost
DB_PORT=3307
DB_NAME=youth_connect_db
DB_USER=root
DB_PASSWORD=Douglas20!

# Redis Configuration
REDIS_HOST=localhost
REDIS_PORT=6379

# JWT Configuration
JWT_SECRET=aVeryLongAndSecureSecretKeyForYouthConnectUgandaHackathonProjectWith256BitsMinimum
JWT_EXPIRATION=3600000          # 1 hour
JWT_REFRESH_EXPIRATION=604800000 # 7 days

# Service Discovery
EUREKA_URL=http://localhost:8761/eureka/

# Application
SPRING_PROFILES_ACTIVE=dev
SERVER_PORT=8082
```

## Quick Start

### 1. Clone and Build

```bash
cd auth-service
mvn clean install
```

### 2. Run Locally

```bash
# Ensure MySQL and Redis are running
# Ensure Eureka service registry is running on port 8761

mvn spring-boot:run
```

### 3. Run with Docker

```bash
# Build image
docker build -t youthconnect/auth-service:1.0.0 .

# Run container
docker run -p 8082:8082 \
  -e SPRING_PROFILES_ACTIVE=docker \
  -e DB_HOST=mysql \
  -e REDIS_HOST=redis \
  --network youthconnect-network \
  youthconnect/auth-service:1.0.0
```

### 4. Run with Docker Compose

```bash
# From project root
docker-compose up auth-service
```

## API Endpoints

### Authentication Endpoints

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| POST | `/api/auth/login` | User login | No |
| POST | `/api/auth/register` | User registration | No |
| POST | `/api/auth/ussd/login` | USSD login | No |
| POST | `/api/auth/refresh` | Refresh access token | No |
| POST | `/api/auth/logout` | Logout user | Yes |
| GET | `/api/auth/validate` | Validate token | Yes |
| GET | `/api/auth/health` | Health check | No |

### API Documentation

Once running, access:
- **Swagger UI**: http://localhost:8082/api/auth/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8082/api/auth/api-docs

## Request/Response Examples

### Login Request

```bash
curl -X POST http://localhost:8082/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "identifier": "damienpapers3@gmail.com",
    "password": "Youth@123"
  }'
```

### Login Response

```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "tokenType": "Bearer",
    "expiresIn": 3600,
    "userId": 2,
    "email": "damienpapers3@gmail.com",
    "role": "YOUTH"
  },
  "timestamp": 1704092400000
}
```

### USSD Login Request

```bash
curl -X POST http://localhost:8082/api/auth/ussd/login \
  -H "Content-Type: application/json" \
  -d '{
    "phoneNumber": "+256701430234",
    "sessionId": "USSD_SESSION_12345"
  }'
```

### Token Refresh Request

```bash
curl -X POST http://localhost:8082/api/auth/refresh \
  -H "Content-Type: application/json" \
  -d '{
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
  }'
```

### Logout Request

```bash
curl -X POST http://localhost:8082/api/auth/logout \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." \
  -H "Content-Type: application/json" \
  -d '{
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
  }'
```

## Database Schema

### Refresh Tokens Table

```sql
CREATE TABLE refresh_tokens (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    token VARCHAR(500) NOT NULL UNIQUE,
    user_id BIGINT NOT NULL,
    user_email VARCHAR(255) NOT NULL,
    user_role VARCHAR(50),
    expires_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL,
    revoked BOOLEAN NOT NULL DEFAULT FALSE,
    revoked_at TIMESTAMP NULL,
    last_used_at TIMESTAMP NULL
);
```

### Password Reset Tokens Table

```sql
CREATE TABLE password_reset_tokens (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    token VARCHAR(255) NOT NULL UNIQUE,
    user_id BIGINT NOT NULL,
    user_email VARCHAR(255) NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL,
    used BOOLEAN NOT NULL DEFAULT FALSE,
    used_at TIMESTAMP NULL
);
```

## Configuration Files

### application.yml Structure

```
src/main/resources/
├── application.yml          # Base configuration
├── application-dev.yml      # Development overrides
├── application-docker.yml   # Docker environment
├── application-prod.yml     # Production settings
└── db/migration/
    ├── V1__Create_refresh_tokens_table.sql
    ├── V2__Create_password_reset_tokens_table.sql
    └── V3__Create_audit_logs_table.sql
```

## Monitoring & Health Checks

### Actuator Endpoints

- **Health**: http://localhost:8082/api/auth/actuator/health
- **Metrics**: http://localhost:8082/api/auth/actuator/metrics
- **Prometheus**: http://localhost:8082/api/auth/actuator/prometheus
- **Info**: http://localhost:8082/api/auth/actuator/info

### Health Check Response

```json
{
  "status": "UP",
  "components": {
    "db": {"status": "UP"},
    "redis": {"status": "UP"},
    "diskSpace": {"status": "UP"},
    "ping": {"status": "UP"}
  }
}
```

## Security Considerations

1. **JWT Secret**: Use a strong, randomly generated secret (min 256 bits)
2. **Password Hashing**: BCrypt with strength 12
3. **Token Expiry**: Short-lived access tokens (1 hour), longer refresh tokens (7 days)
4. **Token Blacklisting**: Revoked tokens stored in Redis with TTL
5. **HTTPS**: Always use HTTPS in production
6. **Rate Limiting**: Implement at API Gateway level
7. **CORS**: Configure allowed origins appropriately

## Testing

```bash
# Run unit tests
mvn test

# Run integration tests
mvn verify

# Run specific test
mvn test -Dtest=AuthServiceTest

# Generate test coverage report
mvn jacoco:report
```

## Troubleshooting

### Common Issues

1. **Cannot connect to MySQL**
   - Verify MySQL is running: `docker ps | grep mysql`
   - Check port mapping: `3307:3306`
   - Verify credentials in application.yml

2. **Cannot connect to Redis**
   - Verify Redis is running: `docker ps | grep redis`
   - Test connection: `redis-cli ping`

3. **Eureka registration fails**
   - Ensure service-registry is running on port 8761
   - Check eureka.client.service-url.defaultZone

4. **Token validation fails**
   - Ensure JWT secret matches across services
   - Check token expiration
   - Verify token is not blacklisted in Redis

## Project Structure

```
auth-service/
├── src/
│   ├── main/
│   │   ├── java/com/youthconnect/auth/
│   │   │   ├── AuthServiceApplication.java
│   │   │   ├── config/
│   │   │   │   ├── SecurityConfig.java
│   │   │   │   ├── RedisConfig.java
│   │   │   │   └── SwaggerConfig.java
│   │   │   ├── controller/
│   │   │   │   └── AuthController.java
│   │   │   ├── service/
│   │   │   │   ├── AuthService.java
│   │   │   │   ├── TokenBlacklistService.java
│   │   │   │   └── CustomUserDetailsService.java
│   │   │   ├── client/
│   │   │   │   ├── UserServiceClient.java
│   │   │   │   └── NotificationServiceClient.java
│   │   │   ├── dto/
│   │   │   ├── entity/
│   │   │   ├── repository/
│   │   │   ├── security/
│   │   │   ├── exception/
│   │   │   └── util/
│   │   │       └── JwtUtil.java
│   │   └── resources/
│   │       ├── application.yml
│   │       ├── application-dev.yml
│   │       ├── application-docker.yml
│   │       └── db/migration/
│   └── test/
├── pom.xml
├── Dockerfile
└── README.md
```

## Next Steps

1. ✅ **Completed**: Core authentication service
2. 🚧 **In Progress**: Integration with user-service
3. ⏳ **Pending**: OAuth2 integration (Google, Facebook)
4. ⏳ **Pending**: Two-factor authentication (2FA)
5. ⏳ **Pending**: Biometric authentication support

## Contributing

1. Create feature branch: `git checkout -b feature/your-feature`
2. Commit changes: `git commit -am 'Add feature'`
3. Push branch: `git push origin feature/your-feature`
4. Create Pull Request

## License

MIT License - Youth Connect Uganda Platform

## Support

- **Email**: douglaskings2@gmail.com
- **Documentation**: https://docs.youthconnect.ug
- **Issues**: https://github.com/youthconnect/auth-service/issues

---

**Version**: 1.0.0
**Last Updated**: OCTOBER 2025
**Maintained By**: Douglas Kings Kato