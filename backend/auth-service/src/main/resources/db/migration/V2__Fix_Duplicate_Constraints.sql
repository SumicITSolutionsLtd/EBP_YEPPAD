-- ═══════════════════════════════════════════════════════════════════════════
-- MIGRATION: Fix Duplicate Unique Constraints (PostgreSQL Compatible)
-- ═══════════════════════════════════════════════════════════════════════════
-- File: V2__Fix_Duplicate_Constraints.sql
-- Location: backend/auth-service/src/main/resources/db/migration/
-- Purpose: Remove duplicate constraints and ensure clean schema
-- Author: Douglas Kings Kato
-- Date: 2025-11-16
-- Database: PostgreSQL 16+
-- ═══════════════════════════════════════════════════════════════════════════

-- ═══════════════════════════════════════════════════════════════════════════
-- STEP 1: DROP EXISTING CONSTRAINTS (PostgreSQL-Compatible Approach)
-- ═══════════════════════════════════════════════════════════════════════════
-- NOTE: PostgreSQL uses "CONSTRAINT" not "INDEX" for unique constraints
-- We use DO blocks to handle "IF EXISTS" logic safely
-- ═══════════════════════════════════════════════════════════════════════════

-- Drop constraints from users table
DO $$
BEGIN
    -- Drop UK_6dotkott2kjsp8vw4d0m25fb7 if it exists (JPA auto-generated constraint)
    IF EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'uk_6dotkott2kjsp8vw4d0m25fb7'
        AND conrelid = 'users'::regclass
    ) THEN
        ALTER TABLE users DROP CONSTRAINT uk_6dotkott2kjsp8vw4d0m25fb7;
        RAISE NOTICE 'Dropped constraint: uk_6dotkott2kjsp8vw4d0m25fb7';
    END IF;

    -- Drop UK9q63snka3mdh91as4io72espi if it exists (JPA auto-generated constraint)
    IF EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'uk9q63snka3mdh91as4io72espi'
        AND conrelid = 'users'::regclass
    ) THEN
        ALTER TABLE users DROP CONSTRAINT uk9q63snka3mdh91as4io72espi;
        RAISE NOTICE 'Dropped constraint: uk9q63snka3mdh91as4io72espi';
    END IF;

    -- Drop uk_email if it exists (manually created constraint)
    IF EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'uk_email'
        AND conrelid = 'users'::regclass
    ) THEN
        ALTER TABLE users DROP CONSTRAINT uk_email;
        RAISE NOTICE 'Dropped constraint: uk_email';
    END IF;

    -- Drop uk_phone_number if it exists (manually created constraint)
    IF EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'uk_phone_number'
        AND conrelid = 'users'::regclass
    ) THEN
        ALTER TABLE users DROP CONSTRAINT uk_phone_number;
        RAISE NOTICE 'Dropped constraint: uk_phone_number';
    END IF;

    -- Drop uk_users_email if it exists (renamed constraint)
    IF EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'uk_users_email'
        AND conrelid = 'users'::regclass
    ) THEN
        ALTER TABLE users DROP CONSTRAINT uk_users_email;
        RAISE NOTICE 'Dropped constraint: uk_users_email';
    END IF;

    -- Drop uk_users_phone_number if it exists (renamed constraint)
    IF EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'uk_users_phone_number'
        AND conrelid = 'users'::regclass
    ) THEN
        ALTER TABLE users DROP CONSTRAINT uk_users_phone_number;
        RAISE NOTICE 'Dropped constraint: uk_users_phone_number';
    END IF;
END $$;

-- Drop constraints from refresh_tokens table
DO $$
BEGIN
    -- Drop UK_refresh_tokens_token if it exists
    IF EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'uk_refresh_tokens_token'
        AND conrelid = 'refresh_tokens'::regclass
    ) THEN
        ALTER TABLE refresh_tokens DROP CONSTRAINT uk_refresh_tokens_token;
        RAISE NOTICE 'Dropped constraint: uk_refresh_tokens_token';
    END IF;

    -- Drop uk_token if it exists
    IF EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'uk_token'
        AND conrelid = 'refresh_tokens'::regclass
    ) THEN
        ALTER TABLE refresh_tokens DROP CONSTRAINT uk_token;
        RAISE NOTICE 'Dropped constraint: uk_token';
    END IF;
END $$;

