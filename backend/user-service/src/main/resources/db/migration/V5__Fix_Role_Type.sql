-- ═══════════════════════════════════════════════════════════════════════════
-- MIGRATION V5: FIX ROLE COLUMN TYPE TO VARCHAR
-- ═══════════════════════════════════════════════════════════════════════════
-- Author: Douglas Kings Kato
-- Date: 2025-11-21
-- Purpose: Convert role column from PostgreSQL enum to VARCHAR to fix
--          "operator does not exist: user_role = character varying" error
--
-- WHAT THIS DOES:
-- 1. Converts existing role enum values to VARCHAR
-- 2. Drops the custom user_role enum type
-- 3. Adds CHECK constraint to maintain data integrity
--
-- ROLLBACK STRATEGY:
-- To revert, run the commented rollback script at the bottom
-- ═══════════════════════════════════════════════════════════════════════════

-- Start transaction (rollback if anything fails)
BEGIN;

-- ═══════════════════════════════════════════════════════════════════════════
-- STEP 1: ALTER role COLUMN TO VARCHAR
-- ═══════════════════════════════════════════════════════════════════════════
-- This converts the role column from user_role enum to VARCHAR(50)
-- PostgreSQL automatically converts the enum values to strings

DO $$
BEGIN
    -- Check if column exists and is currently an enum type
    IF EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_name = 'users'
          AND column_name = 'role'
          AND udt_name = 'user_role'
    ) THEN
        -- Convert enum to VARCHAR
        -- Using CAST to explicitly convert enum values to text
        ALTER TABLE users
        ALTER COLUMN role TYPE VARCHAR(50) USING role::TEXT;

        RAISE NOTICE 'Converted role column from enum to VARCHAR(50)';
    ELSE
        RAISE NOTICE 'Role column already VARCHAR or not found';
    END IF;
END $$;

-- ═══════════════════════════════════════════════════════════════════════════
-- STEP 2: ADD CHECK CONSTRAINT
-- ═══════════════════════════════════════════════════════════════════════════
-- This maintains data integrity by ensuring only valid role values are stored
-- Acts as a replacement for the PostgreSQL enum constraint

DO $$
BEGIN
    -- Drop constraint if it exists (idempotent migration)
    IF EXISTS (
        SELECT 1
        FROM information_schema.table_constraints
        WHERE constraint_name = 'chk_user_role'
          AND table_name = 'users'
    ) THEN
        ALTER TABLE users DROP CONSTRAINT chk_user_role;
        RAISE NOTICE 'Dropped existing chk_user_role constraint';
    END IF;

    -- Add new check constraint
    ALTER TABLE users
    ADD CONSTRAINT chk_user_role
    CHECK (role IN ('YOUTH', 'MENTOR', 'NGO', 'FUNDER', 'SERVICE_PROVIDER', 'ADMIN'));

    RAISE NOTICE 'Added CHECK constraint for role values';
END $$;

-- ═══════════════════════════════════════════════════════════════════════════
-- STEP 3: DROP CUSTOM ENUM TYPE
-- ═══════════════════════════════════════════════════════════════════════════
-- Now that role column is VARCHAR, we can safely drop the enum type
-- CASCADE removes any dependencies

DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM pg_type
        WHERE typname = 'user_role'
    ) THEN
        DROP TYPE user_role CASCADE;
        RAISE NOTICE 'Dropped user_role enum type';
    ELSE
        RAISE NOTICE 'user_role enum type does not exist';
    END IF;
END $$;

-- ═══════════════════════════════════════════════════════════════════════════
-- STEP 4: VERIFY MIGRATION
-- ═══════════════════════════════════════════════════════════════════════════
-- Display role column info to confirm migration

DO $$
DECLARE
    role_data_type TEXT;
    role_char_max_len INTEGER;
    constraint_count INTEGER;
