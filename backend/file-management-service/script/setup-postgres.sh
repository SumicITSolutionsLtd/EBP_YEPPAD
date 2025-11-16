#!/bin/bash
# ═══════════════════════════════════════════════════════════════════════════
# PostgreSQL Database Setup Script for File Management Service
# ═══════════════════════════════════════════════════════════════════════════
# Version: 2.1.0
# Author: Douglas Kings Kato
# Description: Creates database, user, and initializes schema for file service
# ═══════════════════════════════════════════════════════════════════════════

set -e  # Exit on any error

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Script configuration
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ENV_FILE="${SCRIPT_DIR}/.env"

# Default values (will be overridden by .env if present)
DB_HOST=${DB_HOST:-localhost}
DB_PORT=${DB_PORT:-5432}
DB_NAME=${DB_NAME:-epb_file}
DB_USER=${DB_USER:-epb_app_user}
DB_PASSWORD=${DB_PASSWORD:-changeme}
POSTGRES_SUPERUSER=${POSTGRES_SUPERUSER:-postgres}

# =============================================================================
# UTILITY FUNCTIONS
# =============================================================================

print_header() {
    echo -e "${BLUE}═══════════════════════════════════════════════════════════════${NC}"
    echo -e "${BLUE}  $1${NC}"
    echo -e "${BLUE}═══════════════════════════════════════════════════════════════${NC}"
}

print_success() {
    echo -e "${GREEN}✅ $1${NC}"
}

print_error() {
    echo -e "${RED}❌ $1${NC}"
}

print_warning() {
    echo -e "${YELLOW}⚠️  $1${NC}"
}

print_info() {
    echo -e "${BLUE}ℹ️  $1${NC}"
}

# =============================================================================
# LOAD ENVIRONMENT VARIABLES
# =============================================================================

load_env() {
    print_header "Loading Environment Variables"

    if [ -f "$ENV_FILE" ]; then
        print_info "Loading configuration from .env file..."
        export $(cat "$ENV_FILE" | grep -v '^#' | grep -v '^$' | xargs)
        print_success "Environment variables loaded"
    else
        print_warning ".env file not found, using default values"
        print_info "To customize, copy .env.example to .env and edit values"
    fi
}

# =============================================================================
# CHECK PREREQUISITES
# =============================================================================

check_prerequisites() {
    print_header "Checking Prerequisites"

    # Check if psql is installed
    if ! command -v psql &> /dev/null; then
        print_error "psql command not found!"
        print_info "Install PostgreSQL client:"
        print_info "  Ubuntu/Debian: sudo apt-get install postgresql-client"
        print_info "  CentOS/RHEL:   sudo yum install postgresql"
        print_info "  macOS:         brew install postgresql"
        exit 1
    fi
    print_success "PostgreSQL client found: $(psql --version)"

    # Check PostgreSQL connection
    print_info "Testing PostgreSQL connection to ${DB_HOST}:${DB_PORT}..."
    if PGPASSWORD=${POSTGRES_PASSWORD:-} psql -h "$DB_HOST" -p "$DB_PORT" -U "$POSTGRES_SUPERUSER" -d postgres -c '\q' 2>/dev/null; then
        print_success "PostgreSQL connection successful"
    else
        print_error "Cannot connect to PostgreSQL"
        print_info "Make sure PostgreSQL is running and credentials are correct"
        exit 1
    fi
}

# =============================================================================
# CREATE DATABASE
# =============================================================================

create_database() {
    print_header "Creating Database"

    print_info "Database name: ${DB_NAME}"

    # Check if database already exists
    DB_EXISTS=$(PGPASSWORD=${POSTGRES_PASSWORD:-} psql -h "$DB_HOST" -p "$DB_PORT" -U "$POSTGRES_SUPERUSER" -d postgres -tAc "SELECT 1 FROM pg_database WHERE datname='${DB_NAME}'")

    if [ "$DB_EXISTS" = "1" ]; then
        print_warning "Database '${DB_NAME}' already exists"
        read -p "Do you want to drop and recreate it? (yes/no): " CONFIRM
        if [ "$CONFIRM" = "yes" ]; then
            print_info "Dropping existing database..."
            PGPASSWORD=${POSTGRES_PASSWORD:-} psql -h "$DB_HOST" -p "$DB_PORT" -U "$POSTGRES_SUPERUSER" -d postgres -c "DROP DATABASE IF EXISTS ${DB_NAME};"
            print_success "Existing database dropped"
        else
            print_info "Keeping existing database"
            return
        fi
    fi

    # Create database
    print_info "Creating database with UTF-8 encoding..."
    PGPASSWORD=${POSTGRES_PASSWORD:-} psql -h "$DB_HOST" -p "$DB_PORT" -U "$POSTGRES_SUPERUSER" -d postgres <<EOF
CREATE DATABASE ${DB_NAME}
    WITH
    ENCODING = 'UTF8'
    LC_COLLATE = 'en_US.UTF-8'
    LC_CTYPE = 'en_US.UTF-8'
    TEMPLATE = template0
    CONNECTION LIMIT = -1;
EOF

    print_success "Database '${DB_NAME}' created successfully"
}

# =============================================================================
# CREATE USER
# =============================================================================

