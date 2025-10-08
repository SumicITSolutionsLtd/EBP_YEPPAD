#!/bin/bash

# Youth Connect Uganda - User Service Deployment Script
# Usage: ./scripts/deploy.sh [environment]

set -e  # Exit on any error

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Default environment
ENVIRONMENT=${1:-"development"}
APP_NAME="user-service"
VERSION="1.0.0"

log() {
    echo -e "${GREEN}[$(date +'%Y-%m-%d %H:%M:%S')]${NC} $1"
}

warn() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

check_dependencies() {
    log "Checking dependencies..."

    # Check Docker
    if ! command -v docker &> /dev/null; then
        error "Docker is not installed. Please install Docker first."
        exit 1
    fi

    # Check Docker Compose
    if ! command -v docker-compose &> /dev/null; then
        error "Docker Compose is not installed. Please install Docker Compose first."
        exit 1
    fi

    log "All dependencies are satisfied."
}

build_application() {
    log "Building application for $ENVIRONMENT environment..."

    # Build the application
    mvn clean package -DskipTests -P$ENVIRONMENT

    if [ $? -ne 0 ]; then
        error "Build failed. Please check the errors above."
        exit 1
    fi

    log "Application built successfully."
}

build_docker_image() {
    log "Building Docker image..."

    docker build -t youthconnect/$APP_NAME:$VERSION -t youthconnect/$APP_NAME:latest -f docker/Dockerfile .

    if [ $? -ne 0 ]; then
        error "Docker build failed."
        exit 1
    fi

    log "Docker image built successfully."
}

deploy_application() {
    log "Deploying application..."

    case $ENVIRONMENT in
        "development")
            deploy_development
            ;;
        "production")
            deploy_production
            ;;
        *)
            error "Unknown environment: $ENVIRONMENT"
            exit 1
            ;;
    esac
}

deploy_development() {
    log "Starting development deployment..."

    # Stop existing containers
    docker-compose -f docker/docker-compose.yml down

    # Start new containers
    docker-compose -f docker/docker-compose.yml up -d

    # Wait for services to be healthy
    wait_for_services
}

deploy_production() {
    log "Starting production deployment..."

    # In production, you might use:
    # - Kubernetes
    # - Docker Swarm
    # - Cloud deployment

    warn "Production deployment not fully implemented. Please configure for your production environment."

    # Example for production with Docker Compose
    docker-compose -f docker/docker-compose.prod.yml up -d

    wait_for_services
}

wait_for_services() {
    log "Waiting for services to be healthy..."

    # Wait for MySQL
    until docker exec youthconnect-mysql mysqladmin ping -h localhost -u root -pDouglas20! --silent; do
        sleep 5
    done
    log "MySQL is healthy"

    # Wait for User Service
    until curl -f http://localhost:8081/actuator/health > /dev/null 2>&1; do
        sleep 10
    done
    log "User Service is healthy"

    # Wait for Eureka
    until curl -f http://localhost:8761/actuator/health > /dev/null 2>&1; do
        sleep 10
    done
    log "Eureka Server is healthy"
}

run_migrations() {
    log "Running database migrations..."

    # This would typically use Flyway or similar
    # For now, we rely on Flyway auto-migration on startup
    log "Migrations will run automatically on application startup."
}

health_check() {
    log "Performing health checks..."

    # Check user service
    if curl -s http://localhost:8081/actuator/health | grep -q '"status":"UP"'; then
        log "User Service: HEALTHY"
    else
        error "User Service: UNHEALTHY"
        exit 1
    fi

    # Check database connectivity through service
    if curl -s http://localhost:8081/actuator/health | grep -q '"db":{"status":"UP"'; then
        log "Database Connectivity: HEALTHY"
    else
        error "Database Connectivity: UNHEALTHY"
        exit 1
    fi

    log "All health checks passed."
}

display_info() {
    log "Deployment completed successfully!"
    echo ""
    echo "=== Youth Connect Uganda - User Service ==="
    echo "Environment: $ENVIRONMENT"
    echo "Version: $VERSION"
    echo ""
    echo "Access URLs:"
    echo "  - User Service: http://localhost:8081"
    echo "  - API Documentation: http://localhost:8081/swagger-ui.html"
    echo "  - Health Checks: http://localhost:8081/actuator/health"
    echo "  - Eureka Dashboard: http://localhost:8761"
    echo ""
    echo "To view logs: docker-compose -f docker/docker-compose.yml logs -f user-service"
    echo ""
}

main() {
    log "Starting Youth Connect Uganda User Service deployment..."

    check_dependencies
    build_application
    build_docker_image
    deploy_application
    run_migrations
    health_check
    display_info

    log "Deployment process completed!"
}

# Run main function
main