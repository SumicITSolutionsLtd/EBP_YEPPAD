-- =============================================================================
-- MENTOR SERVICE - Initial Database Schema (PostgreSQL)
-- =============================================================================
-- Migration: V1__Initial_Schema.sql
-- Description: Creates all tables for mentor service using UUIDs
-- Database: PostgreSQL 12+
-- Author: Douglas Kings Kato
-- Date: 2025-11-06
-- =============================================================================

-- Enable UUID extension
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- =============================================================================
-- CUSTOM TYPES (PostgreSQL ENUMs)
-- =============================================================================

-- Session status enumeration
CREATE TYPE session_status AS ENUM (
    'SCHEDULED',
    'IN_PROGRESS',
    'COMPLETED',
    'CANCELLED',
    'NO_SHOW'
);

-- Day of week enumeration
CREATE TYPE day_of_week AS ENUM (
    'MONDAY',
    'TUESDAY',
    'WEDNESDAY',
    'THURSDAY',
    'FRIDAY',
    'SATURDAY',
    'SUNDAY'
);

-- Reminder type enumeration
CREATE TYPE reminder_type AS ENUM (
    '_24_HOURS',
    '_1_HOUR',
    '_15_MINUTES'
);

-- Review type enumeration
CREATE TYPE review_type AS ENUM (
    'MENTOR_SESSION',
    'SERVICE_DELIVERY',
    'GENERAL'
);

-- Availability status enumeration
CREATE TYPE availability_status AS ENUM (
    'AVAILABLE',
    'BUSY',
    'ON_LEAVE'
);

-- =============================================================================
-- MENTOR AVAILABILITY TABLE
-- =============================================================================

CREATE TABLE mentor_availability (
    -- Primary key using UUID
    availability_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),

    -- Mentor reference (UUID from user-service)
    mentor_id UUID NOT NULL,

    -- Weekly schedule fields
    day_of_week day_of_week NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,

    -- Active status
    is_active BOOLEAN NOT NULL DEFAULT TRUE,

    -- Audit fields
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- Constraints
    CONSTRAINT chk_time_order CHECK (start_time < end_time)
);

-- Indexes for mentor_availability
CREATE INDEX idx_availability_mentor_day
    ON mentor_availability(mentor_id, day_of_week, is_active);

CREATE INDEX idx_availability_active
    ON mentor_availability(is_active) WHERE is_active = TRUE;

-- Comments
COMMENT ON TABLE mentor_availability IS 'Mentor weekly recurring availability schedule';
COMMENT ON COLUMN mentor_availability.mentor_id IS 'UUID reference to user in user-service';

-- =============================================================================
-- MENTORSHIP SESSIONS TABLE
-- =============================================================================

CREATE TABLE mentorship_sessions (
    -- Primary key using UUID
    session_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),

    -- Participant references (UUIDs from user-service)
    mentor_id UUID NOT NULL,
    mentee_id UUID NOT NULL,

    -- Session details
    session_datetime TIMESTAMP NOT NULL,
    duration_minutes INTEGER NOT NULL DEFAULT 60,
    topic VARCHAR(255),

    -- Status tracking
    status session_status NOT NULL DEFAULT 'SCHEDULED',

    -- Session notes (private to each party)
    mentor_notes TEXT,
    mentee_notes TEXT,

    -- Audit fields
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- Constraints
    CONSTRAINT chk_different_participants CHECK (mentor_id != mentee_id),
    CONSTRAINT chk_positive_duration CHECK (duration_minutes > 0)
);

-- Indexes for mentorship_sessions
CREATE INDEX idx_session_mentor_status_datetime
    ON mentorship_sessions(mentor_id, status, session_datetime);

CREATE INDEX idx_session_mentee_status_datetime
    ON mentorship_sessions(mentee_id, status, session_datetime);

CREATE INDEX idx_session_datetime
    ON mentorship_sessions(session_datetime);

CREATE INDEX idx_session_status
    ON mentorship_sessions(status);

-- Comments
COMMENT ON TABLE mentorship_sessions IS 'Mentorship session scheduling and tracking';
COMMENT ON COLUMN mentorship_sessions.mentor_id IS 'UUID reference to mentor user';
COMMENT ON COLUMN mentorship_sessions.mentee_id IS 'UUID reference to mentee user';