BEGIN
    -- Get column data type
    SELECT data_type, character_maximum_length
    INTO role_data_type, role_char_max_len
    FROM information_schema.columns
    WHERE table_name = 'users' AND column_name = 'role';

    -- Count check constraints
    SELECT COUNT(*)
    INTO constraint_count
    FROM information_schema.table_constraints
    WHERE table_name = 'users' AND constraint_name = 'chk_user_role';

    -- Display results
    RAISE NOTICE '═══════════════════════════════════════════════════════════';
    RAISE NOTICE 'MIGRATION VERIFICATION';
    RAISE NOTICE '═══════════════════════════════════════════════════════════';
    RAISE NOTICE 'Role column type: % (max_length: %)', role_data_type, role_char_max_len;
    RAISE NOTICE 'CHECK constraint exists: %', (constraint_count > 0);
    RAISE NOTICE 'Migration completed successfully!';
    RAISE NOTICE '═══════════════════════════════════════════════════════════';
END $$;

-- ═══════════════════════════════════════════════════════════════════════════
-- COMMIT TRANSACTION
-- ═══════════════════════════════════════════════════════════════════════════
COMMIT;

-- ═══════════════════════════════════════════════════════════════════════════
-- POST-MIGRATION VALIDATION QUERIES
-- ═══════════════════════════════════════════════════════════════════════════
-- Run these manually to verify the migration

-- 1. Check role column definition
SELECT
    column_name,
    data_type,
    character_maximum_length,
    is_nullable,
    column_default
FROM information_schema.columns
WHERE table_name = 'users' AND column_name = 'role';

-- 2. Check role constraint
SELECT
    constraint_name,
    check_clause
FROM information_schema.check_constraints
WHERE constraint_name = 'chk_user_role';

-- 3. Verify existing user roles (should work without errors now)
SELECT role, COUNT(*) as count
FROM users
GROUP BY role
ORDER BY role;

-- 4. Test role-based query (was failing before)
SELECT * FROM users WHERE role = 'YOUTH' LIMIT 1;
SELECT * FROM users WHERE role = 'MENTOR' LIMIT 1;

-- ═══════════════════════════════════════════════════════════════════════════
-- ROLLBACK SCRIPT (IF NEEDED)
-- ═══════════════════════════════════════════════════════════════════════════
-- Run this script if you need to revert to PostgreSQL enum type
-- WARNING: Only use this if you're certain you want to go back

/*
BEGIN;

-- Step 1: Recreate enum type
CREATE TYPE user_role AS ENUM ('YOUTH', 'MENTOR', 'NGO', 'FUNDER', 'SERVICE_PROVIDER', 'ADMIN');

-- Step 2: Drop check constraint
ALTER TABLE users DROP CONSTRAINT IF EXISTS chk_user_role;

-- Step 3: Convert VARCHAR back to enum
ALTER TABLE users
ALTER COLUMN role TYPE user_role USING role::user_role;

-- Step 4: Verify rollback
SELECT column_name, udt_name, data_type
FROM information_schema.columns
WHERE table_name = 'users' AND column_name = 'role';

COMMIT;
*/

-- ═══════════════════════════════════════════════════════════════════════════
-- NOTES FOR FUTURE MIGRATIONS
-- ═══════════════════════════════════════════════════════════════════════════
--
-- Adding new roles:
-- ALTER TABLE users DROP CONSTRAINT chk_user_role;
-- ALTER TABLE users ADD CONSTRAINT chk_user_role
--   CHECK (role IN ('YOUTH', 'MENTOR', 'NGO', 'FUNDER', 'SERVICE_PROVIDER', 'ADMIN', 'NEW_ROLE'));
--
-- Removing roles (ensure no users have that role first):
-- DELETE FROM users WHERE role = 'OLD_ROLE'; -- or UPDATE to different role
-- ALTER TABLE users DROP CONSTRAINT chk_user_role;
-- ALTER TABLE users ADD CONSTRAINT chk_user_role
--   CHECK (role IN ('YOUTH', 'MENTOR', 'NGO', 'FUNDER', 'SERVICE_PROVIDER', 'ADMIN'));
--
-- ═══════════════════════════════════════════════════════════════════════════