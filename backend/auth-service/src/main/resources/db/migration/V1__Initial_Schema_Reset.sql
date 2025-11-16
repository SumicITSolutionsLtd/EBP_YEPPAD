-- ═══════════════════════════════════════════════════════════════════════════
-- COMPLETE DATABASE SCHEMA RESET
-- File: V1__Initial_Schema_Reset.sql
-- Location: backend/auth-service/src/main/resources/db/migration/
-- ═══════════════════════════════════════════════════════════════════════════
-- PURPOSE: Clean slate migration to fix checksum mismatch
-- This completely rebuilds the schema with correct checksums
-- ═══════════════════════════════════════════════════════════════════════════

-- Step 1: Drop ALL existing tables (clean slate)
DROP TABLE IF EXISTS email_verification_tokens CASCADE;
DROP TABLE IF EXISTS login_attempts CASCADE;
DROP TABLE IF EXISTS security_audit_log CASCADE;
DROP TABLE IF EXISTS ussd_sessions CASCADE;
DROP TABLE IF EXISTS password_reset_tokens CASCADE;
DROP TABLE IF EXISTS refresh_tokens CASCADE;
DROP TABLE IF EXISTS users CASCADE;

-- Step 2: Drop custom types
DROP TYPE IF EXISTS user_role CASCADE;
DROP TYPE IF EXISTS account_status CASCADE;

-- ═══════════════════════════════════════════════════════════════════════════
-- CUSTOM TYPES (ENUMS)
-- ═══════════════════════════════════════════════════════════════════════════

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

CREATE TYPE account_status AS ENUM (
    'ACTIVE',
    'INACTIVE',
    'SUSPENDED',
    'LOCKED',
    'PENDING_VERIFICATION',
    'DELETED'
);

-- ═══════════════════════════════════════════════════════════════════════════
-- USERS TABLE (Core Authentication)
-- ═══════════════════════════════════════════════════════════════════════════

CREATE TABLE users (
    -- Primary Key
    user_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),

    -- Credentials
    email VARCHAR(100) NOT NULL UNIQUE,
    phone_number VARCHAR(20) UNIQUE,
    password_hash VARCHAR(255) NOT NULL,

    -- Profile
    first_name VARCHAR(100),
    last_name VARCHAR(100),

    -- Authorization
    role user_role NOT NULL DEFAULT 'YOUTH',

    -- Status
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    email_verified BOOLEAN NOT NULL DEFAULT FALSE,
    phone_verified BOOLEAN NOT NULL DEFAULT FALSE,

    -- Security
    last_login TIMESTAMP,
    last_login_ip VARCHAR(45),
    failed_login_attempts INTEGER NOT NULL DEFAULT 0,
    account_locked_until TIMESTAMP,

    -- OAuth2 Integration
    oauth2_provider VARCHAR(50), -- 'google', 'facebook', 'apple', null for email/password
    oauth2_user_id VARCHAR(255), -- Provider's user ID

    -- Audit
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by UUID,
    updated_by UUID,

    -- Soft Delete
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMP,

    -- Indexes
    CONSTRAINT chk_oauth2_consistency CHECK (
        (oauth2_provider IS NULL AND oauth2_user_id IS NULL) OR
        (oauth2_provider IS NOT NULL AND oauth2_user_id IS NOT NULL)
    )
);

CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_phone ON users(phone_number);
CREATE INDEX idx_users_oauth2 ON users(oauth2_provider, oauth2_user_id);
CREATE INDEX idx_users_active ON users(is_active) WHERE is_deleted = FALSE;

-- ═══════════════════════════════════════════════════════════════════════════
-- REFRESH TOKENS TABLE (JWT Session Management)
-- ═══════════════════════════════════════════════════════════════════════════

CREATE TABLE refresh_tokens (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),

    -- Token
    token VARCHAR(500) NOT NULL UNIQUE,

    -- User Reference
    user_id UUID NOT NULL,
    user_email VARCHAR(255) NOT NULL,
    user_role VARCHAR(50) NOT NULL,

    -- Lifecycle
    expires_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_used_at TIMESTAMP,

    -- Revocation
    revoked BOOLEAN NOT NULL DEFAULT FALSE,
    revoked_at TIMESTAMP,

    -- Device Tracking
    ip_address VARCHAR(45),
    user_agent TEXT,

    -- Constraints
    CONSTRAINT fk_refresh_tokens_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

CREATE INDEX idx_refresh_tokens_token ON refresh_tokens(token);
CREATE INDEX idx_refresh_tokens_user ON refresh_tokens(user_id);
CREATE INDEX idx_refresh_tokens_expires ON refresh_tokens(expires_at);

-- ═══════════════════════════════════════════════════════════════════════════
-- PASSWORD RESET TOKENS TABLE (Password Recovery)
-- ═══════════════════════════════════════════════════════════════════════════

