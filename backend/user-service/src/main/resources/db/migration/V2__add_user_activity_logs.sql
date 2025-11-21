-- ═════════════════════════════════════════════════════════════════════════════
-- USER ACTIVITY LOGS TABLE - MIGRATION V2
-- ═════════════════════════════════════════════════════════════════════════════
-- Purpose: Track all user interactions for AI recommendations and analytics
-- Author: Douglas Kings Kato
-- Date: 2025-11-20
-- ═════════════════════════════════════════════════════════════════════════════

-- Create user_activity_logs table
CREATE TABLE IF NOT EXISTS user_activity_logs (
    log_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    activity_type VARCHAR(50) NOT NULL,
    target_id BIGINT,
    target_type VARCHAR(50),
    session_id VARCHAR(255),
    user_agent TEXT,
    ip_address VARCHAR(45),
    metadata JSONB,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for performance
CREATE INDEX IF NOT EXISTS idx_activity_user_type_time
    ON user_activity_logs(user_id, activity_type, created_at);

CREATE INDEX IF NOT EXISTS idx_activity_target
    ON user_activity_logs(target_type, target_id);

CREATE INDEX IF NOT EXISTS idx_activity_session
    ON user_activity_logs(session_id);

CREATE INDEX IF NOT EXISTS idx_activity_created_at
    ON user_activity_logs(created_at);

-- Add comments
COMMENT ON TABLE user_activity_logs IS
    'User interaction tracking for AI recommendations and analytics';

COMMENT ON COLUMN user_activity_logs.activity_type IS
    'Type: VIEW_OPPORTUNITY, APPLY_OPPORTUNITY, LISTEN_AUDIO, etc.';

COMMENT ON COLUMN user_activity_logs.metadata IS
    'JSON metadata for activity-specific data';