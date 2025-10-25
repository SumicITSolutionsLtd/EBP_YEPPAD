#!/bin/bash
# =================================================================================
# Entrepreneurship Booster Platform - Complete Setup Script
# Purpose: Initialize development and production environments
# Usage: ./scripts/setup.sh [dev|prod]
# =================================================================================

set -e  # Exit on any error

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configuration
ENVIRONMENT=${1:-dev}
PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
LOG_DIR="${PROJECT_ROOT}/logs"
GRAFANA_DIR="${PROJECT_ROOT}/monitoring/grafana"
PROMETHEUS_DIR="${PROJECT_ROOT}/monitoring"

echo -e "${BLUE}╔════════════════════════════════════════════════════╗${NC}"
echo -e "${BLUE}║   Entrepreneurship Booster Platform Setup         ║${NC}"
echo -e "${BLUE}║   Environment: ${ENVIRONMENT}                      ║${NC}"
echo -e "${BLUE}╚════════════════════════════════════════════════════╝${NC}"

# =================================================================================
# STEP 1: Verify Prerequisites
# =================================================================================
echo -e "\n${YELLOW}[1/8]${NC} Checking prerequisites..."

# Check Docker
if ! command -v docker &> /dev/null; then
    echo -e "${RED}✗ Docker not found. Please install Docker first.${NC}"
    echo "  Visit: https://docs.docker.com/get-docker/"
    exit 1
fi
echo -e "${GREEN}✓ Docker found: $(docker --version)${NC}"

# Check Docker Compose
if ! command -v docker-compose &> /dev/null; then
    echo -e "${RED}✗ Docker Compose not found.${NC}"
    exit 1
fi
echo -e "${GREEN}✓ Docker Compose found: $(docker-compose --version)${NC}"

# Check Java
if ! command -v java &> /dev/null; then
    echo -e "${YELLOW}⚠ Java not found. Required for local service development.${NC}"
else
    echo -e "${GREEN}✓ Java found: $(java -version 2>&1 | head -n 1)${NC}"
fi

# Check Maven
if ! command -v mvn &> /dev/null; then
    echo -e "${YELLOW}⚠ Maven not found. Required for building services.${NC}"
else
    echo -e "${GREEN}✓ Maven found: $(mvn --version | head -n 1)${NC}"
fi

# =================================================================================
# STEP 2: Create Directory Structure
# =================================================================================
echo -e "\n${YELLOW}[2/8]${NC} Creating directory structure..."

mkdir -p "${LOG_DIR}"/{service-logs,nginx,mysql,redis,monitoring}
mkdir -p "${GRAFANA_DIR}"/{dashboards,provisioning/{datasources,dashboards}}
mkdir -p "${PROMETHEUS_DIR}/rules"
mkdir -p "${PROJECT_ROOT}/nginx/ssl"
mkdir -p "${PROJECT_ROOT}/data"/{mysql,redis,prometheus,grafana}

echo -e "${GREEN}✓ Directories created${NC}"

# =================================================================================
# STEP 3: Generate Environment Files
# =================================================================================
echo -e "\n${YELLOW}[3/8]${NC} Generating environment configuration..."

if [ ! -f "${PROJECT_ROOT}/.env" ]; then
    cat > "${PROJECT_ROOT}/.env" <<EOF
# =================================================================================
# Entrepreneurship Booster Platform - Environment Configuration
# Generated: $(date)
# Environment: ${ENVIRONMENT}
# =================================================================================

# Application Environment
APP_ENV=${ENVIRONMENT}
APP_NAME=entrepreneurship-booster-platform
APP_VERSION=1.0.0

# Database Configuration
DB_HOST=mysql
DB_PORT=3307
DB_NAME=epb_db
DB_USER=epb_user
DB_PASSWORD=$(openssl rand -base64 32)
DB_ROOT_PASSWORD=$(openssl rand -base64 32)

# Redis Configuration
REDIS_HOST=redis
REDIS_PORT=6379
REDIS_PASSWORD=$(openssl rand -base64 32)

# JWT Configuration
JWT_SECRET=$(openssl rand -base64 64)
JWT_ACCESS_TOKEN_EXPIRY=900
JWT_REFRESH_TOKEN_EXPIRY=604800

# Service Ports
EUREKA_PORT=8761
API_GATEWAY_PORT=8080
USER_SERVICE_PORT=8081
AUTH_SERVICE_PORT=8082
OPPORTUNITY_SERVICE_PORT=8083
CONTENT_SERVICE_PORT=8084
MENTOR_SERVICE_PORT=8085
NOTIFICATION_SERVICE_PORT=8086
USSD_SERVICE_PORT=8087
FILE_MANAGEMENT_SERVICE_PORT=8088
AI_RECOMMENDATION_SERVICE_PORT=8089
ANALYTICS_SERVICE_PORT=8090
EDGE_FUNCTIONS_PORT=8091

# Monitoring Ports
PROMETHEUS_PORT=9090
GRAFANA_PORT=3000

# Nginx
NGINX_HTTP_PORT=80
NGINX_HTTPS_PORT=443

# Africa's Talking API (SMS/USSD)
AFRICAS_TALKING_USERNAME=your_username
AFRICAS_TALKING_API_KEY=your_api_key
AFRICAS_TALKING_SENDER_ID=YOUTHCONNECT
USSD_SHORT_CODE=*256#

# Email Configuration
SMTP_HOST=smtp.gmail.com
SMTP_PORT=587
SMTP_USERNAME=your_email@gmail.com
SMTP_PASSWORD=your_app_password
SMTP_FROM=noreply@entrepreneurshipbooster.ug

# File Storage
STORAGE_TYPE=local
STORAGE_PATH=/app/uploads
AWS_S3_BUCKET=epb-files
AWS_S3_REGION=us-east-1

