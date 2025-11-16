#!/bin/bash

################################################################################
# Step 2: Verify PostgreSQL Databases
################################################################################

set -e

echo "════════════════════════════════════════════════════════════════"
echo "  STEP 2: Verifying PostgreSQL Databases"
echo "════════════════════════════════════════════════════════════════"

# Load environment variables
if [ -f "../.env" ]; then
    export $(grep -v '^#' ../.env | xargs)
fi

DB_HOST=${DB_HOST:-localhost}
DB_PORT=${DB_PORT:-5432}
DB_USER=${DB_USER:-youthconnect_user}
DB_PASSWORD=${DB_PASSWORD:-YouthConnect2024!}

# Database list
DATABASES=(
    "youthconnect_auth"
    "youthconnect_user"
    "youthconnect_job"
    "youthconnect_opportunity"
    "youthconnect_mentor"
    "youthconnect_content"
    "youthconnect_notification"
    "youthconnect_file"
    "youthconnect_ai"
    "youthconnect_analytics"
    "youthconnect_ussd"
)

echo "🔍 Checking PostgreSQL connection..."
if ! PGPASSWORD=$DB_PASSWORD psql -h $DB_HOST -p $DB_PORT -U $DB_USER -d youthconnect_auth -c "SELECT 1" > /dev/null 2>&1; then
    echo "❌ ERROR: Cannot connect to PostgreSQL"
    echo "   Host: $DB_HOST:$DB_PORT"
    echo "   User: $DB_USER"
    echo ""
    echo "Troubleshooting:"
    echo "  1. Check if PostgreSQL is running: pg_ctl status"
    echo "  2. Verify credentials in .env file"
    echo "  3. Check pg_hba.conf for access permissions"
    exit 1
fi

echo "✅ PostgreSQL connection successful"
echo ""

# Verify each database
echo "📊 Verifying databases..."
echo ""

for db in "${DATABASES[@]}"; do
    if PGPASSWORD=$DB_PASSWORD psql -h $DB_HOST -p $DB_PORT -U $DB_USER -d $db -c "SELECT 1" > /dev/null 2>&1; then
        # Get table count
        TABLE_COUNT=$(PGPASSWORD=$DB_PASSWORD psql -h $DB_HOST -p $DB_PORT -U $DB_USER -d $db -t -c "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema='public'")
        echo "✅ $db - Tables: $TABLE_COUNT"
    else
        echo "❌ $db - NOT ACCESSIBLE"
    fi
done

echo ""
echo "════════════════════════════════════════════════════════════════"
echo "  Database Verification Complete"
echo "════════════════════════════════════════════════════════════════"