create_user() {
    print_header "Creating Database User"

    print_info "Username: ${DB_USER}"

    # Check if user already exists
    USER_EXISTS=$(PGPASSWORD=${POSTGRES_PASSWORD:-} psql -h "$DB_HOST" -p "$DB_PORT" -U "$POSTGRES_SUPERUSER" -d postgres -tAc "SELECT 1 FROM pg_roles WHERE rolname='${DB_USER}'")

    if [ "$USER_EXISTS" = "1" ]; then
        print_warning "User '${DB_USER}' already exists"
        print_info "Updating user password..."
        PGPASSWORD=${POSTGRES_PASSWORD:-} psql -h "$DB_HOST" -p "$DB_PORT" -U "$POSTGRES_SUPERUSER" -d postgres -c "ALTER USER ${DB_USER} WITH PASSWORD '${DB_PASSWORD}';"
        print_success "User password updated"
    else
        print_info "Creating new user..."
        PGPASSWORD=${POSTGRES_PASSWORD:-} psql -h "$DB_HOST" -p "$DB_PORT" -U "$POSTGRES_SUPERUSER" -d postgres <<EOF
CREATE USER ${DB_USER} WITH PASSWORD '${DB_PASSWORD}';
EOF
        print_success "User '${DB_USER}' created successfully"
    fi
}

# =============================================================================
# GRANT PERMISSIONS
# =============================================================================

grant_permissions() {
    print_header "Granting Permissions"

    print_info "Granting privileges to ${DB_USER} on database ${DB_NAME}..."

    PGPASSWORD=${POSTGRES_PASSWORD:-} psql -h "$DB_HOST" -p "$DB_PORT" -U "$POSTGRES_SUPERUSER" -d postgres <<EOF
-- Grant database connection privilege
GRANT ALL PRIVILEGES ON DATABASE ${DB_NAME} TO ${DB_USER};

-- Connect to the database
\c ${DB_NAME}

-- Grant schema privileges
GRANT ALL ON SCHEMA public TO ${DB_USER};

-- Grant privileges on all existing tables
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO ${DB_USER};

-- Grant privileges on all existing sequences
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO ${DB_USER};

-- Grant privileges on all existing functions
GRANT ALL PRIVILEGES ON ALL FUNCTIONS IN SCHEMA public TO ${DB_USER};

-- Grant default privileges for future objects
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TABLES TO ${DB_USER};
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON SEQUENCES TO ${DB_USER};
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON FUNCTIONS TO ${DB_USER};
EOF

    print_success "Permissions granted successfully"
}

# =============================================================================
# RUN MIGRATIONS
# =============================================================================

run_migrations() {
    print_header "Running Database Migrations"

    MIGRATION_DIR="${SCRIPT_DIR}/src/main/resources/db/migration"

    if [ ! -d "$MIGRATION_DIR" ]; then
        print_warning "Migration directory not found: ${MIGRATION_DIR}"
        print_info "Skipping migrations"
        return
    fi

    # Find all migration files
    MIGRATION_FILES=$(find "$MIGRATION_DIR" -name "V*.sql" | sort)

    if [ -z "$MIGRATION_FILES" ]; then
        print_warning "No migration files found in ${MIGRATION_DIR}"
        return
    fi

    print_info "Found migration files:"
    echo "$MIGRATION_FILES" | while read -r file; do
        echo "  - $(basename "$file")"
    done

    # Execute each migration
    for migration_file in $MIGRATION_FILES; do
        print_info "Running migration: $(basename "$migration_file")"

        if PGPASSWORD=${DB_PASSWORD} psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d "$DB_NAME" -f "$migration_file"; then
            print_success "Migration completed: $(basename "$migration_file")"
        else
            print_error "Migration failed: $(basename "$migration_file")"
            exit 1
        fi
    done

    print_success "All migrations completed successfully"
}

# =============================================================================
# VERIFY SETUP
# =============================================================================

verify_setup() {
    print_header "Verifying Database Setup"

    # Check database connection
    print_info "Testing connection with application user..."
    if PGPASSWORD=${DB_PASSWORD} psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d "$DB_NAME" -c '\q' 2>/dev/null; then
        print_success "Application user can connect to database"
    else
        print_error "Application user cannot connect to database"
        exit 1
    fi

    # List tables
    print_info "Checking database tables..."
    TABLES=$(PGPASSWORD=${DB_PASSWORD} psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d "$DB_NAME" -tAc "SELECT tablename FROM pg_tables WHERE schemaname='public' ORDER BY tablename")

    if [ -n "$TABLES" ]; then
        print_success "Database tables found:"
        echo "$TABLES" | while read -r table; do
            echo "  - $table"
        done
    else
        print_warning "No tables found (migrations may not have run)"
    fi
}

# =============================================================================
# PRINT CONNECTION INFO
# =============================================================================

print_connection_info() {
    print_header "Database Connection Information"

    cat <<EOF

${GREEN}✅ PostgreSQL database setup complete!${NC}

${BLUE}Connection Details:${NC}
  Host:     ${DB_HOST}
  Port:     ${DB_PORT}
  Database: ${DB_NAME}
  Username: ${DB_USER}
  Password: ${DB_PASSWORD}

${BLUE}JDBC URL:${NC}
  jdbc:postgresql://${DB_HOST}:${DB_PORT}/${DB_NAME}?currentSchema=public&stringtype=unspecified

${BLUE}Connection String:${NC}
  postgresql://${DB_USER}:${DB_PASSWORD}@${DB_HOST}:${DB_PORT}/${DB_NAME}

${BLUE}psql Command:${NC}
  PGPASSWORD='${DB_PASSWORD}' psql -h ${DB_HOST} -p ${DB_PORT} -U ${DB_USER} -d ${DB_NAME}

${YELLOW}⚠️  Security Note:${NC}
  Remember to change the default password in production!
  Never commit credentials to version control.

EOF
}

# =============================================================================
# MAIN EXECUTION
# =============================================================================

main() {
    echo ""
    print_header "PostgreSQL Database Setup for File Management Service"
    echo ""

    load_env
    check_prerequisites
    create_database
    create_user
    grant_permissions
    run_migrations
    verify_setup
    print_connection_info

    print_success "Setup completed successfully!"
}

# Run main function
main