-- ============================================================================
-- YOUTH CONNECT PLATFORM - POSTGRESQL DATABASE INITIALIZATION (SIMPLIFIED)
-- ============================================================================
-- File: backend/scripts/init-databases.sql
-- Version: 3.2 (Simplified - Single Database Per Service)
-- Author: Douglas Kings Kato
-- Date: 2025-11-16
-- Database: PostgreSQL 15+
--
-- PURPOSE:
--   Creates dedicated databases for each microservice WITHOUT nested schemas.
--   Each service gets its own database (e.g., youthconnect_auth) with all
--   tables in the default 'public' schema. This simplifies management
--   and aligns with common microservice patterns and Flyway migration strategies.
--
-- CHANGES FROM v3.1:
--   - Removed schema complexity (no nested auth/user schemas).
--   - Each service now corresponds to one database with all tables in the 'public' schema.
--   - Simplified connection strings for microservices.
--   - Explicitly aligns with Flyway migration expectations where each service
--     manages its own database schema.
--   - Added detailed comments for each database and extension, explaining their purpose.
--   - Enhanced verification section to show database size and collation.
--
-- EXECUTION (Windows PowerShell):
--   Ensure PostgreSQL bin directory is in your PATH or use full path.
--   $env:PGCLIENTENCODING = "UTF8"
--   & "C:\Program Files\PostgreSQL\16\bin\psql.exe" -U postgres -f scripts/init-databases.sql
--
-- EXECUTION (Linux/Mac):
--   Ensure psql is accessible in your PATH.
--   PGCLIENTENCODING=UTF8 psql -U postgres -f scripts/init-databases.sql
--
-- SECURITY:
--   WARNING: The default password 'YouthConnect2024!' is for development purposes only.
--   IT MUST BE CHANGED before deploying to any production or publicly accessible environment.
-- ============================================================================

\echo '===================================================================='
\echo 'Youth Connect Platform - Database Initialization v3.2'
\echo 'Simplified: One database per service, no nested schemas'
\echo 'Starting initialization process...'
\echo '===================================================================='
\echo ''

-- ============================================================================
-- SECTION 1: CREATE APPLICATION USER
-- ============================================================================
-- This section creates a dedicated PostgreSQL user that microservices will use
-- to connect to their respective databases. This adheres to the principle of
-- least privilege by not using the superuser 'postgres' directly for applications.
-- ============================================================================

\echo 'Step 1/5: Creating application user...'

-- Drop user if it already exists to ensure a clean slate on re-execution.
DROP USER IF EXISTS youthconnect_user;

-- Create the application user with login capabilities and the ability to create databases.
-- CREATEDB privilege is granted to allow the user to own the newly created service databases.
-- WARNING: Change this password before production deployment for security reasons!
CREATE USER youthconnect_user WITH
    LOGIN
    PASSWORD 'YouthConnect2024!'
    CREATEDB;

\echo 'SUCCESS: Application user created'
\echo '  Username: youthconnect_user'
\echo '  Password: YouthConnect2024! (WARNING: CHANGE THIS IN PRODUCTION!)'
\echo ''

-- ============================================================================
-- SECTION 2: CREATE DATABASES FOR EACH MICROSERVICE
-- ============================================================================
-- Each microservice in the Youth Connect platform will have its own dedicated
-- PostgreSQL database. This provides strong isolation between services,
-- simplifies data model management, and supports independent deployment.
-- All tables within each database will reside in the default 'public' schema.
-- ============================================================================

\echo 'Step 2/5: Creating databases for all microservices...'

-- Drop existing databases if they exist. This is crucial for idempotent script execution,
-- allowing the script to be run multiple times without errors and ensuring a fresh setup.
DROP DATABASE IF EXISTS youthconnect_auth;
DROP DATABASE IF EXISTS youthconnect_user;
DROP DATABASE IF EXISTS youthconnect_job;
DROP DATABASE IF EXISTS youthconnect_opportunity;
DROP DATABASE IF EXISTS youthconnect_mentor;
DROP DATABASE IF EXISTS youthconnect_content;
DROP DATABASE IF EXISTS youthconnect_notification;
DROP DATABASE IF EXISTS youthconnect_file;
DROP DATABASE IF EXISTS youthconnect_ai;
DROP DATABASE IF EXISTS youthconnect_analytics;
DROP DATABASE IF EXISTS youthconnect_ussd;