-- Drop constraints from password_reset_tokens table
DO $$
BEGIN
    -- Drop UK_password_reset_token if it exists
    IF EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'uk_password_reset_token'
        AND conrelid = 'password_reset_tokens'::regclass
    ) THEN
        ALTER TABLE password_reset_tokens DROP CONSTRAINT uk_password_reset_token;
        RAISE NOTICE 'Dropped constraint: uk_password_reset_token';
    END IF;

    -- Drop uk_password_reset_tokens_token if it exists
    IF EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'uk_password_reset_tokens_token'
        AND conrelid = 'password_reset_tokens'::regclass
    ) THEN
        ALTER TABLE password_reset_tokens DROP CONSTRAINT uk_password_reset_tokens_token;
        RAISE NOTICE 'Dropped constraint: uk_password_reset_tokens_token';
    END IF;
END $$;

-- Drop constraints from email_verification_tokens table
DO $$
BEGIN
    -- Drop uk_email_verification_tokens_token if it exists
    IF EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'uk_email_verification_tokens_token'
        AND conrelid = 'email_verification_tokens'::regclass
    ) THEN
        ALTER TABLE email_verification_tokens DROP CONSTRAINT uk_email_verification_tokens_token;
        RAISE NOTICE 'Dropped constraint: uk_email_verification_tokens_token';
    END IF;
END $$;

-- ═══════════════════════════════════════════════════════════════════════════
-- STEP 2: CREATE CLEAN, STANDARDIZED UNIQUE CONSTRAINTS
-- ═══════════════════════════════════════════════════════════════════════════
-- Naming Convention: uk_{table_name}_{column_name}
-- This ensures consistency and prevents Hibernate conflicts
-- ═══════════════════════════════════════════════════════════════════════════

-- Users table constraints (only if they don't already exist from V1)
DO $$
BEGIN
    -- Add email constraint if it doesn't exist
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'uk_users_email'
        AND conrelid = 'users'::regclass
    ) THEN
        ALTER TABLE users ADD CONSTRAINT uk_users_email UNIQUE (email);
        RAISE NOTICE 'Created constraint: uk_users_email';
    END IF;

    -- Add phone_number constraint if it doesn't exist
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'uk_users_phone_number'
        AND conrelid = 'users'::regclass
    ) THEN
        ALTER TABLE users ADD CONSTRAINT uk_users_phone_number UNIQUE (phone_number);
        RAISE NOTICE 'Created constraint: uk_users_phone_number';
    END IF;
END $$;

-- Refresh tokens constraints
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'uk_refresh_tokens_token'
        AND conrelid = 'refresh_tokens'::regclass
    ) THEN
        ALTER TABLE refresh_tokens ADD CONSTRAINT uk_refresh_tokens_token UNIQUE (token);
        RAISE NOTICE 'Created constraint: uk_refresh_tokens_token';
    END IF;
END $$;

-- Password reset tokens constraints
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'uk_password_reset_tokens_token'
        AND conrelid = 'password_reset_tokens'::regclass
    ) THEN
        ALTER TABLE password_reset_tokens ADD CONSTRAINT uk_password_reset_tokens_token UNIQUE (token);
        RAISE NOTICE 'Created constraint: uk_password_reset_tokens_token';
    END IF;
END $$;

-- Email verification tokens constraints
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'uk_email_verification_tokens_token'
        AND conrelid = 'email_verification_tokens'::regclass
    ) THEN
        ALTER TABLE email_verification_tokens ADD CONSTRAINT uk_email_verification_tokens_token UNIQUE (token);
        RAISE NOTICE 'Created constraint: uk_email_verification_tokens_token';
    END IF;
END $$;

-- ═══════════════════════════════════════════════════════════════════════════
-- STEP 3: VERIFICATION QUERY (For Debugging)
-- ═══════════════════════════════════════════════════════════════════════════
-- This query shows all unique constraints on our tables
-- Uncomment the SELECT below if you want to see the results in logs
-- ═══════════════════════════════════════════════════════════════════════════

-- SELECT
--     tc.table_name,
--     tc.constraint_name,
--     tc.constraint_type,
--     kcu.column_name
-- FROM
--     information_schema.table_constraints tc
-- JOIN
--     information_schema.key_column_usage kcu
--     ON tc.constraint_name = kcu.constraint_name
--     AND tc.table_schema = kcu.table_schema
-- WHERE
--     tc.constraint_type = 'UNIQUE'
--     AND tc.table_name IN ('users', 'refresh_tokens', 'password_reset_tokens', 'email_verification_tokens')
-- ORDER BY
--     tc.table_name, tc.constraint_name;

-- ═══════════════════════════════════════════════════════════════════════════
-- MIGRATION COMPLETE
-- ═══════════════════════════════════════════════════════════════════════════