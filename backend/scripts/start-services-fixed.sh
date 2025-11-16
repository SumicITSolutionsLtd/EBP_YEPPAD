#!/bin/bash

################################################################################
# FIXED: Start All Services (Corrected Paths & Credentials)
################################################################################

set -e

echo "════════════════════════════════════════════════════════════════"
echo "  Starting Youth Connect Platform Services"
echo "════════════════════════════════════════════════════════════════"

# Navigate to backend directory
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
BACKEND_DIR="$(dirname "$SCRIPT_DIR")"
LOG_DIR="$BACKEND_DIR/logs"

mkdir -p "$LOG_DIR"

echo "Backend directory: $BACKEND_DIR"
echo "Log directory: $LOG_DIR"
echo ""

# Kill any existing Java processes
echo "🛑 Stopping existing services..."
taskkill //F //IM java.exe 2>/dev/null || true
sleep 3

# Function to start a service
start_service() {
    local name=$1
    local port=$2
    local jar_path=$3
    local db_name=$4

    echo ""
    echo "🚀 Starting $name on port $port..."

    cd "$BACKEND_DIR"

    if [ ! -f "$jar_path" ]; then
        echo "   ❌ ERROR: JAR not found at $jar_path"
        echo "   Building service..."

        service_dir=$(dirname "$jar_path")
        cd "$service_dir/.."
        mvn clean package -DskipTests
        cd "$BACKEND_DIR"
    fi

    if [ -n "$db_name" ]; then
        # Service with database
        java -Dspring.profiles.active=dev \
             -Dspring.datasource.url="jdbc:postgresql://localhost:5432/$db_name" \
             -Dspring.datasource.username=youthconnect_user \
             -Dspring.datasource.password=YouthConnect2024! \
             -jar "$jar_path" \
             > "$LOG_DIR/${name}.log" 2>&1 &
    else
        # Service without database (Eureka)
        java -Dspring.profiles.active=dev \
             -jar "$jar_path" \
             > "$LOG_DIR/${name}.log" 2>&1 &
    fi

    local pid=$!
    echo $pid > "$LOG_DIR/${name}.pid"
    echo "   ✅ Started with PID: $pid"

    # Wait for service
    local max_wait=60
    local count=0
    while [ $count -lt $max_wait ]; do
        if curl -s "http://localhost:$port/actuator/health" > /dev/null 2>&1; then
            echo "   ✅ $name is ready!"
            return 0
        fi
        sleep 2
        count=$((count + 2))
    done

    echo "   ⚠️  $name did not start within ${max_wait}s"
    echo "   Check logs: $LOG_DIR/${name}.log"
}

# Start services in order
start_service "service-registry" 8761 "service-registry/target/service-registry.jar" ""
sleep 10

start_service "auth-service" 8083 "auth-service/target/auth-service.jar" "youthconnect_auth"
sleep 5

start_service "user-service" 8081 "user-service/target/user-service.jar" "youthconnect_user"
sleep 5

start_service "job-service" 8000 "job-services/target/job-services.jar" "youthconnect_job"
sleep 5

start_service "notification-service" 7077 "notification-service/target/notification-service.jar" "youthconnect_notification"
sleep 5

start_service "file-service" 8089 "file-management-service/target/file-management-service.jar" "youthconnect_file"

echo ""
echo "════════════════════════════════════════════════════════════════"
echo "  All Services Started"
echo "════════════════════════════════════════════════════════════════"
echo ""
echo "📊 Service Registry: http://localhost:8761"
echo "🔐 Auth Service: http://localhost:8083/api/auth"
echo "👤 User Service: http://localhost:8081"
echo "💼 Job Service: http://localhost:8000"
echo "📧 Notification Service: http://localhost:7077"
echo "📁 File Service: http://localhost:8089"
echo ""
echo "📝 Logs: $LOG_DIR"
echo ""