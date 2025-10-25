# Youth Connect Uganda - API Gateway

## Overview

The API Gateway is the single entry point for all client requests in the Youth Connect Uganda platform. It provides intelligent routing, security, and monitoring capabilities.

## Features

✅ **Intelligent Routing** - Routes requests to appropriate microservices based on URL patterns  
✅ **Rate Limiting** - Prevents API abuse with token bucket algorithm (100 req/min)  
✅ **Circuit Breaker** - Resilience4j circuit breaker for fault tolerance  
✅ **CORS Configuration** - Supports web and mobile clients  
✅ **Security Headers** - Automatic injection of security headers  
✅ **Request Logging** - Comprehensive request/response logging with request IDs  
✅ **Load Balancing** - Automatic load balancing across service instances  
✅ **Service Discovery** - Integration with Eureka for dynamic service discovery  
✅ **Health Checks** - Multiple health check endpoints for monitoring  
✅ **Global Exception Handling** - Standardized error responses

---

## Architecture

```
[Client] → [API Gateway:8080] → [Eureka:8761] → [Backend Services]
```

### Request Flow
1. Client sends request to API Gateway (port 8080)
2. Rate limiting filter checks request limits
3. Request logging filter logs the request
4. Gateway routes to appropriate backend service via Eureka
5. Circuit breaker protects against service failures
6. Security headers added to response
7. Response returned to client

---

## Project Structure

```
api-gateway/
├── src/
│   ├── main/
│   │   ├── java/com/youthconnect/api_gateway/
│   │   │   ├── ApiGatewayApplication.java       # Main application
│   │   │   ├── config/
│   │   │   │   ├── CorsConfig.java              # CORS configuration
│   │   │   │   ├── RateLimitConfig.java         # Rate limiting config
│   │   │   │   └── GatewayConfig.java           # Additional beans
│   │   │   ├── controller/
│   │   │   │   ├── FallbackController.java      # Circuit breaker fallbacks
│   │   │   │   └── HealthCheckController.java   # Health endpoints
│   │   │   ├── exception/
│   │   │   │   └── GlobalExceptionHandler.java  # Error handling
│   │   │   └── filter/
│   │   │       ├── RateLimitGatewayFilter.java  # Rate limiting filter
│   │   │       ├── SecurityHeadersFilter.java   # Security headers
│   │   │       └── RequestLoggingFilter.java    # Request logging
│   │   └── resources/
│   │       └── application.yml                  # Configuration
│   └── test/
│       └── java/com/youthconnect/api_gateway/
│           └── ApiGatewayApplicationTests.java
├── pom.xml                                      # Maven dependencies
└── README.md                                    # This file
```

---

## Configuration

### application.yml

The gateway is configured via `application.yml`. Key sections:

#### Service Routes
```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: user-service
          uri: lb://user-service
          predicates:
            - Path=/api/users/**
```

#### Rate Limiting
```yaml
app:
  security:
    rate-limit:
      enabled: true
      requests-per-minute: 100
      auth-requests-per-minute: 20
```

#### CORS
```yaml
spring:
  cloud:
    gateway:
      globalcors:
        cors-configurations:
          '[/**]':
            allowedOrigins: ["http://localhost:3000"]
```

---

## Endpoints

### Backend Service Routes

| Service | Path Pattern | Backend Service |
|---------|-------------|-----------------|
| Auth | `/api/auth/**` | auth-service |
| Users | `/api/users/**` | user-service |
| Opportunities | `/api/opportunities/**` | opportunity-service |
| Applications | `/api/applications/**` | opportunity-service |
| Learning | `/api/learning/**` | content-service |
| Posts | `/api/posts/**`, `/api/feed/**` | content-service |
| Mentorship | `/api/mentorship/**` | mentor-service |
| USSD | `/api/ussd/**` | ussd-service |
| Recommendations | `/api/recommendations/**` | ai-recommendation-service |
| Analytics | `/api/analytics/**` | analytics-service |
| Notifications | `/api/notifications/**` | notification-service |
| Files | `/api/files/**` | file-management-service |

### Health Check Endpoints

| Endpoint | Purpose |
|----------|---------|
| `/health` | Simple health check (200 OK if running) |
| `/health/detailed` | Detailed health with registered services |
| `/health/ready` | Kubernetes readiness probe |
| `/health/live` | Kubernetes liveness probe |

### Actuator Endpoints

| Endpoint | Purpose |
|----------|---------|
| `/actuator/health` | Spring Boot health check |
| `/actuator/prometheus` | Prometheus metrics |
| `/actuator/metrics` | Application metrics |
| `/actuator/gateway` | Gateway-specific metrics |

---

## Rate Limiting

The gateway implements token bucket rate limiting:

### Rate Limits

| Endpoint Type | Requests/Minute | Use Case |
|--------------|-----------------|----------|
| **Auth Endpoints** | 20 | Login, register, password reset |
| **USSD Endpoints** | 50 | USSD session management |
| **General Endpoints** | 100 | All other API endpoints |

### Rate Limit Headers

Responses include rate limit information:

```
X-Rate-Limit-Remaining: 95
X-Rate-Limit-Retry-After-Seconds: 45
```

### Rate Limit Exceeded Response

```json
{
  "timestamp": "2025-01-20T10:30:00",
  "status": 429,
  "error": "Too Many Requests",
  "message": "Rate limit exceeded",
  "path": "/api/auth/login"
}
```

---

## Circuit Breaker

Resilience4j circuit breaker protects against cascading failures.

