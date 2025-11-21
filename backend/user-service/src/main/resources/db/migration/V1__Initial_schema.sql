-- ═════════════════════════════════════════════════════════════════════════════
-- USER SERVICE - INITIAL SCHEMA MIGRATION (PostgreSQL)
-- ═════════════════════════════════════════════════════════════════════════════

-- Enable UUID extension
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- ═════════════════════════════════════════════════════════════════════════════
-- ENUM TYPES
-- ═════════════════════════════════════════════════════════════════════════════

CREATE TYPE user_role AS ENUM (
    'YOUTH', 'MENTOR', 'NGO', 'FUNDER', 'SERVICE_PROVIDER',
    'ADMIN', 'COMPANY', 'RECRUITER', 'GOVERNMENT'
);

CREATE TYPE availability_status AS ENUM (
    'AVAILABLE', 'BUSY', 'FULL', 'UNAVAILABLE'
);

-- ═════════════════════════════════════════════════════════════════════════════
-- CORE TABLES
-- ═════════════════════════════════════════════════════════════════════════════

CREATE TABLE users (
    user_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    email VARCHAR(255) NOT NULL UNIQUE,
    phone_number VARCHAR(20) UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role user_role NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    email_verified BOOLEAN NOT NULL DEFAULT FALSE,
    phone_verified BOOLEAN NOT NULL DEFAULT FALSE,
    last_login TIMESTAMP,
    failed_login_attempts INTEGER DEFAULT 0,
    account_locked_until TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE youth_profiles (
    profile_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL UNIQUE REFERENCES users(user_id) ON DELETE CASCADE,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    gender VARCHAR(10),
    date_of_birth DATE,
    district VARCHAR(50),
    profession VARCHAR(100),
    description TEXT,
    has_disability BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE mentor_profiles (
    mentor_profile_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL UNIQUE REFERENCES users(user_id) ON DELETE CASCADE,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    bio TEXT,
    area_of_expertise VARCHAR(100),
    experience_years INTEGER,
    availability_status availability_status NOT NULL DEFAULT 'AVAILABLE',
    max_mentees INTEGER DEFAULT 5,
    current_mentees INTEGER DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE ngo_profiles (
    ngo_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL UNIQUE REFERENCES users(user_id) ON DELETE CASCADE,
    organisation_name VARCHAR(150) NOT NULL,
    location VARCHAR(100),
    description TEXT,
    is_verified BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE funder_profiles (
    funder_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL UNIQUE REFERENCES users(user_id) ON DELETE CASCADE,
    funder_name VARCHAR(150) NOT NULL,
    funding_focus TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE service_provider_profiles (
    provider_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL UNIQUE REFERENCES users(user_id) ON DELETE CASCADE,
    provider_name VARCHAR(150) NOT NULL,
    location VARCHAR(100),
    area_of_expertise TEXT NOT NULL,
    is_verified BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ═════════════════════════════════════════════════════════════════════════════
-- INDEXES
-- ═════════════════════════════════════════════════════════════════════════════

CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_phone ON users(phone_number);
CREATE INDEX idx_users_role ON users(role);
CREATE INDEX idx_users_active ON users(is_active) WHERE is_active = TRUE;
CREATE INDEX idx_youth_district ON youth_profiles(district);
CREATE INDEX idx_mentor_availability ON mentor_profiles(availability_status);

-- ═════════════════════════════════════════════════════════════════════════════
-- TRIGGERS
-- ═════════════════════════════════════════════════════════════════════════════

CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER update_users_updated_at
    BEFORE UPDATE ON users
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_youth_profiles_updated_at
    BEFORE UPDATE ON youth_profiles
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_mentor_profiles_updated_at
    BEFORE UPDATE ON mentor_profiles
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_ngo_profiles_updated_at
    BEFORE UPDATE ON ngo_profiles
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_funder_profiles_updated_at
    BEFORE UPDATE ON funder_profiles
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_service_provider_profiles_updated_at
    BEFORE UPDATE ON service_provider_profiles
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- ═════════════════════════════════════════════════════════════════════════════
-- COMMENTS
-- ═════════════════════════════════════════════════════════════════════════════

COMMENT ON TABLE users IS 'Core user authentication and authorization data';
COMMENT ON TABLE youth_profiles IS 'Profile information for youth entrepreneurs';
COMMENT ON TABLE mentor_profiles IS 'Profile information for mentors';
COMMENT ON TABLE ngo_profiles IS 'Profile information for NGO organizations';
COMMENT ON TABLE funder_profiles IS 'Profile information for funding organizations';
COMMENT ON TABLE service_provider_profiles IS 'Profile information for service providers';