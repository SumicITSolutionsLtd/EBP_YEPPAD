# Entrepreneurship Booster Platform - PostgreSQL Database Setup Guide

## 📋 Table of Contents
- [Prerequisites](#prerequisites)
- [Quick Start](#quick-start)
- [Detailed Setup](#detailed-setup)
- [Migration File Structure](#migration-file-structure)
- [Verification](#verification)
- [Default Users](#default-users)
- [Database Maintenance](#database-maintenance)
- [Troubleshooting](#troubleshooting)
- [Production Deployment](#production-deployment)

## Prerequisites

- **PostgreSQL 12+** installed and running
- **Java 21** (OpenJDK or Oracle)
- **Maven 3.8+**
- **pgAdmin** or **psql** command-line tool
- At least **2GB** free disk space

## Quick Start

### 1. Create Database

```bash
# Connect to PostgreSQL
psql -U postgres

# Create database
CREATE DATABASE youth_connect_db;

# Grant permissions
GRANT ALL PRIVILEGES ON DATABASE youth_connect_db TO postgres;

# Exit
\q
```

### 2. Configure Application

Your `application.yml` is already configured for PostgreSQL. Just update the password if needed:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/youth_connect_db?currentSchema=public
    username: postgres
    password: YOUR_PASSWORD_HERE  # Change this!
```

### 3. Run Application

```bash
cd backend/user-service
mvn clean install
mvn spring-boot:run
```

That's it! Flyway will automatically:
- ✅ Create all tables
- ✅ Add indexes and constraints
- ✅ Insert seed data
- ✅ Set up views and functions

## Detailed Setup

### Step 1: Install PostgreSQL

#### Windows
```bash
# Download from https://www.postgresql.org/download/windows/
# Or use Chocolatey
choco install postgresql12
```

#### Mac
```bash
brew install postgresql@12
brew services start postgresql@12
```

#### Ubuntu/Debian
```bash
sudo apt update
sudo apt install postgresql postgresql-contrib
sudo systemctl start postgresql
```

### Step 2: Create Database & User

```sql
-- Connect as postgres superuser
psql -U postgres

-- Create database
CREATE DATABASE youth_connect_db
    WITH 
    OWNER = postgres
    ENCODING = 'UTF8'
    LC_COLLATE = 'en_US.UTF-8'
    LC_CTYPE = 'en_US.UTF-8'
    TABLESPACE = pg_default
    CONNECTION LIMIT = -1;

-- Connect to the database
\c youth_connect_db

-- Enable required extensions
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pg_trgm";
CREATE EXTENSION IF NOT EXISTS "btree_gin";

-- Verify extensions
\dx

-- Exit
\q
```

### Step 3: Project Structure

Create the following structure in your project:

```
backend/user-service/
├── src/main/resources/
│   ├── db/migration/
│   │   ├── V1__Initial_schema.sql
│   │   └── V2__Maintenance_functions.sql
│   ├── application.yml
│   └── logback-spring.xml
├── pom.xml
└── README.md
```

### Step 4: Place Migration Files

Copy the SQL files to `src/main/resources/db/migration/`:

1. **V1__Initial_schema.sql** - Contains:
    - Custom ENUM types
    - All tables with proper constraints
    - Indexes for performance
    - Views for analytics
    - Initial seed data
    - Triggers for auto-updates

2. **V2__Maintenance_functions.sql** - Contains:
    - Cleanup procedures
    - Statistics functions
    - User management functions
    - Interest management functions
    - Utility functions

### Step 5: Configure pom.xml

Ensure you have the PostgreSQL driver:

```xml
<dependencies>
    <!-- PostgreSQL Driver -->
    <dependency>
        <groupId>org.postgresql</groupId>
        <artifactId>postgresql</artifactId>
        <scope>runtime</scope>
    </dependency>
    
    <!-- Flyway for migrations -->
    <dependency>
        <groupId>org.flywaydb</groupId>
        <artifactId>flyway-core</artifactId>
    </dependency>
</dependencies>
```

### Step 6: Run the Application

```bash
# Clean and compile
mvn clean compile

# Run tests (optional)
mvn test

# Run the application
mvn spring-boot:run

# Or with specific profile
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

## Migration File Structure

### V1__Initial_schema.sql Features

#### Custom Types (ENUMs)
- `user_role` - YOUTH, NGO, FUNDER, SERVICE_PROVIDER, MENTOR, ADMIN, etc.
- `opportunity_type` - GRANT, LOAN, JOB, TRAINING, SKILL_MARKET
- `application_status` - PENDING, UNDER_REVIEW, APPROVED, REJECTED
- And 12 more custom types

#### Core Tables (25+ tables)
- **Users & Authentication**: users, refresh_tokens, password_reset_tokens
- **Profiles**: youth_profiles, mentor_profiles, ngo_profiles, etc.
- **Opportunities**: opportunities, applications
- **Mentorship**: mentorship_sessions, mentor_availability, reviews
- **Content**: posts, comments, learning_modules
- **Analytics**: user_activity_logs, user_interests
- **System**: file_records, notification_logs, audit_trail, ussd_sessions

#### Features
- ✅ Audit fields (created_by, updated_by, created_at, updated_at)
- ✅ Soft delete support (is_deleted, deleted_at)
- ✅ Automatic timestamp updates via triggers
- ✅ Performance indexes on all foreign keys
- ✅ Full-text search indexes
- ✅ JSONB for flexible data storage
- ✅ Comprehensive constraints and validations

### V2__Maintenance_functions.sql Features

#### Cleanup Functions
- `sp_clean_expired_tokens()` - Removes expired tokens and sessions
- `archive_old_audit_logs(days)` - Archives old audit records
- `archive_old_activity_logs(days)` - Archives activity logs

#### Statistics Functions
- `get_database_statistics()` - Table sizes and row counts
- `get_user_registration_stats(days)` - Registration trends
- `get_active_users_count()` - User activity metrics
- `get_opportunity_stats()` - Opportunity analytics
- `get_mentorship_stats(days)` - Mentorship statistics

#### User Management
- `soft_delete_user(user_id, deleted_by)` - Soft delete with cascade
- `restore_deleted_user(user_id, restored_by)` - Restore deleted user
- `bulk_upsert_user_interests(...)` - Bulk interest management

#### Utility Functions
- `is_valid_ugandan_phone(phone)` - Phone validation
- `format_ugandan_phone(phone)` - Phone formatting
- `get_user_display_name(user_id)` - Get display name

## Verification

### Check Tables Created

```sql
-- Connect to database
psql -U postgres -d youth_connect_db

-- List all tables
\dt

-- Should show 25+ tables including:
-- users, youth_profiles, mentor_profiles, opportunities, applications, etc.

-- Check table details
\d users
\d youth_profiles
```

### Verify Sample Data

```sql
-- Check users
SELECT user_id, email, role, is_active, created_at 
FROM users 
ORDER BY created_at;

-- Check opportunities
SELECT opportunity_id, title, opportunity_type, status, funding_amount 
FROM opportunities;

-- Check learning modules
SELECT module_id, title, content_type, is_active 
FROM learning_modules;
```

### Test Functions

```sql
-- Get database statistics
SELECT * FROM get_database_statistics();

-- Get user counts
SELECT * FROM get_active_users_count();

-- Get user registration stats (last 30 days)
SELECT * FROM get_user_registration_stats(30);

-- Clean expired tokens
SELECT * FROM sp_clean_expired_tokens();
```

### Verify Views

```sql
-- List all views
\dv

-- Query views
SELECT * FROM user_complete_profiles LIMIT 10;
SELECT * FROM active_opportunities_view;
SELECT * FROM v_user_engagement_metrics LIMIT 10;
```

## Default Users

The system comes with pre-configured test users:

### 1. Admin User
```
Email:    admin@youthconnect.ug
Phone:    +256700000001
Password: Admin123!
Role:     ADMIN
Status:   Active, Email Verified, Phone Verified
```

### 2. Youth User (Sample)
```
Email:    damienpapers3@gmail.com
Phone:    +256701430234
Password: Youth123!
Role:     YOUTH
Name:     Damien Papers
Status:   Active, Email Verified
```

**⚠️ CRITICAL SECURITY**:
1. Change these passwords immediately after deployment!
2. Never use these credentials in production
3. Generate strong random passwords for production

## Database Maintenance

### Daily Maintenance (Automated)

Run cleanup function daily:

```sql
-- Clean expired tokens and sessions
SELECT * FROM sp_clean_expired_tokens();
```

### Weekly Maintenance

```sql
-- Update database statistics
ANALYZE;

-- Vacuum to reclaim space
VACUUM ANALYZE;

-- Check database size
SELECT 
    pg_size_pretty(pg_database_size('youth_connect_db')) as db_size;
```

### Monthly Maintenance

```sql
-- Archive old audit logs (keep 90 days)
SELECT * FROM archive_old_audit_logs(90);

-- Archive old activity logs (keep 180 days)
SELECT * FROM archive_old_activity_logs(180);

-- Reindex if needed
REINDEX DATABASE youth_connect_db;
```

### Performance Monitoring

```sql
-- Check slow queries
SELECT 
    query,
    calls,
    total_exec_time,
    mean_exec_time,
    max_exec_time
FROM pg_stat_statements
ORDER BY mean_exec_time DESC
LIMIT 10;

-- Check table bloat
SELECT 
    schemaname,
    tablename,
    pg_size_pretty(pg_total_relation_size(schemaname||'.'||tablename)) AS size,
    n_tup_ins as inserts,
    n_tup_upd as updates,
    n_tup_del as deletes
FROM pg_stat_user_tables
ORDER BY pg_total_relation_size(schemaname||'.'||tablename) DESC
LIMIT 10;
```

## Troubleshooting

### Issue: Cannot connect to PostgreSQL

```bash
# Check if PostgreSQL is running
sudo systemctl status postgresql  # Linux
brew services list                # Mac
sc query postgresql-x64-12        # Windows

# Check connection
psql -U postgres -h localhost -p 5432

# Check pg_hba.conf for authentication settings
# Linux: /etc/postgresql/12/main/pg_hba.conf
# Mac: /usr/local/var/postgres/pg_hba.conf
```

### Issue: Flyway validation error

```sql
-- Check Flyway history
SELECT * FROM flyway_schema_history ORDER BY installed_rank;

-- Repair Flyway if needed (USE WITH CAUTION!)
-- In application.yml, temporarily set:
spring:
  flyway:
    repair: true
```

### Issue: Permission denied

```sql
-- Grant all privileges
GRANT ALL PRIVILEGES ON DATABASE youth_connect_db TO postgres;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO postgres;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO postgres;
GRANT ALL PRIVILEGES ON ALL FUNCTIONS IN SCHEMA public TO postgres;
```

### Issue: Out of disk space

```sql
-- Check database size
SELECT pg_size_pretty(pg_database_size('youth_connect_db'));

-- Find largest tables
SELECT 
    schemaname || '.' || tablename AS table_name,
    pg_size_pretty(pg_total_relation_size(schemaname||'.'||tablename)) AS size
FROM pg_stat_user_tables
ORDER BY pg_total_relation_size(schemaname||'.'||tablename) DESC
LIMIT 10;

-- Archive old data
SELECT * FROM archive_old_audit_logs(60);
SELECT * FROM archive_old_activity_logs(90);

-- Vacuum full (requires downtime)
VACUUM FULL;
```

### Issue: Slow queries

```sql
-- Enable pg_stat_statements extension
CREATE EXTENSION IF NOT EXISTS pg_stat_statements;

-- Check slow queries
SELECT 
    substring(query, 1, 100) as short_query,
    calls,
    round(total_exec_time::numeric, 2) as total_time,
    round(mean_exec_time::numeric, 2) as mean_time,
    round(max_exec_time::numeric, 2) as max_time
FROM pg_stat_statements
WHERE mean_exec_time > 100  -- queries taking > 100ms
ORDER BY mean_exec_time DESC
LIMIT 20;

-- Add missing indexes if needed
-- Check the explain plan for slow queries
EXPLAIN ANALYZE SELECT * FROM your_slow_query;
```

## Production Deployment

### 1. Environment Variables

Set these environment variables in production:

```bash
# Database
export DB_HOST=your-db-host.amazonaws.com
export DB_PORT=5432
export DB_NAME=youth_connect_prod_db
export DB_USER=youthconnect_user
export DB_PASSWORD=STRONG_RANDOM_PASSWORD_HERE

# Application
export SERVER_PORT=8081
export ENVIRONMENT=production

# Security
export JWT_SECRET=GENERATE_STRONG_256_BIT_SECRET_KEY
export INTERNAL_API_KEY=GENERATE_STRONG_RANDOM_KEY

# Eureka
export EUREKA_ENABLED=true
export EUREKA_URL=http://eureka-server:8761/eureka/

# File Storage
export APP_UPLOAD_DIR=/var/youthconnect/uploads
```

### 2. Database Configuration

```sql
-- Create production database with proper settings
CREATE DATABASE youth_connect_prod_db
    WITH 
    OWNER = youthconnect_user
    ENCODING = 'UTF8'
    LC_COLLATE = 'en_US.UTF-8'
    LC_CTYPE = 'en_US.UTF-8'
    TABLESPACE = pg_default
    CONNECTION LIMIT = 100;

-- Enable SSL
ALTER DATABASE youth_connect_prod_db 
SET ssl = on;

-- Create dedicated user with limited privileges
CREATE USER youthconnect_user WITH PASSWORD 'STRONG_PASSWORD';
GRANT ALL PRIVILEGES ON DATABASE youth_connect_prod_db TO youthconnect_user;
```

### 3. SSL Configuration

Update `application-prod.yml`:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://${DB_HOST}:5432/${DB_NAME}?ssl=true&sslmode=require
```

### 4. Backup Strategy

```bash
# Daily automated backup script
#!/bin/bash
BACKUP_DIR="/backups/postgres"
TIMESTAMP=$(date +%Y%m%d_%H%M%S)
DB_NAME="youth_connect_prod_db"

# Create backup
pg_dump -U youthconnect_user -h localhost -F c \
    -f "$BACKUP_DIR/${DB_NAME}_$TIMESTAMP.dump" \
    $DB_NAME

# Keep only last 30 days
find $BACKUP_DIR -name "*.dump" -mtime +30 -delete

# Upload to S3 (optional)
aws s3 cp "$BACKUP_DIR/${DB_NAME}_$TIMESTAMP.dump" \
    s3://your-backup-bucket/postgres/
```

### 5. Monitoring

```sql
-- Create monitoring user
CREATE USER monitoring_user WITH PASSWORD 'monitoring_pass';
GRANT pg_monitor TO monitoring_user;

-- Monitor connection count
SELECT count(*) as connections, 
       usename, 
       application_name
FROM pg_stat_activity
WHERE datname = 'youth_connect_prod_db'
GROUP BY usename, application_name;

-- Monitor database size growth
SELECT 
    NOW() as timestamp,
    pg_database_size('youth_connect_prod_db') as size_bytes,
    pg_size_pretty(pg_database_size('youth_connect_prod_db')) as size_pretty;
```

## Performance Tuning

### PostgreSQL Configuration

Edit `postgresql.conf` for production:

```ini
# Memory Settings
shared_buffers = 256MB
effective_cache_size = 1GB
maintenance_work_mem = 64MB
work_mem = 16MB

# Checkpoint Settings
checkpoint_completion_target = 0.9
wal_buffers = 16MB
default_statistics_target = 100

# Query Planning
random_page_cost = 1.1  # For SSD
effective_io_concurrency = 200

# Connection Settings
max_connections = 200
```

## Support & Documentation

- **PostgreSQL Documentation**: https://www.postgresql.org/docs/
- **Flyway Documentation**: https://flywaydb.org/documentation/
- **Spring Boot Data JPA**: https://spring.io/guides/gs/accessing-data-jpa/

---

**Last Updated**: November 2, 2025  
**Database Version**: 1.0.0  
**PostgreSQL Minimum**: 12+  
**Author**: Douglas Kings Kato