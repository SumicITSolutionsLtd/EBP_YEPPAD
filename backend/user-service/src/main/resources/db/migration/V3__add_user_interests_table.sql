-- ═════════════════════════════════════════════════════════════════════════════
-- USER INTERESTS TABLE - MIGRATION V3
-- ═════════════════════════════════════════════════════════════════════════════
-- Purpose: Store user interests for personalized recommendations and matching
-- Author: Douglas Kings Kato
-- Date: 2025-11-21
-- Version: 3
--
-- This table supports:
-- - AI-powered content recommendations
-- - Opportunity matching based on user interests
-- - User profile enhancement
-- - Interest-based analytics
-- ═════════════════════════════════════════════════════════════════════════════

-- Create user_interests table
CREATE TABLE IF NOT EXISTS user_interests (
    user_interest_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    interest_name VARCHAR(100) NOT NULL,
    interest_category VARCHAR(50),
    proficiency_level VARCHAR(20),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- Ensure a user doesn't have duplicate interests
    CONSTRAINT uk_user_interest UNIQUE (user_id, interest_name)
);

-- ═════════════════════════════════════════════════════════════════════════════
-- INDEXES FOR PERFORMANCE
-- ═════════════════════════════════════════════════════════════════════════════

-- Index for finding all interests of a specific user (most common query)
CREATE INDEX IF NOT EXISTS idx_user_interests_user_id
    ON user_interests(user_id);

-- Index for finding users with specific interests (for matching)
CREATE INDEX IF NOT EXISTS idx_user_interests_name
    ON user_interests(interest_name);

-- Composite index for category-based filtering
CREATE INDEX IF NOT EXISTS idx_user_interests_category
    ON user_interests(interest_category, interest_name);

-- Index for proficiency-based queries
CREATE INDEX IF NOT EXISTS idx_user_interests_proficiency
    ON user_interests(proficiency_level);

-- ═════════════════════════════════════════════════════════════════════════════
-- TRIGGERS FOR AUTOMATIC TIMESTAMP UPDATES
-- ═════════════════════════════════════════════════════════════════════════════

-- Reuse the existing update_updated_at_column() function
CREATE TRIGGER update_user_interests_updated_at
    BEFORE UPDATE ON user_interests
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- ═════════════════════════════════════════════════════════════════════════════
-- TABLE AND COLUMN COMMENTS
-- ═════════════════════════════════════════════════════════════════════════════

COMMENT ON TABLE user_interests IS
    'Stores user interests for personalized recommendations and opportunity matching';

COMMENT ON COLUMN user_interests.user_interest_id IS
    'Primary key - auto-generated UUID';

COMMENT ON COLUMN user_interests.user_id IS
    'Reference to the user who has this interest';

COMMENT ON COLUMN user_interests.interest_name IS
    'Name of the interest (e.g., "Agriculture", "Technology", "Fashion")';

COMMENT ON COLUMN user_interests.interest_category IS
    'Category grouping (e.g., "INDUSTRY", "SKILL", "HOBBY")';

COMMENT ON COLUMN user_interests.proficiency_level IS
    'User proficiency level: BEGINNER, INTERMEDIATE, ADVANCED, EXPERT';

COMMENT ON COLUMN user_interests.created_at IS
    'Timestamp when the interest was added';

COMMENT ON COLUMN user_interests.updated_at IS
    'Timestamp of last update (auto-updated by trigger)';

-- ═════════════════════════════════════════════════════════════════════════════
-- SAMPLE DATA (OPTIONAL - COMMENT OUT FOR PRODUCTION)
-- ═════════════════════════════════════════════════════════════════════════════

-- Uncomment the following lines to insert sample data for testing:

/*
-- Insert sample interests for testing (requires existing users)
-- Replace with actual user UUIDs from your users table

INSERT INTO user_interests (user_id, interest_name, interest_category, proficiency_level) VALUES
    ((SELECT user_id FROM users LIMIT 1), 'Agriculture', 'INDUSTRY', 'INTERMEDIATE'),
    ((SELECT user_id FROM users LIMIT 1), 'Technology', 'INDUSTRY', 'BEGINNER'),
    ((SELECT user_id FROM users LIMIT 1), 'Business Management', 'SKILL', 'ADVANCED')
ON CONFLICT (user_id, interest_name) DO NOTHING;
*/

-- ═════════════════════════════════════════════════════════════════════════════
-- VALIDATION QUERIES (FOR TESTING)
-- ═════════════════════════════════════════════════════════════════════════════

-- Verify table structure
-- SELECT column_name, data_type, character_maximum_length, is_nullable
-- FROM information_schema.columns
-- WHERE table_name = 'user_interests'
-- ORDER BY ordinal_position;

-- Verify indexes
-- SELECT indexname, indexdef
-- FROM pg_indexes
-- WHERE tablename = 'user_interests';

-- ═════════════════════════════════════════════════════════════════════════════
-- END OF MIGRATION
-- ═════════════════════════════════════════════════════════════════════════════