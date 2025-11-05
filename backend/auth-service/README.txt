# Entrepreneurship Booster Platform – Auth Service
**Authentication & Authorization Microservice**

A robust, secure, and scalable JWT-based authentication service for the Youth Connect Uganda Platform, supporting web, mobile, and USSD interfaces.

---

## 📋 Table of Contents
- [Overview](#overview)
- [Features](#features)
- [Architecture](#architecture)
- [Technology Stack](#technology-stack)
- [Quick Start](#quick-start)
- [API Documentation](#api-documentation)
- [Configuration](#configuration)
- [Development Guidelines](#development-guidelines)
- [Deployment](#deployment)
- [Testing](#testing)
- [Monitoring](#monitoring)
- [Security](#security)
- [Support](#support)

---

## 🎯 Overview

The Auth Service is a standalone microservice that handles authentication, authorization, and token management for all Youth Connect platform users. It integrates with the User Service and Notification Service via Feign clients and supports resilient inter-service communication using Resilience4j and Eureka for service discovery.

### Key Responsibilities
- ✅ User Authentication (Web & USSD)
- ✅ JWT Token Management
- ✅ Password Reset Workflow
- ✅ Refresh Token Rotation
- ✅ Token Blacklisting
- ✅ Security Audit Logging

---

## 🚀 Features

### ✅ Implemented (95% Complete)

#### Authentication
- [x] Web login (email/phone + password)
- [x] USSD login (phone-only authentication)
- [x] JWT-based session management
- [x] Refresh token mechanism (7-day expiry)
- [x] Token blacklisting via Redis
- [x] Multi-factor authentication support

#### User Registration
- [x] Delegated registration to User Service
- [x] Role-based profiles (Youth, NGO, Mentor, Funder, Service Provider)
- [x] Email and phone validation (Uganda format)
- [x] Password hashing (BCrypt strength 12)
- [x] Welcome email notifications

#### Security & Token Management
- [x] Access tokens (1-hour expiry)
- [x] Refresh tokens (7-day expiry, database persisted)
- [x] Token validation endpoint
- [x] Password reset workflow with secure tokens (15-minute expiry)
- [x] Circuit breaker and retry patterns for resilience
- [x] Account lockout (5 failed attempts, 30-minute lockout)

#### Observability & Monitoring
- [x] Actuator health and metrics
- [x] Prometheus endpoint for monitoring
- [x] Centralized structured logging
- [x] Comprehensive audit trail

### 🔄 In Progress (5% Remaining)
- [ ] Device fingerprinting for login sessions
- [ ] Token rotation on refresh
- [ ] OAuth2 (Google, Facebook) integration
- [ ] Advanced integration tests and security audits

---

## 🏗️ Architecture
```
┌─────────────────────────────────────────────────────────────┐
│                    API Gateway (Port 8080)                  │
│               (Single entry point for all clients)          │
└────────────────────────────┬────────────────────────────────┘
                             │
                             ▼
┌─────────────────────────────────────────────────────────────┐
│                 AUTH SERVICE (Port 8083)                    │
│─────────────────────────────────────────────────────────────│
│  Controllers:                                                │
│    • AuthController (Login, Register, Logout)               │
│    • PasswordResetController (Reset workflow)               │
│                                                             │
│  Services:                                                  │
│    • AuthService (Core authentication logic)                │
│    • PasswordResetService (Reset workflow)                  │
│    • TokenBlacklistService (Redis-based)                    │
│    • CustomUserDetailsService (Spring Security)             │
│                                                             │
│  Security:                                                  │
│    • JwtUtil (Token generation/validation)                  │
│    • JwtAuthenticationFilter (Request interception)         │
│    • SecurityConfig (Security chain configuration)          │
│                                                             │
│  Feign Clients:                                             │
│    • UserServiceClient (User data retrieval)                │
│    • NotificationServiceClient (Email/SMS)                  │
└─────────────────────────────────────────────────────────────┘
         │                    │                    │
         ▼                    ▼                    ▼
┌──────────────┐    ┌──────────────┐    ┌──────────────┐
│ User Service │    │ Notification │    │ PostgreSQL   │
│ (Port 8081)  │    │ Service      │    │ (Port 5432)  │
└──────────────┘    └──────────────┘    └──────────────┘
                                                │
                                                ▼
                                        ┌──────────────┐
                                        │ Redis Cache  │
                                        │ (Port 6379)  │
                                        └──────────────┘
```

---

## 🛠️ Technology Stack

| Component | Technology | Version |
|-----------|-----------|---------|
| Framework | Spring Boot | 3.1.5 |
| Language | Java | 17 |
| Security | Spring Security + JWT | 6.1.x / 0.11.5 |
| Database | PostgreSQL | 15+ |
| Cache | Redis | 7.0+ |
| Service Discovery | Netflix Eureka | 4.0.x |
| Resilience | Resilience4j | 2.x |
| API Docs | SpringDoc OpenAPI | 2.2.0 |
| Monitoring | Actuator + Prometheus | 3.1.x |
| Migration | Flyway | 9.x |
| Build Tool | Maven | 3.9+ |

---

## ⚡ Quick Start

### Prerequisites
- ✅ Java 17+
- ✅ Maven 3.9+
- ✅ PostgreSQL 15+
- ✅ Redis 7.0+
- ✅ Docker & Docker Compose (optional)

### 1️⃣ Clone Repository
```bash
git clone https://github.com/youthconnect/auth-service.git
cd auth-service
```

### 2️⃣ Configure Environment
```bash
cp .env.example .env
# Edit .env with your configuration
```

### 3️⃣ Setup Database
```bash
# Create database
createdb -U postgres ebp_db

# Run migrations (automatic on startup)
mvn flyway:migrate
```

### 4️⃣ Build & Run

#### Option A: Local Development
```bash
mvn clean install
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

#### Option B: Docker Compose (Recommended)
```bash
docker-compose up -d
```

### 5️⃣ Verify Service
```bash
# Health check
curl http://localhost:8083/api/auth/health

# API Documentation
open http://localhost:8083/swagger-ui.html
```

---

## 📚 API Documentation

### Authentication Endpoints

#### 1️⃣ User Login
```http
POST /api/auth/login
Content-Type: application/json

{
  "identifier": "user@example.com",
  "password": "SecurePass@123"
}
```

**Response:**
```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "tokenType": "Bearer",
    "expiresIn": 3600,
    "userId": "550e8400-e29b-41d4-a716-446655440000",
    "email": "user@example.com",
    "role": "YOUTH"
  },
  "timestamp": 1699123456789
}
```

#### 2️⃣ USSD Login
```http
POST /api/auth/ussd/login
Content-Type: application/json

{
  "phoneNumber": "+256701430234",
  "sessionId": "ATUid_abc123xyz"
}
```

#### 3️⃣ User Registration
```http
POST /api/auth/register
Content-Type: application/json

{
  "email": "newuser@example.com",
  "phoneNumber": "+256701430234",
  "password": "SecurePass@123",
  "role": "YOUTH",
  "firstName": "John",
  "lastName": "Doe"
}
```

#### 4️⃣ Refresh Token
```http
POST /api/auth/refresh
Content-Type: application/json

{
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

#### 5️⃣ Logout
```http
POST /api/auth/logout
Authorization: Bearer <access-token>
Content-Type: application/json

{
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

#### 6️⃣ Validate Token
```http
GET /api/auth/validate
Authorization: Bearer <access-token>
```

### Password Reset Endpoints

#### 1️⃣ Request Password Reset
```http
POST /api/auth/password/forgot
Content-Type: application/json

{
  "email": "user@example.com"
}
```

#### 2️⃣ Validate Reset Token
```http
GET /api/auth/password/validate-reset-token?token=<reset-token>
```

#### 3️⃣ Reset Password
```http
POST /api/auth/password/reset
Content-Type: application/json

{
  "token": "<reset-token>",
  "newPassword": "NewSecurePass@123"
}
```

### Complete API Documentation
📖 **Swagger UI:** http://localhost:8083/swagger-ui.html  
📄 **OpenAPI JSON:** http://localhost:8083/api-docs

---

## ⚙️ Configuration

### Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `SPRING_PROFILES_ACTIVE` | Active profile (dev/docker/prod) | `dev` |
| `SERVER_PORT` | Service port | `8083` |
| `DB_HOST` | PostgreSQL host | `localhost` |
| `DB_PORT` | PostgreSQL port | `5432` |
| `DB_NAME` | Database name | `ebp_db` |
| `DB_USERNAME` | Database user | `postgres` |
| `DB_PASSWORD` | Database password | - |
| `REDIS_HOST` | Redis host | `localhost` |
| `REDIS_PORT` | Redis port | `6379` |
| `JWT_SECRET` | JWT signing key (256-bit min) | - |
| `JWT_EXPIRATION` | Access token expiry (ms) | `3600000` |
| `JWT_REFRESH_EXPIRATION` | Refresh token expiry (ms) | `604800000` |
| `EUREKA_URL` | Eureka server URL | `http://localhost:8761/eureka/` |

### Application Profiles

#### `local` - Local Development
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/ebp_db?currentSchema=auth
  jpa:
    show-sql: true
logging:
  level:
    com.youthconnect.auth_service: DEBUG
```

#### `docker` - Docker Compose
```yaml
spring:
  datasource:
    url: jdbc:postgresql://postgres:5432/ebp_db?currentSchema=auth
  data:
    redis:
      host: redis
eureka:
  client:
    service-url:
      defaultZone: http://service-registry:8761/eureka/
```

#### `prod` - Production
```yaml
spring:
  datasource:
    url: jdbc:postgresql://${DB_HOST}:${DB_PORT}/${DB_NAME}?currentSchema=auth&ssl=true
    hikari:
      maximum-pool-size: 20
jwt:
  secret: ${JWT_SECRET}
logging:
  level:
    root: WARN
    com.youthconnect.auth_service: INFO
```

---

## 👨‍💻 Development Guidelines

### Code Standards
1. ✅ **No ResponseEntity Wrappers** - Return DTOs directly with `@ResponseStatus`
2. ✅ **Use UUIDs** - All identifiers must be UUIDs
3. ✅ **Pagination** - All list endpoints must return paged responses
4. ✅ **Health Checks** - Every service must have `/health` endpoint
5. ✅ **API Documentation** - Swagger/OpenAPI required
6. ✅ **Docker Support** - Must be containerizable

### Branching Strategy
```bash
# Always create feature branches from development
git checkout development
git pull origin development
git checkout -b feature/your-feature-name

# After completion
git add .
git commit -m "feat: description"
git push origin feature/your-feature-name
# Create Pull Request to development
```

### Code Quality
```bash
# Run tests
mvn test

# Check code coverage
mvn jacoco:report

# Static code analysis
mvn spotbugs:check
```

---

## 🚀 Deployment

### Docker Build
```bash
docker build -t youthconnect/auth-service:1.0.0 .
```

### Docker Compose (Full Stack)
```bash
docker-compose up -d
```

### Production Deployment
```bash
# Build production image
docker build -t youthconnect/auth-service:1.0.0 -f Dockerfile .

# Push to registry
docker push youthconnect/auth-service:1.0.0

# Deploy with environment variables
docker run -d \
  -p 8083:8083 \
  -e SPRING_PROFILES_ACTIVE=prod \
  -e DB_HOST=prod-postgres \
  -e DB_PASSWORD=${DB_PASSWORD} \
  -e REDIS_HOST=prod-redis \
  -e JWT_SECRET=${JWT_SECRET} \
  --network youthconnect-network \
  youthconnect/auth-service:1.0.0
```

---

## 🧪 Testing

### Unit Tests
```bash
mvn test
```

### Integration Tests
```bash
mvn verify
```

### Test Coverage Report
```bash
mvn jacoco:report
open target/site/jacoco/index.html
```

### Manual Testing with cURL
```bash
# Login
curl -X POST http://localhost:8083/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "identifier": "damienpapers3@gmail.com",
    "password": "Youth123!"
  }'

# Refresh Token
curl -X POST http://localhost:8083/api/auth/refresh \
  -H "Content-Type: application/json" \
  -d '{
    "refreshToken": "<your-refresh-token>"
  }'

# Logout
curl -X POST http://localhost:8083/api/auth/logout \
  -H "Authorization: Bearer <access-token>" \
  -H "Content-Type: application/json" \
  -d '{
    "refreshToken": "<your-refresh-token>"
  }'
```

---

## 📊 Monitoring

### Health Check
```bash
curl http://localhost:8083/api/auth/health
```

### Metrics Endpoints
- **Health:** http://localhost:8083/actuator/health
- **Metrics:** http://localhost:8083/actuator/metrics
- **Prometheus:** http://localhost:8083/actuator/prometheus
- **Info:** http://localhost:8083/actuator/info

### Prometheus Configuration
```yaml
scrape_configs:
  - job_name: 'auth-service'
    metrics_path: '/actuator/prometheus'
    static_configs:
      - targets: ['localhost:8083']
```

### Grafana Dashboard
Import dashboard ID: `TBD` (Custom dashboard coming soon)

---

## 🔒 Security

### Best Practices
1. ✅ **Strong JWT Secrets** - Minimum 256 bits
2. ✅ **HTTPS Only** - Deploy only over HTTPS in production
3. ✅ **Secret Rotation** - Rotate JWT secrets periodically
4. ✅ **Failed Login Monitoring** - Track and alert on failed attempts
5. ✅ **Dependency Updates** - Keep all dependencies up to date
6. ✅ **Redis TTL** - Store tokens with expiration
7. ✅ **BCrypt Hashing** - Password hashing with strength 12

### Password Requirements
- ✅ Minimum 8 characters
- ✅ At least one uppercase letter
- ✅ At least one lowercase letter
- ✅ At least one digit
- ✅ At least one special character

### Account Lockout
- ✅ 5 failed login attempts
- ✅ 30-minute lockout duration
- ✅ Automatic unlock after timeout

---

## 📞 Support

### Development Team
**Youth Connect Uganda Development Team**
- **Backend Lead:** Douglas Kings Kato
- **Email:** tech@youthconnect.ug
- **Documentation:** https://docs.youthconnect.ug
- **Issues:** https://github.com/youthconnect/auth-service/issues

### Communication Channels
- **Slack:** #backend-support
- **Email:** tech@youthconnect.ug
- **GitHub Issues:** Preferred for bug reports and feature requests

---

## 📄 License

MIT License – Youth Connect Uganda Platform  
© 2025 Youth Connect Uganda. All rights reserved.

---

## 📝 Version History

| Version | Date | Changes |
|---------|------|---------|
| 1.0.0 | 2025-01-01 | Initial production release |
| 0.9.0 | 2024-12-15 | Beta release with core features |
| 0.5.0 | 2024-11-01 | Alpha release for testing |

---

## 🎯 Roadmap

### Q1 2025
- [ ] OAuth2 integration (Google, Facebook)
- [ ] Device fingerprinting
- [ ] Enhanced audit logging
- [ ] Performance optimization

### Q2 2025
- [ ] Multi-language support
- [ ] Advanced analytics
- [ ] Rate limiting per user
- [ ] Biometric authentication support

---

**Status:** ✅ Production Ready (95% Complete)  
**Last Updated:** January 2025  
**Maintainer:** Douglas Kings Kato

---
Quick Start Commands
# 1. Start PostgreSQL and Redis only
docker-compose -f docker-compose-dev.yml up -d postgres redis

# 2. Wait for databases to be ready (about 10 seconds)
docker-compose -f docker-compose-dev.yml ps

# 3. Verify PostgreSQL is running
docker exec -it auth-postgres-dev psql -U postgres -d ebp_db -c "SELECT version();"

# 4. Verify Redis is running
docker exec -it auth-redis-dev redis-cli ping

# 5. Now run your Auth Service
mvn spring-boot:run -Dspring-boot.run.profiles=local


## 🙏 Acknowledgments

Special thanks to:
- Entrepreneurship Booster Platform team
- Namatovu Florence
- Damien Papers
- Jim Daniels Wasswa
- Open source community
- Spring Boot team
- All contributors

---

**Built with ❤️ for Youth Connect Uganda**