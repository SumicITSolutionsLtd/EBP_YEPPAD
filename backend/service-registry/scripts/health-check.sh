#!/bin/bash

################################################################################
# Service Registry Health Check Script
################################################################################
# This script performs comprehensive health checks on the Eureka Server
# Usage: ./health-check.sh [options]
# Options:
#   -h, --host     Eureka server host (default: localhost)
#   -p, --port     Eureka server port (default: 8761)
#   -v, --verbose  Verbose output
################################################################################

set -e  # Exit on error

# ============================================
# Configuration
# ============================================
EUREKA_HOST="${EUREKA_HOST:-localhost}"
EUREKA_PORT="${EUREKA_PORT:-8761}"
EUREKA_URL="http://${EUREKA_HOST}:${EUREKA_PORT}"
VERBOSE=false

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# ============================================
# Parse Arguments
# ============================================
while [[ $# -gt 0 ]]; do
    case $1 in
        -h|--host)
            EUREKA_HOST="$2"
            shift 2
            ;;
        -p|--port)
            EUREKA_PORT="$2"
            shift 2
            ;;
        -v|--verbose)
            VERBOSE=true
            shift
            ;;
        --help)
            echo "Usage: $0 [options]"
            echo "Options:"
            echo "  -h, --host     Eureka server host (default: localhost)"
            echo "  -p, --port     Eureka server port (default: 8761)"
            echo "  -v, --verbose  Verbose output"
            exit 0
            ;;
        *)
            echo "Unknown option: $1"
            exit 1
            ;;
    esac
done

# ============================================
# Helper Functions
# ============================================

log_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

log_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

log_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# ============================================
# Health Check Functions
# ============================================

check_server_reachable() {
    log_info "Checking if Eureka server is reachable..."

    if curl -s -f --connect-timeout 5 "${EUREKA_URL}/actuator/health" > /dev/null; then
        log_success "Server is reachable"
        return 0
    else
        log_error "Server is not reachable at ${EUREKA_URL}"
        return 1
    fi
}

check_liveness() {
    log_info "Checking liveness probe..."

    RESPONSE=$(curl -s -w "\n%{http_code}" "${EUREKA_URL}/actuator/health/liveness")
    HTTP_CODE=$(echo "$RESPONSE" | tail -n1)
    BODY=$(echo "$RESPONSE" | sed '$d')

    if [ "$HTTP_CODE" -eq 200 ]; then
        log_success "Liveness check passed"
        [ "$VERBOSE" = true ] && echo "$BODY" | jq '.'
        return 0
    else
        log_error "Liveness check failed (HTTP $HTTP_CODE)"
        [ "$VERBOSE" = true ] && echo "$BODY"
        return 1
    fi
}

check_readiness() {
    log_info "Checking readiness probe..."

    RESPONSE=$(curl -s -w "\n%{http_code}" "${EUREKA_URL}/actuator/health/readiness")
    HTTP_CODE=$(echo "$RESPONSE" | tail -n1)
    BODY=$(echo "$RESPONSE" | sed '$d')

    if [ "$HTTP_CODE" -eq 200 ]; then
        log_success "Readiness check passed"
        [ "$VERBOSE" = true ] && echo "$BODY" | jq '.'
        return 0
    else
        log_error "Readiness check failed (HTTP $HTTP_CODE)"
        [ "$VERBOSE" = true ] && echo "$BODY"
        return 1
    fi
}

check_registered_services() {
    log_info "Checking registered services..."

    RESPONSE=$(curl -s "${EUREKA_URL}/eureka/apps" -H "Accept: application/json")

    if [ -n "$RESPONSE" ]; then
        SERVICE_COUNT=$(echo "$RESPONSE" | jq -r '.applications.application | length // 0')
        log_success "Found $SERVICE_COUNT registered service(s)"

        if [ "$VERBOSE" = true ] && [ "$SERVICE_COUNT" -gt 0 ]; then
            echo "$RESPONSE" | jq -r '.applications.application[] | "  - \(.name): \(.instance | length) instance(s)"'
        fi
        return 0
    else
        log_warning "No services registered yet"
        return 0
    fi
}

check_metrics() {
    log_info "Checking metrics endpoint..."

    if curl -s -f "${EUREKA_URL}/actuator/metrics" > /dev/null; then
        log_success "Metrics endpoint is accessible"

        if [ "$VERBOSE" = true ]; then
            # Get some key metrics
            JVM_MEMORY=$(curl -s "${EUREKA_URL}/actuator/metrics/jvm.memory.used" | jq -r '.measurements[0].value')
            CPU_USAGE=$(curl -s "${EUREKA_URL}/actuator/metrics/system.cpu.usage" | jq -r '.measurements[0].value')

            echo "  - JVM Memory Used: $(echo "scale=2; $JVM_MEMORY / 1048576" | bc) MB"
            echo "  - CPU Usage: $(echo "scale=2; $CPU_USAGE * 100" | bc)%"
        fi
        return 0
    else
        log_error "Metrics endpoint is not accessible"
        return 1
    fi
}

check_info() {
    log_info "Checking application info..."

    RESPONSE=$(curl -s "${EUREKA_URL}/actuator/info")

    if [ -n "$RESPONSE" ]; then
        log_success "Application info retrieved"
        [ "$VERBOSE" = true ] && echo "$RESPONSE" | jq '.'
        return 0
    else
        log_warning "Could not retrieve application info"
        return 0
    fi
}

# ============================================
# Main Execution
# ============================================

main() {
    echo "=================================="
    echo "Service Registry Health Check"
    echo "=================================="
    echo "Target: ${EUREKA_URL}"
    echo "=================================="
    echo ""

    EXIT_CODE=0

    # Run all checks
    check_server_reachable || EXIT_CODE=1
    echo ""

    check_liveness || EXIT_CODE=1
    echo ""

    check_readiness || EXIT_CODE=1
    echo ""

    check_registered_services || EXIT_CODE=1
    echo ""

    check_metrics || EXIT_CODE=1
    echo ""

    check_info || EXIT_CODE=1
    echo ""

    # Final summary
    echo "=================================="
    if [ $EXIT_CODE -eq 0 ]; then
        log_success "All health checks passed!"
    else
        log_error "Some health checks failed!"
    fi
    echo "=================================="

    exit $EXIT_CODE
}

# Run main function
mainecho -e "${GREEN}[INFO]${NC} $1"
    }

    log_error() {
        echo -e "${RED}[ERROR]${NC} $1"
    }

    check_requirements() {
        log_info "Checking requirements..."

        # Check Java
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

        # Check Maven
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

        # JVM options for development
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

        # Start the application
        java $JAVA_OPTS -jar "$APP_JAR"
    }

    # ============================================
    # Main Execution
    # ============================================

    main() {
        print_banner
        check_requirements
        build_application
        start_application
    }

    # Trap Ctrl+C
    trap 'echo -e "\n${YELLOW}Shutting down...${NC}"; exit 0' INT

    main