-- Youth Connect Uganda - User Service Database Schema
-- Version: 1.1
-- Description: Add audit fields and performance indexes

-- =================================================================================
-- ADD AUDIT FIELDS TO EXISTING TABLES
-- =================================================================================

-- Add created_by and updated_by to users table
ALTER TABLE users
ADD COLUMN created_by BIGINT NULL AFTER updated_at,
ADD COLUMN updated_by BIGINT NULL AFTER created_by,
ADD FOREIGN KEY (created_by) REFERENCES users(user_id) ON DELETE SET NULL,
ADD FOREIGN KEY (updated_by) REFERENCES users(user_id) ON DELETE SET NULL;

-- Add created_by and updated_by to youth_profiles table
ALTER TABLE youth_profiles
ADD COLUMN created_by BIGINT NULL AFTER updated_at,
ADD COLUMN updated_by BIGINT NULL AFTER created_by,
ADD FOREIGN KEY (created_by) REFERENCES users(user_id) ON DELETE SET NULL,
ADD FOREIGN KEY (updated_by) REFERENCES users(user_id) ON DELETE SET NULL;

-- Add created_by and updated_by to mentor_profiles table
ALTER TABLE mentor_profiles
ADD COLUMN created_by BIGINT NULL AFTER updated_at,
ADD COLUMN updated_by BIGINT NULL AFTER created_by,
ADD FOREIGN KEY (created_by) REFERENCES users(user_id) ON DELETE SET NULL,
ADD FOREIGN KEY (updated_by) REFERENCES users(user_id) ON DELETE SET NULL;

-- Add created_by and updated_by to ngo_profiles table
ALTER TABLE ngo_profiles
ADD COLUMN created_by BIGINT NULL AFTER updated_at,
ADD COLUMN updated_by BIGINT NULL AFTER created_by,
ADD FOREIGN KEY (created_by) REFERENCES users(user_id) ON DELETE SET NULL,
ADD FOREIGN KEY (updated_by) REFERENCES users(user_id) ON DELETE SET NULL;

-- Add created_by and updated_by to funder_profiles table
ALTER TABLE funder_profiles
ADD COLUMN created_by BIGINT NULL AFTER updated_at,
ADD COLUMN updated_by BIGINT NULL AFTER created_by,
ADD FOREIGN KEY (created_by) REFERENCES users(user_id) ON DELETE SET NULL,
ADD FOREIGN KEY (updated_by) REFERENCES users(user_id) ON DELETE SET NULL;

-- Add created_by and updated_by to service_provider_profiles table
ALTER TABLE service_provider_profiles
ADD COLUMN created_by BIGINT NULL AFTER updated_at,
ADD COLUMN updated_by BIGINT NULL AFTER created_by,
ADD FOREIGN KEY (created_by) REFERENCES users(user_id) ON DELETE SET NULL,
ADD FOREIGN KEY (updated_by) REFERENCES users(user_id) ON DELETE SET NULL;

-- =================================================================================
-- ADD PERFORMANCE INDEXES
-- =================================================================================

-- Composite indexes for better query performance
CREATE INDEX idx_users_role_active_created ON users(role, is_active, created_at);
CREATE INDEX idx_youth_district_stage ON youth_profiles(district, business_stage, created_at);
CREATE INDEX idx_mentor_expertise_verified ON mentor_profiles(area_of_expertise(100), is_verified, availability_status);
CREATE INDEX idx_audit_user_created ON audit_trail(user_id, created_at);

-- Full-text indexes for search functionality
ALTER TABLE youth_profiles ADD FULLTEXT idx_youth_search (first_name, last_name, profession, skills, interests);
ALTER TABLE mentor_profiles ADD FULLTEXT idx_mentor_search (first_name, last_name, area_of_expertise, bio);
ALTER TABLE ngo_profiles ADD FULLTEXT idx_ngo_search (organisation_name, description, focus_areas);

-- =================================================================================
-- ADD DATA RETENTION POLICY FIELDS
-- =================================================================================

-- Add soft delete flag to all main tables
ALTER TABLE users ADD COLUMN is_deleted BOOLEAN DEFAULT FALSE AFTER is_active;
ALTER TABLE youth_profiles ADD COLUMN is_deleted BOOLEAN DEFAULT FALSE;
ALTER TABLE mentor_profiles ADD COLUMN is_deleted BOOLEAN DEFAULT FALSE;
ALTER TABLE ngo_profiles ADD COLUMN is_deleted BOOLEAN DEFAULT FALSE;
ALTER TABLE funder_profiles ADD COLUMN is_deleted BOOLEAN DEFAULT FALSE;
ALTER TABLE service_provider_profiles ADD COLUMN is_deleted BOOLEAN DEFAULT FALSE;

-- Add data retention timestamp
ALTER TABLE users ADD COLUMN deleted_at TIMESTAMP NULL AFTER is_deleted;

-- Update indexes to include soft delete flag
CREATE INDEX idx_users_active_not_deleted ON users(is_active, is_deleted);
CREATE INDEX idx_youth_active_not_deleted ON youth_profiles(is_deleted);

-- =================================================================================
-- VERIFICATION: Confirm all changes applied successfully
-- =================================================================================

-- Verify new columns
SELECT
    TABLE_NAME,
    COLUMN_NAME,
    DATA_TYPE,
    IS_NULLABLE,
    COLUMN_DEFAULT
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = DATABASE()
AND TABLE_NAME IN ('users', 'youth_profiles', 'mentor_profiles', 'ngo_profiles', 'funder_profiles', 'service_provider_profiles')
AND COLUMN_NAME IN ('created_by', 'updated_by', 'is_deleted', 'deleted_at')
ORDER BY TABLE_NAME, ORDINAL_POSITION;

-- Verify new indexes
SELECT
    TABLE_NAME,
    INDEX_NAME,
    COLUMN_NAME,
    INDEX_TYPE
FROM INFORMATION_SCHEMA.STATISTICS
WHERE TABLE_SCHEMA = DATABASE()
AND INDEX_NAME LIKE 'idx_%'
ORDER BY TABLE_NAME, INDEX_NAME, SEQ_IN_INDEX;