-- 1. AUTH SERVICE DATABASE
-- Manages user authentication, authorization (JWT, RBAC), and security auditing.
CREATE DATABASE youthconnect_auth
    WITH
    OWNER = youthconnect_user        -- The application user owns this database.
    ENCODING = 'UTF8'                -- Standard encoding for broad character support.
    TEMPLATE = template0             -- Use template0 to avoid copying locale-dependent objects,
                                     -- ensuring consistent database creation across environments.
    LC_COLLATE = 'en_US.UTF-8'       -- Locale for string comparison and sorting.
    LC_CTYPE = 'en_US.UTF-8'         -- Locale for character classification.
    TABLESPACE = pg_default          -- Default tablespace for data storage.
    CONNECTION LIMIT = -1            -- No limit on concurrent connections.
    IS_TEMPLATE = FALSE;             -- Not intended to be a template database itself.

COMMENT ON DATABASE youthconnect_auth IS
    'Authentication and authorization service - JWT, RBAC, security auditing';

\echo '  [1/11] Created: youthconnect_auth'

-- 2. USER SERVICE DATABASE
-- Stores and manages user profiles, supporting a multi-role profile system.
CREATE DATABASE youthconnect_user
    WITH
    OWNER = youthconnect_user
    ENCODING = 'UTF8'
    TEMPLATE = template0
    LC_COLLATE = 'en_US.UTF-8'
    LC_CTYPE = 'en_US.UTF-8'
    TABLESPACE = pg_default
    CONNECTION LIMIT = -1
    IS_TEMPLATE = FALSE;

COMMENT ON DATABASE youthconnect_user IS
    'User profiles and management - Multi-role profile system';

\echo '  [2/11] Created: youthconnect_user'

-- 3. JOB SERVICE DATABASE
-- Manages job opportunities and application processes, forming an employment marketplace.
CREATE DATABASE youthconnect_job
    WITH
    OWNER = youthconnect_user
    ENCODING = 'UTF8'
    TEMPLATE = template0
    LC_COLLATE = 'en_US.UTF-8'
    LC_CTYPE = 'en_US.UTF-8'
    TABLESPACE = pg_default
    CONNECTION LIMIT = -1
    IS_TEMPLATE = FALSE;

COMMENT ON DATABASE youthconnect_job IS
    'Job opportunities and applications - Employment marketplace';

\echo '  [3/11] Created: youthconnect_job'

-- 4. OPPORTUNITY SERVICE DATABASE
-- Handles business opportunities such as grants, loans, and training programs.
CREATE DATABASE youthconnect_opportunity
    WITH
    OWNER = youthconnect_user
    ENCODING = 'UTF8'
    TEMPLATE = template0
    LC_COLLATE = 'en_US.UTF-8'
    LC_CTYPE = 'en_US.UTF-8'
    TABLESPACE = pg_default
    CONNECTION LIMIT = -1
    IS_TEMPLATE = FALSE;

COMMENT ON DATABASE youthconnect_opportunity IS
    'Business opportunities - Grants, loans, training programs';

\echo '  [4/11] Created: youthconnect_opportunity'

-- 5. MENTOR SERVICE DATABASE
-- Supports mentorship programs, including session scheduling and automated reminders.
CREATE DATABASE youthconnect_mentor
    WITH
    OWNER = youthconnect_user
    ENCODING = 'UTF8'
    TEMPLATE = template0
    LC_COLLATE = 'en_US.UTF-8'
    LC_CTYPE = 'en_US.UTF-8'
    TABLESPACE = pg_default
    CONNECTION LIMIT = -1
    IS_TEMPLATE = FALSE;

COMMENT ON DATABASE youthconnect_mentor IS
    'Mentorship programs - Session scheduling and automated reminders';

\echo '  [5/11] Created: youthconnect_mentor'

-- 6. CONTENT SERVICE DATABASE
-- Stores and manages educational content, featuring learning management and multi-language support.
CREATE DATABASE youthconnect_content
    WITH
    OWNER = youthconnect_user
    ENCODING = 'UTF8'
    TEMPLATE = template0
    LC_COLLATE = 'en_US.UTF-8'
    LC_CTYPE = 'en_US.UTF-8'
    TABLESPACE = pg_default
    CONNECTION LIMIT = -1
    IS_TEMPLATE = FALSE;