-- =============================================================================
-- SESSION REMINDERS TABLE
-- =============================================================================

CREATE TABLE session_reminders (
    -- Primary key using UUID
    reminder_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),

    -- Session reference
    session_id UUID NOT NULL REFERENCES mentorship_sessions(session_id) ON DELETE CASCADE,

    -- Reminder details
    reminder_type reminder_type NOT NULL,
    scheduled_time TIMESTAMP NOT NULL,

    -- Delivery status tracking
    sent_to_mentor BOOLEAN NOT NULL DEFAULT FALSE,
    sent_to_mentee BOOLEAN NOT NULL DEFAULT FALSE,
    sent_at TIMESTAMP,

    -- Audit fields
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Indexes for session_reminders
CREATE INDEX idx_reminder_session_scheduled
    ON session_reminders(session_id, scheduled_time);

CREATE INDEX idx_reminder_scheduled_pending
    ON session_reminders(scheduled_time)
    WHERE sent_to_mentor = FALSE OR sent_to_mentee = FALSE;

CREATE INDEX idx_reminder_type
    ON session_reminders(reminder_type);

-- Comments
COMMENT ON TABLE session_reminders IS 'Automated session reminder tracking';

-- =============================================================================
-- REVIEWS TABLE
-- =============================================================================

CREATE TABLE reviews (
    -- Primary key using UUID
    review_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),

    -- Participant references (UUIDs)
    reviewer_id UUID NOT NULL,  -- The person giving the review
    reviewee_id UUID NOT NULL,  -- The person being reviewed

    -- Session reference (optional - can review outside of sessions)
    session_id UUID REFERENCES mentorship_sessions(session_id) ON DELETE SET NULL,

    -- Review content
    rating SMALLINT NOT NULL CHECK (rating BETWEEN 1 AND 5),
    comment TEXT,
    review_type review_type NOT NULL DEFAULT 'GENERAL',

    -- Moderation fields
    is_approved BOOLEAN NOT NULL DEFAULT TRUE,
    is_flagged BOOLEAN NOT NULL DEFAULT FALSE,

    -- Audit fields
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,

    -- Constraints
    CONSTRAINT chk_different_users CHECK (reviewer_id != reviewee_id)
);

-- Indexes for reviews
CREATE INDEX idx_review_reviewee_type_approved
    ON reviews(reviewee_id, review_type, is_approved);

CREATE INDEX idx_review_session_id
    ON reviews(session_id);

CREATE INDEX idx_review_rating
    ON reviews(rating);

CREATE INDEX idx_review_created
    ON reviews(created_at DESC);

-- Comments
COMMENT ON TABLE reviews IS 'Review system for mentors and services';
COMMENT ON COLUMN reviews.reviewer_id IS 'UUID of user giving the review';
COMMENT ON COLUMN reviews.reviewee_id IS 'UUID of user being reviewed';

-- =============================================================================
-- MENTOR PROFILES TABLE (Denormalized from user-service)
-- =============================================================================
-- This table caches mentor profile data from user-service for performance

