#!/bin/bash

################################################################################
# Step 5: Start All Microservices
################################################################################

set -e

echo "════════════════════════════════════════════════════════════════"
echo "  STEP 5: Starting All Microservices"
echo "════════════════════════════════════════════════════════════════"

BACKEND_DIR="$(dirname "$0")/.."
LOG_DIR="$BACKEND_DIR/logs"
mkdir -p "$LOG_DIR"

# Service definitions: name:port:path:database
SERVICES=(
    "service-registry:8761:service-registry:"
    "auth-service:8083:auth-service:youthconnect_auth"
    "user-service:8081:user-service:youthconnect_user"
    "job-services:8000:job-services:youthconnect_job"
    "notification-service:7077:notification-service:youthconnect_notification"
    "file-management-service:8089:file-management-service:youthconnect_file"
)

# Function to start a service
start_service() {
    local name=$1
    local port=$2
    local path=$3
    local database=$4

    echo ""
    echo "🚀 Starting $name..."

    cd "$BACKEND_DIR/$path"

    # Build if needed
    if [ ! -f "target/${name}.jar" ]; then
        echo "   📦 Building $name..."
        mvn clean package -DskipTests > /dev/null 2>&1
    fi

    # Start service
    if [ -n "$database" ]; then
        java -jar "target/${name}.jar" \
            -Dspring.profiles.active=dev \
            -Dspring.datasource.url=jdbc:postgresql://localhost:5432/$database \
            -Dspring.datasource.username=youthconnect_user \
            -Dspring.datasource.password=YouthConnect2024! \
            > "$LOG_DIR/${name}.log" 2>&1 &
    else
        java -jar "target/${name}.jar" \
            -Dspring.profiles.active=dev \
            > "$LOG_DIR/${name}.log" 2>&1 &
    fi

    local pid=$!
    echo $pid > "$LOG_DIR/${name}.pid"
    echo "   ✅ Started with PID: $pid (logs: $LOG_DIR/${name}.log)"

    # Wait for service to be ready
    local max_wait=60
    local count=0
    while [ $count -lt $max_wait ]; do
        if curl -s "http://localhost:$port/actuator/health" > /dev/null 2>&1; then
            echo "   ✅ $name is ready on port $port"
            return 0
        fi
        sleep 2
        count=$((count + 2))
    done

    echo "   ⚠️  $name did not start within ${max_wait}s (check logs)"
}

# Start services sequentially
for service in "${SERVICES[@]}"; do
    IFS=':' read -r name port path database <<< "$service"
    start_service "$name" "$port" "$path" "$database"
    sleep 5  # Small delay between services
done

echo ""
echo "════════════════════════════════════════════════════════════════"
echo "  All Services Started"
echo "════════════════════════════════════════════════════════════════"
echo ""
echo "📊 Service Status:"
echo "   Eureka Dashboard: http://localhost:8761"
echo "   Auth Service: http://localhost:8083/api/auth"
echo "   User Service: http://localhost:8081"
echo "   Job Service: http://localhost:8000"
echo "   Notification Service: http://localhost:7077"
echo "   File Management: http://localhost:8089"
echo ""
echo "📝 Logs location: $LOG_DIR"
echo "🛑 To stop all: ./scripts/stop-all-services.sh"
echo ""