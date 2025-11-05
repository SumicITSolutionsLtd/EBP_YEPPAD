# Youth Connect Uganda - API Gateway with JWT Authentication

## 🔐 Overview

The API Gateway is the **centralized entry point** for all client requests in the Youth Connect Uganda platform. It provides intelligent routing, **JWT authentication**, security, and monitoring capabilities.

### **NEW: JWT Authentication Integration**

The gateway now validates JWT tokens before routing requests to backend services, ensuring secure access control across the entire platform.

---

## ✨ Features

✅ **JWT Authentication** - Validates tokens before routing to backend services  
✅ **Intelligent Routing** - Routes requests to appropriate microservices  
✅ **Rate Limiting** - Token bucket algorithm (100 req/min)  
✅ **Circuit Breaker** - Resilience4j for fault tolerance  
✅ **CORS Configuration** - Supports web and mobile clients  
✅ **Security Headers** - Automatic injection of security headers  
✅ **Request Logging** - Comprehensive logging with request IDs  
✅ **Load Balancing** - Automatic load balancing across instances  
✅ **Service Discovery** - Integration with Eureka  
✅ **User Context Injection** - Adds user info headers for downstream services

---

## 🏗️ Architecture

```
[Client] → [JWT Token] → [API Gateway:8088] 
    ↓ Validate Token
    ↓ Extract User Info
    ↓ Add Headers (X-User-Id, X-User-Email, X-User-Roles)
    → [Eureka:8761] → [Backend Services]
```

### Request Flow with Authentication

1. Client sends request with `Authorization: Bearer <token>` header
2. **JWT Authentication Filter** validates token signature and expiration
3. Gateway extracts user info (userId, email, roles) from token
4. **User context headers** added to request for backend services
5. Rate limiting filter checks request limits
6. Request logging filter logs the request
7. Gateway routes to appropriate backend service via Eureka
8. Circuit breaker protects against service failures
9. Security headers added to response
10. Response returned to client

---

## 📁 Project Structure

```
api-gateway/
├── src/
│   ├── main/
│   │   ├── java/com/youthconnect/api_gateway/
│   │   │   ├── ApiGatewayApplication.java
│   │   │   ├── config/
│   │   │   │   ├── CorsConfig.java
│   │   │   │   ├── RateLimitConfig.java
│   │   │   │   └── GatewayConfig.java
│   │   │   ├── controller/
│   │   │   │   ├── FallbackController.java
│   │   │   │   └── HealthCheckController.java
│   │   │   ├── exception/
│   │   │   │   └── GlobalExceptionHandler.java
│   │   │   ├── filter/
│   │   │   │   ├── JwtAuthenticationFilter.java       ⭐ NEW
│   │   │   │   ├── RateLimitGatewayFilter.java
│   │   │   │   ├── SecurityHeadersFilter.java
│   │   │   │   └── RequestLoggingFilter.java
│   │   │   └── util/
│   │   │       └── JwtUtil.java                       ⭐ NEW
│   │   └── resources/
│   │       └── application.yml
│   └── test/
├── pom.xml
└── README.md
```

---

## 🔐 JWT Authentication Details

### Public Endpoints (No Authentication Required)

These endpoints are accessible without a JWT token:

```
POST   /api/auth/register
POST   /api/auth/login
POST   /api/auth/refresh
POST   /api/auth/reset-password/**
GET    /health/**
GET    /actuator/**
GET    /swagger-ui/**
GET    /v3/api-docs/**
```

### Protected Endpoints (JWT Required)

All other endpoints require a valid JWT token in the `Authorization` header:

```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

### User Context Headers

After JWT validation, the gateway adds these headers for backend services:

| Header | Description | Example |
|--------|-------------|---------|
| `X-User-Id` | User's unique identifier (UID) | `550e8400-e29b-41d4-a716-446655440000` |
| `X-User-Email` | User's email address | `john.doe@example.com` |
| `X-User-Roles` | Comma-separated user roles | `USER,MENTOR,ADMIN` |
| `X-Auth-Token` | Original JWT token | `eyJhbGciOiJIUz...` |

Backend services can use these headers to identify users without re-validating the JWT.

### JWT Configuration

JWT settings in `application.yml`:

```yaml
jwt:
  secret: ${JWT_SECRET:YouthConnectUgandaSecureSecretKey2025MinimumLengthRequired}
  expiration: 86400000  # 24 hours
  issuer: youth-connect-uganda
```

**⚠️ IMPORTANT**: The `jwt.secret` **MUST** match the secret used by the `auth-service`.

---

## 🚀 Running the Gateway

### Prerequisites

1. **Java 17** installed
2. **Maven 3.8+** installed
3. **Service Registry** running on port 8761
4. **Auth Service** configured with matching JWT secret

### Step 1: Set JWT Secret (Production)

For production, use environment variable:

```bash
export JWT_SECRET="YourSecureSecretKeyMinimum32CharactersLong"
```

### Step 2: Start the Gateway

```bash
# Navigate to api-gateway directory
cd api-gateway

# Clean and install dependencies
mvn clean install

# Run the application
mvn spring-boot:run
```

The gateway will start on **port 8088**.

### Step 3: Verify Startup

1. Check console for startup message
2. Visit health endpoint: `http://localhost:8088/health`
3. Check Eureka dashboard: `http://localhost:8761`

Expected output:
```
🚀 API Gateway Started Successfully!
🌐 Local URL: http://localhost:8088
📊 Eureka Server: http://localhost:8761/eureka
```

---

## 🧪 Testing Authentication

### Test 1: Public Endpoint (No Token Required)

```bash
curl http://localhost:8088/api/auth/login \
  -X POST \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@example.com",
    "password": "password123"
  }'
```

