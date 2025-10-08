#!/bin/bash

# Youth Connect Uganda - Database Migration Script
# Usage: ./scripts/db-migration.sh [environment] [action]

set -e

# Colors
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m'

ENVIRONMENT=${1:-"development"}
ACTION=${2:-"migrate"}
DB_HOST="localhost"
DB_PORT="3307"
DB_NAME="youth_connect_db"
DB_USER="root"
DB_PASS="Douglas20!"

log() {
    echo -e "${GREEN}[$(date +'%Y-%m-%d %H:%M:%S')]${NC} $1"
}

error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

warn() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

check_mysql_client() {
    if ! command -v mysql &> /dev/null; then
        error "MySQL client is not installed. Please install mysql-client."
        exit 1
    fi
    log "MySQL client is available"
}

check_database_connection() {
    log "Checking database connection..."

    if mysql -h $DB_HOST -P $DB_PORT -u $DB_USER -p$DB_PASS -e "SELECT 1;" &> /dev/null; then
        log "Database connection: SUCCESS"
        return 0
    else
        error "Database connection: FAILED"
        return 1
    fi
}

create_database() {
    log "Creating database if not exists..."

    mysql -h $DB_HOST -P $DB_PORT -u $DB_USER -p$DB_PASS -e "CREATE DATABASE IF NOT EXISTS $DB_NAME CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"

    log "Database created/verified successfully"
}

run_migrations() {
    log "Running database migrations..."

    # This would typically use Flyway or similar tool
    # For now, we'll manually apply SQL files

    local migration_dir="./src/main/resources/db/migration"

    if [ ! -d "$migration_dir" ]; then
        error "Migration directory not found: $migration_dir"
        exit 1
    fi

    # Get all migration files sorted
    local migration_files
    migration_files=$(find "$migration_dir" -name "V*.sql" | sort)

    for file in $migration_files; do
        log "Applying migration: $(basename "$file")"

        # Check if migration has already been applied
        local migration_name
        migration_name=$(basename "$file" .sql)

        if ! check_migration_applied "$migration_name"; then
            # Apply migration
            mysql -h $DB_HOST -P $DB_PORT -u $DB_USER -p$DB_PASS $DB_NAME < "$file"

            if [ $? -eq 0 ]; then
                # Record migration
                record_migration "$migration_name"
                log "Migration applied successfully: $migration_name"
            else
                error "Migration failed: $migration_name"
                exit 1
            fi
        else
            log "Migration already applied: $migration_name"
        fi
    done
}

check_migration_applied() {
    local migration_name=$1

    # Create migrations table if not exists
    mysql -h $DB_HOST -P $DB_PORT -u $DB_USER -p$DB_PASS $DB_NAME -e "
        CREATE TABLE IF NOT EXISTS schema_migrations (
            version VARCHAR(50) PRIMARY KEY,
            description VARCHAR(255),
            applied_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
        );
    " 2>/dev/null

    # Check if migration is recorded
    local result
    result=$(mysql -h $DB_HOST -P $DB_PORT -u $DB_USER -p$DB_PASS $DB_NAME -sN -e "
        SELECT COUNT(*) FROM schema_migrations WHERE version = '$migration_name';
    " 2>/dev/null)

    [ "$result" -eq 1 ]
}

record_migration() {
    local migration_name=$1
    local description

    # Extract description from filename
    description=$(echo "$migration_name" | sed 's/V[0-9]*__//' | tr '_' ' ')

    mysql -h $DB_HOST -P $DB_PORT -u $DB_USER -p$DB_PASS $DB_NAME -e "
        INSERT INTO schema_migrations (version, description)
        VALUES ('$migration_name', '$description');
    " 2>/dev/null
}

rollback_migration() {
    local migration_name=$1

    warn "Rollback functionality not fully implemented for manual migrations"
    warn "Please restore from backup or manually reverse changes for: $migration_name"
}

show_migration_status() {
    log "Current migration status:"

    mysql -h $DB_HOST -P $DB_PORT -u $DB_USER -p$DB_PASS $DB_NAME -e "
        SELECT
            version as 'Migration',
            description as 'Description',
            applied_at as 'Applied At'
        FROM schema_migrations
        ORDER BY applied_at;
    " 2>/dev/null || warn "No migration history found"
}

validate_schema() {
    log "Validating database schema..."

    # Check required tables exist
    local required_tables=("users" "youth_profiles" "mentor_profiles" "audit_trail")

    for table in "${required_tables[@]}"; do
        local table_exists
        table_exists=$(mysql -h $DB_HOST -P $DB_PORT -u $DB_USER -p$DB_PASS $DB_NAME -sN -e "
            SELECT COUNT(*) FROM information_schema.tables
            WHERE table_schema = '$DB_NAME' AND table_name = '$table';
        " 2>/dev/null)

        if [ "$table_exists" -eq 1 ]; then
            log "✅ Table exists: $table"
        else
            error "❌ Table missing: $table"
            return 1
        fi
    done

    log "Schema validation completed successfully"
}

create_backup() {
    local backup_dir="./backups"
    local timestamp
    timestamp=$(date +'%Y%m%d_%H%M%S')
    local backup_file="$backup_dir/backup_${DB_NAME}_${timestamp}.sql"

    log "Creating database backup..."

    mkdir -p "$backup_dir"

    if mysqldump -h $DB_HOST -P $DB_PORT -u $DB_USER -p$DB_PASS $DB_NAME > "$backup_file"; then
        log "Backup created successfully: $backup_file"

        # Compress backup
        gzip "$backup_file"
        log "Backup compressed: ${backup_file}.gz"
    else
        error "Backup failed"
        exit 1
    fi
}

main() {
    log "Starting Youth Connect Uganda database migration..."
    log "Environment: $ENVIRONMENT"
    log "Action: $ACTION"

    check_mysql_client

    case $ACTION in
        "migrate")
            check_database_connection
            create_database
            create_backup
            run_migrations
            validate_schema
            show_migration_status
            ;;
        "status")
            check_database_connection
            show_migration_status
            ;;
        "validate")
            check_database_connection
            validate_schema
            ;;
        "backup")
            create_backup
            ;;
        *)
            error "Unknown action: $ACTION"
            echo "Usage: $0 [environment] [action]"
            echo "Actions: migrate, status, validate, backup"
            exit 1
            ;;
    esac

    log "Database migration process completed!"
}

# Run main function
main