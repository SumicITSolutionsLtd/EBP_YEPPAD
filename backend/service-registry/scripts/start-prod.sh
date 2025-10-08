#!/bin/bash

################################################################################
# Service Registry - Production Startup Script
################################################################################
# This script starts the Eureka Server in production mode with:
# - Optimized JVM settings
# - Production profile
# - Health monitoring
# - Logging configuration
################################################################################

set -e

# ============================================
# Configuration
# ============================================
APP_NAME="service-registry"
APP_JAR="${APP_JAR:-/app/service-registry.jar}"
SPRING_PROFILE="${SPRING_PROFILE:-prod}"
SERVER_PORT="${SERVER_PORT:-8761}"
LOG_DIR="${LOG_DIR:-/var/log/youth-connect}"
PID_FILE="${PID_FILE:-/var/run/${APP_NAME}.pid}"

# JVM Memory Settings (adjust based on available resources)
XMS="${XMS:-512m}"
XMX="${XMX:-1g}"
MAX_METASPACE="${MAX_METASPACE:-256m}"

# Colors
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m'

# ============================================
# Functions
# ============================================

log_info() {
    echo -e "${GREEN}[$(date +'%Y-%m-%d %H:%M:%S')]${NC} $1"
}

log_error() {
    echo -e "${RED}[$(date +'%Y-%m-%d %H:%M:%S')] ERROR:${NC} $1"
}

check_running() {
    if [ -f "$PID_FILE" ]; then
        PID=$(cat "$PID_FILE")
        if ps -p "$PID" > /dev/null 2>&1; then
            log_error "Service is already running (PID: $PID)"
            exit 1
        else
            log_info "Removing stale PID file"
            rm -f "$PID_FILE"
        fi
    fi
}

check_requirements() {
    log_info "Checking production requirements..."

    # Check Java
    if ! command -v java &> /dev/null; then
        log_error "Java is not installed"
        exit 1
    fi

    # Check JAR file
    if [ ! -f "$APP_JAR" ]; then
        log_error "Application JAR not found: $APP_JAR"
        exit 1
    fi

    # Check/Create log directory
    if [ ! -d "$LOG_DIR" ]; then
        log_info "Creating log directory: $LOG_DIR"
        mkdir -p "$LOG_DIR"
    fi

    # Check environment variables
    if [ -z "$EUREKA_USERNAME" ] || [ -z "$EUREKA_PASSWORD" ]; then
        log_error "EUREKA_USERNAME and EUREKA_PASSWORD must be set"
        exit 1
    fi

    log_info "All requirements satisfied ✓"
}

configure_jvm() {
    # Production-optimized JVM settings
    JAVA_OPTS="-server \
        -Xms${XMS} \
        -Xmx${XMX} \
        -XX:MaxMetaspaceSize=${MAX_METASPACE} \
        -XX:MetaspaceSize=128m \
        -XX:+UseG1GC \
        -XX:MaxGCPauseMillis=100 \
        -XX:+ParallelRefProcEnabled \
        -XX:+UseStringDeduplication \
        -XX:+HeapDumpOnOutOfMemoryError \
        -XX:HeapDumpPath=${LOG_DIR}/heap_dump.hprof \
        -XX:ErrorFile=${LOG_DIR}/hs_err_pid%p.log \
        -XX:+PrintGCDetails \
        -XX:+PrintGCDateStamps \
        -Xloggc:${LOG_DIR}/gc.log \
        -XX:+UseGCLogFileRotation \
        -XX:NumberOfGCLogFiles=5 \
        -XX:GCLogFileSize=10M \
        -Djava.security.egd=file:/dev/./urandom \
        -Dfile.encoding=UTF-8 \
        -Duser.timezone=Africa/Kampala \
        -Dspring.profiles.active=${SPRING_PROFILE} \
        -Dserver.port=${SERVER_PORT}"

    # Add custom JVM opts if provided
    if [ -n "$CUSTOM_JAVA_OPTS" ]; then
        JAVA_OPTS="$JAVA_OPTS $CUSTOM_JAVA_OPTS"
    fi

    export JAVA_OPTS
}

start_application() {
    log_info "Starting Service Registry in production mode..."
    log_info "Profile: $SPRING_PROFILE"
    log_info "Port: $SERVER_PORT"
    log_info "Memory: $XMS to $XMX"
    log_info "Log Directory: $LOG_DIR"

    # Start the application in background
    nohup java $JAVA_OPTS -jar "$APP_JAR" \
        > "${LOG_DIR}/application.log" 2>&1 &

    APP_PID=$!
    echo $APP_PID > "$PID_FILE"

    log_info "Service started with PID: $APP_PID"

    # Wait a few seconds and check if still running
    sleep 5

    if ps -p $APP_PID > /dev/null 2>&1; then
        log_info "Service is running successfully ✓"
        log_info "Dashboard: http://localhost:$SERVER_PORT"
        log_info "Health: http://localhost:$SERVER_PORT/actuator/health"

        # Run health check
        sleep 10
        if command -v curl &> /dev/null; then
            if curl -f -s "http://localhost:$SERVER_PORT/actuator/health" > /dev/null; then
                log_info "Health check passed ✓"
            else
                log_error "Health check failed. Check logs: ${LOG_DIR}/application.log"
            fi
        fi
    else
        log_error "Service failed to start. Check logs: ${LOG_DIR}/application.log"
        rm -f "$PID_FILE"
        exit 1
    fi
}

# ============================================
# Main Execution
# ============================================

main() {
    echo "========================================="
    echo "Service Registry - Production Startup"
    echo "========================================="

    check_running
    check_requirements
    configure_jvm
    start_application

    echo "========================================="
    log_info "Service Registry started successfully!"
    echo "========================================="
}

main "$@"