-- =================================================================================
-- Entrepreneurship Booster Platform - Complete PostgreSQL Database Schema
-- Version: 1.0.0
-- Date: 2025-11-02
-- Description: Production-ready schema with audit trails, soft deletes, and analytics
-- Database: PostgreSQL 12+
-- Author: Douglas Kings Kato
-- =================================================================================

-- =================================================================================
-- ENABLE REQUIRED EXTENSIONS
-- =================================================================================

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";      -- UUID generation
CREATE EXTENSION IF NOT EXISTS "pg_trgm";        -- Text search & similarity
CREATE EXTENSION IF NOT EXISTS "btree_gin";      -- Better indexing for JSONB

-- =================================================================================
-- SECTION 1: CUSTOM TYPES (PostgreSQL ENUMs)
-- =================================================================================

-- User role types
CREATE TYPE user_role AS ENUM (
    'YOUTH', 'NGO', 'FUNDER', 'SERVICE_PROVIDER', 'MENTOR', 'ADMIN',
    'COMPANY', 'RECRUITER', 'GOVERNMENT'
);

-- Opportunity types
CREATE TYPE opportunity_type AS ENUM ('GRANT', 'LOAN', 'JOB', 'TRAINING', 'SKILL_MARKET');

-- Application status
CREATE TYPE application_status AS ENUM ('PENDING', 'UNDER_REVIEW', 'APPROVED', 'REJECTED', 'WITHDRAWN');

-- Opportunity status
CREATE TYPE opportunity_status AS ENUM ('DRAFT', 'OPEN', 'CLOSED', 'IN_REVIEW', 'COMPLETED');

-- Gender types
CREATE TYPE gender_type AS ENUM ('MALE', 'FEMALE', 'OTHER');

