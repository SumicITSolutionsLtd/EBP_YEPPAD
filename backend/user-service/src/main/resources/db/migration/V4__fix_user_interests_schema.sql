-- ═════════════════════════════════════════════════════════════════════════════
-- USER INTERESTS TABLE - MIGRATION V4 (FIX SCHEMA MISMATCH)
-- ═════════════════════════════════════════════════════════════════════════════
-- Purpose: Align database schema with UserInterest.java entity
-- Author: Douglas Kings Kato
-- Date: 2025-11-21
-- Version: 4
--
-- PROBLEM: V3 migration created columns that don't match the Java entity:
--   Database (V3)          | Java Entity
--   -----------------------|------------------
--   interest_name          | interest_tag
--   proficiency_level      | interest_level
--   interest_category      | (not needed)
--   (missing)              | source
--
-- This migration fixes the schema to match UserInterest.java
-- ═════════════════════════════════════════════════════════════════════════════

-- Step 1: Rename 'interest_name' to 'interest_tag'
ALTER TABLE user_interests
    RENAME COLUMN interest_name TO interest_tag;

-- Step 2: Rename 'proficiency_level' to 'interest_level'
ALTER TABLE user_interests
    RENAME COLUMN proficiency_level TO interest_level;

-- Step 3: Add 'source' column (required by entity)
-- Default to 'USER_SELECTED' for existing records
ALTER TABLE user_interests
    ADD COLUMN IF NOT EXISTS source VARCHAR(20) NOT NULL DEFAULT 'USER_SELECTED';

-- Step 4: Drop 'interest_category' column (not in entity)
ALTER TABLE user_interests
    DROP COLUMN IF EXISTS interest_category;

-- Step 5: Update constraint name to match new column name
-- First drop the old constraint, then create new one
ALTER TABLE user_interests
    DROP CONSTRAINT IF EXISTS uk_user_interest;

ALTER TABLE user_interests
    ADD CONSTRAINT unique_user_interest UNIQUE (user_id, interest_tag);

-- Step 6: Update indexes for renamed columns
DROP INDEX IF EXISTS idx_user_interests_name;
DROP INDEX IF EXISTS idx_user_interests_category;

CREATE INDEX IF NOT EXISTS idx_interest_tag ON user_interests(interest_tag);
CREATE INDEX IF NOT EXISTS idx_user_interests ON user_interests(user_id, interest_level);

-- ═════════════════════════════════════════════════════════════════════════════
-- VERIFICATION (Optional - comment out in production)
-- ═════════════════════════════════════════════════════════════════════════════

-- Verify final column structure:
-- SELECT column_name, data_type, is_nullable, column_default
-- FROM information_schema.columns
-- WHERE table_name = 'user_interests'
-- ORDER BY ordinal_position;

-- ═════════════════════════════════════════════════════════════════════════════
-- COLUMN COMMENTS
-- ═════════════════════════════════════════════════════════════════════════════

COMMENT ON COLUMN user_interests.interest_tag IS
    'Interest keyword (e.g., Agriculture, Technology, Fintech)';

COMMENT ON COLUMN user_interests.interest_level IS
    'Priority level: LOW, MEDIUM, HIGH - affects recommendation weight';

COMMENT ON COLUMN user_interests.source IS
    'How interest was added: USER_SELECTED, AI_INFERRED, ACTIVITY_BASED';

-- ═════════════════════════════════════════════════════════════════════════════
-- END OF MIGRATION V4
-- ═════════════════════════════════════════════════════════════════════════════