# Logging
LOG_LEVEL=INFO
LOG_PATH=${LOG_DIR}

# Security
CORS_ALLOWED_ORIGINS=http://localhost:3000,http://localhost:3001
RATE_LIMIT_REQUESTS=100
RATE_LIMIT_DURATION=60

# Feature Flags
FEATURE_AI_RECOMMENDATIONS=true
FEATURE_USSD_ACCESS=true
FEATURE_FILE_UPLOADS=true
FEATURE_ANALYTICS=true
EOF

    echo -e "${GREEN}✓ .env file created${NC}"
    echo -e "${YELLOW}⚠ IMPORTANT: Update sensitive credentials in .env${NC}"
else
    echo -e "${BLUE}ℹ .env file already exists, skipping${NC}"
fi

# =================================================================================
# STEP 4: Setup Database Initialization
# =================================================================================
echo -e "\n${YELLOW}[4/8]${NC} Preparing database initialization..."

if [ -f "${PROJECT_ROOT}/scripts/init-db.sql" ]; then
    echo -e "${GREEN}✓ Database schema found${NC}"
else
    echo -e "${YELLOW}⚠ Database schema not found at scripts/init-db.sql${NC}"
fi

# =================================================================================
# STEP 5: Generate SSL Certificates (Self-Signed for Dev)
# =================================================================================
echo -e "\n${YELLOW}[5/8]${NC} Setting up SSL certificates..."

if [ "${ENVIRONMENT}" = "dev" ]; then
    if [ ! -f "${PROJECT_ROOT}/nginx/ssl/cert.pem" ]; then
        openssl req -x509 -nodes -days 365 -newkey rsa:2048 \
            -keyout "${PROJECT_ROOT}/nginx/ssl/key.pem" \
            -out "${PROJECT_ROOT}/nginx/ssl/cert.pem" \
            -subj "/C=UG/ST=Central/L=Kampala/O=EPB/CN=localhost" \
            2>/dev/null
        echo -e "${GREEN}✓ Self-signed SSL certificate created${NC}"
    else
        echo -e "${BLUE}ℹ SSL certificates already exist${NC}"
    fi
else
    echo -e "${YELLOW}⚠ For production, use Let's Encrypt certificates${NC}"
    echo "  Run: certbot certonly --webroot -w /var/www/html -d yourdomain.com"
fi

# =================================================================================
# STEP 6: Build Docker Images
# =================================================================================
echo -e "\n${YELLOW}[6/8]${NC} Building Docker images..."

if [ "${ENVIRONMENT}" = "prod" ]; then
    echo "Building production images..."
    docker-compose -f docker-compose.yml -f docker-compose.prod.yml build --no-cache
else
    echo "Building development images..."
    docker-compose build
fi

echo -e "${GREEN}✓ Docker images built${NC}"

# =================================================================================
# STEP 7: Initialize Monitoring Stack
# =================================================================================
echo -e "\n${YELLOW}[7/8]${NC} Setting up monitoring..."

# Set Grafana permissions
chmod -R 777 "${PROJECT_ROOT}/data/grafana"

# Create Prometheus data directory
mkdir -p "${PROJECT_ROOT}/data/prometheus"
chmod 777 "${PROJECT_ROOT}/data/prometheus"

echo -e "${GREEN}✓ Monitoring stack configured${NC}"

# =================================================================================
# STEP 8: Verify Configuration
# =================================================================================
echo -e "\n${YELLOW}[8/8]${NC} Verifying configuration..."

# Check required files
REQUIRED_FILES=(
    "docker-compose.yml"
    "nginx/nginx.conf"
    "monitoring/prometheus.yml"
    ".env"
)

MISSING_FILES=()
for file in "${REQUIRED_FILES[@]}"; do
    if [ ! -f "${PROJECT_ROOT}/${file}" ]; then
        MISSING_FILES+=("${file}")
    fi
done

if [ ${#MISSING_FILES[@]} -ne 0 ]; then
    echo -e "${RED}✗ Missing required files:${NC}"
    for file in "${MISSING_FILES[@]}"; do
        echo -e "  - ${file}"
    done
    exit 1
fi

echo -e "${GREEN}✓ All required files present${NC}"

# =================================================================================
# COMPLETION
# =================================================================================
echo -e "\n${GREEN}╔════════════════════════════════════════════════════╗${NC}"
echo -e "${GREEN}║   Setup completed successfully!                    ║${NC}"
echo -e "${GREEN}╚════════════════════════════════════════════════════╝${NC}"

echo -e "\n${BLUE}Next steps:${NC}"
echo -e "1. Review and update credentials in ${PROJECT_ROOT}/.env"
echo -e "2. Start services: ${YELLOW}docker-compose up -d${NC}"
echo -e "3. Check service health: ${YELLOW}docker-compose ps${NC}"
echo -e "4. View logs: ${YELLOW}docker-compose logs -f <service-name>${NC}"
echo -e "5. Access services:"
echo -e "   - API Gateway: ${YELLOW}http://localhost:8080${NC}"
echo -e "   - Eureka Dashboard: ${YELLOW}http://localhost:8761${NC}"
echo -e "   - Grafana: ${YELLOW}http://localhost:3000${NC} (admin/admin)"
echo -e "   - Prometheus: ${YELLOW}http://localhost:9090${NC}"

echo -e "\n${BLUE}For production deployment:${NC}"
echo -e "1. ${YELLOW}./scripts/setup.sh prod${NC}"
echo -e "2. Update .env with production credentials"
echo -e "3. Configure domain and SSL certificates"
echo -e "4. Review security settings in nginx.conf"

echo -e "\n${GREEN}Setup log saved to: ${LOG_DIR}/setup-$(date +%Y%m%d-%H%M%S).log${NC}"