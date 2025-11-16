-- ═══════════════════════════════════════════════════════════════════════════
-- USER SERVICE - INITIAL SCHEMA MIGRATION
-- ═══════════════════════════════════════════════════════════════════════════
-- Version: V1__Initial_Schema.sql
-- Database: PostgreSQL 15+
-- Schema: public
-- Description: User profiles, interests, and activity tracking
-- Author: Douglas Kings Kato
-- Date: 2025-11-12
-- ═══════════════════════════════════════════════════════════════════════════

-- ═══════════════════════════════════════════════════════════════════════════
-- SECTION 1: EXTENSIONS
-- ═══════════════════════════════════════════════════════════════════════════

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pg_trgm";
CREATE EXTENSION IF NOT EXISTS "btree_gin";

-- ═══════════════════════════════════════════════════════════════════════════
-- SECTION 2: CUSTOM TYPES
-- ═══════════════════════════════════════════════════════════════════════════

-- User roles (matches auth-service)
CREATE TYPE user_role AS ENUM (
    'YOUTH',
    'NGO',
    'FUNDER',
    'SERVICE_PROVIDER',
    'MENTOR',
    'ADMIN',
    'SUPER_ADMIN',
    'MODERATOR'
);

-- Gender
CREATE TYPE gender_type AS ENUM (
    'MALE',
    'FEMALE',
    'NON_BINARY',
    'PREFER_NOT_TO_SAY'
);

-- Business stages
CREATE TYPE business_stage AS ENUM (
    'IDEA',
    'EARLY_STAGE',
    'GROWTH',
    'ESTABLISHED'
);

-- Organization types
CREATE TYPE organization_type AS ENUM (
    'NGO',
    'FOUNDATION',
    'GOVERNMENT',
    'PRIVATE_SECTOR',
    'INTERNATIONAL_ORG'
);

-- Profile completion status
CREATE TYPE profile_completion_status AS ENUM (
    'INCOMPLETE',
    'PARTIAL',
    'COMPLETE'
);

-- ═══════════════════════════════════════════════════════════════════════════
-- SECTION 3: CORE TABLES
-- ═══════════════════════════════════════════════════════════════════════════