Expected: **200 OK** with JWT token in response

### Test 2: Protected Endpoint (Token Required)

```bash
# Without token - should fail
curl http://localhost:8088/api/v1/users/me \
  -H "Content-Type: application/json"
```

Expected: **401 Unauthorized**
```json
{
  "timestamp": "2025-11-02T16:30:00",
  "status": 401,
  "error": "Unauthorized",
  "message": "Missing or invalid Authorization header",
  "path": "/api/v1/users/me"
}
```

```bash
# With valid token - should succeed
curl http://localhost:8088/api/v1/users/me \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

Expected: **200 OK** with user data

### Test 3: Expired Token

```bash
curl http://localhost:8088/api/v1/users/me \
  -H "Authorization: Bearer <expired_token>"
```

Expected: **401 Unauthorized**
```json
{
  "timestamp": "2025-11-02T16:30:00",
  "status": 401,
  "error": "Unauthorized",
  "message": "Invalid or expired token",
  "path": "/api/v1/users/me"
}
```

### Test 4: Verify User Context Headers

Backend services should receive user context headers:

```bash
# In backend service logs, you should see:
X-User-Id: 550e8400-e29b-41d4-a716-446655440000
X-User-Email: john.doe@example.com
X-User-Roles: USER,MENTOR
X-Auth-Token: eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

---

## 📋 Service Routes

### Authentication Required Services

| Service | Path Pattern | Authentication | Backend Service |
|---------|-------------|----------------|-----------------|
| Auth | `/api/auth/login`, `/api/auth/register` | ❌ Public | auth-service |
| Users | `/api/v1/users/**` | ✅ Required | user-service |
| Jobs | `/api/v1/jobs/**` | ✅ Required | job-service |
| Opportunities | `/api/opportunities/**` | ✅ Required | opportunity-service |
| Mentorship | `/api/mentorship/**` | ✅ Required | mentor-service |
| Files | `/api/files/**` | ✅ Required | file-management-service |
| Notifications | `/api/notifications/**` | ✅ Required | notification-service |
| AI Recommendations | `/api/v1/ai/**` | ✅ Required | ai-recommendation-service |
| Content | `/api/learning/**`, `/api/posts/**` | ⚠️ Mixed | content-service |
| Analytics | `/api/analytics/**` | ✅ Internal | analytics-service |

---

## 🔧 Configuration

### Rate Limiting

Rate limits apply **after** JWT validation:

| Endpoint Type | Requests/Minute |
|--------------|-----------------|
| Auth Endpoints | 20 |
| USSD Endpoints | 50 |
| General Endpoints | 100 |

### Circuit Breaker

Circuit breaker configuration:

```yaml
resilience4j:
  circuitbreaker:
    configs:
      default:
        failure-rate-threshold: 50
        wait-duration-in-open-state: 10s
        sliding-window-size: 10
```

---

## 🐳 Docker Deployment

### Dockerfile

```dockerfile
FROM openjdk:17-jdk-slim
WORKDIR /app
COPY target/api-gateway-1.0.0.jar app.jar
EXPOSE 8088

# Set JWT secret via environment variable
ENV JWT_SECRET=${JWT_SECRET}

ENTRYPOINT ["java", "-jar", "app.jar"]
```

### Docker Compose

```yaml
version: '3.8'
services:
  api-gateway:
    build: ./api-gateway
    ports:
      - "8088:8088"
    environment:
      - JWT_SECRET=${JWT_SECRET}
      - EUREKA_SERVER=http://service-registry:8761/eureka
    depends_on:
      - service-registry
      - auth-service
```

---

## 🔍 Monitoring

### Health Checks

```bash
# Simple health check
curl http://localhost:8088/health

# Detailed health with services
curl http://localhost:8088/health/detailed
```

### Prometheus Metrics

```bash
curl http://localhost:8088/actuator/prometheus
```

Key metrics:
- `gateway_jwt_validations_total` - Total JWT validations
- `gateway_jwt_validation_failures_total` - Failed validations
- `gateway_requests_total` - Total requests
- `resilience4j_circuitbreaker_state` - Circuit breaker state

---

## 🛠️ Troubleshooting

### JWT Validation Fails

**Problem**: All protected endpoints return 401  
**Solution**: Check JWT secret matches auth-service

```bash
# Check gateway logs
tail -f logs/api-gateway.log | grep JWT

# Verify secret in application.yml matches auth-service
```

### "Missing Authorization Header"

**Problem**: Token not being sent  
**Solution**: Ensure client sends token in correct format

```javascript
// Correct format
headers: {
  'Authorization': `Bearer ${token}`
}
```

### Backend Services Don't Receive User Context

**Problem**: X-User-Id header missing in backend  
**Solution**: Check JWT filter order and header injection

```bash
# Check logs for filter execution order
grep "JwtAuthenticationFilter" logs/api-gateway.log
```

---

## 📊 Development Guidelines Compliance

This implementation follows all backend development guidelines:

✅ **Authentication via Gateway** - JWT validation centralized in API Gateway  
✅ **Token-Based Auth** - Uses JWT (JSON Web Tokens)  
✅ **Stateless Services** - Backend services don't validate tokens  
✅ **User Context Injection** - Gateway adds user headers  
✅ **Public Endpoints** - Login/register accessible without auth  
✅ **Protected Routes** - All user-facing services secured  
✅ **Health Checks** - Available at `/health` endpoints  
✅ **Docker Ready** - Dockerfile and compose configuration included

---

## 📝 License

Copyright © 2025 Youth Connect Uganda. All rights reserved.

---

## 👥 Support

For issues or questions:
- **Email**: support@youthconnect.ug
- **Slack**: #api-gateway channel
- **Documentation**: https://docs.youthconnect.ug