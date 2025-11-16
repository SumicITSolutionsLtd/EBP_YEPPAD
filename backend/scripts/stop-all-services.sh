#!/bin/bash

################################################################################
# Stop All Microservices
################################################################################

set -e

echo "════════════════════════════════════════════════════════════════"
echo "  Stopping All Microservices"
echo "════════════════════════════════════════════════════════════════"

LOG_DIR="$(dirname "$0")/../logs"

# Find all PID files
if [ -d "$LOG_DIR" ]; then
    for pidfile in "$LOG_DIR"/*.pid; do
        if [ -f "$pidfile" ]; then
            pid=$(cat "$pidfile")
            service=$(basename "$pidfile" .pid)

            if ps -p $pid > /dev/null 2>&1; then
                echo "🛑 Stopping $service (PID: $pid)..."
                kill $pid

                # Wait for graceful shutdown
                for i in {1..10}; do
                    if ! ps -p $pid > /dev/null 2>&1; then
                        echo "   ✅ $service stopped"
                        break
                    fi
                    sleep 1
                done

                # Force kill if still running
                if ps -p $pid > /dev/null 2>&1; then
                    echo "   ⚠️  Force killing $service..."
                    kill -9 $pid
                fi
            else
                echo "⚠️  $service (PID: $pid) not running"
            fi

            rm "$pidfile"
        fi
    done
else
    echo "⚠️  No PID files found. Services may not be running."
fi

# Kill any remaining Java processes
echo ""
echo "🔍 Checking for remaining Java processes..."
JAVA_PIDS=$(ps aux | grep "[j]ava.*jar" | awk '{print $2}')

if [ -n "$JAVA_PIDS" ]; then
    echo "   Found running Java processes: $JAVA_PIDS"
    echo "   Kill them? (y/n)"
    read -r response
    if [[ "$response" =~ ^[Yy]$ ]]; then
        echo $JAVA_PIDS | xargs kill -9
        echo "   ✅ Killed remaining processes"
    fi
fi

echo ""
echo "════════════════════════════════════════════════════════════════"
echo "  All Services Stopped"
echo "════════════════════════════════════════════════════════════════"