COMMENT ON DATABASE youthconnect_content IS
    'Educational content - Learning management with multi-language support';

\echo '  [6/11] Created: youthconnect_content'

-- 7. NOTIFICATION SERVICE DATABASE
-- Manages a multi-channel notification system (SMS, Email, Push notifications).
CREATE DATABASE youthconnect_notification
    WITH
    OWNER = youthconnect_user
    ENCODING = 'UTF8'
    TEMPLATE = template0
    LC_COLLATE = 'en_US.UTF-8'
    LC_CTYPE = 'en_US.UTF-8'
    TABLESPACE = pg_default
    CONNECTION LIMIT = -1
    IS_TEMPLATE = FALSE;

COMMENT ON DATABASE youthconnect_notification IS
    'Notification system - Multi-channel delivery (SMS, Email, Push)';

\echo '  [7/11] Created: youthconnect_notification'

-- 8. FILE MANAGEMENT SERVICE DATABASE
-- Provides file storage and document management with version control capabilities.
CREATE DATABASE youthconnect_file
    WITH
    OWNER = youthconnect_user
    ENCODING = 'UTF8'
    TEMPLATE = template0
    LC_COLLATE = 'en_US.UTF-8'
    LC_CTYPE = 'en_US.UTF-8'
    TABLESPACE = pg_default
    CONNECTION LIMIT = -1
    IS_TEMPLATE = FALSE;

COMMENT ON DATABASE youthconnect_file IS
    'File storage - Document management with version control';

\echo '  [8/11] Created: youthconnect_file'

-- 9. AI RECOMMENDATION SERVICE DATABASE
-- Implements AI-driven recommendations, serving as a personalization engine with collaborative filtering.
CREATE DATABASE youthconnect_ai
    WITH
    OWNER = youthconnect_user
    ENCODING = 'UTF8'
    TEMPLATE = template0
    LC_COLLATE = 'en_US.UTF-8'
    LC_CTYPE = 'en_US.UTF-8'
    TABLESPACE = pg_default
    CONNECTION LIMIT = -1
    IS_TEMPLATE = FALSE;

COMMENT ON DATABASE youthconnect_ai IS
    'AI recommendations - Personalization engine with collaborative filtering';

\echo '  [9/11] Created: youthconnect_ai'

-- 10. ANALYTICS SERVICE DATABASE
-- Collects and processes data for analytics and reporting, tracking Business Intelligence KPIs.
CREATE DATABASE youthconnect_analytics
    WITH
    OWNER = youthconnect_user
    ENCODING = 'UTF8'
    TEMPLATE = template0
    LC_COLLATE = 'en_US.UTF-8'
    LC_CTYPE = 'en_US.UTF-8'
    TABLESPACE = pg_default
    CONNECTION LIMIT = -1
    IS_TEMPLATE = FALSE;

COMMENT ON DATABASE youthconnect_analytics IS
    'Analytics and reporting - Business intelligence with KPI tracking';

\echo '  [10/11] Created: youthconnect_analytics'

-- 11. USSD SERVICE DATABASE
-- Supports the USSD service for feature phone accessibility, including multi-language support.
CREATE DATABASE youthconnect_ussd
    WITH
    OWNER = youthconnect_user
    ENCODING = 'UTF8'
    TEMPLATE = template0
    LC_COLLATE = 'en_US.UTF-8'
    LC_CTYPE = 'en_US.UTF-8'
    TABLESPACE = pg_default
    CONNECTION LIMIT = -1
    IS_TEMPLATE = FALSE;

COMMENT ON DATABASE youthconnect_ussd IS
    'USSD service - Feature phone accessibility with multi-language support';

\echo '  [11/11] Created: youthconnect_ussd'

\echo ''
\echo 'SUCCESS: Created 11 databases for all microservices'
\echo ''

-- ============================================================================
-- SECTION 3: GRANT PRIVILEGES TO APPLICATION USER
-- ============================================================================
-- After creating the databases, the application user needs specific privileges
-- to perform DDL and DML operations within these databases.
-- ============================================================================

\echo 'Step 3/5: Granting privileges to application user...'

