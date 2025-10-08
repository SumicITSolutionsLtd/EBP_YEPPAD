-- Youth Connect Uganda - User Service Database Schema
-- Version: 1.0
-- Description: Initial schema for user service with multi-role support

SET FOREIGN_KEY_CHECKS = 0;

-- =================================================================================
-- USERS TABLE: Central authentication for all platforms (Web + USSD)
-- =================================================================================
CREATE TABLE users (
    user_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(100) UNIQUE NOT NULL COMMENT 'Web login identifier',
    phone_number VARCHAR(20) UNIQUE COMMENT 'USSD login identifier',
    password_hash VARCHAR(255) NOT NULL COMMENT 'BCrypt hashed password',
    role ENUM('YOUTH', 'NGO', 'FUNDER', 'SERVICE_PROVIDER', 'MENTOR', 'ADMIN') NOT NULL,

    -- Account Status
    is_active BOOLEAN DEFAULT TRUE,
    email_verified BOOLEAN DEFAULT FALSE,
    phone_verified BOOLEAN DEFAULT FALSE,

    -- Security Audit
    last_login TIMESTAMP NULL,
    failed_login_attempts INT DEFAULT 0,
    account_locked_until TIMESTAMP NULL,

    -- Timestamps
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    -- Indexes for performance
    INDEX idx_users_phone_active (phone_number, is_active),
    INDEX idx_users_email_active (email, is_active),
    INDEX idx_users_role (role, is_active),

    -- Uganda phone validation constraint
    CONSTRAINT chk_phone_format CHECK (phone_number IS NULL OR phone_number REGEXP '^\\+?256[0-9]{9}$|^0[0-9]{9}$')
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Central user authentication for web and USSD';

-- =================================================================================
-- YOUTH PROFILES TABLE: Detailed profiles for youth users
-- =================================================================================
CREATE TABLE youth_profiles (
    profile_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT UNIQUE NOT NULL,

    -- Personal Information
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    gender ENUM('MALE', 'FEMALE', 'OTHER'),
    date_of_birth DATE,

    -- Location & Demographics
    district VARCHAR(50) COMMENT 'Ugandan districts: Kampala, Wakiso, Mbarara, etc.',
    age_group VARCHAR(20) COMMENT '16-20, 21-25, 26-30, 31-35',

    -- Professional Information
    profession VARCHAR(100),
    business_stage ENUM('IDEA_PHASE', 'EARLY_STAGE', 'GROWTH_STAGE', 'MATURE') DEFAULT 'IDEA_PHASE',
    description TEXT,

    -- Skills and Interests
    skills TEXT COMMENT 'Comma-separated skills',
    interests TEXT COMMENT 'Comma-separated interests',

    -- Accessibility
    has_disability BOOLEAN DEFAULT FALSE,
    disability_details TEXT,

    -- Timestamps
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    -- Foreign Key
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,

    -- Indexes for search and filtering
    INDEX idx_youth_district (district),
    INDEX idx_youth_business_stage (business_stage),
    INDEX idx_youth_age_group (age_group),
    INDEX idx_youth_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Youth user profiles for opportunity matching';

-- =================================================================================
-- MENTOR PROFILES TABLE: Profiles for mentor users
-- =================================================================================
CREATE TABLE mentor_profiles (
    mentor_profile_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT UNIQUE NOT NULL,

    -- Personal Information
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,

    -- Professional Information
    bio TEXT,
    area_of_expertise TEXT NOT NULL COMMENT 'Comma-separated expertise areas',
    experience_years INT,

    -- Availability
    availability_status ENUM('AVAILABLE', 'BUSY', 'ON_LEAVE') DEFAULT 'AVAILABLE',
    max_mentees INT DEFAULT 5,

    -- Verification
    is_verified BOOLEAN DEFAULT FALSE,
    verification_notes TEXT,

    -- Contact Information
    linkedin_url VARCHAR(255),
    website_url VARCHAR(255),

    -- Timestamps
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    -- Foreign Key
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,

    -- Indexes
    INDEX idx_mentor_expertise (area_of_expertise(100)),
    INDEX idx_mentor_availability (availability_status),
    INDEX idx_mentor_verified (is_verified)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Mentor profiles for mentorship system';

-- =================================================================================
-- NGO PROFILES TABLE: Profiles for NGO organizations
-- =================================================================================
CREATE TABLE ngo_profiles (
    ngo_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT UNIQUE NOT NULL,

    -- Organization Information
    organisation_name VARCHAR(150) NOT NULL,
    registration_number VARCHAR(50),
    location VARCHAR(100),
    description TEXT,

    -- Focus Areas
    focus_areas TEXT COMMENT 'Comma-separated focus areas',
    target_audience VARCHAR(100),

    -- Verification
    is_verified BOOLEAN DEFAULT FALSE,
    verification_date TIMESTAMP NULL,
    verified_by BIGINT NULL,

    -- Contact Information
    contact_person VARCHAR(100),
    contact_email VARCHAR(100),
    contact_phone VARCHAR(20),

    -- Timestamps
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    -- Foreign Key
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,

    -- Indexes
    INDEX idx_ngo_name (organisation_name),
    INDEX idx_ngo_verified (is_verified),
    INDEX idx_ngo_location (location)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='NGO profiles with verification system';

-- =================================================================================
-- FUNDER PROFILES TABLE: Profiles for funding organizations
-- =================================================================================
CREATE TABLE funder_profiles (
    funder_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT UNIQUE NOT NULL,

    -- Organization Information
    funder_name VARCHAR(150) NOT NULL,
    funder_type ENUM('GOVERNMENT', 'PRIVATE', 'INTERNATIONAL', 'INDIVIDUAL') DEFAULT 'PRIVATE',
    description TEXT,

    -- Funding Information
    funding_focus TEXT COMMENT 'Areas of funding interest',
    min_funding_amount DECIMAL(15,2),
    max_funding_amount DECIMAL(15,2),
    funding_currency VARCHAR(3) DEFAULT 'UGX',

    -- Contact Information
    contact_person VARCHAR(100),
    contact_email VARCHAR(100),
    website_url VARCHAR(255),

    -- Timestamps
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    -- Foreign Key
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,

    -- Indexes
    INDEX idx_funder_name (funder_name),
    INDEX idx_funder_type (funder_type),
    INDEX idx_funder_focus (funding_focus(100))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Funder organization profiles';

-- =================================================================================
-- SERVICE PROVIDER PROFILES TABLE: Profiles for service providers
-- =================================================================================
CREATE TABLE service_provider_profiles (
    provider_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT UNIQUE NOT NULL,

    -- Business Information
    provider_name VARCHAR(150) NOT NULL,
    business_type VARCHAR(100),
    location VARCHAR(100),
    area_of_expertise TEXT NOT NULL,

    -- Services
    services_offered TEXT COMMENT 'Comma-separated services',
    service_areas TEXT COMMENT 'Geographic areas served',

    -- Verification
    is_verified BOOLEAN DEFAULT FALSE,
    verification_date TIMESTAMP NULL,
    verified_by BIGINT NULL,

    -- Business Details
    years_in_operation INT,
    team_size VARCHAR(50),

    -- Contact Information
    contact_person VARCHAR(100),
    contact_email VARCHAR(100),
    website_url VARCHAR(255),

    -- Timestamps
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    -- Foreign Key
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,

    -- Indexes
    INDEX idx_provider_name (provider_name),
    INDEX idx_provider_verified (is_verified),
    INDEX idx_provider_expertise (area_of_expertise(100)),
    INDEX idx_provider_location (location)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Service providers with NGO verification';

-- =================================================================================
-- USER SESSIONS TABLE: Track user sessions for audit and analytics
-- =================================================================================
CREATE TABLE user_sessions (
    session_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    session_token VARCHAR(500) NOT NULL,
    device_type ENUM('WEB', 'MOBILE', 'USSD') DEFAULT 'WEB',
    ip_address VARCHAR(45),
    user_agent TEXT,

    -- Session Management
    login_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_activity TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    logout_time TIMESTAMP NULL,
    expires_at TIMESTAMP NOT NULL,

    -- Foreign Key
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,

    -- Indexes
    INDEX idx_sessions_user (user_id),
    INDEX idx_sessions_token (session_token),
    INDEX idx_sessions_expires (expires_at),
    INDEX idx_sessions_device (device_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='User session tracking for security and analytics';

-- =================================================================================
-- AUDIT TRAIL TABLE: Security and compliance logging
-- =================================================================================
CREATE TABLE audit_trail (
    audit_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NULL,

    -- Audit Information
    action_type VARCHAR(100) NOT NULL COMMENT 'LOGIN, REGISTER, UPDATE_PROFILE, etc.',
    entity_type VARCHAR(100) NULL,
    entity_id BIGINT NULL,
    description TEXT,

    -- Request Details
    ip_address VARCHAR(45),
    user_agent TEXT,
    request_url VARCHAR(500),

    -- Status
    status ENUM('SUCCESS', 'FAILED', 'PARTIAL') NOT NULL,
    error_message TEXT,

    -- Timestamps
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    -- Foreign Key
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE SET NULL,

    -- Indexes
    INDEX idx_audit_user (user_id),
    INDEX idx_audit_action (action_type),
    INDEX idx_audit_entity (entity_type, entity_id),
    INDEX idx_audit_created (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Audit trail for security and compliance';

-- =================================================================================
-- INITIAL DATA: Default admin user and sample data
-- =================================================================================

-- Default Admin User (Change password immediately after deployment)
INSERT INTO users (email, phone_number, password_hash, role, is_active, email_verified, phone_verified)
VALUES (
    'admin@youthconnect.ug',
    '+256700000000',
    '$2a$12$rQOZgIwI6k8QpqZtDlEPUeF5HYD8v4x1z0fXhRtF8gHy7uPjI9QeK', -- Admin@123
    'ADMIN',
    TRUE,
    TRUE,
    TRUE
);

-- Sample Youth User
INSERT INTO users (email, phone_number, password_hash, role, is_active, email_verified)
VALUES (
    'sample.youth@youthconnect.ug',
    '+256701234567',
    '$2a$12$rQOZgIwI6k8QpqZtDlEPUeF5HYD8v4x1z0fXhRtF8gHy7uPjI9QeK', -- Youth@123
    'YOUTH',
    TRUE,
    TRUE
);

INSERT INTO youth_profiles (user_id, first_name, last_name, gender, district, profession, business_stage, skills)
SELECT user_id, 'John', 'Doe', 'MALE', 'Kampala', 'Software Developer', 'EARLY_STAGE', 'Java,Spring Boot,MySQL'
FROM users WHERE email = 'sample.youth@youthconnect.ug';

-- Sample Mentor User
INSERT INTO users (email, phone_number, password_hash, role, is_active, email_verified)
VALUES (
    'sample.mentor@youthconnect.ug',
    '+256702345678',
    '$2a$12$rQOZgIwI6k8QpqZtDlEPUeF5HYD8v4x1z0fXhRtF8gHy7uPjI9QeK', -- Mentor@123
    'MENTOR',
    TRUE,
    TRUE
);

INSERT INTO mentor_profiles (user_id, first_name, last_name, bio, area_of_expertise, experience_years, is_verified)
SELECT user_id, 'Jane', 'Smith', 'Experienced business consultant with 10+ years in entrepreneurship', 'Business Strategy,Financial Planning,Startup Development', 10, TRUE
FROM users WHERE email = 'sample.mentor@youthconnect.ug';

SET FOREIGN_KEY_CHECKS = 1;

-- =================================================================================
-- DATABASE VERIFICATION QUERIES
-- =================================================================================

-- Verify table creation
SELECT
    TABLE_NAME,
    TABLE_ROWS,
    ROUND((DATA_LENGTH + INDEX_LENGTH) / 1024 / 1024, 2) AS 'SIZE_MB'
FROM INFORMATION_SCHEMA.TABLES
WHERE TABLE_SCHEMA = DATABASE()
AND TABLE_TYPE = 'BASE TABLE'
ORDER BY TABLE_NAME;

-- Verify foreign keys
SELECT
    TABLE_NAME,
    CONSTRAINT_NAME,
    REFERENCED_TABLE_NAME
FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE
WHERE TABLE_SCHEMA = DATABASE()
AND REFERENCED_TABLE_NAME IS NOT NULL;

-- Database statistics
SELECT
    'Tables' as OBJECT_TYPE, COUNT(*) as COUNT
FROM INFORMATION_SCHEMA.TABLES
WHERE TABLE_SCHEMA = DATABASE() AND TABLE_TYPE = 'BASE TABLE'
UNION ALL
SELECT 'Foreign Keys' as OBJECT_TYPE, COUNT(DISTINCT CONSTRAINT_NAME) as COUNT
FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE
WHERE TABLE_SCHEMA = DATABASE() AND REFERENCED_TABLE_NAME IS NOT NULL;