CREATE TABLE mentor_profiles_cache (
    -- Primary key (matches user_id from user-service)
    mentor_id UUID PRIMARY KEY,

    -- Basic information
    first_name VARCHAR(50),
    last_name VARCHAR(50),
    bio TEXT,
    area_of_expertise TEXT,
    experience_years INTEGER,

    -- Availability status
    availability_status availability_status DEFAULT 'AVAILABLE',

    -- Verification
    is_verified BOOLEAN DEFAULT FALSE,

    -- Media
    profile_picture_url VARCHAR(255),

    -- Cache metadata
    cached_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    cache_expires_at TIMESTAMP NOT NULL DEFAULT (CURRENT_TIMESTAMP + INTERVAL '30 minutes'),

    -- Audit fields
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Indexes for mentor_profiles_cache
CREATE INDEX idx_mentor_cache_availability
    ON mentor_profiles_cache(availability_status);

CREATE INDEX idx_mentor_cache_verified
    ON mentor_profiles_cache(is_verified);

CREATE INDEX idx_mentor_cache_expires
    ON mentor_profiles_cache(cache_expires_at);

-- Comments
COMMENT ON TABLE mentor_profiles_cache IS 'Cached mentor profile data from user-service for performance';
COMMENT ON COLUMN mentor_profiles_cache.cache_expires_at IS 'TTL for cache invalidation';

-- =============================================================================
-- TRIGGERS FOR AUTOMATIC UPDATED_AT
-- =============================================================================

-- Function to update updated_at timestamp
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Apply trigger to all tables with updated_at
CREATE TRIGGER update_mentor_availability_updated_at
    BEFORE UPDATE ON mentor_availability
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_mentorship_sessions_updated_at
    BEFORE UPDATE ON mentorship_sessions
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_reviews_updated_at
    BEFORE UPDATE ON reviews
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_mentor_profiles_cache_updated_at
    BEFORE UPDATE ON mentor_profiles_cache
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- =============================================================================
-- VIEWS FOR ANALYTICS
-- =============================================================================

-- Active sessions view
CREATE OR REPLACE VIEW v_active_sessions AS
SELECT
    s.session_id,
    s.mentor_id,
    s.mentee_id,
    s.session_datetime,
    s.duration_minutes,
    s.topic,
    s.status,
    s.created_at,
    EXTRACT(EPOCH FROM (s.session_datetime - CURRENT_TIMESTAMP))/3600 AS hours_until_session
FROM mentorship_sessions s
WHERE s.status IN ('SCHEDULED', 'IN_PROGRESS')
AND s.session_datetime >= CURRENT_TIMESTAMP
ORDER BY s.session_datetime;

COMMENT ON VIEW v_active_sessions IS 'Active and upcoming mentorship sessions';

-- Mentor statistics view
CREATE OR REPLACE VIEW v_mentor_statistics AS
SELECT
    m.mentor_id,
    COUNT(DISTINCT s.session_id) AS total_sessions,
    COUNT(DISTINCT s.session_id) FILTER (WHERE s.status = 'COMPLETED') AS completed_sessions,
    COUNT(DISTINCT s.session_id) FILTER (WHERE s.status = 'CANCELLED') AS cancelled_sessions,
    COUNT(DISTINCT s.session_id) FILTER (WHERE s.status = 'NO_SHOW') AS no_show_sessions,
    ROUND(AVG(r.rating), 2) AS average_rating,
    COUNT(DISTINCT r.review_id) AS total_reviews,
    COUNT(DISTINCT s.mentee_id) AS unique_mentees
FROM mentor_profiles_cache m
LEFT JOIN mentorship_sessions s ON m.mentor_id = s.mentor_id
LEFT JOIN reviews r ON m.mentor_id = r.reviewee_id AND r.review_type = 'MENTOR_SESSION'
GROUP BY m.mentor_id;

COMMENT ON VIEW v_mentor_statistics IS 'Aggregated mentor performance statistics';

-- =============================================================================
-- INITIAL DATA (Optional)
-- =============================================================================

-- Sample availability slots for testing (using placeholder UUIDs)
-- In production, these would be created via API calls

-- Note: Replace these UUIDs with actual mentor UUIDs from user-service
-- INSERT INTO mentor_availability (mentor_id, day_of_week, start_time, end_time)
-- VALUES
-- ('00000000-0000-0000-0000-000000000001', 'MONDAY', '09:00:00', '17:00:00'),
-- ('00000000-0000-0000-0000-000000000001', 'WEDNESDAY', '09:00:00', '17:00:00'),
-- ('00000000-0000-0000-0000-000000000001', 'FRIDAY', '09:00:00', '17:00:00');

-- =============================================================================
-- GRANTS (adjust as needed for your database user)
-- =============================================================================

-- GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO mentor_service_user;
-- GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO mentor_service_user;

-- =============================================================================
-- VERIFICATION
-- =============================================================================

-- Verify tables created
DO $$
DECLARE
    table_count INTEGER;
BEGIN
    SELECT COUNT(*) INTO table_count
    FROM information_schema.tables
    WHERE table_schema = 'public'
    AND table_type = 'BASE TABLE';

    RAISE NOTICE 'Created % tables for mentor-service', table_count;
END $$;

-- =============================================================================
-- END OF MIGRATION
-- =============================================================================