-- =================================================================================
-- NOTIFICATION SERVICE - PostgreSQL Database Schema (Flyway Migration)
-- =================================================================================
-- Migration: V1__Initial_Schema.sql
-- Database: epb_notification (PostgreSQL 15+)
-- Description: Notification tracking for SMS, Email, and Push notifications
-- Author: Douglas Kings Kato
-- Version: 1.0.0
-- Date: 2025-11-08
-- =================================================================================

-- =================================================================================
-- EXTENSIONS
-- =================================================================================

-- Enable UUID generation
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- =================================================================================
-- CUSTOM TYPES (PostgreSQL ENUMs)
-- =================================================================================

-- Notification type enumeration
CREATE TYPE notification_type AS ENUM (
    'SMS',
    'EMAIL',
    'PUSH',
    'IN_APP'
);

-- Notification status enumeration
CREATE TYPE notification_status AS ENUM (
    'PENDING',
    'SENT',
    'DELIVERED',
    'FAILED',
    'BOUNCED',
    'CANCELLED'
);

-- Notification priority enumeration
CREATE TYPE notification_priority AS ENUM (
    'LOW',
    'MEDIUM',
    'HIGH',
    'URGENT'
);

-- =================================================================================
-- SECTION 1: NOTIFICATION LOGS TABLE
-- =================================================================================

CREATE TABLE notification_logs (
    -- Primary Key (UUID)
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),

    -- User Reference (UUID from user-service)
    user_id UUID NOT NULL,

    -- Notification Details
    notification_type notification_type NOT NULL,
    recipient VARCHAR(255) NOT NULL COMMENT 'Phone number, email address, or device token',
    subject VARCHAR(255),
    content TEXT NOT NULL,

    -- Status Tracking
    status notification_status NOT NULL DEFAULT 'PENDING',
    priority notification_priority NOT NULL DEFAULT 'MEDIUM',
    sent_at TIMESTAMP,
    delivered_at TIMESTAMP,

    -- Retry Logic
    retry_count INTEGER NOT NULL DEFAULT 0,
    max_retries INTEGER NOT NULL DEFAULT 3,
    next_retry_at TIMESTAMP,

    -- Error Tracking
    error_message TEXT,
    error_code VARCHAR(50),

    -- Provider Details
    provider VARCHAR(50) COMMENT 'AFRICAS_TALKING, SMTP, FIREBASE, etc.',
    provider_message_id VARCHAR(255) COMMENT 'Provider tracking ID',
    provider_response JSONB COMMENT 'Full provider response',

    -- Metadata
    template_id VARCHAR(100),
    template_variables JSONB,
    tags TEXT[],
    metadata JSONB COMMENT 'Additional context',

    -- Audit Timestamps
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- =================================================================================
-- SECTION 2: INDEXES
-- =================================================================================

-- Performance indexes
CREATE INDEX idx_notification_user_status ON notification_logs(user_id, status);
CREATE INDEX idx_notification_created_at ON notification_logs(created_at DESC);
CREATE INDEX idx_notification_status ON notification_logs(status);
CREATE INDEX idx_notification_type ON notification_logs(notification_type);

-- Retry queue index (for failed notifications)
CREATE INDEX idx_notification_retry ON notification_logs(status, next_retry_at)
    WHERE status = 'FAILED' AND retry_count < max_retries;

-- Sent notifications index
CREATE INDEX idx_notification_sent ON notification_logs(sent_at DESC)
    WHERE status IN ('SENT', 'DELIVERED');

-- Provider tracking index
CREATE INDEX idx_notification_provider ON notification_logs(provider, provider_message_id)
    WHERE provider_message_id IS NOT NULL;

-- JSONB indexes
CREATE INDEX idx_notification_metadata ON notification_logs USING gin(metadata);
CREATE INDEX idx_notification_provider_response ON notification_logs USING gin(provider_response);
CREATE INDEX idx_notification_tags ON notification_logs USING gin(tags);

-- =================================================================================
-- SECTION 3: NOTIFICATION TEMPLATES TABLE
-- =================================================================================

