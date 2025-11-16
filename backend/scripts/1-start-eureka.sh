#!/bin/bash

################################################################################
# Step 1: Start Eureka Service Registry
################################################################################

set -e

echo "════════════════════════════════════════════════════════════════"
echo "  STEP 1: Starting Eureka Service Registry"
echo "════════════════════════════════════════════════════════════════"

cd "$(dirname "$0")/../service-registry"

# Check if Java is installed
if ! command -v java &> /dev/null; then
    echo "❌ ERROR: Java not found. Please install Java 17+"
    exit 1
fi

# Build if JAR doesn't exist
if [ ! -f "target/service-registry.jar" ]; then
    echo "📦 Building service-registry..."
    mvn clean package -DskipTests
fi

# Start Eureka
echo "🚀 Starting Eureka Server on port 8761..."
java -jar target/service-registry.jar &

EUREKA_PID=$!
echo "✅ Eureka started with PID: $EUREKA_PID"

# Wait for Eureka to be ready
echo "⏳ Waiting for Eureka to be ready..."
for i in {1..30}; do
    if curl -s http://localhost:8761/actuator/health > /dev/null 2>&1; then
        echo "✅ Eureka is ready!"
        echo ""
        echo "📊 Eureka Dashboard: http://localhost:8761"
        echo "🏥 Health Check: http://localhost:8761/actuator/health"
        echo ""
        exit 0
    fi
    echo "   Attempt $i/30..."
    sleep 2
done

echo "❌ ERROR: Eureka failed to start within 60 seconds"
kill $EUREKA_PID 2>/dev/null || true
exit 1