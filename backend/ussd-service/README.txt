# USSD Service - YouthConnect Uganda

## 📱 Overview

The USSD Service is a critical microservice in the YouthConnect Uganda platform, enabling feature phone users to access entrepreneurship opportunities, training programs, and mentorship services via USSD (*256#).

### Key Features
- ✅ **Registration via USSD** - Complete user onboarding without internet
- ✅ **Opportunity Discovery** - Browse grants, jobs, and training programs
- ✅ **Profile Management** - Update user information via menu
- ✅ **Multi-language Support** - English, Luganda, Alur, Lugbara (planned)
- ✅ **Session Management** - Intelligent state tracking across interactions
- ✅ **Security First** - IP whitelisting, input validation, rate limiting

---

## 🏗️ Architecture

### Technology Stack
- **Framework**: Spring Boot 3.3.4
- **Java Version**: 17
- **Service Discovery**: Eureka Client
- **HTTP Client**: OpenFeign
- **Circuit Breaker**: Resilience4j
- **Caching**: Redis + Caffeine
- **Database**: PostgreSQL (Production), H2 (Development)
- **Monitoring**: Micrometer + Prometheus
- **Testing**: JUnit 5, MockMVC, TestContainers

### Microservices Integration
```
┌─────────────────────────────────────────────────────────────┐
│                     API Gateway (8088)                       │
└──────────────┬──────────────────────────────┬───────────────┘
               │                              │
     ┌─────────▼─────────┐         ┌─────────▼─────────┐
     │  Auth Service     │         │  User Service     │
     │  (8080)           │         │  (8082)           │
     └─────────┬─────────┘         └─────────┬─────────┘
               │                              │
               │         ┌────────────────────┤
               │         │                    │
     ┌─────────▼─────────▼──────┐  ┌─────────▼─────────┐
     │   USSD Service (8004)    │  │ Notification Svc  │
     │   - Session Management   │  │ (8086)            │
     │   - Menu Navigation      │  └───────────────────┘
     │   - User Registration    │
     └──────────────────────────┘
```

---

## 🚀 Getting Started

### Prerequisites
- Java 17+
- Maven 3.8+
- Redis Server (Production)
- PostgreSQL 15+ (Production)

### Local Development Setup

1. **Clone the Repository**
```bash
git clone https://github.com/youthconnect/ussd-service.git
cd ussd-service
```

2. **Configure Environment Variables**
```bash
cp .env.example .env

# Edit .env with your configuration
nano .env
```

3. **Start Dependencies (Docker Compose)**
```bash
docker-compose up -d redis postgres
```

4. **Run the Application**
```bash
mvn spring-boot:run
```

5. **Verify Service Health**
```bash
curl http://localhost:8004/actuator/health
```

---

## 📂 Project Structure

```
ussd-service/
├── src/
│   ├── main/
│   │   ├── java/com/youthconnect/ussd_service/
│   │   │   ├── UssdServiceApplication.java        # Main application
│   │   │   │
│   │   │   ├── client/                            # Feign Clients
│   │   │   │   ├── AuthServiceClient.java         # Auth integration
│   │   │   │   ├── AuthServiceFallback.java       # Circuit breaker
│   │   │   │   ├── GatewayClient.java             # Gateway HTTP client
│   │   │   │   └── NotificationServiceClient.java # SMS/Email notifications
│   │   │   │
│   │   │   ├── config/                            # Configuration
│   │   │   │   ├── FeignConfig.java               # Feign client config
│   │   │   │   ├── MonitoringConfig.java          # Metrics setup
│   │   │   │   └── WebConfig.java                 # CORS & REST config
│   │   │   │
│   │   │   ├── controller/                        # API Controllers
│   │   │   │   └── UssdController.java            # USSD callback endpoint
│   │   │   │
│   │   │   ├── dto/                               # Data Transfer Objects
│   │   │   │   ├── UssdRegistrationRequest.java   # Registration DTO
│   │   │   │   ├── UserProfileDTO.java            # User profile data
│   │   │   │   └── OpportunityDTO.java            # Opportunity data
│   │   │   │
│   │   │   ├── exception/                         # Error Handling
│   │   │   │   ├── UssdSecurityException.java     # Security violations
│   │   │   │   └── GlobalExceptionHandler.java    # Centralized errors
│   │   │   │
│   │   │   ├── model/                             # Domain Models
│   │   │   │   └── UssdSession.java               # Session state
│   │   │   │
│   │   │   ├── repository/                        # Data Access
│   │   │   │   ├── UssdSessionRepository.java     # Session interface
│   │   │   │   └── impl/
│   │   │   │       └── InMemoryUssdSessionRepository.java
│   │   │   │
│   │   │   ├── service/                           # Business Logic
│   │   │   │   ├── UssdMenuService.java           # Menu orchestration
│   │   │   │   └── SessionCleanupScheduler.java   # Session cleanup
│   │   │   │
│   │   │   └── util/                              # Utilities
│   │   │       ├── SecurityUtil.java              # Security helpers
│   │   │       ├── RequestValidator.java          # Input validation
│   │   │       └── PhoneNumberValidator.java      # Phone validation
│   │   │
│   │   └── resources/
│   │       ├── application.yml                    # Dev configuration
│   │       ├── application-prod.yml               # Production config
│   │       └── logback-spring.xml                 # Logging config
│   │
│   └── test/                                      # Test Suite
│       └── java/com/youthconnect/ussd_service/
│           ├── controller/
│           │   └── UssdControllerTest.java
│           ├── service/
│           │   └── UssdMenuServiceTest.java
│           └── integration/
│               └── UssdIntegrationTest.java
│
├── pom.xml                                        # Maven dependencies
├── Dockerfile                                     # Container image
├── docker-compose.yml                             # Local stack
├── .env.example                                   # Environment template
└── README.md                                      # This file
```

---

## 🔐 Security Implementation

### Multi-Layer Security

#### 1. IP Whitelisting
```yaml
ussd:
  security:
    allowed-ips:
      - 196.216.167.0/24  # Africa's Talking IPs
      - 196.216.168.0/24
    enable-ip-whitelist: true
```

**Purpose**: Only accept callbacks from Africa's Talking servers

#### 2. Input Validation
- Phone number format validation (Uganda: `^256[7-9]\d{8}# USSD Service - YouthConnect Uganda

## 📱 Overview

The USSD Service is a critical microservice in the YouthConnect Uganda platform, enabling feature phone users to access entrepreneurship opportunities, training programs, and mentorship services via USSD (*256#).

### Key Features
- ✅ **Registration via USSD** - Complete user onboarding without internet
- ✅ **Opportunity Discovery** - Browse grants, jobs, and training programs
- ✅ **Profile Management** - Update user information via menu
- ✅ **Multi-language Support** - English, Luganda, Alur, Lugbara (planned)
- ✅ **Session Management** - Intelligent state tracking across interactions
- ✅ **Security First** - IP whitelisting, input validation, rate limiting

---

## 🏗️ Architecture

### Technology Stack
- **Framework**: Spring Boot 3.3.4
- **Java Version**: 17
- **Service Discovery**: Eureka Client
- **HTTP Client**: OpenFeign
- **Circuit Breaker**: Resilience4j
- **Caching**: Redis + Caffeine
- **Database**: PostgreSQL (Production), H2 (Development)
- **Monitoring**: Micrometer + Prometheus
- **Testing**: JUnit 5, MockMVC, TestContainers

### Microservices Integration
```
┌─────────────────────────────────────────────────────────────┐
│                     API Gateway (8088)                       │
└──────────────┬──────────────────────────────┬───────────────┘
               │                              │
     ┌─────────▼─────────┐         ┌─────────▼─────────┐
     │  Auth Service     │         │  User Service     │
     │  (8080)           │         │  (8082)           │
     └─────────┬─────────┘         └─────────┬─────────┘
               │                              │
               │         ┌────────────────────┤
               │         │                    │
     ┌─────────▼─────────▼──────┐  ┌─────────▼─────────┐
     │   USSD Service (8004)    │  │ Notification Svc  │
     │   - Session Management   │  │ (8086)            │
     │   - Menu Navigation      │  └───────────────────┘
     │   - User Registration    │
     └──────────────────────────┘
```

---

## 🚀 Getting Started

### Prerequisites
- Java 17+
- Maven 3.8+
- Redis Server (Production)
- PostgreSQL 15+ (Production)

### Local Development Setup

1. **Clone the Repository**
```bash
git clone https://github.com/youthconnect/ussd-service.git
cd ussd-service
```

2. **Configure Environment Variables**
```bash
cp .env.example .env

# Edit .env with your configuration
nano .env
```

3. **Start Dependencies (Docker Compose)**
```bash
docker-compose up -d redis postgres
```

4. **Run the Application**
```bash
mvn spring-boot:run
```

5. **Verify Service Health**
```bash
curl http://localhost:8004/actuator/health
```

---

## 📂 Project Structure

```
ussd-service/
├── src/
│   ├── main/
│   │   ├── java/com/youthconnect/ussd_service/
│   │   │   ├── UssdServiceApplication.java        # Main application
│   │   │   │
│   │   │   ├── client/                            # Feign Clients
)
- Session ID validation (alphanumeric, 8-64 chars)
- Text input sanitization (remove control characters)
- Service code validation (USSD format)

#### 3. Rate Limiting
- 100 requests/minute per IP
- 2000 requests/hour per user
- Sliding window algorithm
- Automatic blocking on threshold breach

#### 4. Request Validation
```java
// Example from SecurityUtil.java
public boolean isValidPhoneNumber(String phoneNumber) {
    if (!StringUtils.hasText(phoneNumber)) return false;
    return PHONE_NUMBER_PATTERN.matcher(phoneNumber).matches();
}
```

---

## 📡 API Endpoints

### USSD Callback Endpoints

#### 1. Africa's Talking Callback (Primary)
```http
POST /api/ussd/at-callback
Content-Type: application/x-www-form-urlencoded

sessionId=ATUid_12345&serviceCode=*256#&phoneNumber=+256700123456&text=1*2
```

**Response Format**:
```
CON Welcome to YouthConnect!
1. Register Now
2. Find Opportunities
3. My Profile
```

#### 2. Test Endpoint (Development)
```http
POST /api/ussd/test
Content-Type: application/json

{
  "sessionId": "test-session-123",
  "phoneNumber": "+256700123456",
  "serviceCode": "*256#",
  "text": "1*2"
}
```

#### 3. Health Check
```http
GET /api/ussd/health

Response:
USSD Service is running
Client IP: 127.0.0.1
IP Allowed: true
Status: Healthy
```

---

## 🔄 USSD Flow Diagrams

### User Registration Flow
```
User Dials *256#
      ↓
  Welcome Screen
      ↓
  1. Register (selected)
      ↓
  Enter Full Name
      ↓
  Select Gender
  (Male/Female/Other)
      ↓
  Select Age Group
  (18-24/25-30/31+)
      ↓
  Select District
  (Madi Okollo/Zombo/Nebbi)
      ↓
  Select Business Stage
  (Idea/Early/Growth/N/A)
      ↓
  Registration Processing
      ↓
  ✅ Success Message
  (Credentials sent via SMS)
```

### Opportunity Discovery Flow
```
Registered User Dials *256#
      ↓
  Main Menu
      ↓
  1. Find Opportunities (selected)
      ↓
  Opportunity Type Menu
  (Grants/Training/Jobs)
      ↓
  2. Training (selected)
      ↓
  Fetch from Opportunity Service
      ↓
  Display Results (paginated)
  1. Digital Marketing - ISBAT
  2. Business Plan - Muni
  3. Financial Literacy - Zombo
      ↓
  User selects option
      ↓
  Show Details + Application Link
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

### Load Testing (with Apache Bench)
```bash
ab -n 1000 -c 10 -p ussd-payload.json \
   -T application/json \
   http://localhost:8004/api/ussd/test
```

### Test Coverage
```bash
mvn clean verify jacoco:report
# Report: target/site/jacoco/index.html
```

---

## 📊 Monitoring & Observability

### Prometheus Metrics
```yaml
# Available at: /actuator/prometheus

# Key Metrics:
- ussd_requests_total{status="success",operator="mtn"}
- ussd_request_duration_seconds{quantile="0.95"}
- ussd_sessions_active_gauge
- ussd_security_events_total{event_type="ip_violation"}
```

### Health Checks
```bash
# Liveness Probe
curl http://localhost:8004/actuator/health/liveness

# Readiness Probe
curl http://localhost:8004/actuator/health/readiness
```

### Logging
```bash
# View logs in real-time
tail -f logs/ussd-service.log

# Search for errors
grep "ERROR" logs/ussd-service.log

# Filter by session
grep "sessionId=12345" logs/ussd-service.log
```

---

## 🚀 Deployment

### Docker Deployment
```bash
# Build image
docker build -t youthconnect/ussd-service:1.0.0 .

# Run container
docker run -d \
  --name ussd-service \
  -p 8004:8004 \
  -e SPRING_PROFILES_ACTIVE=prod \
  -e DB_HOST=postgres \
  -e REDIS_HOST=redis \
  youthconnect/ussd-service:1.0.0
```

### Kubernetes Deployment
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: ussd-service
spec:
  replicas: 3
  selector:
    matchLabels:
      app: ussd-service
  template:
    metadata:
      labels:
        app: ussd-service
    spec:
      containers:
      - name: ussd-service
        image: youthconnect/ussd-service:1.0.0
        ports:
        - containerPort: 8004
        env:
        - name: SPRING_PROFILES_ACTIVE
          value: "prod"
        resources:
          requests:
            memory: "512Mi"
            cpu: "500m"
          limits:
            memory: "1Gi"
            cpu: "1000m"
        livenessProbe:
          httpGet:
            path: /actuator/health/liveness
            port: 8004
          initialDelaySeconds: 30
          periodSeconds: 10
        readinessProbe:
          httpGet:
            path: /actuator/health/readiness
            port: 8004
          initialDelaySeconds: 20
          periodSeconds: 5
```

---

## 🔧 Configuration Reference

### Key Configuration Parameters

| Parameter | Description | Default | Production |
|-----------|-------------|---------|------------|
| `server.port` | HTTP port | 8004 | 8004 |
| `ussd.session.timeout-minutes` | Session expiry | 5 | 5 |
| `ussd.security.enable-ip-whitelist` | IP filtering | false (dev) | true |
| `gateway.base-url` | API Gateway URL | localhost:8088 | api.youthconnect.ug |
| `spring.redis.host` | Redis server | localhost | redis-cluster |

### Environment Variables
```bash
# Database
DB_HOST=localhost
DB_PORT=5432
DB_NAME=youth_connect_db
DB_USERNAME=ussd_user
DB_PASSWORD=secure_password

# Redis
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=redis_password

# External Services
AUTH_SERVICE_URL=http://auth-service:8080
GATEWAY_URL=http://api-gateway:8088/api
SMS_API_KEY=your_africas_talking_api_key
```

---

## 🐛 Troubleshooting

### Common Issues

#### 1. Session Not Found
**Problem**: User gets "Session expired" immediately
**Solution**: Check Redis connection and session timeout settings

#### 2. IP Whitelist Blocking
**Problem**: Requests return 403 Forbidden
**Solution**: Verify Africa's Talking IPs in `allowed-ips` configuration

#### 3. Slow Response Times
**Problem**: USSD responses take >3 seconds
**Solution**:
- Check database query performance
- Verify Redis caching is enabled
- Review circuit breaker status

#### 4. Registration Fails
**Problem**: User registration returns error
**Solution**:
- Check auth-service availability
- Verify user-service database connection
- Review application logs for stack traces

---

## 📞 Support & Contact

### Development Team
- **Email**: douglaskings2@gmail.com
- **Slack**: #ussd-service-dev
- **Issue Tracker**: https://github.com/youthconnect/ussd-service/issues

### On-Call Rotation
- Monitor PagerDuty for production alerts
- Response SLA: 15 minutes for P1 issues
- Escalation: platform-team@youthconnect.ug

---

## 📝 License

Copyright © 2025 YouthConnect Uganda. All rights reserved.

---

## 🙏 Acknowledgments

- **Woord en Daad** - Project sponsor
- **European Union** - YEPPAD funding
- **Africa's Talking** - USSD infrastructure