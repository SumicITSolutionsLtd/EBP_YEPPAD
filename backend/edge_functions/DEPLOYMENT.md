# Production Deployment Guide

## 📋 Pre-Deployment Checklist

### ✅ Code Review
- [ ] All code reviewed and approved
- [ ] No hardcoded credentials or API keys
- [ ] Logging levels appropriate for production
- [ ] Error handling comprehensive
- [ ] Unit tests passing (>80% coverage)
- [ ] Integration tests passing

### ✅ Configuration
- [ ] Production `application-prod.yml` configured
- [ ] Environment variables template updated
- [ ] Database connection strings verified
- [ ] OpenAI API key obtained and tested
- [ ] Africa's Talking credentials configured
- [ ] SMTP settings for production email

### ✅ Infrastructure
- [ ] MySQL 8.0 database provisioned
- [ ] Redis 7.0 cache provisioned
- [ ] Service Registry (Eureka) deployed
- [ ] Load balancer configured
- [ ] SSL certificates installed
- [ ] Firewall rules configured

### ✅ Monitoring
- [ ] Prometheus configured
- [ ] Grafana dashboards imported
- [ ] Alert rules configured
- [ ] Log aggregation setup (ELK/Splunk)
- [ ] PagerDuty/on-call rotation configured

### ✅ Security
- [ ] Security audit completed
- [ ] Penetration testing performed
- [ ] SSL/TLS certificates valid
- [ ] Secrets management configured
- [ ] Backup and disaster recovery plan

---

## 🚀 Deployment Steps

### 1. Build Application

```bash
# Clone repository
git clone https://github.com/your-org/entrepreneurship-booster.git
cd entrepreneurship-booster/edge_functions

# Checkout production branch
git checkout main

# Run tests
mvn clean test

# Build JAR
mvn clean package -DskipTests

# Verify JAR created
ls -lh target/*.jar
```

### 2. Build Docker Image

```bash
# Build with version tag
docker build -t youthconnect/edge-functions:1.0.0 .
docker tag youthconnect/edge-functions:1.0.0 youthconnect/edge-functions:latest

# Test locally
docker run --rm -p 8091:8091 \
  -e SPRING_PROFILES_ACTIVE=prod \
  -e OPENAI_API_KEY=test-key \
  youthconnect/edge-functions:1.0.0

# Push to registry
docker push youthconnect/edge-functions:1.0.0
docker push youthconnect/edge-functions:latest
```

### 3. Database Setup

```bash
# Connect to production MySQL
mysql -h production-db.amazonaws.com -u admin -p

# Create database
CREATE DATABASE IF NOT EXISTS epb_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

# Run schema migration
mysql -h production-db.amazonaws.com -u admin -p epb_db < scripts/schema.sql

# Verify tables created
mysql -h production-db.amazonaws.com -u admin -p epb_db -e "SHOW TABLES;"
```

### 4. Deploy to Production

**Option A: Docker Compose (Single Server)**

```bash
# Copy docker-compose files to server
scp docker-compose.yml docker-compose.prod.yml production-server:/opt/app/

# SSH to server
ssh production-server

cd /opt/app

# Create .env file
nano .env
# Add production credentials

# Pull latest images
docker-compose -f docker-compose.yml -f docker-compose.prod.yml pull

# Start services
docker-compose -f docker-compose.yml -f docker-compose.prod.yml up -d

# Check status
docker-compose ps
docker-compose logs -f edge-functions
```

**Option B: Kubernetes (Scalable)**

```bash
# Apply Kubernetes manifests
kubectl apply -f k8s/namespace.yml
kubectl apply -f k8s/configmap.yml
kubectl apply -f k8s/secrets.yml
kubectl apply -f k8s/deployment.yml
kubectl apply -f k8s/service.yml
kubectl apply -f k8s/ingress.yml

# Verify deployment
kubectl get pods -n youthconnect
kubectl describe pod edge-functions-xxx -n youthconnect
kubectl logs -f edge-functions-xxx -n youthconnect

# Check service
kubectl get svc -n youthconnect
```

### 5. Post-Deployment Verification

```bash
# Health check
curl https://api.entrepreneurshipbooster.ug/edge/actuator/health

# Expected response:
# {"status":"UP"}

# Test USSD endpoint
curl -X POST https://api.entrepreneurshipbooster.ug/ussd/callback \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "sessionId=test123&serviceCode=*256#&phoneNumber=256701234567&text="

# Test AI endpoint (requires authentication)
curl -X POST https://api.entrepreneurshipbooster.ug/edge/chat \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "message": "Test message",
    "systemPrompt": "You are a helpful assistant"
  }'

# Check metrics
curl https://api.entrepreneurshipbooster.ug/edge/actuator/prometheus
```

### 6. Configure Monitoring

```bash
# Grafana Dashboards
# 1. Login to Grafana: https://monitoring.entrepreneurshipbooster.ug
# 2. Import dashboard from monitoring/grafana/dashboards/
# 3. Configure data source (Prometheus)

# Prometheus Alerts
# Verify alert rules are active
curl http://prometheus:9090/api/v1/rules

# Test alert firing
# Simulate service down by stopping container
docker stop edge-functions
# Wait 2 minutes
# Verify alert notification received
```

---

## 📊 Monitoring & Alerting

### Key Metrics to Monitor

**Service Health**
- Uptime percentage (target: 99.5%)
- Response time (p50, p95, p99)
- Error rate (target: <1%)
- Circuit breaker state

**Resource Usage**
- CPU utilization (target: <70%)
- Memory usage (target: <80%)
- Disk I/O
- Network bandwidth

**Business Metrics**
- USSD sessions per hour
- AI chat requests per hour
- Multi-service operations success rate
- Average workfl