-- Grant all privileges on each newly created database to the 'youthconnect_user'.
-- This includes connect, create schema, and temporary table creation.
GRANT ALL PRIVILEGES ON DATABASE youthconnect_auth TO youthconnect_user;
GRANT ALL PRIVILEGES ON DATABASE youthconnect_user TO youthconnect_user;
GRANT ALL PRIVILEGES ON DATABASE youthconnect_job TO youthconnect_user;
GRANT ALL PRIVILEGES ON DATABASE youthconnect_opportunity TO youthconnect_user;
GRANT ALL PRIVILEGES ON DATABASE youthconnect_mentor TO youthconnect_user;
GRANT ALL PRIVILEGES ON DATABASE youthconnect_content TO youthconnect_user;
GRANT ALL PRIVILEGES ON DATABASE youthconnect_notification TO youthconnect_user;
GRANT ALL PRIVILEGES ON DATABASE youthconnect_file TO youthconnect_user;
GRANT ALL PRIVILEGES ON DATABASE youthconnect_ai TO youthconnect_user;
GRANT ALL PRIVILEGES ON DATABASE youthconnect_analytics TO youthconnect_user;
GRANT ALL PRIVILEGES ON DATABASE youthconnect_ussd TO youthconnect_user;

\echo 'SUCCESS: Granted all privileges to youthconnect_user on all databases.'
\echo ''

-- ============================================================================
-- SECTION 4: INSTALL POSTGRESQL EXTENSIONS
-- ============================================================================
-- PostgreSQL extensions provide additional functionality. Each database
-- will have specific extensions installed based on its microservice's needs.
-- ============================================================================

\echo 'Step 4/5: Installing PostgreSQL extensions...'
\echo ''

-- Connect to each database to install its required extensions.
-- This ensures extensions are installed in the correct database context.

-- AUTH SERVICE EXTENSIONS
\c youthconnect_auth
-- uuid-ossp: Generates Universally Unique Identifiers (UUIDs), often used for primary keys.
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
-- pgcrypto: Provides cryptographic functions, essential for secure password hashing.
CREATE EXTENSION IF NOT EXISTS "pgcrypto";
-- pg_trgm: Enables trigram matching, useful for fuzzy text search and "likeness" comparisons.
CREATE EXTENSION IF NOT EXISTS "pg_trgm";
-- btree_gin: Provides GIN index support for B-tree key types, improving performance
-- for certain types of queries, especially with pg_trgm.
CREATE EXTENSION IF NOT EXISTS "btree_gin";
\echo '  [1/11] youthconnect_auth: uuid-ossp, pgcrypto, pg_trgm, btree_gin'

-- USER SERVICE EXTENSIONS
\c youthconnect_user
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pg_trgm";
CREATE EXTENSION IF NOT EXISTS "btree_gin";
\echo '  [2/11] youthconnect_user: uuid-ossp, pg_trgm, btree_gin'

-- JOB SERVICE EXTENSIONS
\c youthconnect_job
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pg_trgm";
CREATE EXTENSION IF NOT EXISTS "btree_gin";
\echo '  [3/11] youthconnect_job: uuid-ossp, pg_trgm, btree_gin'

-- OPPORTUNITY SERVICE EXTENSIONS
\c youthconnect_opportunity
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pg_trgm";
CREATE EXTENSION IF NOT EXISTS "btree_gin";
\echo '  [4/11] youthconnect_opportunity: uuid-ossp, pg_trgm, btree_gin'

-- MENTOR SERVICE EXTENSIONS
\c youthconnect_mentor
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pg_trgm";
CREATE EXTENSION IF NOT EXISTS "btree_gin";
\echo '  [5/11] youthconnect_mentor: uuid-ossp, pg_trgm, btree_gin'

-- CONTENT SERVICE EXTENSIONS
\c youthconnect_content
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pg_trgm";
CREATE EXTENSION IF NOT EXISTS "btree_gin";
\echo '  [6/11] youthconnect_content: uuid-ossp, pg_trgm, btree_gin'

-- NOTIFICATION SERVICE EXTENSIONS
\c youthconnect_notification
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pg_trgm";
CREATE EXTENSION IF NOT EXISTS "btree_gin";
\echo '  [7/11] youthconnect_notification: uuid-ossp, pg_trgm, btree_gin'

-- FILE SERVICE EXTENSIONS
\c youthconnect_file
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pg_trgm";
CREATE EXTENSION IF NOT EXISTS "btree_gin";
\echo '  [8/11] youthconnect_file: uuid-ossp, pg_trgm, btree_gin'

