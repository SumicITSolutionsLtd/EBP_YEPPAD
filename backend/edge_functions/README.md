# Edge Functions Service

**Version:** 1.0.0  
**Status:** ✅ Production Ready  
**Author:** Douglas Kings Kato  
**Project:** Youth Entrepreneurship Booster Platform

---

## 🎯 Purpose

Edge Functions Service is a critical orchestration layer that:
- **Bridges multiple microservices** for complex workflows
- **Handles USSD integration** for feature phone accessibility (*256#)
- **Provides AI-powered features** via OpenAI GPT-4
- **Coordinates multi-service operations** with resilience patterns

---

## 🏗️ Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                  Edge Functions Service                      │
├─────────────────────────────────────────────────────────────┤
│  • USSD Integration (*256# - Feature Phone Access)          │
│  • OpenAI Integration (GPT-4, TTS, Whisper)                 │
│  • Multi-Service Orchestration (Saga Pattern)               │
│  • Circuit Breaker & Retry Patterns                         │
│  • Async Operations with CompletableFuture                  │
└─────────────────────────────────────────────────────────────┘
         │                │                │
         ▼                ▼                ▼
┌──────────────┐  ┌──────────────┐  ┌──────────────┐
│ User Service │  │ Opportunity  │  │ Notification │
│   (8081)     │  │   Service    │  │   Service    │
│              │  │   (8083)     │  │   (8086)     │
└──────────────┘  └──────────────┘  └──────────────┘
```

---

## 📋 Features

### 🤖 AI Services (OpenAI Integration)
- **Chat Completions**: GPT-4 powered conversational AI
- **Text-to-Speech**: Generate audio for learning modules
- **Speech-to-Text**: Transcribe voice questions

### 📱 USSD Integration
- **Feature Phone Access**: No smartphone required
- **Multi-Language Support**: English, Luganda, Lugbara, Alur
- **Menu Navigation**: Hierarchical USSD menus
- **Opportunity Browsing**: View and apply via *256#

### 🔄 Multi-Service Orchestration
- **User Registration Workflow**: Coordinate user creation, notifications, AI setup
- **Application Workflow**: Handle opportunity applications end-to-end
- **Saga Pattern**: Distributed transaction management
- **Compensation Logic**: Rollback on failures

### 🛡️ Resilience Patterns
- **Circuit Breaker**: Prevent cascading failures
- **Retry Logic**: Exponential backoff for transient failures
- **Rate Limiting**: Token bucket algorithm
- **Timeout Management**: Configurable timeouts per operation

---

## 🚀 Quick Start

### Prerequisites
- Java 17 or higher
- Maven 3.9+
- Docker & Docker Compose
- OpenAI API key (optional)

### 1. Environment Setup

Create `.env` file from template:
```bash
cp .env.template .env
# Edit .env with your credentials
```

### 2. Start Infrastructure

```bash
# Start required services (Eureka, MySQL, Redis)
docker-compose up -d service-registry mysql redis

# Wait for services to be healthy (30-60 seconds)
docker-compose ps
```

### 3. Build the Service

```bash
# Build with Maven
mvn clean install -DskipTests

# Or build Docker image
docker build -t youthconnect/edge-functions:latest .
```

### 4. Run the Service

**Option A: Local (Maven)**
```bash
mvn spring-boot:run
```

**Option B: Docker**
```bash
docker-compose up -d edge-functions
```

**Option C: Complete Stack**
```bash
# Start all services
docker-compose up -d

# View logs
docker-compose logs -f edge-functions
```

### 5. Verify Service

```bash
# Health check
curl http://localhost:8091/actuator/health

# Swagger UI
open http://localhost:8091/swagger-ui.html

# Eureka Dashboard
open http://localhost:8761
```

---

## 📡 API Endpoints

### AI Services

#### Chat with AI
```http
POST /api/edge/chat
Content-Type: application/json

{
  "message": "How do I start a business in Uganda?",
  "systemPrompt": "You are a business advisor",
  "conversationHistory": []
}
```

**Response:**
```json
{
  "response": "Starting a business in Uganda involves...",
  "usage": {
    "prompt_tokens": 25,
    "completion_tokens": 150,
    "total_tokens": 175
  }
}
```

#### Text-to-Speech
```http
POST /api/edge/text-to-speech
Content-Type: application/json

{
  "text": "Welcome to the platform",
  "voice": "alloy"
}
```

#### Speech-to-Text
```http
POST /api/edge/voice-to-text
Content-Type: application/json

{
  "audio": "base64-encoded-audio-data"
}
```

### USSD Integration

#### USSD Callback (Africa's Talking)
```http
POST /api/ussd/callback
Content-Type: application/x-www-form-urlencoded

sessionId=ATUid_xxxx&serviceCode=*256#&phoneNumber=+256701234567&text=1*2
```

**Response:**
```
CON Select opportunity:
1. Youth Grant - UGX 5M
2. Agric Loan - UGX 3M
3. Tech Training
0. Back
```

---

## 🔧 Configuration

### Key Configuration Files

**application.yml** - Main configuration
```yaml
server:
  port: 8091

openai:
  api:
    key: ${OPENAI_API_KEY}
    default-chat-model: gpt-4o-mini

resilience4j:
  circuitbreaker:
    instances:
      default:
        sliding-window-size: 10
        failure-rate-threshold: 50
```

**application-prod.yml** - Production overrides
```yaml
logging:
  level:
    root: WARN
    com.youthconnect.edge_functions: INFO
```

---

## 🧪 Testing

### Run Tests
```bash
# Unit tests
mvn test

# Integration tests
mvn verify

# With coverage
mvn clean test jacoco:report
```

### Test USSD Flow
```bash
# Initial dial
curl -X POST "http://localhost:8091/api/ussd/test?sessionId=test1&phoneNumber=256701234567&text="

# Select opportunities
curl -X POST "http://localhost:8091/api/ussd/test?sessionId=test1&phoneNumber=256701234567&text=1"
```

### Test AI Services
```bash
# Chat with AI
curl -X POST http://localhost:8091/api/edge/chat \
  -H "Content-Type: application/json" \
  -d '{
    "message": "What are business opportunities in Uganda?",
    "systemPrompt": "You are a business advisor"
  }'
```

---

## 📊 Monitoring

### Health Checks
```bash
# Overall health
curl http://localhost:8091/actuator/health

# Detailed health
curl http://localhost:8091/actuator/health/detailed
```

### Metrics
```bash
# Prometheus metrics
curl http://localhost:8091/actuator/prometheus

# Application metrics
curl http://localhost:8091/actuator/metrics
```

### Service Dependencies
- **Eureka**: http://localhost:8761
- **Prometheus**: http://localhost:9090
- **Grafana**: http://localhost:3000 (admin/admin123)

---

## 🐳 Docker Deployment

### Build Image
```bash
docker build -t youthconnect/edge-functions:latest .
```

### Run Container
```bash
docker run -d \
  --name edge-functions \
  -p 8091:8091 \
  -e SPRING_PROFILES_ACTIVE=prod \
  -e OPENAI_API_KEY=your-key-here \
  -e EUREKA_URL=http://service-registry:8761/eureka/ \
  --network youthconnect-network \
  youthconnect/edge-functions:latest
```

### Docker Compose
```bash
# Start all services
docker-compose up -d

# Scale edge-functions
docker-compose up -d --scale edge-functions=3

# View logs
docker-compose logs -f edge-functions

# Stop services
docker-compose down

# Clean volumes
docker-compose down -v
```

---

## 🔒 Security

### API Authentication
All endpoints require JWT token:
```http
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

### Environment Variables
Never commit sensitive data. Use environment variables:
- `OPENAI_API_KEY` - OpenAI API key
- `DB_PASSWORD` - Database password
- `REDIS_PASSWORD` - Redis password

---

## 🐛 Troubleshooting

### OpenAI API Key Not Configured
```
⚠️ OpenAI API key not configured! AI features disabled.
```
**Solution**: Set `OPENAI_API_KEY` environment variable

### Service Not Registering with Eureka
```
❌ Cannot execute request on any known server
```
**Solution**: Ensure Eureka is running at http://localhost:8761

### Circuit Breaker Open
```
🔴 Circuit Breaker OPEN: Service unavailable
```
**Solution**: Downstream service is failing. Check service health.

---

## 📚 Additional Resources

- [OpenAI API Documentation](https://platform.openai.com/docs)
- [Africa's Talking USSD Guide](https://developers.africastalking.com/docs/ussd)
- [Spring Cloud Documentation](https://spring.io/projects/spring-cloud)
- [Resilience4j Documentation](https://resilience4j.readme.io)

---

## 📄 License

Copyright © 2025 Youth Entrepreneurship Booster Platform  
Licensed under MIT License

---

## 👥 Authors

- **Douglas Kings Kato** - Backend Developer
- **Woord en Daad** - Project Sponsor
- **EU-YEPPAD** - Funding Partner

---

## 📞 Support

- **Email**: support@entrepreneurshipbooster.ug
- **Phone**: +256 700 000 001
- **Website**: https://entrepreneurshipbooster.ug

---

**Built with ❤️ for Ugandan Youth Empowerment**