CREATE TABLE password_reset_tokens (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),

    -- Token
    token VARCHAR(255) NOT NULL UNIQUE,

    -- User Reference
    user_id UUID NOT NULL,
    user_email VARCHAR(255) NOT NULL,

    -- Lifecycle
    expires_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- Usage
    used BOOLEAN NOT NULL DEFAULT FALSE,
    used_at TIMESTAMP,

    -- Security (Brute Force Protection)
    attempts INTEGER NOT NULL DEFAULT 0,
    max_attempts INTEGER NOT NULL DEFAULT 3,

    -- Device Tracking
    ip_address VARCHAR(45),
    user_agent TEXT,

    -- Constraints
    CONSTRAINT fk_password_reset_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    CONSTRAINT chk_password_reset_attempts CHECK (attempts >= 0),
    CONSTRAINT chk_password_reset_max_attempts CHECK (max_attempts > 0)
);

CREATE INDEX idx_password_reset_token ON password_reset_tokens(token);
CREATE INDEX idx_password_reset_user ON password_reset_tokens(user_id);
CREATE INDEX idx_password_reset_expires ON password_reset_tokens(expires_at);

-- ═══════════════════════════════════════════════════════════════════════════
-- EMAIL VERIFICATION TOKENS TABLE
-- ═══════════════════════════════════════════════════════════════════════════

CREATE TABLE email_verification_tokens (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    token VARCHAR(255) NOT NULL UNIQUE,
    user_id UUID NOT NULL,
    email VARCHAR(255) NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    verified_at TIMESTAMP,
    is_verified BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_email_verify_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

CREATE INDEX idx_email_verify_token ON email_verification_tokens(token);
CREATE INDEX idx_email_verify_user ON email_verification_tokens(user_id);

-- ═══════════════════════════════════════════════════════════════════════════
-- SECURITY AUDIT LOG TABLE
-- ═══════════════════════════════════════════════════════════════════════════

CREATE TABLE security_audit_log (
    log_id BIGSERIAL PRIMARY KEY,
    user_id UUID,
    event_type VARCHAR(50) NOT NULL,
    event_description TEXT,
    ip_address VARCHAR(45),
    user_agent TEXT,
    request_path VARCHAR(500),
    http_method VARCHAR(10),
    metadata JSONB DEFAULT '{}'::jsonb,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_audit_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE SET NULL
);

CREATE INDEX idx_audit_user ON security_audit_log(user_id, created_at DESC);
CREATE INDEX idx_audit_event ON security_audit_log(event_type, created_at DESC);
CREATE INDEX idx_audit_metadata ON security_audit_log USING gin(metadata);

-- ═══════════════════════════════════════════════════════════════════════════
-- SEED DATA (Admin User)
-- ═══════════════════════════════════════════════════════════════════════════

INSERT INTO users (
    email,
    password_hash,
    first_name,
    last_name,
    role,
    is_active,
    email_verified
) VALUES (
    'admin@youthconnect.ug',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', -- Password: Admin123!@#
    'System',
    'Administrator',
    'ADMIN',
    TRUE,
    TRUE
) ON CONFLICT (email) DO NOTHING;

-- ═══════════════════════════════════════════════════════════════════════════
-- CLEANUP FUNCTION (Scheduled Task)
-- ═══════════════════════════════════════════════════════════════════════════

CREATE OR REPLACE FUNCTION cleanup_expired_tokens()
RETURNS INTEGER AS $$
DECLARE
    deleted_count INTEGER := 0;
    temp_count INTEGER;
BEGIN
    -- Delete expired refresh tokens
    DELETE FROM refresh_tokens WHERE expires_at < CURRENT_TIMESTAMP AND revoked = FALSE;
    GET DIAGNOSTICS temp_count = ROW_COUNT;
    deleted_count := deleted_count + temp_count;

    -- Delete used/expired password reset tokens
    DELETE FROM password_reset_tokens
    WHERE (used = TRUE OR expires_at < CURRENT_TIMESTAMP)
    AND created_at < CURRENT_TIMESTAMP - INTERVAL '24 hours';
    GET DIAGNOSTICS temp_count = ROW_COUNT;
    deleted_count := deleted_count + temp_count;

    -- Delete verified email tokens
    DELETE FROM email_verification_tokens
    WHERE is_verified = TRUE
    AND created_at < CURRENT_TIMESTAMP - INTERVAL '24 hours';
    GET DIAGNOSTICS temp_count = ROW_COUNT;
    deleted_count := deleted_count + temp_count;

    RETURN deleted_count;
END;
$$ LANGUAGE plpgsql;

-- ═══════════════════════════════════════════════════════════════════════════
-- COMMENTS FOR DOCUMENTATION
-- ═══════════════════════════════════════════════════════════════════════════

COMMENT ON TABLE users IS 'Central user authentication and authorization';
COMMENT ON TABLE refresh_tokens IS 'JWT refresh tokens for session management';
COMMENT ON TABLE password_reset_tokens IS 'Password reset workflow tokens';
COMMENT ON TABLE email_verification_tokens IS 'Email verification tokens';
COMMENT ON TABLE security_audit_log IS 'Security event audit trail';

COMMENT ON COLUMN users.oauth2_provider IS 'OAuth2 provider: google, facebook, apple, or NULL';
COMMENT ON COLUMN users.oauth2_user_id IS 'Provider-specific user ID';