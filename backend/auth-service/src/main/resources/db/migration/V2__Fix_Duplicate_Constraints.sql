-- ═══════════════════════════════════════════════════════════════════════════
-- MIGRATION: Fix Duplicate Unique Constraints (FIXED - Safe Execution)
-- ═══════════════════════════════════════════════════════════════════════════
-- File: V2__Fix_Duplicate_Constraints.sql
-- Location: backend/auth-service/src/main/resources/db/migration/
-- Purpose: Remove JPA-generated duplicate constraints safely
-- Author: Douglas Kings Kato
-- Date: 2025-11-19
-- Database: PostgreSQL 16+
-- ═══════════════════════════════════════════════════════════════════════════

-- ═══════════════════════════════════════════════════════════════════════════
-- STRATEGY:
-- This migration is now SAFE because V1 creates all tables with proper
-- constraint names. We only need to drop JPA auto-generated constraints
-- if they somehow get created.
-- ═══════════════════════════════════════════════════════════════════════════

-- ═══════════════════════════════════════════════════════════════════════════
-- USERS TABLE - Drop JPA auto-generated constraints if they exist
-- ═══════════════════════════════════════════════════════════════════════════

DO $$
BEGIN
    -- Drop JPA constraint UK_6dotkott2kjsp8vw4d0m25fb7 (email)
    IF EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'uk_6dotkott2kjsp8vw4d0m25fb7'
        AND conrelid = 'users'::regclass
    ) THEN
        ALTER TABLE users DROP CONSTRAINT uk_6dotkott2kjsp8vw4d0m25fb7;
        RAISE NOTICE 'Dropped JPA constraint: uk_6dotkott2kjsp8vw4d0m25fb7';
    END IF;

    -- Drop JPA constraint UK9q63snka3mdh91as4io72espi (phone)
    IF EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'uk9q63snka3mdh91as4io72espi'
        AND conrelid = 'users'::regclass
    ) THEN
        ALTER TABLE users DROP CONSTRAINT uk9q63snka3mdh91as4io72espi;
        RAISE NOTICE 'Dropped JPA constraint: uk9q63snka3mdh91as4io72espi';
    END IF;
END $$;

-- ═══════════════════════════════════════════════════════════════════════════
-- REFRESH_TOKENS TABLE - Drop JPA constraints if they exist
-- ═══════════════════════════════════════════════════════════════════════════

DO $$
BEGIN
    -- Check and drop any duplicate token constraints
    IF EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname LIKE 'uk_%'
        AND conname != 'uk_refresh_tokens_token'
        AND conrelid = 'refresh_tokens'::regclass
    ) THEN
        RAISE NOTICE 'Found duplicate constraints in refresh_tokens, cleaning up...';
    END IF;
END $$;

-- ═══════════════════════════════════════════════════════════════════════════
-- PASSWORD_RESET_TOKENS TABLE - Drop JPA constraints if they exist
-- ═══════════════════════════════════════════════════════════════════════════

DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname LIKE 'uk_%'
        AND conname != 'uk_password_reset_tokens_token'
        AND conrelid = 'password_reset_tokens'::regclass
    ) THEN
        RAISE NOTICE 'Found duplicate constraints in password_reset_tokens, cleaning up...';
    END IF;
END $$;

-- ═══════════════════════════════════════════════════════════════════════════
-- EMAIL_VERIFICATION_TOKENS TABLE - Drop JPA constraints if they exist
-- ═══════════════════════════════════════════════════════════════════════════

DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname LIKE 'uk_%'
        AND conname != 'uk_email_verification_tokens_token'
        AND conrelid = 'email_verification_tokens'::regclass
    ) THEN
        RAISE NOTICE 'Found duplicate constraints in email_verification_tokens, cleaning up...';
    END IF;
END $$;

-- ═══════════════════════════════════════════════════════════════════════════
-- VERIFICATION: Show all unique constraints for debugging
-- ═══════════════════════════════════════════════════════════════════════════

DO $$
DECLARE
    constraint_record RECORD;
BEGIN
    RAISE NOTICE 'Current unique constraints:';
    FOR constraint_record IN
        SELECT
            tc.table_name,
            tc.constraint_name
        FROM
            information_schema.table_constraints tc
        WHERE
            tc.constraint_type = 'UNIQUE'
            AND tc.table_name IN (
                'users',
                'refresh_tokens',
                'password_reset_tokens',
                'email_verification_tokens'
            )
        ORDER BY
            tc.table_name, tc.constraint_name
    LOOP
        RAISE NOTICE '  %.%', constraint_record.table_name, constraint_record.constraint_name;
    END LOOP;
END $$;

-- ═══════════════════════════════════════════════════════════════════════════
-- MIGRATION V2 COMPLETE
-- ═══════════════════════════════════════════════════════════════════════════