CREATE TABLE notification_templates (
    -- Primary key
    template_id VARCHAR(100) PRIMARY KEY,

    -- Template details
    template_name VARCHAR(255) NOT NULL,
    description TEXT,
    notification_type notification_type NOT NULL,

    -- Template content
    subject_template VARCHAR(255),
    content_template TEXT NOT NULL,

    -- Variables
    required_variables TEXT[] COMMENT 'Array of required variable names',
    optional_variables TEXT[],

    -- Settings
    is_active BOOLEAN DEFAULT TRUE NOT NULL,
    default_provider VARCHAR(50),

    -- Audit fields
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by UUID,
    updated_by UUID
);

-- Indexes for templates
CREATE INDEX idx_template_type_active ON notification_templates(notification_type, is_active);
CREATE INDEX idx_template_name ON notification_templates(template_name);

-- Comments
COMMENT ON TABLE notification_templates IS 'Reusable notification templates';
COMMENT ON COLUMN notification_templates.content_template IS 'Template with placeholders like {{firstName}}';

-- =================================================================================
-- SECTION 4: NOTIFICATION PREFERENCES TABLE
-- =================================================================================

CREATE TABLE notification_preferences (
    -- Primary key
    preference_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),

    -- User reference
    user_id UUID UNIQUE NOT NULL,

    -- Channel preferences
    email_enabled BOOLEAN DEFAULT TRUE NOT NULL,
    sms_enabled BOOLEAN DEFAULT TRUE NOT NULL,
    push_enabled BOOLEAN DEFAULT TRUE NOT NULL,
    in_app_enabled BOOLEAN DEFAULT TRUE NOT NULL,

    -- Category preferences (JSONB for flexibility)
    category_preferences JSONB DEFAULT '{}'::JSONB,

    -- Quiet hours
    quiet_hours_enabled BOOLEAN DEFAULT FALSE NOT NULL,
    quiet_hours_start TIME,
    quiet_hours_end TIME,
    quiet_hours_timezone VARCHAR(50) DEFAULT 'Africa/Kampala',

    -- Frequency limits
    max_sms_per_day INTEGER DEFAULT 5,
    max_emails_per_day INTEGER DEFAULT 10,

    -- Audit fields
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Indexes
CREATE INDEX idx_preferences_user ON notification_preferences(user_id);

-- Comments
COMMENT ON TABLE notification_preferences IS 'User notification preferences and quiet hours';
COMMENT ON COLUMN notification_preferences.category_preferences IS 'JSON object with category-specific preferences';

-- =================================================================================
-- SECTION 5: NOTIFICATION BATCHES TABLE
-- =================================================================================

CREATE TABLE notification_batches (
    -- Primary key
    batch_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),

    -- Batch details
    batch_name VARCHAR(255) NOT NULL,
    description TEXT,
    notification_type notification_type NOT NULL,

    -- Status
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    total_recipients INTEGER DEFAULT 0 NOT NULL,
    sent_count INTEGER DEFAULT 0 NOT NULL,
    failed_count INTEGER DEFAULT 0 NOT NULL,

    -- Scheduling
    scheduled_at TIMESTAMP,
    started_at TIMESTAMP,
    completed_at TIMESTAMP,

    -- Template reference
    template_id VARCHAR(100),
    template_variables JSONB,

    -- Audit fields
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by UUID NOT NULL,

    FOREIGN KEY (template_id) REFERENCES notification_templates(template_id)
);

-- Indexes
CREATE INDEX idx_batch_status ON notification_batches(status);
CREATE INDEX idx_batch_scheduled ON notification_batches(scheduled_at);
CREATE INDEX idx_batch_created ON notification_batches(created_at DESC);
CREATE INDEX idx_batch_created_by ON notification_batches(created_by);

-- Comments
COMMENT ON TABLE notification_batches IS 'Bulk notification campaigns';

-- =================================================================================
-- SECTION 6: TRIGGERS FOR AUTOMATIC UPDATED_AT
-- =================================================================================

