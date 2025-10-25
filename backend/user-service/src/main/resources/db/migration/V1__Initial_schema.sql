-- ============================================================================
-- Youth Connect Uganda - User Service Initial Database Schema
-- ============================================================================
-- Version: V1
-- Description: Complete initial schema with corrected column definitions
-- Fixed: email_verified and phone_verified columns added to users table
-- ============================================================================

-- Ensure we're using the correct database
USE youth_connect_db;

-- Temporarily disable foreign key checks for clean setup
SET FOREIGN_KEY_CHECKS = 0;

-- ============================================================================
-- USERS TABLE - Core authentication for all platforms (Web + USSD)
-- ============================================================================
CREATE TABLE IF NOT EXISTS users (
    user_id BIGINT AUTO_INCREMENT PRIMARY KEY,

    -- Authentication Credentials
    email VARCHAR(100) NOT NULL UNIQUE COMMENT 'Primary identifier for web login',
    phone_number VARCHAR(20) UNIQUE COMMENT 'Primary identifier for USSD login',
    password_hash VARCHAR(255) NOT NULL COMMENT 'BCrypt hashed password',

    -- User Role
    role ENUM('YOUTH', 'NGO', 'FUNDER', 'SERVICE_PROVIDER', 'MENTOR', 'ADMIN') NOT NULL,

    -- Account Status & Verification (CRITICAL FIELDS)
    is_active BOOLEAN DEFAULT TRUE COMMENT 'Account active status',
    is_verified BOOLEAN DEFAULT FALSE COMMENT 'Overall verification status',
    email_verified BOOLEAN DEFAULT FALSE COMMENT 'Email verification status',
    phone_verified BOOLEAN DEFAULT FALSE COMMENT 'Phone verification status',

    -- Security Tokens
    verification_token VARCHAR(255) COMMENT 'Email/phone verification token',
    reset_token VARCHAR(255) COMMENT 'Password reset token',
    reset_token_expiry DATETIME COMMENT 'Reset token expiration time',

    -- Security Audit
    last_login TIMESTAMP NULL COMMENT 'Last successful login timestamp',
    failed_login_attempts INT DEFAULT 0 COMMENT 'Failed login counter',
    account_locked_until TIMESTAMP NULL COMMENT 'Account lock expiry time',

    -- Audit Fields
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(100) COMMENT 'Who created this record',
    updated_by VARCHAR(100) COMMENT 'Who last updated this record',

    -- Soft Delete
    is_deleted BOOLEAN DEFAULT FALSE,
    deleted_at TIMESTAMP NULL,

    -- Performance Indexes
    INDEX idx_users_email (email),
    INDEX idx_users_phone (phone_number),
    INDEX idx_users_phone_active (phone_number, is_active),
    INDEX idx_users_email_active (email, is_active),
    INDEX idx_users_role (role, is_active),
    INDEX idx_users_created_at (created_at),
    INDEX idx_users_role_active_created (role, is_active, created_at),
    INDEX idx_users_deleted (is_deleted, deleted_at),

    -- Uganda phone number validation
    CONSTRAINT chk_phone_format CHECK (
        phone_number IS NULL OR
        phone_number REGEXP '^\\+?256[0-9]{9}$|^0[0-9]{9}$'
    )
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Central user authentication for web and USSD platforms';

-- ============================================================================
-- YOUTH PROFILES TABLE
-- ============================================================================
CREATE TABLE IF NOT EXISTS youth_profiles (
    profile_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,

    -- Personal Information
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    gender ENUM('MALE', 'FEMALE', 'OTHER'),
    date_of_birth DATE,

    -- Location & Demographics
    district VARCHAR(50),
    location VARCHAR(100),
    age_group VARCHAR(20),

    -- Professional Information
    profession VARCHAR(100),
    education_level VARCHAR(50),
    business_stage ENUM('IDEA_PHASE', 'EARLY_STAGE', 'GROWTH_STAGE', 'MATURE') DEFAULT 'IDEA_PHASE',

    -- Profile Content
    bio TEXT,
    description TEXT,
    skills TEXT,
    interests TEXT,
    looking_for VARCHAR(255),

    -- Media
    profile_picture_url VARCHAR(255),

    -- Accessibility
    has_disability BOOLEAN DEFAULT FALSE,
    disability_details TEXT,

    -- Audit Fields
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by BIGINT NULL,
    updated_by BIGINT NULL,
    is_deleted BOOLEAN DEFAULT FALSE,
    deleted_at TIMESTAMP NULL,

    -- Foreign Key
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,

    -- Performance Indexes
    INDEX idx_youth_user_id (user_id),
    INDEX idx_youth_district (district),
    INDEX idx_youth_business_stage (business_stage),
    INDEX idx_youth_created_by (created_by),
    INDEX idx_youth_updated_by (updated_by),

    -- Full-text search
    FULLTEXT INDEX idx_youth_search (first_name, last_name, profession, skills, interests)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================================
-- MENTOR PROFILES TABLE
-- ============================================================================
CREATE TABLE IF NOT EXISTS mentor_profiles (
    mentor_profile_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,

    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    bio TEXT,
    area_of_expertise TEXT NOT NULL,
    years_of_experience INT,
    organization VARCHAR(100),
    position VARCHAR(100),

    -- Availability
    availability_status ENUM('AVAILABLE', 'BUSY', 'ON_LEAVE') DEFAULT 'AVAILABLE',
    max_mentees INT DEFAULT 5,

    -- Verification
    is_verified BOOLEAN DEFAULT FALSE,

    -- Media
    profile_picture_url VARCHAR(255),
    linkedin_url VARCHAR(255),
    website_url VARCHAR(255),

    -- Audit Fields
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by BIGINT NULL,
    updated_by BIGINT NULL,
    is_deleted BOOLEAN DEFAULT FALSE,
    deleted_at TIMESTAMP NULL,

    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,

    INDEX idx_mentor_user_id (user_id),
    INDEX idx_mentor_availability (availability_status),
    INDEX idx_mentor_verified (is_verified),
    INDEX idx_mentor_created_by (created_by),
    INDEX idx_mentor_updated_by (updated_by),

    FULLTEXT INDEX idx_mentor_search (first_name, last_name, area_of_expertise, bio)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================================
-- NGO PROFILES TABLE
-- ============================================================================
CREATE TABLE IF NOT EXISTS ngo_profiles (
    ngo_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,

    organisation_name VARCHAR(150) NOT NULL,
    registration_number VARCHAR(50),
    location VARCHAR(100),
    address VARCHAR(255),
    district VARCHAR(50),
    description TEXT,
    focus_areas TEXT,
    target_audience VARCHAR(100),

    -- Verification
    is_verified BOOLEAN DEFAULT FALSE,
    verification_date TIMESTAMP NULL,
    verified_by BIGINT NULL,

    -- Contact
    contact_person VARCHAR(100),
    contact_email VARCHAR(100),
    contact_phone VARCHAR(20),
    logo_url VARCHAR(255),
    website_url VARCHAR(255),

    -- Audit Fields
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by BIGINT NULL,
    updated_by BIGINT NULL,
    is_deleted BOOLEAN DEFAULT FALSE,
    deleted_at TIMESTAMP NULL,

    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,

    INDEX idx_ngo_user_id (user_id),
    INDEX idx_ngo_verified (is_verified),
    INDEX idx_ngo_district (district),
    INDEX idx_ngo_created_by (created_by),
    INDEX idx_ngo_updated_by (updated_by),

    FULLTEXT INDEX idx_ngo_search (organisation_name, description, focus_areas)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================================
-- FUNDER PROFILES TABLE
-- ============================================================================
CREATE TABLE IF NOT EXISTS funder_profiles (
    funder_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,

    funder_name VARCHAR(150) NOT NULL,
    funder_type ENUM('GOVERNMENT', 'PRIVATE', 'INTERNATIONAL', 'INDIVIDUAL') DEFAULT 'PRIVATE',
    description TEXT,
    funding_focus TEXT,
    min_funding_amount DECIMAL(15,2),
    max_funding_amount DECIMAL(15,2),
    funding_currency VARCHAR(3) DEFAULT 'UGX',

    -- Contact
    contact_person VARCHAR(100),
    contact_email VARCHAR(100),
    website_url VARCHAR(255),
    logo_url VARCHAR(255),

    -- Audit Fields
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by BIGINT NULL,
    updated_by BIGINT NULL,
    is_deleted BOOLEAN DEFAULT FALSE,
    deleted_at TIMESTAMP NULL,

    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,

    INDEX idx_funder_user_id (user_id),
    INDEX idx_funder_type (funder_type),
    INDEX idx_funder_created_by (created_by),
    INDEX idx_funder_updated_by (updated_by)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================================
-- SERVICE PROVIDER PROFILES TABLE
-- ============================================================================
CREATE TABLE IF NOT EXISTS service_provider_profiles (
    provider_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,

    provider_name VARCHAR(150) NOT NULL,
    business_type VARCHAR(100),
    service_category VARCHAR(50),
    location VARCHAR(100),
    address VARCHAR(255),
    district VARCHAR(50),
    area_of_expertise TEXT NOT NULL,
    services_offered TEXT,
    description TEXT,

    -- Verification
    is_verified BOOLEAN DEFAULT FALSE,
    verification_date TIMESTAMP NULL,
    verified_by BIGINT NULL,

    -- Business Details
    years_in_operation INT,
    team_size VARCHAR(50),

    -- Contact
    contact_person VARCHAR(100),
    contact_email VARCHAR(100),
    website_url VARCHAR(255),
    logo_url VARCHAR(255),

    -- Audit Fields
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by BIGINT NULL,
    updated_by BIGINT NULL,
    is_deleted BOOLEAN DEFAULT FALSE,
    deleted_at TIMESTAMP NULL,

    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,

    INDEX idx_provider_user_id (user_id),
    INDEX idx_provider_verified (is_verified),
    INDEX idx_provider_district (district),
    INDEX idx_provider_created_by (created_by),
    INDEX idx_provider_updated_by (updated_by)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================================
-- USER INTERESTS TABLE
-- ============================================================================
CREATE TABLE IF NOT EXISTS user_interests (
    user_interest_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,

    interest_tag VARCHAR(50) NOT NULL,
    interest_level ENUM('LOW', 'MEDIUM', 'HIGH') DEFAULT 'MEDIUM',
    interest_category VARCHAR(50),
    source ENUM('USER_SELECTED', 'AI_INFERRED', 'ACTIVITY_BASED', 'SURVEY', 'IMPORTED') DEFAULT 'USER_SELECTED',
    confidence_score DECIMAL(3,2) DEFAULT 1.00,

    interaction_count INT DEFAULT 0,
    last_interaction TIMESTAMP NULL,
    is_active BOOLEAN DEFAULT TRUE,
    is_primary BOOLEAN DEFAULT FALSE,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by BIGINT NULL,
    updated_by BIGINT NULL,

    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,

    UNIQUE KEY unique_user_interest (user_id, interest_tag),

    INDEX idx_user_interests_user (user_id, is_active),
    INDEX idx_user_interests_tag (interest_tag, is_active),
    INDEX idx_user_interests_created_by (created_by)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================================
-- USER ACTIVITY LOGS TABLE
-- ============================================================================
CREATE TABLE IF NOT EXISTS user_activity_logs (
    log_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,

    activity_type VARCHAR(50) NOT NULL,
    activity_category VARCHAR(50),
    activity_description TEXT,

    target_id BIGINT,
    target_type VARCHAR(50),
    target_name VARCHAR(255),

    action_result ENUM('SUCCESS', 'FAILED', 'PARTIAL', 'CANCELLED') DEFAULT 'SUCCESS',
    duration_seconds INT,

    session_id VARCHAR(255),
    device_type ENUM('WEB', 'MOBILE', 'USSD', 'API') DEFAULT 'WEB',
    user_agent TEXT,
    ip_address VARCHAR(45),

    metadata JSON,
    tags TEXT,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    activity_date DATE AS (DATE(created_at)) STORED,

    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,

    INDEX idx_activity_user_date (user_id, activity_date, created_at),
    INDEX idx_activity_type (activity_type),
    INDEX idx_activity_target (target_type, target_id, created_at),
    INDEX idx_activity_session (session_id, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================================
-- USER SESSIONS TABLE
-- ============================================================================
CREATE TABLE IF NOT EXISTS user_sessions (
    session_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    session_token VARCHAR(500) NOT NULL,
    device_type ENUM('WEB', 'MOBILE', 'USSD') DEFAULT 'WEB',
    ip_address VARCHAR(45),
    user_agent TEXT,

    login_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_activity TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    logout_time TIMESTAMP NULL,
    expires_at TIMESTAMP NOT NULL,

    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,

    INDEX idx_sessions_user (user_id),
    INDEX idx_sessions_token (session_token),
    INDEX idx_sessions_expires (expires_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================================
-- AUDIT TRAIL TABLE
-- ============================================================================
CREATE TABLE IF NOT EXISTS audit_trail (
    audit_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NULL,

    action_type VARCHAR(100) NOT NULL,
    entity_type VARCHAR(100),
    entity_id BIGINT,
    description TEXT,

    ip_address VARCHAR(45),
    user_agent TEXT,
    request_url VARCHAR(500),

    status ENUM('SUCCESS', 'FAILED', 'PARTIAL') NOT NULL,
    error_message TEXT,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE SET NULL,

    INDEX idx_audit_user (user_id),
    INDEX idx_audit_action (action_type),
    INDEX idx_audit_entity (entity_type, entity_id),
    INDEX idx_audit_created (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Re-enable foreign key checks
SET FOREIGN_KEY_CHECKS = 1;

-- ============================================================================
-- INITIAL SEED DATA
-- ============================================================================

-- Default Admin User (Password: Admin@123)
INSERT INTO users (email, phone_number, password_hash, role, is_active, email_verified, phone_verified)
VALUES (
    'admin@youthconnect.ug',
    '+256700000000',
    '$2a$12$rQOZgIwI6k8QpqZtDlEPUeF5HYD8v4x1z0fXhRtF8gHy7uPjI9QeK',
    'ADMIN',
    TRUE,
    TRUE,
    TRUE
) ON DUPLICATE KEY UPDATE email = email;

-- Sample Youth User (Password: Youth@123)
INSERT INTO users (email, phone_number, password_hash, role, is_active, email_verified, phone_verified)
VALUES (
    'sample.youth@youthconnect.ug',
    '+256701234567',
    '$2a$12$rQOZgIwI6k8QpqZtDlEPUeF5HYD8v4x1z0fXhRtF8gHy7uPjI9QeK',
    'YOUTH',
    TRUE,
    TRUE,
    FALSE
) ON DUPLICATE KEY UPDATE email = email;

-- Youth Profile for sample user
INSERT INTO youth_profiles (user_id, first_name, last_name, gender, district, profession, business_stage, skills)
SELECT user_id, 'John', 'Doe', 'MALE', 'Kampala', 'Software Developer', 'EARLY_STAGE',
       'Java,Spring Boot,MySQL,React'
FROM users WHERE email = 'sample.youth@youthconnect.ug'
ON DUPLICATE KEY UPDATE first_name = first_name;

-- Sample Mentor User (Password: Mentor@123)
INSERT INTO users (email, phone_number, password_hash, role, is_active, email_verified, phone_verified)
VALUES (
    'sample.mentor@youthconnect.ug',
    '+256702345678',
    '$2a$12$rQOZgIwI6k8QpqZtDlEPUeF5HYD8v4x1z0fXhRtF8gHy7uPjI9QeK',
    'MENTOR',
    TRUE,
    TRUE,
    FALSE
) ON DUPLICATE KEY UPDATE email = email;

-- Mentor Profile
INSERT INTO mentor_profiles (user_id, first_name, last_name, bio, area_of_expertise, years_of_experience, is_verified)
SELECT user_id, 'Jane', 'Smith',
       'Experienced business consultant with 10+ years in entrepreneurship',
       'Business Strategy,Financial Planning,Startup Development',
       10, TRUE
FROM users WHERE email = 'sample.mentor@youthconnect.ug'
ON DUPLICATE KEY UPDATE first_name = first_name;

-- Sample Interests
INSERT INTO user_interests (user_id, interest_tag, interest_level, source, interest_category, is_primary)
SELECT user_id, 'technology', 'HIGH', 'USER_SELECTED', 'SECTOR', TRUE
FROM users WHERE email = 'sample.youth@youthconnect.ug'
ON DUPLICATE KEY UPDATE interest_level = 'HIGH';

-- ============================================================================
-- VERIFICATION
-- ============================================================================

SELECT '✓ Youth Connect Uganda database schema created successfully!' AS Result;
SELECT CONCAT('✓ ', COUNT(*), ' tables created') AS Summary
FROM INFORMATION_SCHEMA.TABLES
WHERE TABLE_SCHEMA = 'youth_connect_db' AND TABLE_TYPE = 'BASE TABLE';