-- ───────────────────────────────────────────────────────────────────────────
-- 3.1 USER PROFILES CACHE (Denormalized from auth-service)
-- ───────────────────────────────────────────────────────────────────────────
CREATE TABLE user_profiles_cache (
    user_id UUID PRIMARY KEY,
    email VARCHAR(255) NOT NULL,
    phone_number VARCHAR(20),
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    role user_role NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    is_email_verified BOOLEAN NOT NULL DEFAULT FALSE,
    is_phone_verified BOOLEAN NOT NULL DEFAULT FALSE,

    -- Profile completion tracking
    profile_completion_status profile_completion_status DEFAULT 'INCOMPLETE',
    profile_completion_percentage INTEGER DEFAULT 0,

    -- Cache metadata
    last_synced_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- Audit
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_user_cache_email ON user_profiles_cache(email);
CREATE INDEX idx_user_cache_role ON user_profiles_cache(role);
CREATE INDEX idx_user_cache_synced ON user_profiles_cache(last_synced_at DESC);

COMMENT ON TABLE user_profiles_cache IS 'Cached user data from auth-service for performance';

-- ───────────────────────────────────────────────────────────────────────────
-- 3.2 YOUTH PROFILES
-- ───────────────────────────────────────────────────────────────────────────
CREATE TABLE youth_profiles (
    profile_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL UNIQUE,

    -- Personal Information
    date_of_birth DATE,
    gender gender_type,
    bio TEXT,
    profile_picture_url VARCHAR(500),

    -- Location
    district VARCHAR(100),
    sub_county VARCHAR(100),
    village VARCHAR(100),

    -- Education
    education_level VARCHAR(100),
    field_of_study VARCHAR(200),
    current_institution VARCHAR(200),

    -- Business/Entrepreneurship
    business_stage business_stage,
    business_name VARCHAR(200),
    business_description TEXT,
    business_sector VARCHAR(100),
    business_registration_number VARCHAR(50),

    -- Skills & Interests
    skills TEXT[],
    interests TEXT[],

    -- Social Media
    social_media JSONB DEFAULT '{}'::jsonb,

    -- Preferences
    preferences JSONB DEFAULT '{}'::jsonb,

    -- Audit
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- Soft Delete
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMP
);

CREATE INDEX idx_youth_district ON youth_profiles(district) WHERE is_deleted = FALSE;
CREATE INDEX idx_youth_business_stage ON youth_profiles(business_stage) WHERE is_deleted = FALSE;
CREATE INDEX idx_youth_sector ON youth_profiles(business_sector) WHERE is_deleted = FALSE;
CREATE INDEX idx_youth_skills ON youth_profiles USING gin(skills) WHERE is_deleted = FALSE;

-- Full-text search on business
CREATE INDEX idx_youth_business_search ON youth_profiles USING gin(
    to_tsvector('english', COALESCE(business_name, '') || ' ' || COALESCE(business_description, ''))
) WHERE is_deleted = FALSE;

COMMENT ON TABLE youth_profiles IS 'Detailed profiles for youth entrepreneurs';

-- ───────────────────────────────────────────────────────────────────────────
-- 3.3 NGO PROFILES
-- ───────────────────────────────────────────────────────────────────────────
CREATE TABLE ngo_profiles (
    profile_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL UNIQUE,

    -- Organization Details
    organization_name VARCHAR(200) NOT NULL,
    organization_type organization_type NOT NULL,
    registration_number VARCHAR(100),
    registration_date DATE,
    logo_url VARCHAR(500),

    -- Contact Information
    primary_contact_person VARCHAR(200),
    primary_contact_phone VARCHAR(20),
    primary_contact_email VARCHAR(255),

    -- Location
    headquarters_district VARCHAR(100),
    office_address TEXT,
    coverage_areas TEXT[],

    -- Mission & Vision
    mission_statement TEXT,
    vision_statement TEXT,
    focus_areas TEXT[],

    -- Website & Social
    website_url VARCHAR(255),
    social_media JSONB DEFAULT '{}'::jsonb,

    -- Audit
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- Soft Delete
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMP
);

CREATE INDEX idx_ngo_org_name ON ngo_profiles(organization_name) WHERE is_deleted = FALSE;
CREATE INDEX idx_ngo_type ON ngo_profiles(organization_type) WHERE is_deleted = FALSE;
CREATE INDEX idx_ngo_district ON ngo_profiles(headquarters_district) WHERE is_deleted = FALSE;

COMMENT ON TABLE ngo_profiles IS 'Profiles for NGOs and partner organizations';

-- ───────────────────────────────────────────────────────────────────────────
-- 3.4 USER INTERESTS (for recommendations)
-- ───────────────────────────────────────────────────────────────────────────
CREATE TABLE user_interests (
    interest_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL,

    -- Interest Details
    interest_category VARCHAR(100) NOT NULL,
    interest_name VARCHAR(200) NOT NULL,
    confidence_score DECIMAL(3,2) DEFAULT 0.5,

    -- Source tracking
    source VARCHAR(50), -- 'EXPLICIT', 'IMPLICIT', 'AI_INFERRED'

    -- Audit
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    UNIQUE(user_id, interest_category, interest_name)
);

CREATE INDEX idx_interests_user ON user_interests(user_id);
CREATE INDEX idx_interests_category ON user_interests(interest_category);
CREATE INDEX idx_interests_confidence ON user_interests(confidence_score DESC);

COMMENT ON TABLE user_interests IS 'User interests for AI-powered recommendations';

-- ───────────────────────────────────────────────────────────────────────────
-- 3.5 USER ACTIVITY LOGS (for analytics)
-- ───────────────────────────────────────────────────────────────────────────
CREATE TABLE user_activity_logs (
    log_id BIGSERIAL PRIMARY KEY,
    user_id UUID NOT NULL,

    -- Activity Details
    activity_type VARCHAR(100) NOT NULL,
    activity_description TEXT,
    entity_type VARCHAR(50), -- 'JOB', 'OPPORTUNITY', 'MENTOR', etc.
    entity_id UUID,

    -- Context
    session_id VARCHAR(255),
    ip_address VARCHAR(45),
    user_agent TEXT,

    -- Metadata
    metadata JSONB DEFAULT '{}'::jsonb,

    -- Timestamp
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_activity_user ON user_activity_logs(user_id, created_at DESC);
CREATE INDEX idx_activity_type ON user_activity_logs(activity_type, created_at DESC);
CREATE INDEX idx_activity_entity ON user_activity_logs(entity_type, entity_id) WHERE entity_id IS NOT NULL;

COMMENT ON TABLE user_activity_logs IS 'User activity tracking for analytics and recommendations';

-- ═══════════════════════════════════════════════════════════════════════════
-- SECTION 4: FUNCTIONS & TRIGGERS
-- ═══════════════════════════════════════════════════════════════════════════

CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Apply triggers
CREATE TRIGGER trg_user_cache_updated_at
    BEFORE UPDATE ON user_profiles_cache
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER trg_youth_profiles_updated_at
    BEFORE UPDATE ON youth_profiles
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER trg_ngo_profiles_updated_at
    BEFORE UPDATE ON ngo_profiles
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- ═══════════════════════════════════════════════════════════════════════════
-- MIGRATION COMPLETE
-- ═══════════════════════════════════════════════════════════════════════════