-- Function to update updated_at timestamp
CREATE OR REPLACE FUNCTION update_notification_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Apply triggers
CREATE TRIGGER trigger_update_notification_logs_updated_at
    BEFORE UPDATE ON notification_logs
    FOR EACH ROW
    EXECUTE FUNCTION update_notification_updated_at();

CREATE TRIGGER trigger_update_notification_templates_updated_at
    BEFORE UPDATE ON notification_templates
    FOR EACH ROW
    EXECUTE FUNCTION update_notification_updated_at();

CREATE TRIGGER trigger_update_notification_preferences_updated_at
    BEFORE UPDATE ON notification_preferences
    FOR EACH ROW
    EXECUTE FUNCTION update_notification_updated_at();

CREATE TRIGGER trigger_update_notification_batches_updated_at
    BEFORE UPDATE ON notification_batches
    FOR EACH ROW
    EXECUTE FUNCTION update_notification_updated_at();

-- =================================================================================
-- SECTION 7: VIEWS FOR ANALYTICS
-- =================================================================================

-- Notification statistics by type
CREATE OR REPLACE VIEW v_notification_stats_by_type AS
SELECT
    notification_type,
    status,
    COUNT(*) AS notification_count,
    COUNT(*) FILTER (WHERE sent_at >= CURRENT_TIMESTAMP - INTERVAL '24 hours') AS count_last_24h,
    COUNT(*) FILTER (WHERE sent_at >= CURRENT_TIMESTAMP - INTERVAL '7 days') AS count_last_7d,
    AVG(retry_count) AS avg_retry_count,
    COUNT(*) FILTER (WHERE status = 'FAILED') AS failed_count
FROM notification_logs
WHERE created_at >= CURRENT_TIMESTAMP - INTERVAL '30 days'
GROUP BY notification_type, status
ORDER BY notification_type, status;

COMMENT ON VIEW v_notification_stats_by_type IS 'Notification delivery statistics by type';

-- Failed notifications requiring retry
CREATE OR REPLACE VIEW v_notifications_pending_retry AS
SELECT
    id,
    user_id,
    notification_type,
    recipient,
    status,
    retry_count,
    max_retries,
    next_retry_at,
    error_message,
    created_at
FROM notification_logs
WHERE status = 'FAILED'
AND retry_count < max_retries
AND (next_retry_at IS NULL OR next_retry_at <= CURRENT_TIMESTAMP)
ORDER BY next_retry_at NULLS FIRST, created_at;

COMMENT ON VIEW v_notifications_pending_retry IS 'Notifications ready for retry';

-- User notification summary
CREATE OR REPLACE VIEW v_user_notification_summary AS
SELECT
    user_id,
    COUNT(*) AS total_notifications,
    COUNT(*) FILTER (WHERE status = 'DELIVERED') AS delivered_count,
    COUNT(*) FILTER (WHERE status = 'FAILED') AS failed_count,
    COUNT(*) FILTER (WHERE notification_type = 'EMAIL') AS email_count,
    COUNT(*) FILTER (WHERE notification_type = 'SMS') AS sms_count,
    COUNT(*) FILTER (WHERE notification_type = 'PUSH') AS push_count,
    MAX(sent_at) AS last_notification_sent,
    COUNT(*) FILTER (WHERE sent_at >= CURRENT_TIMESTAMP - INTERVAL '24 hours') AS count_last_24h
FROM notification_logs
WHERE created_at >= CURRENT_TIMESTAMP - INTERVAL '30 days'
GROUP BY user_id;

COMMENT ON VIEW v_user_notification_summary IS 'Per-user notification statistics';

-- =================================================================================
-- SECTION 8: UTILITY FUNCTIONS
-- =================================================================================

