#!/bin/bash

################################################################################
# QUICK FIX SCRIPT - Run this to fix all issues
################################################################################

set -e

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

echo -e "${GREEN}════════════════════════════════════════════════════════════════${NC}"
echo -e "${GREEN}  Youth Connect Platform - Quick Fix Script${NC}"
echo -e "${GREEN}════════════════════════════════════════════════════════════════${NC}"
echo ""

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
BACKEND_DIR="$(dirname "$SCRIPT_DIR")"

cd "$BACKEND_DIR"

# Step 1: Kill all Java processes
echo -e "${YELLOW}Step 1: Stopping all Java processes...${NC}"
taskkill //F //IM java.exe 2>/dev/null || true
sleep 2
echo -e "${GREEN}✅ All Java processes stopped${NC}"
echo ""

# Step 2: Fix Auth Service Configuration
echo -e "${YELLOW}Step 2: Fixing Auth Service configuration...${NC}"
cat > auth-service/src/main/resources/application.yml << 'EOF'
spring:
  application:
    name: auth-service
  datasource:
    url: jdbc:postgresql://${DB_HOST:localhost}:${DB_PORT:5432}/youthconnect_auth
    username: ${DB_USER:youthconnect_user}
    password: ${DB_PASSWORD:YouthConnect2024!}
    driver-class-name: org.postgresql.Driver
    hikari:
      maximum-pool-size: 10
      minimum-idle: 2
      connection-timeout: 30000
      pool-name: AuthServiceHikariPool
  jpa:
    hibernate:
      ddl-auto: validate
    database-platform: org.hibernate.dialect.PostgreSQLDialect
  flyway:
    enabled: true
    baseline-on-migrate: true
  data:
    redis:
      host: ${REDIS_HOST:localhost}
      port: ${REDIS_PORT:6379}
server:
  port: 8083
  servlet:
    context-path: /api/auth
eureka:
  client:
    enabled: true
    service-url:
      defaultZone: http://admin:changeme@localhost:8761/eureka/
jwt:
  secret: aVeryLongAndSecureSecretKeyForYouthConnectUgandaHackathonProjectWith256BitsMinimum
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics
logging:
  level:
    root: INFO
    com.youthconnect.auth_service: DEBUG
EOF
echo -e "${GREEN}✅ Auth Service configuration fixed${NC}"
echo ""

# Step 3: Rebuild Auth Service
echo -e "${YELLOW}Step 3: Rebuilding Auth Service...${NC}"
cd auth-service
mvn clean package -DskipTests > /dev/null 2>&1 || {
    echo -e "${RED}❌ Build failed. Check Maven logs${NC}"
    exit 1
}
cd ..
echo -e "${GREEN}✅ Auth Service rebuilt${NC}"
echo ""

# Step 4: Start Eureka
echo -e "${YELLOW}Step 4: Starting Eureka Server...${NC}"
cd service-registry
java -jar target/service-registry.jar > ../logs/service-registry.log 2>&1 &
EUREKA_PID=$!
echo $EUREKA_PID > ../logs/service-registry.pid
echo -e "${GREEN}✅ Eureka started (PID: $EUREKA_PID)${NC}"
cd ..

# Wait for Eureka
echo "   Waiting for Eureka..."
for i in {1..30}; do
    if curl -s http://localhost:8761/actuator/health > /dev/null 2>&1; then
        echo -e "${GREEN}   ✅ Eureka is ready!${NC}"
        break
    fi
    sleep 2
done
echo ""

# Step 5: Start Auth Service
echo -e "${YELLOW}Step 5: Starting Auth Service...${NC}"
cd auth-service
java -Dspring.profiles.active=dev \
     -Dspring.datasource.url=jdbc:postgresql://localhost:5432/youthconnect_auth \
     -Dspring.datasource.username=youthconnect_user \
     -Dspring.datasource.password=YouthConnect2024! \
     -jar target/auth-service.jar > ../logs/auth-service.log 2>&1 &
AUTH_PID=$!
echo $AUTH_PID > ../logs/auth-service.pid
echo -e "${GREEN}✅ Auth Service started (PID: $AUTH_PID)${NC}"
cd ..

# Wait for Auth Service
echo "   Waiting for Auth Service..."
for i in {1..30}; do
    if curl -s http://localhost:8083/api/auth/actuator/health > /dev/null 2>&1; then
        echo -e "${GREEN}   ✅ Auth Service is ready!${NC}"
        break
    fi
    sleep 2
done
echo ""

# Step 6: Test Auth Service
echo -e "${YELLOW}Step 6: Testing Auth Service...${NC}"
HEALTH=$(curl -s http://localhost:8083/api/auth/actuator/health)
if echo "$HEALTH" | grep -q '"status":"UP"'; then
    echo -e "${GREEN}✅ Auth Service health check PASSED${NC}"
else
    echo -e "${RED}❌ Auth Service health check FAILED${NC}"
    echo "Response: $HEALTH"
fi
echo ""

echo -e "${GREEN}════════════════════════════════════════════════════════════════${NC}"
echo -e "${GREEN}  Quick Fix Complete!${NC}"
echo -e "${GREEN}════════════════════════════════════════════════════════════════${NC}"
echo ""
echo "🌐 Services Running:"
echo "   • Eureka: http://localhost:8761"
echo "   • Auth Service: http://localhost:8083/api/auth"
echo ""
echo "📝 Logs:"
echo "   • Eureka: logs/service-registry.log"
echo "   • Auth: logs/auth-service.log"
echo ""
echo "🔍 Check Eureka Dashboard to verify registration:"
echo "   http://localhost:8761"
echo ""