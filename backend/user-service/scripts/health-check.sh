#!/bin/bash

# Youth Connect Uganda - User Service Health Check Script
# Usage: ./scripts/health-check.sh

set -e

# Colors
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m'

SERVICE_URL="http://localhost:8081"
TIMEOUT=10

log() {
    echo -e "${GREEN}[$(date +'%Y-%m-%d %H:%M:%S')]${NC} $1"
}

error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

warn() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

check_service_health() {
    log "Checking User Service health..."

    local response
    response=$(curl -s -o /dev/null -w "%{http_code}" --max-time $TIMEOUT $SERVICE_URL/actuator/health || echo "000")

    if [ "$response" -eq 200 ]; then
        log "User Service is responding (HTTP $response)"
        return 0
    else
        error "User Service is not responding properly (HTTP $response)"
        return 1
    fi
}

check_database_health() {
    log "Checking database connectivity through service..."

    local health_json
    health_json=$(curl -s --max-time $TIMEOUT $SERVICE_URL/actuator/health)

    if echo "$health_json" | grep -q '"db":{"status":"UP"'; then
        log "Database connectivity: HEALTHY"
        return 0
    else
        error "Database connectivity: UNHEALTHY"
        return 1
    fi
}

check_disk_space() {
    log "Checking disk space..."

    local available_space
    available_space=$(df / | awk 'NR==2 {print $4}')

    # Convert to MB
    available_space_mb=$((available_space / 1024))

    if [ "$available_space_mb" -lt 1024 ]; then
        warn "Low disk space: ${available_space_mb}MB available"
        return 1
    else
        log "Disk space: ${available_space_mb}MB available - HEALTHY"
        return 0
    fi
}

check_memory_usage() {
    log "Checking memory usage..."

    local total_mem
    local free_mem
    total_mem=$(free -m | awk 'NR==2{print $2}')
    free_mem=$(free -m | awk 'NR==2{print $4}')

    local usage_percentage
    usage_percentage=$((100 - (free_mem * 100 / total_mem)))

    if [ "$usage_percentage" -gt 90 ]; then
        warn "High memory usage: ${usage_percentage}% used"
        return 1
    else
        log "Memory usage: ${usage_percentage}% - HEALTHY"
        return 0
    fi
}

check_container_health() {
    log "Checking Docker container health..."

    if docker ps | grep -q "youthconnect-user-service"; then
        log "User Service container: RUNNING"

        # Check container status
        local container_status
        container_status=$(docker inspect --format='{{.State.Status}}' youthconnect-user-service 2>/dev/null || echo "not found")

        if [ "$container_status" == "running" ]; then
            log "Container status: HEALTHY"
            return 0
        else
            error "Container status: $container_status"
            return 1
        fi
    else
        error "User Service container: NOT RUNNING"
        return 1
    fi
}

check_database_direct() {
    log "Checking direct database connection..."

    if command -v mysql &> /dev/null; then
        if mysql -h localhost -P 3307 -u root -pDouglas20! -e "SELECT 1;" youth_connect_db &> /dev/null; then
            log "Direct database connection: HEALTHY"
            return 0
        else
            error "Direct database connection: FAILED"
            return 1
        fi
    else
        warn "MySQL client not installed, skipping direct database check"
        return 0
    fi
}

run_performance_test() {
    log "Running quick performance test..."

    local start_time
    local end_time
    start_time=$(date +%s%3N)

    # Make a simple API call
    if curl -s --max-time $TIMEOUT $SERVICE_URL/actuator/info > /dev/null; then
        end_time=$(date +%s%3N)
        local response_time
        response_time=$((end_time - start_time))

        if [ "$response_time" -lt 1000 ]; then
            log "Performance: GOOD (${response_time}ms response time)"
            return 0
        elif [ "$response_time" -lt 3000 ]; then
            warn "Performance: ACCEPTABLE (${response_time}ms response time)"
            return 0
        else
            warn "Performance: SLOW (${response_time}ms response time)"
            return 1
        fi
    else
        error "Performance test: FAILED - Service not responding"
        return 1
    fi
}

generate_report() {
    log "Generating health check report..."

    echo ""
    echo "=== YOUTH CONNECT UGANDA - USER SERVICE HEALTH REPORT ==="
    echo "Generated: $(date)"
    echo "Service URL: $SERVICE_URL"
    echo ""

    # Service Health
    if check_service_health; then
        echo "✅ Service Health: HEALTHY"
    else
        echo "❌ Service Health: UNHEALTHY"
    fi

    # Database Health
    if check_database_health; then
        echo "✅ Database Health: HEALTHY"
    else
        echo "❌ Database Health: UNHEALTHY"
    fi

    # Container Health
    if check_container_health; then
        echo "✅ Container Health: HEALTHY"
    else
        echo "❌ Container Health: UNHEALTHY"
    fi

    # Disk Space
    if check_disk_space; then
        echo "✅ Disk Space: HEALTHY"
    else
        echo "⚠️  Disk Space: WARNING"
    fi

    # Memory Usage
    if check_memory_usage; then
        echo "✅ Memory Usage: HEALTHY"
    else
        echo "⚠️  Memory Usage: WARNING"
    fi

    # Performance
    if run_performance_test; then
        echo "✅ Performance: ACCEPTABLE"
    else
        echo "⚠️  Performance: NEEDS ATTENTION"
    fi

    echo ""
    echo "=== END OF REPORT ==="
}

main() {
    log "Starting Youth Connect Uganda User Service health check..."

    generate_report

    log "Health check completed!"
}

# Run main function
main