### Circuit Breaker States

1. **CLOSED** - Normal operation, requests flow through
2. **OPEN** - Too many failures, requests fail fast (fallback)
3. **HALF_OPEN** - Testing if service recovered

### Configuration

- **Failure Threshold**: 50% failure rate
- **Minimum Calls**: 5 calls before evaluation
- **Wait Duration**: 10 seconds in open state
- **Slow Call Threshold**: 3 seconds

### Fallback Endpoints

When circuit breaker opens, requests route to fallback endpoints:

- `/fallback/auth` - Auth service unavailable
- `/fallback/users` - User service unavailable
- `/fallback/opportunities` - Opportunity service unavailable
- `/fallback/mentorship` - Mentorship service unavailable

---

## Security

### Security Headers

All responses include security headers:

```
X-Content-Type-Options: nosniff
X-Frame-Options: DENY
X-XSS-Protection: 1; mode=block
Content-Security-Policy: default-src 'self'
```

### CORS

CORS is configured for:
- Web app: `http://localhost:3000`
- Mobile app: `http://localhost:3001`
- Production: `https://youthconnect.ug`

---

## Running the Gateway

### Prerequisites

1. **Java 17** installed
2. **Maven 3.8+** installed
3. **Service Registry** running on port 8761

### Start the Gateway

```bash
# Navigate to api-gateway directory
cd api-gateway

# Clean and install dependencies
mvn clean install

# Run the application
mvn spring-boot:run
```

The gateway will start on **port 8080**.

### Verify Startup

1. Check console for startup message
2. Visit health endpoint: `http://localhost:8080/health`
3. Check Eureka dashboard: `http://localhost:8761`

Expected output:
```
🚀 API Gateway Started Successfully!
🌐 Local URL: http://localhost:8080
📊 Eureka Server: http://localhost:8761/eureka
```

---

## Testing

### Test Rate Limiting

```bash
# Send 25 rapid requests (should trigger rate limit)
for i in {1..25}; do
  curl -w "\nStatus: %{http_code}\n" http://localhost:8080/api/auth/login
done
```

Expected: First 20 succeed, rest return `429 Too Many Requests`

### Test CORS

```bash
curl -H "Origin: http://localhost:3000" \
     -H "Access-Control-Request-Method: POST" \
     -X OPTIONS \
     http://localhost:8080/api/users
```

Expected: `Access-Control-Allow-Origin: http://localhost:3000` header

### Test Circuit Breaker

1. Stop user-service
2. Make request: `curl http://localhost:8080/api/users`
3. After 5 failures, circuit breaker opens
4. Subsequent requests immediately return fallback response

---

## Monitoring

### Prometheus Metrics

Access metrics at: `http://localhost:8080/actuator/prometheus`

Key metrics:
- `gateway_requests_total` - Total requests processed
- `gateway_requests_seconds` - Request duration
- `resilience4j_circuitbreaker_state` - Circuit breaker state

### Request Logging

All requests logged with:
```
>>> Incoming Request | ID: abc-123 | Method: POST | Path: /api/users | IP: 127.0.0.1
<<< Outgoing Response | ID: abc-123 | Status: 200 | Time: 145ms
```

---

## Troubleshooting

### Gateway won't start

**Problem**: Port 8080 already in use  
**Solution**: Change port in `application.yml` or kill process on 8080

```bash
# Find process on port 8080
lsof -i :8080

# Kill process
kill -9 <PID>
```

### Services not registering

**Problem**: Eureka server not running  
**Solution**: Start service-registry first

```bash
cd service-registry
mvn spring-boot:run
```

### Rate limiting not working

**Problem**: `bucket4j` dependency not found  
**Solution**: Check pom.xml has correct version (8.1.0, NOT 8.10.1)

### CORS errors

**Problem**: Frontend origin not allowed  
**Solution**: Add origin to `application.yml`:

```yaml
spring:
  cloud:
    gateway:
      globalcors:
        cors-configurations:
          '[/**]':
            allowedOrigins:
              - "http://your-frontend-url:port"
```

---

## Production Deployment

### Environment Variables

Override configuration with environment variables:

```bash
SERVER_PORT=80
EUREKA_CLIENT_SERVICE_URL_DEFAULTZONE=http://eureka:8761/eureka
APP_SECURITY_RATE_LIMIT_REQUESTS_PER_MINUTE=1000
```

### Docker Deployment

```dockerfile
FROM openjdk:17-jdk-slim
COPY target/api-gateway-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

### Kubernetes Deployment

```yaml
apiVersion: v1
kind: Service
metadata:
  name: api-gateway
spec:
  type: LoadBalancer
  ports:
    - port: 80
      targetPort: 8080
  selector:
    app: api-gateway
```

---

## Performance Optimization

### Recommended Settings for Production

```yaml
# Increase thread pool
server:
  tomcat:
    threads:
      max: 200
      min-spare: 10

# Increase connection timeout
spring:
  cloud:
    gateway:
      httpclient:
        connect-timeout: 5000
        response-timeout: 30s
```

---

## Contributing

When modifying the API Gateway:

1. Update route configurations in `application.yml`
2. Add/modify filters in `filter/` package
3. Update this README with changes
4. Test all routes after changes
5. Update integration tests

---

## Support

For issues or questions:
- **Email**: support@youthconnect.ug
- **Slack**: #api-gateway channel
- **Documentation**: https://docs.youthconnect.ug

---

## License

Copyright © 2025 Youth Connect Uganda. All rights reserved.