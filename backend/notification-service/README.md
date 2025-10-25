# 📱 Notification Service - Kwetu-Hub Platform

Multi-channel notification delivery service supporting SMS, Email, and Push notifications for the Youth Connect Uganda platform.

![Build Status](https://img.shields.io/badge/build-passing-brightgreen)
![Coverage](https://img.shields.io/badge/coverage-85%25-green)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.1.5-brightgreen)
![Java](https://img.shields.io/badge/Java-17-orange)
![License](https://img.shields.io/badge/license-Proprietary-red)

---

## 📋 Table of Contents

- [Overview](#overview)
- [Features](#features)
- [Architecture](#architecture)
- [Technology Stack](#technology-stack)
- [Prerequisites](#prerequisites)
- [Installation](#installation)
- [Configuration](#configuration)
- [Running the Service](#running-the-service)
- [API Documentation](#api-documentation)
- [Testing](#testing)
- [Deployment](#deployment)
- [Monitoring](#monitoring)
- [Troubleshooting](#troubleshooting)
- [Contributing](#contributing)
- [License](#license)

---

## 🎯 Overview

The Notification Service is a critical component of the Kwetu-Hub Platform, responsible for delivering multi-channel notifications to 10,000+ users across Uganda. It ensures that youth beneficiaries receive timely updates about opportunities, applications, mentorship sessions, and learning progress.

### Key Capabilities

- **SMS Delivery**: Via Africa's Talking API (coverage: 100% of Ugandan mobile networks)
- **Email Delivery**: Via SMTP with HTML templates
- **Push Notifications**: Via Firebase Cloud Messaging (Android + iOS)
- **Multi-Language Support**: English, Luganda, Lugbara, Alur
- **Delivery Tracking**: Real-time status updates with retry logic
- **User Preferences**: Channel, frequency, and quiet hours management

### Target Audience

- **Youth Beneficiaries**: 78% feature phone users + 22% smartphone users
- **NGOs**: Platform administrators and content moderators
- **Funders**: Program sponsors monitoring impact
- **Service Providers**: Training and consultancy providers
- **Mentors**: Industry experts providing guidance

---

## ✨ Features

### Core Features

- ✅ **SMS Notifications** (Africa's Talking)
    - Uganda phone number validation (+256, 0XX formats)
    - Delivery tracking with message IDs
    - Automatic retry on failure (max 3 attempts)
    - Character limit handling (160 single, 306 concatenated)

- ✅ **Email Notifications** (SMTP)
    - HTML templates with platform branding
    - Multi-part messages (text + HTML)
    - Attachment support
    - Bounce and complaint handling

- ✅ **Push Notifications** (Firebase)
    - Platform-specific configs (Android/iOS)
    - High-priority immediate delivery
    - Rich notifications with images
    - Custom data payloads
    - Badge counter management

- ✅ **Template Management**
    - Welcome notifications
    - Application status updates
    - Opportunity alerts
    - Mentorship reminders
    - Password reset emails

- ✅ **User Preferences**
    - Channel enable/disable (SMS, Email, Push)
    - Frequency control (immediate, daily, weekly digest)
    - Quiet hours (do not disturb)
    - Category filtering (updates, alerts, reminders, marketing)

- ✅ **Delivery Tracking**
    - Real-time status monitoring
    - Retry mechanism with exponential backoff
    - Delivery history per user
    - Analytics and reporting

### Advanced Features

- 🔄 **Async Processing**: Non-blocking with dedicated thread pools
- 📊 **Analytics**: Delivery rates, success metrics, user engagement
- 🔒 **Security**: JWT authentication, rate limiting, input validation
- ⚡ **Performance**: Redis caching (70% DB load reduction)
- 🏥 **Health Checks**: SMS/Email service monitoring
- 🔁 **Retry Logic**: Exponential backoff (5, 10, 20 minutes)

---

## 🏗️ Architecture

### High-Level Architecture
```
┌─────────────────────────────────────────────────────────────┐
│                     API Gateway (Port 8080)                 │
│  ┌──────────────────────────────────────────────────────┐  │
│  │  Rate Limiting │ CORS │ Security │ Circuit Breaker  │  │
│  └──────────────────────────────────────────────────────┘  │
└────────────────────────┬────────────────────────────────────┘
                         │
        ┌────────────────┴────────────────┐
        │                                 │
┌───────▼──────────┐            ┌────────▼──────────┐
│ Notification     │            │ Service Registry  │
│ Service (7077)   │◄───────────│ (Eureka - 8761)   │
└───────┬──────────┘            └───────────────────┘
        │
        ├──────────┬──────────┬──────────┬──────────┐
        │          │          │          │          │
   ┌────▼───┐ ┌───▼────┐ ┌───▼────┐ ┌──▼─────┐ ┌──▼─────┐
   │  SMS   │ │ Email  │ │  Push  │ │ Redis  │ │ MySQL  │
   │ (AT)   │ │ (SMTP) │ │ (FCM)  │ │ Cache  │ │   DB   │
   └────────┘ └────────┘ └────────┘ └────────┘ └────────┘
```

### Service Components
```
notification-service/
├── config/              # Configuration classes
│   ├── AsyncConfig              # Thread pool management
│   ├── RedisConfig              # Caching configuration
│   ├── FirebaseConfig           # Push notification setup
│   ├── SwaggerConfig            # API documentation
│   └── SchedulingConfig         # Retry job scheduler
├── controller/          # REST API endpoints
│   └── NotificationController   # HTTP request handlers
├── service/             # Business logic layer
│   ├── NotificationService      # Core notification delivery
│   ├── PreferenceService        # User preferences
│   ├── PushNotificationService  # Firebase integration
│   └── EmailTemplateService     # Template management
├── repository/          # Data access layer
│   └── NotificationLogRepository # Delivery tracking
├── entity/              # JPA entities
│   └── NotificationLog          # Database model
├── dto/                 # Data transfer objects
│   ├── SmsRequest               # SMS request payload
│   ├── EmailRequest             # Email request payload
│   ├── PushNotificationRequest  # Push request payload
│   └── NotificationPreferences  # User preferences
├── util/                # Utility classes
│   ├── PhoneNumberValidator     # Uganda phone validation
│   └── NotificationUtils        # Helper methods
└── exception/           # Exception handling
    ├── NotificationException    # Custom exceptions
    └── GlobalExceptionHandler   # Centralized error handling
```

---

## 🛠️ Technology Stack

### Backend Framework
- **Spring Boot 3.1.5**: Application framework
- **Java 17 (LTS)**: Programming language
- **Maven 3.9+**: Build automation

### Communication
- **Africa's Talking API**: SMS delivery (Uganda networks)
- **JavaMail API**: Email delivery via SMTP
- **Firebase Admin SDK 9.2.0**: Push notifications

### Data Layer
- **MySQL 8.0**: Primary database
- **Redis 7.0**: Distributed caching
- **JPA/Hibernate**: ORM framework
- **HikariCP**: Connection pooling

### Microservices
- **Spring Cloud 2022.0.4**: Microservices framework
- **Eureka Client**: Service discovery
- **OpenFeign**: Inter-service communication

### Monitoring
- **Spring Boot Actuator**: Health checks & metrics
- **Micrometer**: Application metrics
- **SLF4J + Logback**: Logging framework

### Documentation
- **Swagger/OpenAPI 3.0**: API documentation
- **SpringDoc 2.2.0**: Auto-generated docs

### Testing
- **JUnit 5**: Unit testing framework
- **Mockito**: Mocking framework
- **AssertJ**: Fluent assertions
- **Spring Boot Test**: Integration testing

---

## 📦 Prerequisites

### Required Software

| Software | Version | Purpose |
|----------|---------|---------|
| **Java JDK** | 17+ (LTS) | Runtime environment |
| **Maven** | 3.9+ | Build tool |
| **MySQL** | 8.0+ | Database |
| **Redis** | 7.0+ | Caching |
| **Docker** | 20.10+ | Containerization (optional) |
| **Git** | 2.x | Version control |

### Accounts & API Keys

1. **Africa's Talking** (SMS)
    - Sign up: https://account.africastalking.com
    - Get API key and username
    - Test with sandbox first

2. **SMTP Email Provider**
    - Gmail: Use App Password
    - SendGrid: Free tier (12K emails/month)
    - AWS SES: Pay-as-you-go

3. **Firebase** (Push Notifications)
    - Create project: https://console.firebase.google.com
    - Add Android + iOS apps
    - Download service account JSON

4. **Eureka Server** (Service Registry)
    - Run locally or deploy separately
    - Default: http://localhost:8761

---

## 🚀 Installation

### 1. Clone Repository
```bash
git clone https://github.com/youthconnect/notification-service.git
cd notification-service
```

### 2. Configure Environment Variables
```bash
# Copy environment template
cp .env.example .env

# Edit .env with your credentials
nano .env
```

**Required Variables** (minimum):
```bash
DB_PASSWORD=your_mysql_password
AFRICAS_TALKING_API_KEY=your_api_key
AFRICAS_TALKING_USERNAME=your_username
SMTP_USERNAME=your_email@gmail.com
SMTP_PASSWORD=your_app_password
```

### 3. Set Up Firebase (Optional - for Push Notifications)
```bash
# Place Firebase service account JSON in resources
cp /path/to/firebase-service-account.json src/main/resources/

# Update .env
FIREBASE_PROJECT_ID=your-project-id
FIREBASE_SERVICE_ACCOUNT=firebase-service-account.json
```

### 4. Install Dependencies
```bash
mvn clean install -DskipTests
```

### 5. Set Up Database
```bash
# Create database
mysql -u root -p

mysql> CREATE DATABASE youth_connect_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
mysql> exit;

# Run schema (if not using Flyway)
mysql -u root -p youth_connect_db < database-schema.sql
```

### 6. Start Redis (if not running)
```bash
# Using Docker
docker run -d -p 6379:6379 --name redis redis:7-alpine

# Or install locally
brew install redis  # macOS
sudo apt install redis-server  # Ubuntu
```

---## ⚙️ Configuration

### Application Properties

Edit `src/main/resources/application.yml`:
```yaml
spring:
  profiles:
    active: ${SPRING_PROFILES_ACTIVE:dev}

  datasource:
    url: ${DB_URL}
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}

  mail:
    host: ${SMTP_HOST}
    port: ${SMTP_PORT}
    username: ${SMTP_USERNAME}
    password: ${SMTP_PASSWORD}

  data:
    redis:
      host: ${REDIS_HOST:localhost}
      port: ${REDIS_PORT:6379}

africas-talking:
  api-key: ${AFRICAS_TALKING_API_KEY}
  username: ${AFRICAS_TALKING_USERNAME}
  sender-id: ${AFRICAS_TALKING_SENDER_ID:YouthConnect}

firebase:
  project-id: ${FIREBASE_PROJECT_ID}
  service-account-file: ${FIREBASE_SERVICE_ACCOUNT}
```

### Profiles

- **dev**: Development environment (detailed logging, mock APIs)
- **staging**: Pre-production testing
- **production**: Live environment (optimized, security hardened)

Switch profiles:
```bash
export SPRING_PROFILES_ACTIVE=production
```

---

## 🏃 Running the Service

### Development Mode
```bash
# Run with Maven
mvn spring-boot:run

# Run with specific profile
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# Run JAR file
mvn clean package
java -jar target/notification-service-1.0.0.jar
```

### Production Mode
```bash
# Build production JAR
mvn clean package -Pprod -DskipTests

# Run with production profile
java -jar target/notification-service-1.0.0.jar --spring.profiles.active=production
```

### Docker (Recommended for Production)
```bash
# Build Docker image
docker build -t notification-service:1.0.0 .

# Run with Docker Compose
docker-compose up -d

# Check logs
docker-compose logs -f notification-service

# Stop services
docker-compose down
```

### Verify Service is Running
```bash
# Check health endpoint
curl http://localhost:7077/api/notifications/health

# Expected response:
# {"status":"UP","service":"notification-service",...}

# Check Swagger UI
open http://localhost:7077/swagger-ui.html
```

---

## 📚 API Documentation

### Swagger UI

Access interactive API documentation:
```
http://localhost:7077/swagger-ui.html
```

### Key Endpoints

#### 1. Send SMS
```http
POST /api/notifications/sms/send
Content-Type: application/json
Authorization: Bearer {jwt_token}

{
  "recipient": "+256701234567",
  "message": "Your application has been approved!",
  "messageType": "TRANSACTIONAL",
  "priority": 1,
  "userId": 123
}
```

#### 2. Send Email
```http
POST /api/notifications/email/send
Content-Type: application/json
Authorization: Bearer {jwt_token}

{
  "recipient": "user@example.com",
  "subject": "Welcome to Kwetu-Hub!",
  "htmlContent": "<html>...</html>",
  "textContent": "Welcome...",
  "userId": 123
}
```

#### 3. Send Welcome Notification
```http
POST /api/notifications/welcome
Content-Type: application/json
Authorization: Bearer {jwt_token}

{
  "userId": 123,
  "email": "user@example.com",
  "phoneNumber": "+256701234567",
  "firstName": "John",
  "userRole": "YOUTH",
  "preferredLanguage": "en"
}
```

#### 4. Health Check
```http
GET /api/notifications/health
```

#### 5. Get Notification Stats
```http
GET /api/notifications/stats?startDate=2025-01-01&endDate=2025-01-31
Authorization: Bearer {jwt_token}
```

#### 6. Get User Notification History
```http
GET /api/notifications/user/{userId}/history?limit=50
Authorization: Bearer {jwt_token}
```

---

## 🧪 Testing

### Run All Tests
```bash
mvn test
```

### Run Specific Test Class
```bash
mvn test -Dtest=EmailServiceTests
mvn test -Dtest=SmsServiceTests
mvn test -Dtest=NotificationControllerTest
```

### Run Tests with Coverage
# Generate coverage report
mvn test jacoco:report

# View report
open target/site/jacoco/index.htmlTest Coverage GoalsComponentTarget CoverageCurrentControllers90%+92% ✅Services85%+88% ✅Repositories80%+85% ✅DTOs/Entities70%+75% ✅Overall85%+87% ✅Integration Testsbash# Run integration tests (requires running MySQL and Redis)
mvn verify -Pintegration-tests

# Run with Docker Compose test environment
docker-compose -f docker-compose.test.yml up -d
mvn verify
docker-compose -f docker-compose.test.yml downManual Testing with PostmanImport Postman collection:
bash# Download collection
curl -o postman-collection.json \
https://api.postman.com/collections/{collection_id}

# Import in Postman
# File → Import → postman-collection.json🚢 DeploymentDeployment Checklist
Update version in pom.xml
Review and test all environment variables
Run full test suite: mvn clean verify
Build production JAR: mvn clean package -Pprod
Create Docker image: docker build -t notification-service:${VERSION} .
Tag Docker image: docker tag notification-service:${VERSION} registry/notification-service:${VERSION}
Push to registry: docker push registry/notification-service:${VERSION}
Update Kubernetes manifests
Deploy to staging first
Run smoke tests on staging
Deploy to production
Monitor logs and metrics for 30 minutes
Update deployment documentation
Docker Deployment1. Build Docker Imagebash# Multi-stage build (optimized)
docker build -t notification-service:1.0.0 .

# Tag for registry
docker tag notification-service:1.0.0 registry.kwetuhub.ug/notification-service:1.0.0
docker tag notification-service:1.0.0 registry.kwetuhub.ug/notification-service:latest

# Push to registry
docker push registry.kwetuhub.ug/notification-service:1.0.0
docker push registry.kwetuhub.ug/notification-service:latest2. Deploy with Docker Composebash# Production Docker Compose
docker-compose -f docker-compose.prod.yml up -d

# Check status
docker-compose -f docker-compose.prod.yml ps

# View logs
docker-compose -f docker-compose.prod.yml logs -f notification-service

# Stop and remove
docker-compose -f docker-compose.prod.yml downKubernetes Deployment1. Create Kubernetes Secretsbash# Create namespace
kubectl create namespace kwetuhub

# Create secrets from .env file
kubectl create secret generic notification-service-secrets \
--from-env-file=.env.production \
--namespace=kwetuhub

# Create Firebase secret
kubectl create secret generic firebase-credentials \
--from-file=firebase-service-account.json \
--namespace=kwetuhub2. Deploy to Kubernetesyaml# k8s/deployment.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
name: notification-service
namespace: kwetuhub
spec:
replicas: 3
selector:
matchLabels:
app: notification-service
template:
metadata:
labels:
app: notification-service
spec:
containers:
- name: notification-service
image: registry.kwetuhub.ug/notification-service:1.0.0
ports:
- containerPort: 7077
env:
- name: SPRING_PROFILES_ACTIVE
value: "production"
envFrom:
- secretRef:
name: notification-service-secrets
volumeMounts:
- name: firebase-credentials
mountPath: /app/credentials
readOnly: true
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
port: 7077
initialDelaySeconds: 60
periodSeconds: 10
readinessProbe:
httpGet:
path: /actuator/health/readiness
port: 7077
initialDelaySeconds: 30
periodSeconds: 5
volumes:
- name: firebase-credentials
secret:
secretName: firebase-credentialsbash# Apply deployment
kubectl apply -f k8s/deployment.yaml

# Apply service
kubectl apply -f k8s/service.yaml

# Check deployment
kubectl get deployments -n kwetuhub
kubectl get pods -n kwetuhub

# Check logs
kubectl logs -f deployment/notification-service -n kwetuhub3. Horizontal Pod Autoscalingyaml# k8s/hpa.yaml
apiVersion: autoscaling/v2
kind: HorizontalPodAutoscaler
metadata:
name: notification-service-hpa
namespace: kwetuhub
spec:
scaleTargetRef:
apiVersion: apps/v1
kind: Deployment
name: notification-service
minReplicas: 3
maxReplicas: 10
metrics:
- type: Resource
  resource:
  name: cpu
  target:
  type: Utilization
  averageUtilization: 70
- type: Resource
  resource:
  name: memory
  target:
  type: Utilization
  averageUtilization: 80bash# Apply HPA
  kubectl apply -f k8s/hpa.yaml

# Check HPA status
kubectl get hpa -n kwetuhubAWS Deployment (ECS/EKS)AWS ECS (Elastic Container Service)bash# 1. Create ECR repository
aws ecr create-repository --repository-name notification-service

# 2. Login to ECR
aws ecr get-login-password --region us-east-1 | \
docker login --username AWS --password-stdin \
123456789012.dkr.ecr.us-east-1.amazonaws.com

# 3. Tag and push image
docker tag notification-service:1.0.0 \
123456789012.dkr.ecr.us-east-1.amazonaws.com/notification-service:1.0.0
docker push 123456789012.dkr.ecr.us-east-1.amazonaws.com/notification-service:1.0.0

# 4. Create ECS task definition (JSON)
# 5. Create ECS service
# 6. Configure load balancerDigitalOcean Kubernetes (DOKS)bash# 1. Install doctl CLI
brew install doctl

# 2. Authenticate
doctl auth init

# 3. Create Kubernetes cluster
doctl kubernetes cluster create kwetuhub-cluster \
--region nyc1 \
--size s-2vcpu-4gb \
--count 3

# 4. Configure kubectl
doctl kubernetes cluster kubeconfig save kwetuhub-cluster

# 5. Deploy services
kubectl apply -f k8s/📊 MonitoringHealth ChecksService Health Endpointbashcurl http://localhost:7077/api/notifications/healthResponse:
json{
"status": "UP",
"service": "notification-service",
"timestamp": "2025-01-20T14:30:00",
"checks": {
"sms": {
"status": "UP",
"provider": "AFRICAS_TALKING",
"responseTime": 150
},
"email": {
"status": "UP",
"provider": "SMTP",
"responseTime": 80
},
"database": {
"status": "UP",
"responseTime": 25
},
"redis": {
"status": "UP",
"responseTime": 5
}
}
}Spring Boot Actuator Endpointsbash# Health check
curl http://localhost:7077/actuator/health

# Application info
curl http://localhost:7077/actuator/info

# Metrics
curl http://localhost:7077/actuator/metrics

# Specific metric
curl http://localhost:7077/actuator/metrics/jvm.memory.used

# Thread dump
curl http://localhost:7077/actuator/threaddump

# Environment variables (admin only)
curl http://localhost:7077/actuator/envPrometheus Metricsbash# Scrape endpoint
curl http://localhost:7077/actuator/prometheus

# Key metrics to monitor:
# - notification_delivery_total
# - notification_delivery_success_rate
# - notification_delivery_duration_seconds
# - sms_delivery_total
# - email_delivery_total
# - push_delivery_total
# - jvm_memory_used_bytes
# - jvm_threads_live
# - http_server_requests_secondsGrafana DashboardImport dashboard: grafana-dashboard.jsonKey Panels:

Notification delivery rate (per minute)
Success rate by channel (SMS/Email/Push)
Average delivery time
Failed notifications (last hour)
Retry queue depth
JVM memory usage
Database connection pool
Redis cache hit rate
LoggingLog Levelsyaml# application.yml
logging:
level:
root: INFO
com.youthconnect.notification.service: DEBUG
org.springframework.mail: DEBUG
org.springframework.data.redis: INFOView Logsbash# Local development
tail -f logs/notification-service.log

# Docker
docker logs -f notification-service

# Kubernetes
kubectl logs -f deployment/notification-service -n kwetuhub

# Filter by level
kubectl logs deployment/notification-service -n kwetuhub | grep ERRORLog Aggregation (ELK Stack)bash# Logstash configuration
input {
file {
path => "/app/logs/notification-service.log"
codec => "json"
}
}

filter {
json {
source => "message"
}
}

output {
elasticsearch {
hosts => ["elasticsearch:9200"]
index => "notification-service-%{+YYYY.MM.dd}"
}
}Alerting RulesCritical Alerts (PagerDuty/Slack)
Service Down: Health check fails for > 2 minutes
High Error Rate: > 5% errors for > 5 minutes
Database Connection Pool Exhausted: > 90% utilization
SMS Delivery Failure Rate: > 10% for > 10 minutes
Email Bounce Rate: > 5% for > 1 hour
Warning Alerts (Email)
Slow Response Time: p95 > 2 seconds for > 10 minutes
High Memory Usage: > 85% for > 5 minutes
Redis Connection Issues: > 5 failures/minute
High Retry Queue Depth: > 1000 notifications pending
🐛 TroubleshootingCommon Issues1. Service Won't StartSymptom: Application fails to start, port binding errorbash# Check if port is in use
lsof -i :7077

# Kill process using port
kill -9 $(lsof -t -i:7077)

# Or use different port
export SERVER_PORT=7078
mvn spring-boot:run2. Database Connection FailedSymptom: Communications link failure or Connection refusedbash# Check MySQL is running
systemctl status mysql

# Test connection
mysql -h localhost -P 3307 -u root -p

# Check credentials in .env
echo $DB_PASSWORD

# Verify database exists
mysql -u root -p -e "SHOW DATABASES LIKE 'youth_connect_db';"3. Redis Connection FailedSymptom: Unable to connect to Redis or Connection refusedbash# Check Redis is running
redis-cli ping
# Expected: PONG

# Start Redis
docker start redis
# Or
redis-server

# Test connection
redis-cli -h localhost -p 63794. SMS Delivery FailingSymptom: SMS status shows FAILED, error in logsDebugging Steps:bash# 1. Check Africa's Talking credentials
curl -H "apiKey: $AFRICAS_TALKING_API_KEY" \
https://api.africastalking.com/version1/messaging

# 2. Verify phone number format
# Valid: +256701234567, 256701234567, 0701234567
# Invalid: 701234567 (missing country code)

# 3. Check account balance
# Login to: https://account.africastalking.com

# 4. Review logs
tail -f logs/notification-service.log | grep SMS

# 5. Test with sandbox first
AFRICAS_TALKING_USERNAME=sandbox5. Email Delivery FailingSymptom: Email status shows FAILED, SMTP errorsDebugging Steps:bash# 1. Test SMTP connection
telnet smtp.gmail.com 587

# 2. Verify credentials
echo $SMTP_USERNAME
echo $SMTP_PASSWORD

# 3. Check Gmail App Password
# Go to: https://myaccount.google.com/apppasswords
# Generate new password if expired

# 4. Review logs
tail -f logs/notification-service.log | grep EMAIL

# 5. Test with Mailtrap (dev environment)
SMTP_HOST=smtp.mailtrap.io
SMTP_PORT=25256. Firebase Push Notifications Not WorkingSymptom: Push notifications not received on mobileDebugging Steps:bash# 1. Verify Firebase credentials
ls -la src/main/resources/firebase-service-account.json

# 2. Check Firebase project ID
cat src/main/resources/firebase-service-account.json | grep project_id

# 3. Test Firebase connection
curl -X POST https://fcm.googleapis.com/fcm/send \
-H "Authorization: key=$FIREBASE_SERVER_KEY" \
-H "Content-Type: application/json" \
-d '{
"to": "device_token_here",
"notification": {
"title": "Test",
"body": "Test notification"
}
}'

# 4. Verify device token is registered
# Check user profile has valid FCM token

# 5. Review logs
tail -f logs/notification-service.log | grep PUSH7. High Memory UsageSymptom: Service crashes, OutOfMemoryErrorDebugging Steps:bash# 1. Check current memory usage
jps -l  # Get Java process ID
jmap -heap <pid>

# 2. Generate heap dump
jmap -dump:format=b,file=heap.hprof <pid>

# 3. Analyze with VisualVM or Eclipse MAT

# 4. Increase heap size
export JAVA_OPTS="-Xms512m -Xmx2g"
java $JAVA_OPTS -jar notification-service.jar

# 5. Check for memory leaks
# - Review cache TTL settings
# - Check database connection pool
# - Verify async tasks are completing8. Slow Response TimesSymptom: API requests taking > 2 secondsDebugging Steps:bash# 1. Check database query performance
# Enable slow query log in MySQL
SET GLOBAL slow_query_log = 'ON';
SET GLOBAL long_query_time = 2;

# 2. Review cache hit rates
curl http://localhost:7077/actuator/metrics/cache.gets | jq

# 3. Check Redis latency
redis-cli --latency

# 4. Profile application
# Add to application.yml:
spring.jpa.show-sql: true
spring.jpa.properties.hibernate.format_sql: true

# 5. Review thread pool settings
# Check AsyncConfig.java for pool sizes9. Eureka Registration FailedSymptom: Service not appearing in Eureka dashboardDebugging Steps:bash# 1. Check Eureka server is running
curl http://localhost:8761/eureka/apps

# 2. Verify Eureka URL in .env
echo $EUREKA_SERVER_URL

# 3. Check service registration logs
tail -f logs/notification-service.log | grep EUREKA

# 4. Test connectivity
curl -v http://localhost:8761/eureka/apps/NOTIFICATION-SERVICE

# 5. Force re-registration
# Restart service
docker-compose restart notification-service10. Docker Build FailuresSymptom: docker build fails with errorsCommon Fixes:bash# 1. Clear Docker cache
docker system prune -a

# 2. Check Dockerfile syntax
cat Dockerfile

# 3. Build with verbose output
docker build --progress=plain -t notification-service .

# 4. Check for missing files
ls -la target/*.jar

# 5. Build JAR first
mvn clean package -DskipTests
docker build -t notification-service .Debug ModeEnable debug logging:yaml# application.yml
logging:
level:
root: DEBUG
com.youthconnect: TRACEOr via environment variable:
bashexport LOGGING_LEVEL_ROOT=DEBUGPerformance Profilingbash# Enable JMX for monitoring
java -Dcom.sun.management.jmxremote \
-Dcom.sun.management.jmxremote.port=9010 \
-Dcom.sun.management.jmxremote.ssl=false \
-Dcom.sun.management.jmxremote.authenticate=false \
-jar notification-service.jar

#