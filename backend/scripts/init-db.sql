-- =================================================================================
-- Entrepreneurship Booster Platform - Production Database Schema
-- Version: 1.0
-- Date: 2025-10-22
-- Description: Complete production schema with all essential features
-- =================================================================================

DROP DATABASE IF EXISTS epb_db;
CREATE DATABASE epb_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE epb_db;

-- =================================================================================
-- SECTION 1: CORE AUTHENTICATION & USER MANAGEMENT
-- =================================================================================

-- Users Table: Central authentication for all platforms (Web + USSD)
CREATE TABLE users (
    user_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(100) UNIQUE NOT NULL COMMENT 'Web login identifier',
    phone_number VARCHAR(20) UNIQUE COMMENT 'USSD login identifier',
    password_hash VARCHAR(255) NOT NULL COMMENT 'BCrypt hashed password',
    role ENUM('YOUTH', 'NGO', 'FUNDER', 'SERVICE_PROVIDER', 'MENTOR', 'ADMIN') NOT NULL,

    -- Account Security
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

    -- Uganda phone validation
    CONSTRAINT chk_phone_format CHECK (phone_number IS NULL OR phone_number REGEXP '^\+?256[0-9]{9}$|^0[0-9]{9}$')
) ENGINE=InnoDB COMMENT 'Central user authentication for web and USSD';

CREATE INDEX idx_users_phone_active ON users(phone_number, is_active);
CREATE INDEX idx_users_email_active ON users(email, is_active);
CREATE INDEX idx_users_role ON users(role, is_active);

-- Refresh Tokens: JWT management
CREATE TABLE refresh_tokens (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    token VARCHAR(500) NOT NULL UNIQUE,
    user_id BIGINT NOT NULL,
    user_email VARCHAR(255) NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    revoked BOOLEAN DEFAULT FALSE,

    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    INDEX idx_token (token),
    INDEX idx_user_active (user_id, revoked, expires_at)
) ENGINE=InnoDB COMMENT 'JWT refresh token management';

-- Password Reset Tokens
CREATE TABLE password_reset_tokens (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    token VARCHAR(255) NOT NULL UNIQUE,
    user_id BIGINT NOT NULL,
    user_email VARCHAR(255) NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    used BOOLEAN DEFAULT FALSE,

    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    INDEX idx_token (token),
    INDEX idx_user_unused (user_id, used, expires_at)
) ENGINE=InnoDB COMMENT 'Password reset functionality';

-- =================================================================================
-- SECTION 2: USER PROFILES (Role-Specific)
-- =================================================================================

-- Youth Profiles
CREATE TABLE youth_profiles (
    profile_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT UNIQUE NOT NULL,

    -- Personal Information
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    gender ENUM('MALE', 'FEMALE', 'OTHER'),
    date_of_birth DATE,

    -- Location & Demographics
    district VARCHAR(50) COMMENT 'Ugandan districts',
    age_group VARCHAR(20),

    -- Professional Information
    profession VARCHAR(100),
    business_stage VARCHAR(50) COMMENT 'Idea Phase, Early Stage, Growth Stage',
    description TEXT,

    -- Accessibility
    has_disability BOOLEAN DEFAULT FALSE,

    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    INDEX idx_district (district),
    INDEX idx_business_stage (business_stage)
) ENGINE=InnoDB COMMENT 'Youth user profiles';

-- NGO Profiles
CREATE TABLE ngo_profiles (
    ngo_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT UNIQUE NOT NULL,

    organisation_name VARCHAR(150) NOT NULL,
    location VARCHAR(100),
    description TEXT,

    -- Verification
    is_verified BOOLEAN DEFAULT FALSE,
    verification_date TIMESTAMP NULL,
    verified_by BIGINT NULL,

    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (verified_by) REFERENCES users(user_id) ON DELETE SET NULL,
    INDEX idx_verified (is_verified)
) ENGINE=InnoDB COMMENT 'NGO profiles with verification';

