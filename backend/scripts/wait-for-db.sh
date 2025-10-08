#!/bin/bash
# =================================================================================
# Database Connection Wait Script for Youth Connect Uganda User Service
# Waits for MySQL database to be ready before starting the application
# =================================================================================

set -e

# Database connection parameters from environment variables
DB_HOST=${DB_HOST:-localhost}
DB_PORT=${DB_PORT:-3307}
DB_USER=${DB_USER:-root}
DB_PASSWORD=${DB_PASSWORD:-Douglas20!}
DB_NAME=${DB_NAME:-youth_connect_db}

# Maximum wait time in seconds (5 minutes)
MAX_WAIT=300
WAIT_INTERVAL=5
ELAPSED=0

echo "Waiting for MySQL database to be ready..."
echo "Host: $DB_HOST, Port: $DB_PORT, Database: $DB_NAME"

# Function to test database connection
test_db_connection() {
    mysql -h"$DB_HOST" -P"$DB_PORT" -u"$DB_USER" -p"$DB_PASSWORD" -e "SELECT 1;" "$DB_NAME" > /dev/null 2>&1
    return $?
}

# Wait for database to be ready
while [ $ELAPSED -lt $MAX_WAIT ]; do
    if test_db_connection; then
        echo "Database is ready after ${ELAPSED} seconds!"
        break
    else
        echo "Database not ready yet... waiting ${WAIT_INTERVAL} seconds (${ELAPSED}/${MAX_WAIT})"
        sleep $WAIT_INTERVAL
        ELAPSED=$((ELAPSED + WAIT_INTERVAL))
    fi
done

# Check if we timed out
if [ $ELAPSED -ge $MAX_WAIT ]; then
    echo "ERROR: Database connection timeout after ${MAX_WAIT} seconds"
    echo "Please check your database configuration:"
    echo "  - Host: $DB_HOST"
    echo "  - Port: $DB_PORT"
    echo "  - Username: $DB_USER"
    echo "  - Database: $DB_NAME"
    exit 1
fi

echo "Database connection successful! Starting application..."

# Execute the application with all provided arguments
exec "$@"