-- Session status
CREATE TYPE session_status AS ENUM ('SCHEDULED', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED', 'NO_SHOW');

-- Day of week
CREATE TYPE day_of_week AS ENUM ('MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY', 'SUNDAY');

-- Reminder types
CREATE TYPE reminder_type AS ENUM ('24_HOURS', '1_HOUR', '15_MINUTES');

-- Review types
CREATE TYPE review_type AS ENUM ('MENTOR_SESSION', 'SERVICE_DELIVERY', 'GENERAL');

-- Post types
CREATE TYPE post_type AS ENUM ('FORUM_QUESTION', 'SUCCESS_STORY', 'ARTICLE', 'AUDIO_QUESTION');

-- Content types
CREATE TYPE content_type AS ENUM ('AUDIO', 'VIDEO', 'TEXT', 'MIXED');

-- Availability status
CREATE TYPE availability_status AS ENUM ('AVAILABLE', 'BUSY', 'ON_LEAVE');

-- File categories
CREATE TYPE file_category AS ENUM ('PROFILE_PICTURE', 'DOCUMENT', 'AUDIO_MODULE', 'APPLICATION_ATTACHMENT');

-- Notification types
CREATE TYPE notification_type AS ENUM ('SMS', 'EMAIL', 'PUSH');

-- Notification status
CREATE TYPE notification_status AS ENUM ('PENDING', 'SENT', 'DELIVERED', 'FAILED');

-- Audit status
CREATE TYPE audit_status AS ENUM ('SUCCESS', 'FAILED', 'PARTIAL');

-- Interest source
CREATE TYPE interest_source AS ENUM ('USER_SELECTED', 'AI_INFERRED', 'ACTIVITY_BASED', 'SURVEY', 'IMPORTED');

-- Interest level
CREATE TYPE interest_level AS ENUM ('LOW', 'MEDIUM', 'HIGH');

-- =================================================================================
-- SECTION 2: CORE AUTHENTICATION & USER MANAGEMENT
-- =================================================================================

-- Users Table: Central authentication for all platforms (Web + USSD)
CREATE TABLE users (
    user_id BIGSERIAL PRIMARY KEY,
    email VARCHAR(100) UNIQUE NOT NULL,
    phone_number VARCHAR(20) UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role user_role NOT NULL,

    -- Account Security & Verification
    is_active BOOLEAN DEFAULT TRUE NOT NULL,
    is_verified BOOLEAN DEFAULT FALSE NOT NULL,
    email_verified BOOLEAN DEFAULT FALSE NOT NULL,
    phone_verified BOOLEAN DEFAULT FALSE NOT NULL,

    -- Security Tokens
    verification_token VARCHAR(255),
    reset_token VARCHAR(255),
    reset_token_expiry TIMESTAMP,

    -- Security Audit
    last_login TIMESTAMP,
    failed_login_attempts INTEGER DEFAULT 0 NOT NULL,
    account_locked_until TIMESTAMP,

    -- Audit Fields
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    created_by BIGINT,
    updated_by BIGINT,

    -- Soft Delete
    is_deleted BOOLEAN DEFAULT FALSE NOT NULL,
    deleted_at TIMESTAMP,

    -- Uganda phone validation constraint
    CONSTRAINT chk_phone_format CHECK (
        phone_number IS NULL OR
        phone_number ~ '^\+?256[0-9]{9}$|^0[0-9]{9}$'
    )
);

-- Indexes for users table
CREATE INDEX idx_users_phone_active ON users(phone_number, is_active) WHERE is_deleted = FALSE;
CREATE INDEX idx_users_email_active ON users(email, is_active) WHERE is_deleted = FALSE;
CREATE INDEX idx_users_role ON users(role, is_active) WHERE is_deleted = FALSE;
CREATE INDEX idx_users_created_at ON users(created_at DESC);
CREATE INDEX idx_users_deleted ON users(is_deleted, deleted_at);

-- Comments
COMMENT ON TABLE users IS 'Central user authentication for web and USSD platforms';
COMMENT ON COLUMN users.email IS 'Primary web login identifier';
COMMENT ON COLUMN users.phone_number IS 'USSD login identifier - Uganda format';
COMMENT ON COLUMN users.password_hash IS 'BCrypt hashed password';

-- Trigger for updated_at
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER update_users_updated_at BEFORE UPDATE ON users
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- Refresh Tokens: JWT management
CREATE TABLE refresh_tokens (
    id BIGSERIAL PRIMARY KEY,
    token VARCHAR(500) NOT NULL UNIQUE,
    user_id BIGINT NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    user_email VARCHAR(255) NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    revoked BOOLEAN DEFAULT FALSE NOT NULL
);

CREATE INDEX idx_refresh_token ON refresh_tokens(token);
CREATE INDEX idx_refresh_user_active ON refresh_tokens(user_id, revoked, expires_at);
CREATE INDEX idx_refresh_expires ON refresh_tokens(expires_at) WHERE revoked = FALSE;

COMMENT ON TABLE refresh_tokens IS 'JWT refresh token management with expiry tracking';

-- Password Reset Tokens
CREATE TABLE password_reset_tokens (
    id BIGSERIAL PRIMARY KEY,
    token VARCHAR(255) NOT NULL UNIQUE,
    user_id BIGINT NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    user_email VARCHAR(255) NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    used BOOLEAN DEFAULT FALSE NOT NULL
);

CREATE INDEX idx_password_token ON password_reset_tokens(token);
CREATE INDEX idx_password_user_unused ON password_reset_tokens(user_id, used, expires_at);

COMMENT ON TABLE password_reset_tokens IS 'Password reset token management';

-- =================================================================================
-- SECTION 3: USER PROFILES (Role-Specific)
-- =================================================================================

-- Youth Profiles
CREATE TABLE youth_profiles (
    profile_id BIGSERIAL PRIMARY KEY,
    user_id BIGINT UNIQUE NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,

    -- Personal Information
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    gender gender_type,
    date_of_birth DATE,

    -- Location & Demographics
    district VARCHAR(50),
    location VARCHAR(100),
    age_group VARCHAR(20),

    -- Professional Information
    profession VARCHAR(100),
    education_level VARCHAR(50),
    business_stage VARCHAR(50),

    -- Profile Content
    bio TEXT,
    description TEXT,
    skills TEXT,
    interests TEXT,

    -- Media
    profile_picture_url VARCHAR(255),

    -- Accessibility
    has_disability BOOLEAN DEFAULT FALSE NOT NULL,
    disability_details TEXT,

    -- Audit Fields
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    created_by BIGINT,
    updated_by BIGINT,
    is_deleted BOOLEAN DEFAULT FALSE NOT NULL,
    deleted_at TIMESTAMP
);

CREATE INDEX idx_youth_district ON youth_profiles(district) WHERE is_deleted = FALSE;
CREATE INDEX idx_youth_business_stage ON youth_profiles(business_stage) WHERE is_deleted = FALSE;
CREATE INDEX idx_youth_user_id ON youth_profiles(user_id);

CREATE TRIGGER update_youth_profiles_updated_at BEFORE UPDATE ON youth_profiles
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

COMMENT ON TABLE youth_profiles IS 'Youth user profiles with business information';
COMMENT ON COLUMN youth_profiles.district IS 'Ugandan district location';
COMMENT ON COLUMN youth_profiles.business_stage IS 'Idea Phase, Early Stage, Growth Stage, etc.';

-- NGO Profiles
CREATE TABLE ngo_profiles (
    ngo_id BIGSERIAL PRIMARY KEY,
    user_id BIGINT UNIQUE NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,

    organisation_name VARCHAR(150) NOT NULL,
    location VARCHAR(100),
    description TEXT,

    -- Verification
    is_verified BOOLEAN DEFAULT FALSE NOT NULL,
    verification_date TIMESTAMP,
    verified_by BIGINT REFERENCES users(user_id) ON DELETE SET NULL,

    -- Audit Fields
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    created_by BIGINT,
    updated_by BIGINT,
    is_deleted BOOLEAN DEFAULT FALSE NOT NULL,
    deleted_at TIMESTAMP
);

CREATE INDEX idx_ngo_verified ON ngo_profiles(is_verified) WHERE is_deleted = FALSE;
CREATE INDEX idx_ngo_user_id ON ngo_profiles(user_id);

CREATE TRIGGER update_ngo_profiles_updated_at BEFORE UPDATE ON ngo_profiles
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

COMMENT ON TABLE ngo_profiles IS 'NGO organization profiles with verification';

-- Service Provider Profiles
CREATE TABLE service_provider_profiles (
    provider_id BIGSERIAL PRIMARY KEY,
    user_id BIGINT UNIQUE NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,

    provider_name VARCHAR(150) NOT NULL,
    location VARCHAR(100),
    area_of_expertise TEXT NOT NULL,

    -- Verification
    is_verified BOOLEAN DEFAULT FALSE NOT NULL,
    verification_date TIMESTAMP,
    verified_by BIGINT REFERENCES users(user_id) ON DELETE SET NULL,

    -- Audit Fields
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    created_by BIGINT,
    updated_by BIGINT,
    is_deleted BOOLEAN DEFAULT FALSE NOT NULL,
    deleted_at TIMESTAMP
);

CREATE INDEX idx_provider_user_id ON service_provider_profiles(user_id);
CREATE INDEX idx_provider_verified ON service_provider_profiles(is_verified) WHERE is_deleted = FALSE;

CREATE TRIGGER update_provider_profiles_updated_at BEFORE UPDATE ON service_provider_profiles
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

COMMENT ON TABLE service_provider_profiles IS 'Service providers with NGO verification';

-- Funder Profiles
CREATE TABLE funder_profiles (
    funder_id BIGSERIAL PRIMARY KEY,
    user_id BIGINT UNIQUE NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,

    funder_name VARCHAR(150) NOT NULL,
    funding_focus TEXT,

    -- Audit Fields
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    created_by BIGINT,
    updated_by BIGINT,
    is_deleted BOOLEAN DEFAULT FALSE NOT NULL,
    deleted_at TIMESTAMP
);

CREATE INDEX idx_funder_user_id ON funder_profiles(user_id);

CREATE TRIGGER update_funder_profiles_updated_at BEFORE UPDATE ON funder_profiles
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

COMMENT ON TABLE funder_profiles IS 'Funder organization profiles';
COMMENT ON COLUMN funder_profiles.funding_focus IS 'Funding interests and criteria';

-- Mentor Profiles
CREATE TABLE mentor_profiles (
    mentor_profile_id BIGSERIAL PRIMARY KEY,
    user_id BIGINT UNIQUE NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,

    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    bio TEXT,
    area_of_expertise TEXT NOT NULL,
    experience_years INTEGER,

    -- Availability & Verification
    availability_status availability_status DEFAULT 'AVAILABLE' NOT NULL,
    is_verified BOOLEAN DEFAULT FALSE NOT NULL,

    -- Media
    profile_picture_url VARCHAR(255),

    -- Audit Fields
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    created_by BIGINT,
    updated_by BIGINT,
    is_deleted BOOLEAN DEFAULT FALSE NOT NULL,
    deleted_at TIMESTAMP
);

CREATE INDEX idx_mentor_availability ON mentor_profiles(availability_status) WHERE is_deleted = FALSE;
CREATE INDEX idx_mentor_user_id ON mentor_profiles(user_id);

CREATE TRIGGER update_mentor_profiles_updated_at BEFORE UPDATE ON mentor_profiles
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

COMMENT ON TABLE mentor_profiles IS 'Mentor profiles with availability tracking';

-- =================================================================================
-- SECTION 4: OPPORTUNITIES & APPLICATIONS
-- =================================================================================

-- Opportunities
CREATE TABLE opportunities (
    opportunity_id BIGSERIAL PRIMARY KEY,
    posted_by_id BIGINT NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,

    -- Classification
    opportunity_type opportunity_type NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    requirements TEXT,

    -- Management
    status opportunity_status DEFAULT 'DRAFT' NOT NULL,
    application_deadline TIMESTAMP,

    -- Financial details
    funding_amount DECIMAL(15,2),
    currency VARCHAR(3) DEFAULT 'UGX',

    -- Metadata
    is_featured BOOLEAN DEFAULT FALSE NOT NULL,
    view_count INTEGER DEFAULT 0 NOT NULL,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE INDEX idx_opp_type_status ON opportunities(opportunity_type, status);
CREATE INDEX idx_opp_deadline ON opportunities(application_deadline) WHERE status = 'OPEN';
CREATE INDEX idx_opp_posted_by ON opportunities(posted_by_id);
CREATE INDEX idx_opp_featured ON opportunities(is_featured, status) WHERE is_featured = TRUE;

CREATE TRIGGER update_opportunities_updated_at BEFORE UPDATE ON opportunities
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

COMMENT ON TABLE opportunities IS 'Opportunities posted by NGOs, funders, and partners';

-- Applications
CREATE TABLE applications (
    application_id BIGSERIAL PRIMARY KEY,
    opportunity_id BIGINT NOT NULL REFERENCES opportunities(opportunity_id) ON DELETE CASCADE,
    applicant_id BIGINT NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,

    status application_status DEFAULT 'PENDING' NOT NULL,

    -- Review process
    reviewed_by_id BIGINT REFERENCES users(user_id) ON DELETE SET NULL,
    review_notes TEXT,
    reviewed_at TIMESTAMP,
    application_content TEXT,

    submitted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,

    CONSTRAINT unique_application UNIQUE (opportunity_id, applicant_id)
);

CREATE INDEX idx_app_user_status ON applications(applicant_id, status);
CREATE INDEX idx_app_opp_status ON applications(opportunity_id, status);
CREATE INDEX idx_app_submitted ON applications(submitted_at DESC);

CREATE TRIGGER update_applications_updated_at BEFORE UPDATE ON applications
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

COMMENT ON TABLE applications IS 'User applications with review tracking';

-- =================================================================================
-- SECTION 5: MENTORSHIP SYSTEM
-- =================================================================================

-- Mentorship Sessions
CREATE TABLE mentorship_sessions (
    session_id BIGSERIAL PRIMARY KEY,
    mentor_id BIGINT NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    mentee_id BIGINT NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,

    session_datetime TIMESTAMP NOT NULL,
    duration_minutes INTEGER DEFAULT 60 NOT NULL,
    topic VARCHAR(255),

    status session_status DEFAULT 'SCHEDULED' NOT NULL,

    -- Session notes
    mentor_notes TEXT,
    mentee_notes TEXT,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,

    CONSTRAINT chk_different_participants CHECK (mentor_id != mentee_id)
);

CREATE INDEX idx_session_mentor_status_datetime ON mentorship_sessions(mentor_id, status, session_datetime);
CREATE INDEX idx_session_mentee_status_datetime ON mentorship_sessions(mentee_id, status, session_datetime);
CREATE INDEX idx_session_datetime ON mentorship_sessions(session_datetime);

CREATE TRIGGER update_sessions_updated_at BEFORE UPDATE ON mentorship_sessions
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

COMMENT ON TABLE mentorship_sessions IS 'Mentorship session scheduling and tracking';

-- Mentor Availability
CREATE TABLE mentor_availability (
    availability_id BIGSERIAL PRIMARY KEY,
    mentor_id BIGINT NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,

    -- Weekly schedule
    day_of_week day_of_week NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,

    is_active BOOLEAN DEFAULT TRUE NOT NULL,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,

    CONSTRAINT chk_time_order CHECK (start_time < end_time)
);

CREATE INDEX idx_availability_mentor_day ON mentor_availability(mentor_id, day_of_week, is_active);

CREATE TRIGGER update_availability_updated_at BEFORE UPDATE ON mentor_availability
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

COMMENT ON TABLE mentor_availability IS 'Mentor weekly availability schedule';

-- Session Reminders
CREATE TABLE session_reminders (
    reminder_id BIGSERIAL PRIMARY KEY,
    session_id BIGINT NOT NULL REFERENCES mentorship_sessions(session_id) ON DELETE CASCADE,

    reminder_type reminder_type NOT NULL,
    scheduled_time TIMESTAMP NOT NULL,

    -- Delivery status
    sent_to_mentor BOOLEAN DEFAULT FALSE NOT NULL,
    sent_to_mentee BOOLEAN DEFAULT FALSE NOT NULL,
    sent_at TIMESTAMP,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE INDEX idx_reminder_session_scheduled ON session_reminders(session_id, scheduled_time);
CREATE INDEX idx_reminder_scheduled_pending ON session_reminders(scheduled_time)
    WHERE sent_to_mentor = FALSE OR sent_to_mentee = FALSE;

COMMENT ON TABLE session_reminders IS 'Automated session reminder tracking';

-- Reviews
CREATE TABLE reviews (
    review_id BIGSERIAL PRIMARY KEY,
    reviewer_id BIGINT NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    reviewee_id BIGINT NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,

    rating SMALLINT NOT NULL CHECK (rating BETWEEN 1 AND 5),
    comment TEXT,
    review_type review_type DEFAULT 'GENERAL' NOT NULL,
    session_id BIGINT REFERENCES mentorship_sessions(session_id) ON DELETE SET NULL,

    -- Moderation
    is_approved BOOLEAN DEFAULT TRUE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,

    CONSTRAINT chk_different_users CHECK (reviewer_id != reviewee_id)
);

CREATE INDEX idx_review_reviewee_type_approved ON reviews(reviewee_id, review_type, is_approved);
CREATE INDEX idx_review_session_id ON reviews(session_id);
CREATE INDEX idx_review_rating ON reviews(rating);

COMMENT ON TABLE reviews IS 'Review system for mentors and services';

-- =================================================================================
-- SECTION 6: CONTENT & COMMUNITY
-- =================================================================================

-- Posts
CREATE TABLE posts (
    post_id BIGSERIAL PRIMARY KEY,
    author_id BIGINT NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,

    post_type post_type NOT NULL,
    title VARCHAR(255),
    content TEXT,

    -- Moderation
    is_approved BOOLEAN DEFAULT TRUE NOT NULL,
    is_active BOOLEAN DEFAULT TRUE NOT NULL,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE INDEX idx_post_type_approved ON posts(post_type, is_approved) WHERE is_active = TRUE;
CREATE INDEX idx_post_author ON posts(author_id);
CREATE INDEX idx_post_created ON posts(created_at DESC);

COMMENT ON TABLE posts IS 'Community posts and content sharing';

-- Comments
CREATE TABLE comments (
    comment_id BIGSERIAL PRIMARY KEY,
    post_id BIGINT NOT NULL REFERENCES posts(post_id) ON DELETE CASCADE,
    author_id BIGINT NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,

    comment_text TEXT NOT NULL,
    parent_comment_id BIGINT REFERENCES comments(comment_id) ON DELETE CASCADE,

    -- Moderation
    is_approved BOOLEAN DEFAULT TRUE NOT NULL,
    is_active BOOLEAN DEFAULT TRUE NOT NULL,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE INDEX idx_comment_post ON comments(post_id, is_approved) WHERE is_active = TRUE;
CREATE INDEX idx_comment_parent ON comments(parent_comment_id);

COMMENT ON TABLE comments IS 'Post comments with threading support';

-- Learning Modules
CREATE TABLE learning_modules (
    module_id BIGSERIAL PRIMARY KEY,

    title VARCHAR(255) NOT NULL,
    description TEXT,
    content_type content_type DEFAULT 'AUDIO' NOT NULL,

    -- Multi-language audio support
    audio_url_en VARCHAR(255),
    audio_url_lg VARCHAR(255),
    audio_url_lur VARCHAR(255),
    audio_url_lgb VARCHAR(255),

    is_active BOOLEAN DEFAULT TRUE NOT NULL,
    sort_order INTEGER DEFAULT 0 NOT NULL,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE INDEX idx_module_active_order ON learning_modules(sort_order) WHERE is_active = TRUE;

CREATE TRIGGER update_modules_updated_at BEFORE UPDATE ON learning_modules
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

COMMENT ON TABLE learning_modules IS 'Educational content with multi-language audio';
COMMENT ON COLUMN learning_modules.audio_url_en IS 'English audio file URL';
COMMENT ON COLUMN learning_modules.audio_url_lg IS 'Luganda audio file URL';
COMMENT ON COLUMN learning_modules.audio_url_lur IS 'Alur audio file URL';
COMMENT ON COLUMN learning_modules.audio_url_lgb IS 'Lugbara audio file URL';

-- =================================================================================
-- SECTION 7: USSD INTEGRATION
-- =================================================================================

-- USSD Sessions
CREATE TABLE ussd_sessions (
    session_id VARCHAR(255) PRIMARY KEY,
    phone_number VARCHAR(20) NOT NULL,

    current_menu VARCHAR(50) NOT NULL,
    session_data JSONB,

    -- Registration data
    user_name VARCHAR(100),
    user_gender VARCHAR(10),
    user_age_group VARCHAR(10),
    user_district VARCHAR(50),
    user_business_stage VARCHAR(50),

    is_active BOOLEAN DEFAULT TRUE NOT NULL,
    expires_at TIMESTAMP,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE INDEX idx_ussd_phone_active ON ussd_sessions(phone_number, is_active);
CREATE INDEX idx_ussd_expires ON ussd_sessions(expires_at) WHERE is_active = TRUE;
CREATE INDEX idx_ussd_session_data ON ussd_sessions USING GIN (session_data);

CREATE TRIGGER update_ussd_last_updated BEFORE UPDATE ON ussd_sessions
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

COMMENT ON TABLE ussd_sessions IS 'USSD session state management';
COMMENT ON COLUMN ussd_sessions.session_data IS 'Temporary session storage in JSONB format';

-- =================================================================================
-- SECTION 8: AI & ANALYTICS
-- =================================================================================

-- User Activity Logs
CREATE TABLE user_activity_logs (
    log_id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,

    activity_type VARCHAR(50) NOT NULL,
    activity_category VARCHAR(50),
    target_id BIGINT,
    target_type VARCHAR(50),
    target_name VARCHAR(255),

    session_id VARCHAR(255),
    device_type VARCHAR(20) DEFAULT 'WEB',
    duration_seconds INTEGER,
    action_result VARCHAR(20),
    tags TEXT,
    metadata JSONB,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE INDEX idx_activity_user_activity ON user_activity_logs(user_id, activity_type, created_at DESC);
CREATE INDEX idx_activity_target ON user_activity_logs(target_type, target_id);
CREATE INDEX idx_activity_session ON user_activity_logs(session_id);
CREATE INDEX idx_activity_created ON user_activity_logs(created_at DESC);
CREATE INDEX idx_activity_metadata ON user_activity_logs USING GIN (metadata);

COMMENT ON TABLE user_activity_logs IS 'User activity tracking for AI recommendations and analytics';

-- User Interests
CREATE TABLE user_interests (
    user_interest_id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,

    interest_tag VARCHAR(50) NOT NULL,
    interest_name VARCHAR(100),
    interest_category VARCHAR(50),
    interest_level interest_level DEFAULT 'MEDIUM' NOT NULL,
    source interest_source DEFAULT 'USER_SELECTED' NOT NULL,

    -- Engagement tracking
    interaction_count INTEGER DEFAULT 0 NOT NULL,
    confidence_score DECIMAL(3,2) DEFAULT 0.50,
    is_primary BOOLEAN DEFAULT FALSE NOT NULL,
    last_interaction TIMESTAMP,

    is_active BOOLEAN DEFAULT TRUE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    created_by BIGINT,
    updated_by BIGINT,

    CONSTRAINT unique_user_interest UNIQUE (user_id, interest_tag)
);

CREATE INDEX idx_interest_user_interests ON user_interests(user_id, interest_level) WHERE is_active = TRUE;
CREATE INDEX idx_interest_tag ON user_interests(interest_tag) WHERE is_active = TRUE;
CREATE INDEX idx_interest_category ON user_interests(interest_category) WHERE is_active = TRUE;

CREATE TRIGGER update_interests_updated_at BEFORE UPDATE ON user_interests
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

COMMENT ON TABLE user_interests IS 'User interests for personalized recommendations';

-- =================================================================================
-- SECTION 9: FILE MANAGEMENT
-- =================================================================================

-- File Records
CREATE TABLE file_records (
    file_id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES users(user_id) ON DELETE CASCADE,

    file_name VARCHAR(255) NOT NULL,
    original_name VARCHAR(255),
    file_path VARCHAR(500) NOT NULL,
    file_size BIGINT,
    content_type VARCHAR(100),

    file_category file_category NOT NULL,

    is_active BOOLEAN DEFAULT TRUE NOT NULL,
    is_public BOOLEAN DEFAULT FALSE NOT NULL,

    upload_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE INDEX idx_file_user_category ON file_records(user_id, file_category) WHERE is_active = TRUE;
CREATE INDEX idx_file_active ON file_records(is_active);

COMMENT ON TABLE file_records IS 'File management with access control';

-- =================================================================================
-- SECTION 10: NOTIFICATION SYSTEM
-- =================================================================================

-- Notification Logs
CREATE TABLE notification_logs (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,

    notification_type notification_type NOT NULL,
    recipient VARCHAR(255) NOT NULL,

    subject VARCHAR(255),
    content TEXT NOT NULL,

    status notification_status DEFAULT 'PENDING' NOT NULL,
    sent_at TIMESTAMP,

    retry_count INTEGER DEFAULT 0 NOT NULL,
    error_message TEXT,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE INDEX idx_notification_user_status ON notification_logs(user_id, status);
CREATE INDEX idx_notification_retry ON notification_logs(retry_count, status) WHERE status = 'FAILED';
CREATE INDEX idx_notification_created ON notification_logs(created_at DESC);

COMMENT ON TABLE notification_logs IS 'Notification delivery tracking';

-- =================================================================================
-- SECTION 11: AUDIT TRAIL
-- =================================================================================

-- Audit Trail
CREATE TABLE audit_trail (
    audit_id BIGSERIAL PRIMARY KEY,

    user_id BIGINT REFERENCES users(user_id) ON DELETE SET NULL,
    service_name VARCHAR(100) NOT NULL,

    action_type VARCHAR(100) NOT NULL,
    entity_type VARCHAR(100),
    entity_id BIGINT,

    description TEXT,
    old_values JSONB,
    new_values JSONB,

    ip_address VARCHAR(45),
    user_agent TEXT,

    status audit_status NOT NULL,
    error_message TEXT,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE INDEX idx_audit_user_action ON audit_trail(user_id, action_type, created_at DESC);
CREATE INDEX idx_audit_entity ON audit_trail(entity_type, entity_id);
CREATE INDEX idx_audit_service ON audit_trail(service_name, created_at DESC);
CREATE INDEX idx_audit_created ON audit_trail(created_at DESC);
CREATE INDEX idx_audit_values ON audit_trail USING GIN (old_values, new_values);

COMMENT ON TABLE audit_trail IS 'Comprehensive audit trail for security and compliance';

-- =================================================================================
-- SECTION 12: VIEWS FOR ANALYTICS & REPORTING
-- =================================================================================

-- User Complete Profiles View
CREATE OR REPLACE VIEW user_complete_profiles AS
SELECT
    u.user_id,
    u.email,
    u.phone_number,
    u.role,
    u.is_active,
    u.is_verified,
    u.email_verified,
    u.phone_verified,
    u.created_at as registration_date,
    u.last_login,

    CASE
        WHEN u.role = 'YOUTH' THEN CONCAT(yp.first_name, ' ', yp.last_name)
        WHEN u.role = 'MENTOR' THEN CONCAT(mp.first_name, ' ', mp.last_name)
        WHEN u.role = 'NGO' THEN ngp.organisation_name
        WHEN u.role = 'SERVICE_PROVIDER' THEN spp.provider_name
        WHEN u.role = 'FUNDER' THEN fp.funder_name
        ELSE u.email
    END as display_name,

    -- Youth specific fields
    yp.district,
    yp.profession,
    yp.business_stage,
    yp.education_level,

    -- Verification flags
    ngp.is_verified as ngo_verified,
    spp.is_verified as provider_verified,
    mp.is_verified as mentor_verified

FROM users u
LEFT JOIN youth_profiles yp ON u.user_id = yp.user_id AND yp.is_deleted = FALSE
LEFT JOIN mentor_profiles mp ON u.user_id = mp.user_id AND mp.is_deleted = FALSE
LEFT JOIN ngo_profiles ngp ON u.user_id = ngp.user_id AND ngp.is_deleted = FALSE
LEFT JOIN service_provider_profiles spp ON u.user_id = spp.user_id AND spp.is_deleted = FALSE
LEFT JOIN funder_profiles fp ON u.user_id = fp.user_id AND fp.is_deleted = FALSE
WHERE u.is_deleted = FALSE;

COMMENT ON VIEW user_complete_profiles IS 'Unified view of all user profiles across roles';

-- Active Opportunities View
CREATE OR REPLACE VIEW active_opportunities_view AS
SELECT
    o.opportunity_id,
    o.title,
    o.opportunity_type,
    o.description,
    o.requirements,
    o.status,
    o.application_deadline,
    o.funding_amount,
    o.currency,
    o.is_featured,
    o.view_count,
    ucp.display_name as posted_by_name,
    u.role as posted_by_role,
    o.created_at,
    COUNT(a.application_id) as application_count,
    COUNT(a.application_id) FILTER (WHERE a.status = 'APPROVED') as approved_count

FROM opportunities o
JOIN users u ON o.posted_by_id = u.user_id
JOIN user_complete_profiles ucp ON u.user_id = ucp.user_id
LEFT JOIN applications a ON o.opportunity_id = a.opportunity_id
WHERE o.status IN ('OPEN', 'IN_REVIEW')
GROUP BY o.opportunity_id, o.title, o.opportunity_type, o.description, o.requirements,
         o.status, o.application_deadline, o.funding_amount, o.currency,
         o.is_featured, o.view_count, ucp.display_name, u.role, o.created_at;

COMMENT ON VIEW active_opportunities_view IS 'Active opportunities with application statistics';

-- Recent User Activity Summary (Last 30 Days)
CREATE OR REPLACE VIEW v_recent_user_activity AS
SELECT
    u.user_id,
    u.email,
    u.role,
    ucp.display_name,
    COUNT(ual.log_id) AS total_activities,
    COUNT(DISTINCT ual.activity_type) AS distinct_activity_types,
    MAX(ual.created_at) AS last_activity_time,
    COUNT(DISTINCT ual.session_id) AS session_count,
    COUNT(DISTINCT DATE(ual.created_at)) AS active_days
FROM users u
LEFT JOIN user_complete_profiles ucp ON u.user_id = ucp.user_id
LEFT JOIN user_activity_logs ual ON u.user_id = ual.user_id
    AND ual.created_at >= CURRENT_TIMESTAMP - INTERVAL '30 days'
WHERE u.is_active = TRUE AND u.is_deleted = FALSE
GROUP BY u.user_id, u.email, u.role, ucp.display_name;

COMMENT ON VIEW v_recent_user_activity IS 'User activity summary for the last 30 days';

-- User Engagement Metrics
CREATE OR REPLACE VIEW v_user_engagement_metrics AS
SELECT
    u.user_id,
    u.email,
    u.role,
    ucp.display_name,
    u.created_at AS user_since,
    EXTRACT(DAY FROM (CURRENT_TIMESTAMP - u.created_at)) AS days_since_registration,
    COUNT(DISTINCT DATE(ual.created_at)) FILTER (
        WHERE ual.created_at >= CURRENT_TIMESTAMP - INTERVAL '30 days'
    ) AS active_days_last_30d,
    COUNT(ual.log_id) FILTER (
        WHERE ual.created_at >= CURRENT_TIMESTAMP - INTERVAL '30 days'
    ) AS total_actions_last_30d,
    COUNT(DISTINCT ual.activity_type) AS unique_activity_types,
    AVG(ual.duration_seconds) AS avg_session_duration,
    COUNT(DISTINCT ual.target_id) AS unique_items_engaged,
    COUNT(DISTINCT ui.interest_tag) AS total_interests,
    MAX(ual.created_at) AS last_active_date,
    CASE
        WHEN MAX(ual.created_at) >= CURRENT_TIMESTAMP - INTERVAL '7 days' THEN 'ACTIVE'
        WHEN MAX(ual.created_at) >= CURRENT_TIMESTAMP - INTERVAL '30 days' THEN 'MODERATE'
        ELSE 'INACTIVE'
    END AS engagement_status
FROM users u
LEFT JOIN user_complete_profiles ucp ON u.user_id = ucp.user_id
LEFT JOIN user_activity_logs ual ON u.user_id = ual.user_id
LEFT JOIN user_interests ui ON u.user_id = ui.user_id AND ui.is_active = TRUE
WHERE u.is_active = TRUE AND u.is_deleted = FALSE
GROUP BY u.user_id, u.email, u.role, ucp.display_name, u.created_at;

COMMENT ON VIEW v_user_engagement_metrics IS 'Comprehensive user engagement metrics';

-- Activity Heatmap (Day/Hour Distribution)
CREATE OR REPLACE VIEW v_activity_heatmap AS
SELECT
    TO_CHAR(created_at, 'Day') AS day_of_week,
    EXTRACT(HOUR FROM created_at) AS hour_of_day,
    COUNT(*) AS activity_count,
    COUNT(DISTINCT user_id) AS unique_users,
    COUNT(DISTINCT session_id) AS unique_sessions
FROM user_activity_logs
WHERE created_at >= CURRENT_TIMESTAMP - INTERVAL '30 days'
GROUP BY day_of_week, hour_of_day
ORDER BY
    EXTRACT(DOW FROM created_at),
    hour_of_day;

COMMENT ON VIEW v_activity_heatmap IS 'Activity distribution by day and hour';

-- Popular Interests Across Platform
CREATE OR REPLACE VIEW v_popular_interests AS
SELECT
    COALESCE(interest_tag, interest_name) AS interest,
    interest_category,
    COUNT(DISTINCT user_id) AS user_count,
    ROUND(AVG(CASE interest_level
        WHEN 'HIGH' THEN 3
        WHEN 'MEDIUM' THEN 2
        WHEN 'LOW' THEN 1
    END), 2) AS avg_interest_level,
    SUM(interaction_count) AS total_interactions,
    MAX(last_interaction) AS most_recent_interaction,
    COUNT(*) FILTER (WHERE is_primary = TRUE) AS primary_interest_count
FROM user_interests
WHERE is_active = TRUE
GROUP BY interest, interest_category
ORDER BY user_count DESC, total_interactions DESC;

COMMENT ON VIEW v_popular_interests IS 'Most popular interests across the platform';

-- =================================================================================
-- SECTION 13: INITIAL SEED DATA
-- =================================================================================

-- Default Admin User (CHANGE PASSWORD AFTER DEPLOYMENT!)
-- Password: Admin123!
INSERT INTO users (email, phone_number, password_hash, role, is_active, email_verified, phone_verified)
VALUES (
    'admin@youthconnect.ug',
    '+256700000001',
    '$2a$12$rQOZgIwI6k8QpqZtDlEPUeF5HYD8v4x1z0fXhRtF8gHy7uPjI9QeK',
    'ADMIN',
    TRUE,
    TRUE,
    TRUE
) ON CONFLICT (email) DO NOTHING;

-- Sample Youth User
-- Password: Youth123!
INSERT INTO users (email, phone_number, password_hash, role, is_active, email_verified)
VALUES (
    'damienpapers3@gmail.com',
    '+256701430234',
    '$2a$12$rQOZgIwI6k8QpqZtDlEPUeF5HYD8v4x1z0fXhRtF8gHy7uPjI9QeK',
    'YOUTH',
    TRUE,
    TRUE
) ON CONFLICT (email) DO NOTHING;

-- Insert youth profile for sample user
INSERT INTO youth_profiles (user_id, first_name, last_name, gender, district, profession)
SELECT user_id, 'Damien', 'Papers', 'MALE', 'Kampala', 'Software Developer'
FROM users WHERE email = 'damienpapers3@gmail.com'
ON CONFLICT (user_id) DO NOTHING;

-- Sample Learning Modules
INSERT INTO learning_modules (title, description, content_type, audio_url_en, audio_url_lg, sort_order) VALUES
('Business Basics', 'Fundamentals of starting a business', 'AUDIO',
 'https://files.youthconnect.ug/audio/business_basics_en.mp3',
 'https://files.youthconnect.ug/audio/business_basics_lg.mp3', 1),
('Financial Literacy', 'Managing your business finances', 'AUDIO',
 'https://files.youthconnect.ug/audio/financial_literacy_en.mp3',
 'https://files.youthconnect.ug/audio/financial_literacy_lg.mp3', 2),
('Marketing Fundamentals', 'Introduction to marketing your products', 'AUDIO',
 'https://files.youthconnect.ug/audio/marketing_basics_en.mp3',
 'https://files.youthconnect.ug/audio/marketing_basics_lg.mp3', 3)
ON CONFLICT DO NOTHING;

-- Sample Opportunities
INSERT INTO opportunities (posted_by_id, opportunity_type, title, description, status, funding_amount, application_deadline)
SELECT
    u.user_id,
    'GRANT',
    'Young Entrepreneurs Grant 2025',
    'Funding for innovative business ideas from youth aged 18-30. Apply now for up to UGX 5,000,000 in seed funding.',
    'OPEN',
    5000000.00,
    CURRENT_TIMESTAMP + INTERVAL '60 days'
FROM users u WHERE u.role = 'ADMIN' LIMIT 1
ON CONFLICT DO NOTHING;

INSERT INTO opportunities (posted_by_id, opportunity_type, title, description, status, application_deadline)
SELECT
    u.user_id,
    'TRAINING',
    'Digital Marketing Bootcamp',
    'Comprehensive 4-week training in digital marketing strategies including social media, SEO, and content marketing.',
    'OPEN',
    CURRENT_TIMESTAMP + INTERVAL '30 days'
FROM users u WHERE u.role = 'ADMIN' LIMIT 1
ON CONFLICT DO NOTHING;

-- =================================================================================
-- SECTION 14: PERFORMANCE INDEXES
-- =================================================================================

-- Additional performance indexes
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_users_email_lower ON users(LOWER(email));
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_youth_profiles_name ON youth_profiles(first_name, last_name);
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_mentor_profiles_name ON mentor_profiles(first_name, last_name);

-- GIN indexes for text search
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_opportunities_search ON opportunities
    USING GIN (to_tsvector('english', title || ' ' || COALESCE(description, '')));

CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_posts_search ON posts
    USING GIN (to_tsvector('english', COALESCE(title, '') || ' ' || COALESCE(content, '')));

-- =================================================================================
-- SECTION 15: DATABASE STATISTICS
-- =================================================================================

-- Analyze all tables for better query planning
ANALYZE users;
ANALYZE youth_profiles;
ANALYZE mentor_profiles;
ANALYZE ngo_profiles;
ANALYZE funder_profiles;
ANALYZE service_provider_profiles;
ANALYZE opportunities;
ANALYZE applications;
ANALYZE mentorship_sessions;
ANALYZE user_activity_logs;
ANALYZE user_interests;
ANALYZE audit_trail;

-- =================================================================================
-- VERIFICATION & SUMMARY
-- =================================================================================

-- Display schema summary
DO $
DECLARE
    table_count INTEGER;
    view_count INTEGER;
    index_count INTEGER;
    trigger_count INTEGER;
    enum_count INTEGER;
BEGIN
    SELECT COUNT(*) INTO table_count FROM information_schema.tables
    WHERE table_schema = 'public' AND table_type = 'BASE TABLE';

    SELECT COUNT(*) INTO view_count FROM information_schema.views
    WHERE table_schema = 'public';

    SELECT COUNT(*) INTO index_count FROM pg_indexes
    WHERE schemaname = 'public';

    SELECT COUNT(*) INTO trigger_count FROM information_schema.triggers
    WHERE trigger_schema = 'public';

    SELECT COUNT(*) INTO enum_count FROM pg_type
    WHERE typtype = 'e' AND typnamespace = (SELECT oid FROM pg_namespace WHERE nspname = 'public');

    RAISE NOTICE '========================================';
    RAISE NOTICE 'Youth Connect Uganda - Database Created';
    RAISE NOTICE '========================================';
    RAISE NOTICE 'Tables created: %', table_count;
    RAISE NOTICE 'Views created: %', view_count;
    RAISE NOTICE 'Indexes created: %', index_count;
    RAISE NOTICE 'Triggers created: %', trigger_count;
    RAISE NOTICE 'Custom types created: %', enum_count;
    RAISE NOTICE '========================================';
    RAISE NOTICE 'Status: SUCCESS';
    RAISE NOTICE 'Database: youth_connect_db';
    RAISE NOTICE 'Version: 1.0.0';
    RAISE NOTICE '========================================';
END $;

-- =================================================================================
-- END OF SCHEMA
-- =================================================================================