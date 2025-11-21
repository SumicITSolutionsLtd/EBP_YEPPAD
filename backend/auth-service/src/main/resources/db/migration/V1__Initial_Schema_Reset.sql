-- ═══════════════════════════════════════════════════════════════════════════
-- COMPLETE DATABASE SCHEMA RESET (FIXED - INCLUDES ALL TABLES)
-- ═══════════════════════════════════════════════════════════════════════════
-- File: V1__Initial_Schema_Reset.sql
-- Location: backend/auth-service/src/main/resources/db/migration/
-- Author: Douglas Kings Kato
-- Date: 2025-11-19
-- ═══════════════════════════════════════════════════════════════════════════

-- ═══════════════════════════════════════════════════════════════════════════
-- STEP 1: DROP ALL EXISTING TABLES (Clean Slate)
-- ═══════════════════════════════════════════════════════════════════════════

DROP TABLE IF EXISTS security_audit_log CASCADE;
DROP TABLE IF EXISTS email_verification_tokens CASCADE;
DROP TABLE IF EXISTS password_reset_tokens CASCADE;
DROP TABLE IF EXISTS refresh_tokens CASCADE;
DROP TABLE IF EXISTS users CASCADE;

-- ═══════════════════════════════════════════════════════════════════════════
-- STEP 2: DROP AND RECREATE CUSTOM TYPES
-- ═══════════════════════════════════════════════════════════════════════════

DROP TYPE IF EXISTS user_role CASCADE;
DROP TYPE IF EXISTS account_status CASCADE;

-- User roles enum
CREATE TYPE user_role AS ENUM (
    'YOUTH',
    'NGO',
    'FUNDER',
    'SERVICE_PROVIDER',
    'MENTOR',
    'ADMIN',
    'SUPER_ADMIN'
);

-- Account status enum
CREATE TYPE account_status AS ENUM (
    'ACTIVE',
    'INACTIVE',
    'SUSPENDED',
    'LOCKED',
    'PENDING_VERIFICATION'
);

-- ═══════════════════════════════════════════════════════════════════════════
-- STEP 3: CREATE USERS TABLE
-- ═══════════════════════════════════════════════════════════════════════════

CREATE TABLE users (
    -- Primary Key
    user_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    -- Authentication Credentials
    email VARCHAR(100) NOT NULL,
    phone_number VARCHAR(20),
    password_hash VARCHAR(255) NOT NULL,

    -- Personal Information
    first_name VARCHAR(100),
    last_name VARCHAR(100),

    -- User Role and Status
    role user_role NOT NULL DEFAULT 'YOUTH',
    status account_status NOT NULL DEFAULT 'PENDING_VERIFICATION',
    is_active BOOLEAN NOT NULL DEFAULT TRUE,

    -- Verification Flags
    email_verified BOOLEAN NOT NULL DEFAULT FALSE,
    phone_verified BOOLEAN NOT NULL DEFAULT FALSE,

    -- OAuth2 Integration
    oauth2_provider VARCHAR(50),
    oauth2_user_id VARCHAR(255),

    -- Security Fields
    last_login TIMESTAMP,
    last_login_ip VARCHAR(45),
    failed_login_attempts INTEGER NOT NULL DEFAULT 0,
    account_locked_until TIMESTAMP,
    password_changed_at TIMESTAMP,

    -- Audit Fields
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by UUID,
    updated_by UUID,

    -- Soft Delete
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMP,
    deleted_by UUID,

    -- Constraints
    CONSTRAINT uk_users_email UNIQUE (email),
    CONSTRAINT uk_users_phone_number UNIQUE (phone_number),
    CONSTRAINT chk_oauth2_consistency CHECK (
        (oauth2_provider IS NULL AND oauth2_user_id IS NULL) OR
        (oauth2_provider IS NOT NULL AND oauth2_user_id IS NOT NULL)
    )
);

-- Indexes for users table
CREATE INDEX idx_users_email ON users(email) WHERE is_deleted = FALSE;
CREATE INDEX idx_users_phone ON users(phone_number) WHERE is_deleted = FALSE;
CREATE INDEX idx_users_oauth2 ON users(oauth2_provider, oauth2_user_id);
CREATE INDEX idx_users_active ON users(is_active) WHERE is_deleted = FALSE;
CREATE INDEX idx_users_role ON users(role);
CREATE INDEX idx_users_status ON users(status);