-- AI SERVICE EXTENSIONS
\c youthconnect_ai
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
\echo '  [9/11] youthconnect_ai: uuid-ossp'

-- ANALYTICS SERVICE EXTENSIONS
\c youthconnect_analytics
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
-- tablefunc: Provides functions that manipulate tables, including CROSSTAB for pivot table functionality.
CREATE EXTENSION IF NOT EXISTS "tablefunc";
CREATE EXTENSION IF NOT EXISTS "btree_gin";
\echo '  [10/11] youthconnect_analytics: uuid-ossp, tablefunc, btree_gin'

-- USSD SERVICE EXTENSIONS
\c youthconnect_ussd
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "btree_gin";
\echo '  [11/11] youthconnect_ussd: uuid-ossp, btree_gin'

\echo ''
\echo 'SUCCESS: All extensions installed for their respective databases.'
\echo ''

-- ============================================================================
-- SECTION 5: VERIFICATION
-- ============================================================================
-- This final section verifies that the databases have been created successfully
-- and provides important next steps for developers.
-- ============================================================================

\echo 'Step 5/5: Verifying database setup...'
\echo ''

-- Connect back to the default 'postgres' database for system-wide queries.
\c postgres

\echo 'Created Databases:'
-- List all databases whose names start with 'youthconnect_', along with their
-- size and collation, to confirm their successful creation and configuration.
SELECT
    datname AS "Database Name",
    pg_size_pretty(pg_database_size(datname)) AS "Size",
    datcollate AS "Collation"
FROM pg_database
WHERE datname LIKE 'youthconnect_%'
ORDER BY datname;

\echo ''
\echo '===================================================================='
\echo 'DATABASE INITIALIZATION COMPLETED SUCCESSFULLY!'
\echo '===================================================================='
\echo ''
\echo 'SUMMARY OF CREATED DATABASES (11 Microservices):'
\echo '  1.  youthconnect_auth          - Authentication & Authorization'
\echo '  2.  youthconnect_user          - User Profiles & Management'
\echo '  3.  youthconnect_job           - Job Opportunities & Applications'
\echo '  4.  youthconnect_opportunity   - Business Opportunities (Grants, Loans, Training)'
\echo '  5.  youthconnect_mentor        - Mentorship Programs'
\echo '  6.  youthconnect_content       - Educational Content & Learning Management'
\echo '  7.  youthconnect_notification  - Multi-channel Notification System'
\echo '  8.  youthconnect_file          - File Storage & Document Management'
\echo '  9.  youthconnect_ai            - AI Recommendation Engine'
\echo '  10. youthconnect_analytics     - Analytics & Reporting'
\echo '  11. youthconnect_ussd          - USSD Service for Feature Phones'
\echo ''
\echo 'APPLICATION USER CREDENTIALS:'
\echo '  Username: youthconnect_user'
\echo '  Password: YouthConnect2024!'
\echo '  WARNING: CHANGE THIS PASSWORD IMMEDIATELY IN PRODUCTION ENVIRONMENTS!'
\echo ''
\echo 'EXAMPLE CONNECTION STRINGS (for microservices):'
\echo '  Auth Service:'
\echo '    jdbc:postgresql://localhost:5432/youthconnect_auth'
\echo '  User Service:'
\echo '    jdbc:postgresql://localhost:5432/youthconnect_user'
\echo '  (Replace localhost:5432 with your PostgreSQL host and port if different)'
\echo ''
\echo 'NEXT STEPS FOR DEVELOPMENT:'
\echo '  1. Update your microservices'' `application-local.yml` or `.env` files'
\echo '     with the correct database names and `youthconnect_user` credentials.'
\echo '  2. Run Flyway migrations for each service to create tables and initial data.'
\echo '     Example: `mvn flyway:migrate` (from each service''s directory).'
\echo '  3. Start your microservice applications.'
\echo '     Example: `mvn spring-boot:run` (for a Spring Boot service).'
\echo '  4. Verify service registration and health in your service discovery tool (e.g., Eureka).'
\echo '     Example: Check http://localhost:8761 for Eureka Dashboard.'
\echo ''
\echo '===================================================================='

-- ============================================================================
-- END OF SCRIPT
-- ============================================================================