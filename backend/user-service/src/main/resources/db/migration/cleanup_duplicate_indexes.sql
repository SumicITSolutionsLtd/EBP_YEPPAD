-- ============================================================================
-- Youth Connect Uganda - Database Cleanup Script
-- ============================================================================
-- Purpose: Remove duplicate indexes and constraints that cause conflicts
-- Run this script BEFORE starting the application
-- ============================================================================

USE youth_connect_db;

-- ============================================================================
-- STEP 1: Drop Duplicate Unique Constraints on Users Table
-- ============================================================================

-- Drop duplicate username unique constraint if exists
SET @query = (
    SELECT IF(
        COUNT(*) > 0,
        CONCAT('ALTER TABLE users DROP INDEX ', INDEX_NAME, ';'),
        'SELECT "Index UK9q63snka3mdh91as4io72espi not found, skipping...";'
    )
    FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = 'youth_connect_db'
    AND TABLE_NAME = 'users'
    AND INDEX_NAME = 'UK9q63snka3mdh91as4io72espi'
    LIMIT 1
);
PREPARE stmt FROM @query;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Funder Profiles
SET @query = (
    SELECT IF(
        COUNT(*) > 0,
        CONCAT('ALTER TABLE funder_profiles DROP INDEX ', INDEX_NAME, ';'),
        'SELECT "Funder profiles index not found, skipping...";'
    )
    FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = 'youth_connect_db'
    AND TABLE_NAME = 'funder_profiles'
    AND INDEX_NAME LIKE 'UK%'
    LIMIT 1
);
PREPARE stmt FROM @query;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Service Provider Profiles
SET @query = (
    SELECT IF(
        COUNT(*) > 0,
        CONCAT('ALTER TABLE service_provider_profiles DROP INDEX ', INDEX_NAME, ';'),
        'SELECT "Service provider profiles index not found, skipping...";'
    )
    FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = 'youth_connect_db'
    AND TABLE_NAME = 'service_provider_profiles'
    AND INDEX_NAME LIKE 'UK%'
    LIMIT 1
);
PREPARE stmt FROM @query;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- ============================================================================
-- STEP 4: Verify Remaining Indexes
-- ============================================================================

SELECT
    'Users Table Indexes' AS Info,
    TABLE_NAME,
    INDEX_NAME,
    COLUMN_NAME,
    NON_UNIQUE
FROM information_schema.STATISTICS
WHERE TABLE_SCHEMA = 'youth_connect_db'
AND TABLE_NAME = 'users'
ORDER BY TABLE_NAME, INDEX_NAME;

-- ============================================================================
-- STEP 5: Show All Tables
-- ============================================================================

SELECT
    'All Tables in Database' AS Info,
    TABLE_NAME,
    TABLE_ROWS,
    TABLE_COLLATION
FROM information_schema.TABLES
WHERE TABLE_SCHEMA = 'youth_connect_db'
ORDER BY TABLE_NAME;

-- ============================================================================
-- STEP 6: Success Message
-- ============================================================================

SELECT '✓ Database cleanup completed successfully!' AS Result;
SELECT 'You can now start the Spring Boot application.' AS NextStep; stmt FROM @query;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Drop duplicate email unique constraint if exists
SET @query = (
    SELECT IF(
        COUNT(*) > 0,
        CONCAT('ALTER TABLE users DROP INDEX ', INDEX_NAME, ';'),
        'SELECT "Index UKr43af9ap4edm43mmtq01oddj6 not found, skipping...";'
    )
    FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = 'youth_connect_db'
    AND TABLE_NAME = 'users'
    AND INDEX_NAME = 'UKr43af9ap4edm43mmtq01oddj6'
    LIMIT 1
);
PREPARE stmt FROM @query;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Drop duplicate phone_number unique constraint if exists
SET @query = (
    SELECT IF(
        COUNT(*) > 0,
        CONCAT('ALTER TABLE users DROP INDEX ', INDEX_NAME, ';'),
        'SELECT "Index UK6dotkott2kjsp8vw4d0m25fb7 not found, skipping...";'
    )
    FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = 'youth_connect_db'
    AND TABLE_NAME = 'users'
    AND INDEX_NAME = 'UK6dotkott2kjsp8vw4d0m25fb7'
    LIMIT 1
);
PREPARE stmt FROM @query;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- ============================================================================
-- STEP 2: Drop Duplicate Unique Constraints on Youth Profiles Table
-- ============================================================================

SET @query = (
    SELECT IF(
        COUNT(*) > 0,
        CONCAT('ALTER TABLE youth_profiles DROP INDEX ', INDEX_NAME, ';'),
        'SELECT "Youth profiles index not found, skipping...";'
    )
    FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = 'youth_connect_db'
    AND TABLE_NAME = 'youth_profiles'
    AND INDEX_NAME LIKE 'UK%'
    LIMIT 1
);
PREPARE stmt FROM @query;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- ============================================================================
-- STEP 3: Drop Duplicate Unique Constraints on Other Profile Tables
-- ============================================================================

-- Mentor Profiles
SET @query = (
    SELECT IF(
        COUNT(*) > 0,
        CONCAT('ALTER TABLE mentor_profiles DROP INDEX ', INDEX_NAME, ';'),
        'SELECT "Mentor profiles index not found, skipping...";'
    )
    FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = 'youth_connect_db'
    AND TABLE_NAME = 'mentor_profiles'
    AND INDEX_NAME LIKE 'UK%'
    LIMIT 1
);
PREPARE stmt FROM @query;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- NGO Profiles
SET @query = (
    SELECT IF(
        COUNT(*) > 0,
        CONCAT('ALTER TABLE ngo_profiles DROP INDEX ', INDEX_NAME, ';'),
        'SELECT "NGO profiles index not found, skipping...";'
    )
    FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = 'youth_connect_db'
    AND TABLE_NAME = 'ngo_profiles'
    AND INDEX_NAME LIKE 'UK%'
    LIMIT 1
);
PREPARE