-- Function to get retry delay (exponential backoff)
CREATE OR REPLACE FUNCTION calculate_retry_delay(retry_count INTEGER)
RETURNS INTERVAL AS $$
BEGIN
    -- Exponential backoff: 1min, 5min, 15min, 30min, 1hour
    RETURN CASE
        WHEN retry_count = 0 THEN INTERVAL '1 minute'
        WHEN retry_count = 1 THEN INTERVAL '5 minutes'
        WHEN retry_count = 2 THEN INTERVAL '15 minutes'
        WHEN retry_count = 3 THEN INTERVAL '30 minutes'
        ELSE INTERVAL '1 hour'
    END;
END;
$$ LANGUAGE plpgsql IMMUTABLE;

COMMENT ON FUNCTION calculate_retry_delay(INTEGER) IS 'Calculates exponential backoff delay for retries';

-- Function to clean old notifications
CREATE OR REPLACE FUNCTION cleanup_old_notifications(days_to_keep INTEGER DEFAULT 90)
RETURNS TABLE(
    deleted_count BIGINT,
    message TEXT
) AS $$
DECLARE
    v_deleted_count BIGINT;
BEGIN
    -- Delete old delivered/cancelled notifications
    DELETE FROM notification_logs
    WHERE status IN ('DELIVERED', 'CANCELLED', 'BOUNCED')
    AND created_at < CURRENT_TIMESTAMP - (days_to_keep || ' days')::INTERVAL;

    GET DIAGNOSTICS v_deleted_count = ROW_COUNT;

    RETURN QUERY SELECT
        v_deleted_count,
        FORMAT('Deleted %s old notifications older than %s days', v_deleted_count, days_to_keep);
END;
$$ LANGUAGE plpgsql;

COMMENT ON FUNCTION cleanup_old_notifications(INTEGER) IS 'Cleans up old delivered/cancelled notifications';

-- =================================================================================
-- SECTION 9: SAMPLE TEMPLATES (Development)
-- =================================================================================

-- Sample notification templates
INSERT INTO notification_templates (template_id, template_name, notification_type, subject_template, content_template, required_variables) VALUES
('WELCOME_EMAIL', 'Welcome Email', 'EMAIL',
 'Welcome to Entrepreneurship Booster Platform, {{firstName}}!',
 'Hi {{firstName}},

Welcome to the Entrepreneurship Booster Platform! We''re excited to have you join our community of young entrepreneurs.

Get started by completing your profile and exploring opportunities.

Best regards,
EBP Team',
 ARRAY['firstName']),

('PASSWORD_RESET_SMS', 'Password Reset SMS', 'SMS',
 NULL,
 'Your EBP password reset code is: {{resetCode}}. Valid for 15 minutes.',
 ARRAY['resetCode']),

('APPLICATION_APPROVED', 'Application Approved', 'EMAIL',
 'Your application has been approved!',
 'Dear {{firstName}},

Congratulations! Your application for {{opportunityTitle}} has been approved.

Next steps: {{nextSteps}}

Best regards,
EBP Team',
 ARRAY['firstName', 'opportunityTitle', 'nextSteps']);

-- =================================================================================
-- SECTION 10: COMMENTS
-- =================================================================================

-- Table comments
COMMENT ON TABLE notification_logs IS 'Notification delivery tracking for SMS, Email, and Push notifications';
COMMENT ON COLUMN notification_logs.id IS 'Primary key (UUID)';
COMMENT ON COLUMN notification_logs.user_id IS 'Foreign key to users table (UUID)';
COMMENT ON COLUMN notification_logs.recipient IS 'Phone number, email address, or device token';
COMMENT ON COLUMN notification_logs.status IS 'Delivery status (PENDING, SENT, DELIVERED, FAILED, BOUNCED, CANCELLED)';
COMMENT ON COLUMN notification_logs.retry_count IS 'Number of retry attempts made';
COMMENT ON COLUMN notification_logs.next_retry_at IS 'Scheduled time for next retry attempt';
COMMENT ON COLUMN notification_logs.provider IS 'Delivery provider (AFRICAS_TALKING, SMTP, FIREBASE)';
COMMENT ON COLUMN notification_logs.provider_message_id IS 'Message ID from provider for tracking';

-- =================================================================================
-- END OF MIGRATION
-- =================================================================================