-- Service Provider Profiles
CREATE TABLE service_provider_profiles (
    provider_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT UNIQUE NOT NULL,

    provider_name VARCHAR(150) NOT NULL,
    location VARCHAR(100),
    area_of_expertise TEXT NOT NULL,

    -- Verification
    is_verified BOOLEAN DEFAULT FALSE,
    verification_date TIMESTAMP NULL,
    verified_by BIGINT NULL,

    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (verified_by) REFERENCES users(user_id) ON DELETE SET NULL
) ENGINE=InnoDB COMMENT 'Service providers with NGO verification';

-- Funder Profiles
CREATE TABLE funder_profiles (
    funder_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT UNIQUE NOT NULL,

    funder_name VARCHAR(150) NOT NULL,
    funding_focus TEXT COMMENT 'Funding interests and criteria',

    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
) ENGINE=InnoDB COMMENT 'Funder organization profiles';

-- Mentor Profiles
CREATE TABLE mentor_profiles (
    mentor_profile_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT UNIQUE NOT NULL,

    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    bio TEXT,
    area_of_expertise TEXT NOT NULL,
    experience_years INT,

    -- Availability
    availability_status ENUM('AVAILABLE', 'BUSY', 'ON_LEAVE') DEFAULT 'AVAILABLE',

    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    INDEX idx_availability (availability_status)
) ENGINE=InnoDB COMMENT 'Mentor profiles';

-- =================================================================================
-- SECTION 3: OPPORTUNITIES & APPLICATIONS
-- =================================================================================

-- Opportunities
CREATE TABLE opportunities (
    opportunity_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    posted_by_id BIGINT NOT NULL,

    -- Classification
    opportunity_type ENUM('GRANT', 'LOAN', 'JOB', 'TRAINING', 'SKILL_MARKET') NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    requirements TEXT,

    -- Management
    status ENUM('DRAFT', 'OPEN', 'CLOSED', 'IN_REVIEW', 'COMPLETED') DEFAULT 'DRAFT',
    application_deadline TIMESTAMP NULL,

    -- Financial details
    funding_amount DECIMAL(15,2),
    currency VARCHAR(3) DEFAULT 'UGX',

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    FOREIGN KEY (posted_by_id) REFERENCES users(user_id) ON DELETE CASCADE,
    INDEX idx_type_status (opportunity_type, status),
    INDEX idx_deadline (application_deadline),
    INDEX idx_posted_by (posted_by_id)
) ENGINE=InnoDB COMMENT 'Opportunities posted by NGOs, funders, and partners';