-- Comments
COMMENT ON TABLE users IS 'Central user authentication and authorization';
COMMENT ON COLUMN users.oauth2_provider IS 'OAuth2 provider (google, facebook, etc.)';
COMMENT ON COLUMN users.status IS 'Account status for workflow management';

-- ═══════════════════════════════════════════════════════════════════════════
-- STEP 4: CREATE REFRESH TOKENS TABLE
-- ═══════════════════════════════════════════════════════════════════════════

CREATE TABLE refresh_tokens (
    -- Primary Key
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    -- Token Data
    token VARCHAR(500) NOT NULL,

    -- User Reference
    user_id UUID NOT NULL,
    user_email VARCHAR(255) NOT NULL,
    user_role VARCHAR(50) NOT NULL,

    -- Expiration
    expires_at TIMESTAMP NOT NULL,

    -- Tracking
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_used_at TIMESTAMP,

    -- Revocation
    revoked BOOLEAN NOT NULL DEFAULT FALSE,
    revoked_at TIMESTAMP,
    revoked_reason VARCHAR(255),

    -- Metadata
    ip_address VARCHAR(45),
    user_agent TEXT,
    device_info JSONB,

    -- Constraints
    CONSTRAINT uk_refresh_tokens_token UNIQUE (token),
    CONSTRAINT fk_refresh_tokens_user FOREIGN KEY (user_id)
        REFERENCES users(user_id) ON DELETE CASCADE
);

-- Indexes
CREATE INDEX idx_refresh_tokens_token ON refresh_tokens(token);
CREATE INDEX idx_refresh_tokens_user ON refresh_tokens(user_id);
CREATE INDEX idx_refresh_tokens_expires ON refresh_tokens(expires_at);
CREATE INDEX idx_refresh_tokens_revoked ON refresh_tokens(revoked) WHERE revoked = FALSE;

COMMENT ON TABLE refresh_tokens IS 'JWT refresh tokens for session management';

-- ═══════════════════════════════════════════════════════════════════════════
-- STEP 5: CREATE PASSWORD RESET TOKENS TABLE
-- ═══════════════════════════════════════════════════════════════════════════

CREATE TABLE password_reset_tokens (
    -- Primary Key
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    -- Token Data
    token VARCHAR(255) NOT NULL,

    -- User Reference
    user_id UUID NOT NULL,
    user_email VARCHAR(255) NOT NULL,

    -- Expiration
    expires_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- Usage Tracking
    used BOOLEAN NOT NULL DEFAULT FALSE,
    used_at TIMESTAMP,
    attempts INTEGER NOT NULL DEFAULT 0,
    max_attempts INTEGER NOT NULL DEFAULT 3,

    -- Metadata
    ip_address VARCHAR(45),
    user_agent TEXT,

    -- Constraints
    CONSTRAINT uk_password_reset_tokens_token UNIQUE (token),
    CONSTRAINT fk_password_reset_user FOREIGN KEY (user_id)
        REFERENCES users(user_id) ON DELETE CASCADE,
    CONSTRAINT chk_attempts CHECK (attempts <= max_attempts)
);

-- Indexes
CREATE INDEX idx_password_reset_token ON password_reset_tokens(token);
CREATE INDEX idx_password_reset_user ON password_reset_tokens(user_id);
CREATE INDEX idx_password_reset_expires ON password_reset_tokens(expires_at);
CREATE INDEX idx_password_reset_used ON password_reset_tokens(used) WHERE used = FALSE;

COMMENT ON TABLE password_reset_tokens IS 'Password reset workflow tokens';

-- ═══════════════════════════════════════════════════════════════════════════
-- STEP 6: CREATE EMAIL VERIFICATION TOKENS TABLE (MISSING IN ORIGINAL V1)
-- ═══════════════════════════════════════════════════════════════════════════

