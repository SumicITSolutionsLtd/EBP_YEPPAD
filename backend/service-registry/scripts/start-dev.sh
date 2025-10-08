#!/bin/bash

################################################################################
# Service Registry - Development Startup Script
################################################################################

set -e

# Configuration
APP_JAR="target/service-registry.jar"
SERVER_PORT="${SERVER_PORT:-8761}"
DEBUG_PORT="${DEBUG_PORT:-5005}"
SPRING_PROFILE="dev"

# Colors
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m'

print_banner() {
    clear
    echo -e "${BLUE}"
    cat << "EOF"
╔════════════════════════════════════════════════════════════════╗
║                                                                ║
║       🌟 Service Registry - Development Mode 🌟                ║
║                                                                ║
║         YouthConnect Uganda Platform                           ║
║                                                                ║
╚════════════════════════════════════════════════════════════════╝
EOF
    echo -e "${NC}"
}

log_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

check_requirements() {
    log_info "Checking requirements..."

    if ! command -v java &> /dev/null; then
        log_error "Java is not installed. Please install Java 17+"
        exit 1
    fi

    JAVA_VERSION=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}' | cut -d'.' -f1)
    if [ "$JAVA_VERSION" -lt 17 ]; then
        log_error "Java 17 or higher is required (found Java $JAVA_VERSION)"
        exit 1
    fi

    log_info "Java version: $JAVA_VERSION ✓"

    if ! command -v mvn &> /dev/null; then
        log_error "Maven is not installed"
        exit 1
    fi

    log_info "Maven found ✓"
}

build_application() {
    log_info "Building application..."

    if [ ! -f "$APP_JAR" ]; then
        log_info "JAR not found. Running Maven build..."
        mvn clean package -DskipTests
    else
        log_info "Using existing JAR. Run 'mvn clean package' to rebuild."
    fi
}

start_application() {
    log_info "Starting Service Registry in development mode..."
    log_info "Server Port: $SERVER_PORT"
    log_info "Debug Port: $DEBUG_PORT"
    log_info "Profile: $SPRING_PROFILE"
    echo ""

    JAVA_OPTS="-Xms256m \
        -Xmx512m \
        -XX:+UseG1GC \
        -XX:MaxGCPauseMillis=100 \
        -Djava.security.egd=file:/dev/./urandom \
        -Dspring.profiles.active=$SPRING_PROFILE \
        -Dserver.port=$SERVER_PORT \
        -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:$DEBUG_PORT"

    echo -e "${YELLOW}═══════════════════════════════════════════════════════${NC}"
    echo -e "${GREEN}Service Registry is starting...${NC}"
    echo ""
    echo -e "Dashboard: http://localhost:$SERVER_PORT"
    echo -e "Health:    http://localhost:$SERVER_PORT/actuator/health"
    echo -e "Metrics:   http://localhost:$SERVER_PORT/actuator/metrics"
    echo -e "Debug:     localhost:$DEBUG_PORT"
    echo ""
    echo -e "${YELLOW}Press Ctrl+C to stop${NC}"
    echo -e "${YELLOW}═══════════════════════════════════════════════════════${NC}"
    echo ""

    java $JAVA_OPTS -jar "$APP_JAR"
}

main() {
    print_banner
    check_requirements
    build_application
    start_application
}

trap 'echo -e "\n${YELLOW}Shutting down...${NC}"; exit 0' INT

main