-- Applications
CREATE TABLE applications (
    application_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    opportunity_id BIGINT NOT NULL,
    applicant_id BIGINT NOT NULL,

    status ENUM('PENDING', 'UNDER_REVIEW', 'APPROVED', 'REJECTED', 'WITHDRAWN') DEFAULT 'PENDING',

    -- Review process
    reviewed_by_id BIGINT NULL,
    review_notes TEXT,
    reviewed_at TIMESTAMP NULL,
    application_content TEXT,

    submitted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    FOREIGN KEY (opportunity_id) REFERENCES opportunities(opportunity_id) ON DELETE CASCADE,
    FOREIGN KEY (applicant_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (reviewed_by_id) REFERENCES users(user_id) ON DELETE SET NULL,

    UNIQUE KEY unique_application (opportunity_id, applicant_id),
    INDEX idx_user_status (applicant_id, status),
    INDEX idx_opp_status (opportunity_id, status)
) ENGINE=InnoDB COMMENT 'User applications with review tracking';

-- =================================================================================
-- SECTION 4: MENTORSHIP SYSTEM
-- =================================================================================

-- Mentorship Sessions
CREATE TABLE mentorship_sessions (
    session_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    mentor_id BIGINT NOT NULL,
    mentee_id BIGINT NOT NULL,

    session_datetime TIMESTAMP NOT NULL,
    duration_minutes INT DEFAULT 60,
    topic VARCHAR(255),

    status ENUM('SCHEDULED', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED', 'NO_SHOW') DEFAULT 'SCHEDULED',

    -- Session notes
    mentor_notes TEXT,
    mentee_notes TEXT,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    FOREIGN KEY (mentor_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (mentee_id) REFERENCES users(user_id) ON DELETE CASCADE,
    INDEX idx_mentor_status_datetime (mentor_id, status, session_datetime),
    INDEX idx_mentee_status_datetime (mentee_id, status, session_datetime),
    INDEX idx_datetime (session_datetime)
) ENGINE=InnoDB COMMENT 'Mentorship session scheduling and tracking';

-- Mentor Availability
CREATE TABLE mentor_availability (
    availability_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    mentor_id BIGINT NOT NULL,

    -- Weekly schedule
    day_of_week ENUM('MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY', 'SUNDAY') NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,

    is_active BOOLEAN DEFAULT TRUE,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    FOREIGN KEY (mentor_id) REFERENCES users(user_id) ON DELETE CASCADE,
    INDEX idx_mentor_day (mentor_id, day_of_week, is_active),

    CONSTRAINT chk_time_order CHECK (start_time < end_time)
) ENGINE=InnoDB COMMENT 'Mentor weekly availability schedule';

-- Session Reminders
CREATE TABLE session_reminders (
    reminder_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    session_id BIGINT NOT NULL,

    reminder_type ENUM('24_HOURS', '1_HOUR', '15_MINUTES') NOT NULL,
    scheduled_time TIMESTAMP NOT NULL,

    -- Delivery status
    sent_to_mentor BOOLEAN DEFAULT FALSE,
    sent_to_mentee BOOLEAN DEFAULT FALSE,
    sent_at TIMESTAMP NULL,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (session_id) REFERENCES mentorship_sessions(session_id) ON DELETE CASCADE,
    INDEX idx_session_scheduled (session_id, scheduled_time),
    INDEX idx_scheduled_pending (scheduled_time, sent_to_mentor, sent_to_mentee)
) ENGINE=InnoDB COMMENT 'Automated session reminder tracking';

-- Reviews
CREATE TABLE reviews (
    review_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    reviewer_id BIGINT NOT NULL,
    reviewee_id BIGINT NOT NULL,

    rating TINYINT NOT NULL CHECK (rating BETWEEN 1 AND 5),
    comment TEXT,
    review_type ENUM('MENTOR_SESSION', 'SERVICE_DELIVERY', 'GENERAL') DEFAULT 'GENERAL',
    session_id BIGINT NULL,

    -- Moderation
    is_approved BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (reviewer_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (reviewee_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (session_id) REFERENCES mentorship_sessions(session_id) ON DELETE SET NULL,
    INDEX idx_reviewee_type_approved (reviewee_id, review_type, is_approved),
    INDEX idx_session_id (session_id),
    INDEX idx_rating (rating),

    CONSTRAINT chk_different_users CHECK (reviewer_id != reviewee_id)
) ENGINE=InnoDB COMMENT 'Review system for mentors and services';

-- =================================================================================
-- SECTION 5: CONTENT & COMMUNITY
-- =================================================================================

-- Posts
CREATE TABLE posts (
    post_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    author_id BIGINT NOT NULL,

    post_type ENUM('FORUM_QUESTION', 'SUCCESS_STORY', 'ARTICLE', 'AUDIO_QUESTION') NOT NULL,
    title VARCHAR(255),
    content TEXT,

    -- Moderation
    is_approved BOOLEAN DEFAULT TRUE,
    is_active BOOLEAN DEFAULT TRUE,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (author_id) REFERENCES users(user_id) ON DELETE CASCADE,
    INDEX idx_type_approved (post_type, is_approved),
    INDEX idx_author (author_id)
) ENGINE=InnoDB COMMENT 'Community posts and content sharing';

-- Comments
CREATE TABLE comments (
    comment_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    post_id BIGINT NOT NULL,
    author_id BIGINT NOT NULL,

    comment_text TEXT NOT NULL,
    parent_comment_id BIGINT NULL,

    -- Moderation
    is_approved BOOLEAN DEFAULT TRUE,
    is_active BOOLEAN DEFAULT TRUE,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (post_id) REFERENCES posts(post_id) ON DELETE CASCADE,
    FOREIGN KEY (author_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (parent_comment_id) REFERENCES comments(comment_id) ON DELETE CASCADE,
    INDEX idx_post (post_id, is_approved)
) ENGINE=InnoDB COMMENT 'Post comments with threading support';

-- Learning Modules
CREATE TABLE learning_modules (
    module_id BIGINT AUTO_INCREMENT PRIMARY KEY,

    title VARCHAR(255) NOT NULL,
    description TEXT,
    content_type ENUM('AUDIO', 'VIDEO', 'TEXT', 'MIXED') DEFAULT 'AUDIO',

    -- Multi-language audio support
    audio_url_en VARCHAR(255) COMMENT 'English audio',
    audio_url_lg VARCHAR(255) COMMENT 'Luganda audio',
    audio_url_lur VARCHAR(255) COMMENT 'Alur audio',
    audio_url_lgb VARCHAR(255) COMMENT 'Lugbara audio',

    is_active BOOLEAN DEFAULT TRUE,
    sort_order INT DEFAULT 0,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    INDEX idx_active_order (is_active, sort_order)
) ENGINE=InnoDB COMMENT 'Educational content with multi-language audio';

-- =================================================================================
-- SECTION 6: USSD INTEGRATION
-- =================================================================================

-- USSD Sessions
CREATE TABLE ussd_sessions (
    session_id VARCHAR(255) PRIMARY KEY,
    phone_number VARCHAR(20) NOT NULL,

    current_menu VARCHAR(50) NOT NULL,
    session_data JSON COMMENT 'Temporary session storage',

    -- Registration data
    user_name VARCHAR(100),
    user_gender VARCHAR(10),
    user_age_group VARCHAR(10),
    user_district VARCHAR(50),
    user_business_stage VARCHAR(50),

    is_active BOOLEAN DEFAULT TRUE,
    expires_at TIMESTAMP NULL,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    INDEX idx_phone_active (phone_number, is_active),
    INDEX idx_expires (expires_at)
) ENGINE=InnoDB COMMENT 'USSD session state management';

-- =================================================================================
-- SECTION 7: AI & ANALYTICS
-- =================================================================================

-- User Activity Logs
CREATE TABLE user_activity_logs (
    log_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,

    activity_type VARCHAR(50) NOT NULL,
    target_id BIGINT NULL,
    target_type VARCHAR(50) NULL,

    session_id VARCHAR(255) NULL,
    metadata JSON,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    INDEX idx_user_activity (user_id, activity_type, created_at),
    INDEX idx_target (target_type, target_id)
) ENGINE=InnoDB COMMENT 'User activity tracking for AI recommendations';

-- User Interests
CREATE TABLE user_interests (
    user_interest_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,

    interest_tag VARCHAR(50) NOT NULL,
    interest_level ENUM('LOW', 'MEDIUM', 'HIGH') DEFAULT 'MEDIUM',
    source ENUM('USER_SELECTED', 'AI_INFERRED', 'ACTIVITY_BASED') DEFAULT 'USER_SELECTED',

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    UNIQUE KEY unique_user_interest (user_id, interest_tag),
    INDEX idx_user_interests (user_id, interest_level)
) ENGINE=InnoDB COMMENT 'User interests for personalized recommendations';

-- =================================================================================
-- SECTION 8: FILE MANAGEMENT
-- =================================================================================

-- File Records
CREATE TABLE file_records (
    file_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NULL,

    file_name VARCHAR(255) NOT NULL,
    original_name VARCHAR(255),
    file_path VARCHAR(500) NOT NULL,
    file_size BIGINT,
    content_type VARCHAR(100),

    file_category ENUM('PROFILE_PICTURE', 'DOCUMENT', 'AUDIO_MODULE', 'APPLICATION_ATTACHMENT') NOT NULL,

    is_active BOOLEAN DEFAULT TRUE,
    is_public BOOLEAN DEFAULT FALSE,

    upload_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    INDEX idx_user_category (user_id, file_category),
    INDEX idx_active (is_active)
) ENGINE=InnoDB COMMENT 'File management with access control';

-- =================================================================================
-- SECTION 9: NOTIFICATION SYSTEM
-- =================================================================================

-- Notification Logs
CREATE TABLE notification_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,

    notification_type ENUM('SMS', 'EMAIL', 'PUSH') NOT NULL,
    recipient VARCHAR(255) NOT NULL,

    subject VARCHAR(255),
    content TEXT NOT NULL,

    status ENUM('PENDING', 'SENT', 'DELIVERED', 'FAILED') DEFAULT 'PENDING',
    sent_at TIMESTAMP NULL,

    retry_count INT DEFAULT 0,
    error_message TEXT,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    INDEX idx_user_status (user_id, status),
    INDEX idx_retry (retry_count, status)
) ENGINE=InnoDB COMMENT 'Notification delivery tracking';

-- =================================================================================
-- SECTION 10: AUDIT TRAIL
-- =================================================================================

-- Audit Trail
CREATE TABLE audit_trail (
    audit_id BIGINT AUTO_INCREMENT PRIMARY KEY,

    user_id BIGINT NULL,
    service_name VARCHAR(100) NOT NULL,

    action_type VARCHAR(100) NOT NULL,
    entity_type VARCHAR(100) NULL,
    entity_id BIGINT NULL,

    description TEXT,
    old_values JSON,
    new_values JSON,

    ip_address VARCHAR(45),
    user_agent TEXT,

    status ENUM('SUCCESS', 'FAILED', 'PARTIAL') NOT NULL,
    error_message TEXT,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE SET NULL,
    INDEX idx_user_action (user_id, action_type, created_at),
    INDEX idx_entity (entity_type, entity_id),
    INDEX idx_service (service_name, created_at)
) ENGINE=InnoDB COMMENT 'Audit trail for security and compliance';

-- =================================================================================
-- SECTION 11: ANALYTICS VIEWS
-- =================================================================================

-- User Complete Profiles View
CREATE VIEW user_complete_profiles AS
SELECT
    u.user_id,
    u.email,
    u.phone_number,
    u.role,
    u.is_active,
    u.created_at as registration_date,

    CASE
        WHEN u.role = 'YOUTH' THEN CONCAT(yp.first_name, ' ', yp.last_name)
        WHEN u.role = 'MENTOR' THEN CONCAT(mp.first_name, ' ', mp.last_name)
        WHEN u.role = 'NGO' THEN ngp.organisation_name
        WHEN u.role = 'SERVICE_PROVIDER' THEN spp.provider_name
        WHEN u.role = 'FUNDER' THEN fp.funder_name
        ELSE u.email
    END as display_name,

    yp.district,
    yp.profession,
    yp.business_stage,
    ngp.is_verified as ngo_verified,
    spp.is_verified as provider_verified

FROM users u
LEFT JOIN youth_profiles yp ON u.user_id = yp.user_id
LEFT JOIN mentor_profiles mp ON u.user_id = mp.user_id
LEFT JOIN ngo_profiles ngp ON u.user_id = ngp.user_id
LEFT JOIN service_provider_profiles spp ON u.user_id = spp.user_id
LEFT JOIN funder_profiles fp ON u.user_id = fp.user_id;

-- Active Opportunities View
CREATE VIEW active_opportunities_view AS
SELECT
    o.opportunity_id,
    o.title,
    o.opportunity_type,
    o.description,
    o.status,
    o.application_deadline,
    o.funding_amount,
    ucp.display_name as posted_by_name,
    u.role as posted_by_role,
    o.created_at,
    COUNT(a.application_id) as application_count

FROM opportunities o
JOIN users u ON o.posted_by_id = u.user_id
JOIN user_complete_profiles ucp ON u.user_id = ucp.user_id
LEFT JOIN applications a ON o.opportunity_id = a.opportunity_id
WHERE o.status IN ('OPEN', 'IN_REVIEW')
GROUP BY o.opportunity_id, o.title, o.opportunity_type, o.description,
         o.status, o.application_deadline, o.funding_amount,
         ucp.display_name, u.role, o.created_at;

-- =================================================================================
-- SECTION 12: INITIAL SEED DATA
-- =================================================================================

-- Default Admin User (CHANGE PASSWORD AFTER DEPLOYMENT!)
INSERT INTO users (email, phone_number, password_hash, role, is_active, email_verified, phone_verified)
VALUES (
    'admin@youthconnect.ug',
    '+256700000001',
    '$2a$12$rQOZgIwI6k8QpqZtDlEPUeF5HYD8v4x1z0fXhRtF8gHy7uPjI9QeK',
    'ADMIN',
    TRUE,
    TRUE,
    TRUE
);

-- Sample Youth User
INSERT INTO users (email, phone_number, password_hash, role, is_active, email_verified)
VALUES (
    'damienpapers3@gmail.com',
    '+256701430234',
    '$2a$12$rQOZgIwI6k8QpqZtDlEPUeF5HYD8v4x1z0fXhRtF8gHy7uPjI9QeK',
    'YOUTH',
    TRUE,
    TRUE
);

INSERT INTO youth_profiles (user_id, first_name, last_name, gender, district, profession)
VALUES (2, 'Damien', 'Papers', 'MALE', 'Kampala', 'Software Developer');

-- Sample Learning Modules
INSERT INTO learning_modules (title, description, content_type, audio_url_en, audio_url_lg) VALUES
('Business Basics', 'Fundamentals of starting a business', 'AUDIO',
 'https://files.youthconnect.ug/audio/business_basics_en.mp3',
 'https://files.youthconnect.ug/audio/business_basics_lg.mp3'),
('Financial Literacy', 'Managing your business finances', 'AUDIO',
 'https://files.youthconnect.ug/audio/financial_literacy_en.mp3',
 'https://files.youthconnect.ug/audio/financial_literacy_lg.mp3');

-- Sample Opportunities
INSERT INTO opportunities (posted_by_id, opportunity_type, title, description, status, funding_amount) VALUES
(1, 'GRANT', 'Young Entrepreneurs Grant 2025',
 'Funding for innovative business ideas from youth aged 18-30', 'OPEN', 5000000.00),
(1, 'TRAINING', 'Digital Marketing Bootcamp',
 'Comprehensive training in digital marketing strategies', 'OPEN', NULL);

-- =================================================================================
-- SECTION 13: MAINTENANCE PROCEDURES
-- =================================================================================

DELIMITER $$

CREATE PROCEDURE sp_clean_expired_tokens()
BEGIN
    DELETE FROM refresh_tokens
    WHERE expires_at < NOW() AND revoked = TRUE;

    DELETE FROM password_reset_tokens
    WHERE (used = TRUE OR expires_at < NOW())
    AND created_at < DATE_SUB(NOW(), INTERVAL 7 DAY);

    DELETE FROM ussd_sessions
    WHERE expires_at < NOW()
    OR last_updated < DATE_SUB(NOW(), INTERVAL 24 HOUR);

    SELECT 'Cleanup completed' as message,
           ROW_COUNT() as rows_affected,
           NOW() as executed_at;
END$$

DELIMITER ;

-- =================================================================================
-- DATABASE VERIFICATION
-- =================================================================================

SELECT 'Database creation completed successfully!' as STATUS;

SELECT
    TABLE_NAME,
    TABLE_ROWS,
    ROUND((DATA_LENGTH + INDEX_LENGTH) / 1024 / 1024, 2) AS 'SIZE_MB'
FROM INFORMATION_SCHEMA.TABLES
WHERE TABLE_SCHEMA = 'epb_db'
AND TABLE_TYPE = 'BASE TABLE'
ORDER BY TABLE_NAME;

-- =================================================================================
-- END OF PRODUCTION SCHEMA
-- =================================================================================