CREATE TABLE email_verification_tokens (
    -- Primary Key
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    -- Token Data
    token VARCHAR(255) NOT NULL,

    -- User Reference
    user_id UUID NOT NULL,
    user_email VARCHAR(255) NOT NULL,

    -- Expiration
    expires_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- Usage Tracking
    used BOOLEAN NOT NULL DEFAULT FALSE,
    used_at TIMESTAMP,
    attempts INTEGER NOT NULL DEFAULT 0,
    max_attempts INTEGER NOT NULL DEFAULT 5,

    -- Metadata
    ip_address VARCHAR(45),
    user_agent TEXT,

    -- Constraints
    CONSTRAINT uk_email_verification_tokens_token UNIQUE (token),
    CONSTRAINT fk_email_verification_user FOREIGN KEY (user_id)
        REFERENCES users(user_id) ON DELETE CASCADE,
    CONSTRAINT chk_email_verification_attempts CHECK (attempts <= max_attempts)
);

-- Indexes
CREATE INDEX idx_email_verification_token ON email_verification_tokens(token);
CREATE INDEX idx_email_verification_user ON email_verification_tokens(user_id);
CREATE INDEX idx_email_verification_expires ON email_verification_tokens(expires_at);
CREATE INDEX idx_email_verification_used ON email_verification_tokens(used) WHERE used = FALSE;

COMMENT ON TABLE email_verification_tokens IS 'Email verification workflow tokens';

-- ═══════════════════════════════════════════════════════════════════════════
-- STEP 7: CREATE SECURITY AUDIT LOG TABLE
-- ═══════════════════════════════════════════════════════════════════════════

CREATE TABLE security_audit_log (
    -- Primary Key
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    -- User Reference (nullable for failed attempts)
    user_id UUID,
    user_email VARCHAR(255),

    -- Event Details
    event_type VARCHAR(100) NOT NULL,
    event_status VARCHAR(50) NOT NULL,
    event_description TEXT,

    -- Request Metadata
    ip_address VARCHAR(45),
    user_agent TEXT,
    request_path VARCHAR(500),
    request_method VARCHAR(10),

    -- Additional Data
    metadata JSONB,

    -- Timestamp
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- Constraints
    CONSTRAINT fk_audit_log_user FOREIGN KEY (user_id)
        REFERENCES users(user_id) ON DELETE SET NULL
);

-- Indexes
CREATE INDEX idx_audit_log_user ON security_audit_log(user_id);
CREATE INDEX idx_audit_log_event_type ON security_audit_log(event_type);
CREATE INDEX idx_audit_log_created_at ON security_audit_log(created_at);
CREATE INDEX idx_audit_log_ip ON security_audit_log(ip_address);

COMMENT ON TABLE security_audit_log IS 'Security events and audit trail';

-- ═══════════════════════════════════════════════════════════════════════════
-- STEP 8: CREATE TRIGGER FUNCTIONS
-- ═══════════════════════════════════════════════════════════════════════════

-- Function to update 'updated_at' timestamp automatically
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Function to clean up expired tokens
CREATE OR REPLACE FUNCTION cleanup_expired_tokens()
RETURNS void AS $$
BEGIN
    DELETE FROM refresh_tokens WHERE expires_at < CURRENT_TIMESTAMP;
    DELETE FROM password_reset_tokens WHERE expires_at < CURRENT_TIMESTAMP AND used = FALSE;
    DELETE FROM email_verification_tokens WHERE expires_at < CURRENT_TIMESTAMP AND used = FALSE;
END;
$$ LANGUAGE plpgsql;

-- ═══════════════════════════════════════════════════════════════════════════
-- STEP 9: CREATE TRIGGERS
-- ═══════════════════════════════════════════════════════════════════════════

-- Trigger to auto-update 'updated_at' on users table
CREATE TRIGGER trg_users_updated_at
    BEFORE UPDATE ON users
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- ═══════════════════════════════════════════════════════════════════════════
-- STEP 10: SEED DEFAULT ADMIN USER
-- ═══════════════════════════════════════════════════════════════════════════

INSERT INTO users (
    email,
    password_hash,
    first_name,
    last_name,
    role,
    status,
    is_active,
    email_verified
) VALUES (
    'admin@youthconnect.ug',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', -- Password: Admin123!@#
    'System',
    'Administrator',
    'ADMIN',
    'ACTIVE',
    TRUE,
    TRUE
) ON CONFLICT (email) DO NOTHING;

-- ═══════════════════════════════════════════════════════════════════════════
-- MIGRATION V1 COMPLETE
-- ═══════════════════════════════════════════════════════════════════════════