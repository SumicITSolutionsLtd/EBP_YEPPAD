-- ═════════════════════════════════════════════════════════════════════════════
-- DATABASE RESET SCRIPT
-- CAUTION: This will DELETE ALL DATA!
-- ═════════════════════════════════════════════════════════════════════════════

\c postgres

-- Drop and recreate database
DROP DATABASE IF EXISTS youthconnect_user;

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

GRANT ALL PRIVILEGES ON DATABASE youthconnect_user TO youthconnect_user;

\c youthconnect_user

-- Install extensions
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pg_trgm";
CREATE EXTENSION IF NOT EXISTS "btree_gin";

\echo 'Database reset complete. Ready for Flyway migrations.'