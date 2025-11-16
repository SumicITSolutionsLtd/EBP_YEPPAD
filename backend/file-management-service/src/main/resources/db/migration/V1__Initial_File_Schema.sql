-- =================================================================================
-- FILE MANAGEMENT SERVICE - PostgreSQL Database Schema (Flyway Migration)
-- =================================================================================
-- Migration: V1__Initial_File_Schema.sql
-- Database: epb_file (PostgreSQL 15+)
-- Description: Complete file management schema for youth entrepreneurship platform
-- Author: Douglas Kings Kato
-- Version: 1.0.0
-- Date: 2025-11-11
-- =================================================================================

-- =================================================================================
-- EXTENSIONS
-- =================================================================================

-- Enable UUID generation
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Enable better JSONB indexing
CREATE EXTENSION IF NOT EXISTS "btree_gin";

-- =================================================================================
-- CUSTOM TYPES (PostgreSQL ENUMs)
-- =================================================================================

-- File category enumeration
CREATE TYPE file_category AS ENUM (
    'PROFILE_PICTURE',           -- User profile pictures (optimized: thumb, medium, original)
    'DOCUMENT',                  -- CVs, certificates, application documents
    'AUDIO_MODULE',              -- Multi-language learning audio files
    'VIDEO_CONTENT',             -- Video lessons and tutorials
    'APPLICATION_ATTACHMENT',    -- Job/opportunity application files
    'SYSTEM'                     -- System-level files (logos, templates, etc.)
);

COMMENT ON TYPE file_category IS 'File categories supported by the platform';

-- =================================================================================
-- FILE RECORDS TABLE
-- =================================================================================

CREATE TABLE file_records (
    -- Primary key - Auto-increment for performance
    file_id BIGSERIAL PRIMARY KEY,

    -- ✅ User reference (UUID to match auth-service)
    -- Nullable for system files (e.g., learning modules, public content)
    user_id UUID,

    -- File naming (security-safe)
    file_name VARCHAR(255) NOT NULL,
    original_name VARCHAR(255),

    -- Physical storage
    file_path VARCHAR(500) NOT NULL,
    file_size BIGINT,

    -- File type information
    content_type VARCHAR(100),
    file_category file_category NOT NULL,

    -- Access control
    is_public BOOLEAN DEFAULT FALSE NOT NULL,
    is_active BOOLEAN DEFAULT TRUE NOT NULL,

    -- Timestamps
    upload_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_accessed TIMESTAMP,

    -- Audit fields
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- Soft delete
    is_deleted BOOLEAN DEFAULT FALSE NOT NULL,
    deleted_at TIMESTAMP,

    -- Constraints
    CONSTRAINT chk_file_size_positive CHECK (file_size IS NULL OR file_size > 0)
);

-- =================================================================================
-- INDEXES FOR FILE_RECORDS
-- =================================================================================

-- User and category lookups (most common query)
CREATE INDEX idx_file_user_category ON file_records(user_id, file_category)
    WHERE is_active = TRUE AND is_deleted = FALSE;

-- File name lookups (for duplicate checks)
CREATE INDEX idx_file_name ON file_records(file_name)
    WHERE is_active = TRUE AND is_deleted = FALSE;

-- Upload time sorting (for pagination)
CREATE INDEX idx_file_upload_time ON file_records(upload_time DESC);

-- Active status filtering
CREATE INDEX idx_file_active ON file_records(is_active, is_deleted);

-- Public file access
CREATE INDEX idx_file_public ON file_records(is_public, file_category)
    WHERE is_public = TRUE AND is_active = TRUE AND is_deleted = FALSE;

-- Last accessed for analytics and cold storage decisions
CREATE INDEX idx_file_last_accessed ON file_records(last_accessed NULLS FIRST)
    WHERE is_active = TRUE AND is_deleted = FALSE;

-- Category filtering
CREATE INDEX idx_file_category ON file_records(file_category, is_active);

-- =================================================================================
-- TABLE COMMENTS
-- =================================================================================

COMMENT ON TABLE file_records IS 'File metadata and access control for platform files';
COMMENT ON COLUMN file_records.file_id IS 'Auto-increment primary key for database efficiency';
COMMENT ON COLUMN file_records.user_id IS 'UUID reference to user in auth-service (NULL for system files)';
COMMENT ON COLUMN file_records.file_name IS 'Stored filename (security-safe, may differ from original)';
COMMENT ON COLUMN file_records.original_name IS 'Original user-uploaded filename (for display)';
COMMENT ON COLUMN file_records.file_path IS 'Physical file path on server/storage';
COMMENT ON COLUMN file_records.file_category IS 'File type: PROFILE_PICTURE, DOCUMENT, AUDIO_MODULE, etc.';
COMMENT ON COLUMN file_records.is_public IS 'Public access flag (TRUE for learning modules, public profiles)';
COMMENT ON COLUMN file_records.is_active IS 'Active status (FALSE for soft-deleted files)';
COMMENT ON COLUMN file_records.upload_time IS 'File upload timestamp (UTC)';
COMMENT ON COLUMN file_records.last_accessed IS 'Last access timestamp for analytics and cold storage';

-- =================================================================================
-- FILE METADATA TABLE (Extended attributes)
-- =================================================================================

CREATE TABLE file_metadata (
    -- Primary key
    metadata_id BIGSERIAL PRIMARY KEY,

    -- File reference
    file_id BIGINT NOT NULL REFERENCES file_records(file_id) ON DELETE CASCADE,

    -- Extended metadata (JSONB for flexibility)
    metadata JSONB,

    -- Image-specific metadata
    image_width INTEGER,
    image_height INTEGER,
    image_format VARCHAR(10),

    -- Audio-specific metadata
    audio_duration_seconds INTEGER,
    audio_bitrate INTEGER,
    audio_language VARCHAR(10),

    -- Video-specific metadata
    video_duration_seconds INTEGER,
    video_resolution VARCHAR(20),
    video_codec VARCHAR(50),

    -- Document-specific metadata
    document_pages INTEGER,
    document_format VARCHAR(10),

    -- Optimization flags
    is_optimized BOOLEAN DEFAULT FALSE,
    optimization_status VARCHAR(50),
    optimized_versions JSONB,

    -- Timestamps
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- Unique constraint
    CONSTRAINT unique_file_metadata UNIQUE (file_id)
);

-- Index for JSONB metadata
CREATE INDEX idx_file_metadata_jsonb ON file_metadata USING gin(metadata);
CREATE INDEX idx_file_metadata_optimized ON file_metadata(is_optimized);

COMMENT ON TABLE file_metadata IS 'Extended file metadata for different file types';
COMMENT ON COLUMN file_metadata.metadata IS 'Flexible JSONB field for additional metadata';
COMMENT ON COLUMN file_metadata.optimized_versions IS 'JSONB map of optimized versions (thumbnail, medium, etc.)';

-- =================================================================================
-- FILE VERSIONS TABLE (For optimized variants)
-- =================================================================================

CREATE TABLE file_versions (
    -- Primary key
    version_id BIGSERIAL PRIMARY KEY,

    -- Original file reference
    original_file_id BIGINT NOT NULL REFERENCES file_records(file_id) ON DELETE CASCADE,

    -- Version information
    version_type VARCHAR(50) NOT NULL, -- 'thumbnail', 'medium', 'compressed', 'normalized'
    file_name VARCHAR(255) NOT NULL,
    file_path VARCHAR(500) NOT NULL,
    file_size BIGINT,

    -- Dimensions (for images/videos)
    width INTEGER,
    height INTEGER,

    -- Timestamps
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- Unique constraint
    CONSTRAINT unique_file_version UNIQUE (original_file_id, version_type)
);

-- Indexes
CREATE INDEX idx_file_version_original ON file_versions(original_file_id);
CREATE INDEX idx_file_version_type ON file_versions(version_type);

COMMENT ON TABLE file_versions IS 'Optimized file versions (thumbnails, compressed audio, etc.)';

-- =================================================================================
-- FILE ACCESS LOGS (For analytics and audit)
-- =================================================================================

CREATE TABLE file_access_logs (
    -- Primary key
    log_id BIGSERIAL PRIMARY KEY,

    -- File reference
    file_id BIGINT NOT NULL REFERENCES file_records(file_id) ON DELETE CASCADE,

    -- User who accessed (NULL for anonymous public access)
    user_id UUID,

    -- Access details
    access_type VARCHAR(50) NOT NULL, -- 'DOWNLOAD', 'VIEW', 'STREAM'
    ip_address INET,
    user_agent TEXT,
    referrer TEXT,

    -- Success/failure
    success BOOLEAN DEFAULT TRUE,
    error_message TEXT,

    -- Timestamp
    accessed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Indexes
CREATE INDEX idx_file_access_file ON file_access_logs(file_id, accessed_at DESC);
CREATE INDEX idx_file_access_user ON file_access_logs(user_id, accessed_at DESC);
CREATE INDEX idx_file_access_time ON file_access_logs(accessed_at DESC);
CREATE INDEX idx_file_access_ip ON file_access_logs(ip_address);

COMMENT ON TABLE file_access_logs IS 'File access tracking for analytics and security';

-- =================================================================================
-- FILE TAGS TABLE (For categorization and search)
-- =================================================================================

CREATE TABLE file_tags (
    -- Primary key
    tag_id BIGSERIAL PRIMARY KEY,

    -- File reference
    file_id BIGINT NOT NULL REFERENCES file_records(file_id) ON DELETE CASCADE,

    -- Tag information
    tag_name VARCHAR(50) NOT NULL,
    tag_category VARCHAR(50),

    -- Audit
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by UUID,

    -- Unique constraint
    CONSTRAINT unique_file_tag UNIQUE (file_id, tag_name)
);

-- Indexes
CREATE INDEX idx_file_tag_file ON file_tags(file_id);
CREATE INDEX idx_file_tag_name ON file_tags(tag_name);
CREATE INDEX idx_file_tag_category ON file_tags(tag_category);

COMMENT ON TABLE file_tags IS 'Tagging system for file categorization and search';

-- =================================================================================
-- TRIGGERS FOR AUTOMATIC UPDATED_AT
-- =================================================================================

-- Function to update updated_at timestamp
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Apply triggers
CREATE TRIGGER update_file_records_updated_at
    BEFORE UPDATE ON file_records
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_file_metadata_updated_at
    BEFORE UPDATE ON file_metadata
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- =================================================================================
-- UTILITY FUNCTIONS
-- =================================================================================

-- Function to mark file as accessed
CREATE OR REPLACE FUNCTION mark_file_accessed(p_file_id BIGINT)
RETURNS VOID AS $$
BEGIN
    UPDATE file_records
    SET last_accessed = CURRENT_TIMESTAMP
    WHERE file_id = p_file_id;
END;
$$ LANGUAGE plpgsql;

COMMENT ON FUNCTION mark_file_accessed(BIGINT) IS 'Updates last_accessed timestamp for a file';

-- Function to soft delete file
CREATE OR REPLACE FUNCTION soft_delete_file(p_file_id BIGINT)
RETURNS BOOLEAN AS $$
BEGIN
    UPDATE file_records
    SET is_active = FALSE,
        is_deleted = TRUE,
        deleted_at = CURRENT_TIMESTAMP
    WHERE file_id = p_file_id;

    RETURN FOUND;
END;
$$ LANGUAGE plpgsql;

COMMENT ON FUNCTION soft_delete_file(BIGINT) IS 'Soft deletes a file record';

-- Function to restore soft deleted file
CREATE OR REPLACE FUNCTION restore_file(p_file_id BIGINT)
RETURNS BOOLEAN AS $$
BEGIN
    UPDATE file_records
    SET is_active = TRUE,
        is_deleted = FALSE,
        deleted_at = NULL
    WHERE file_id = p_file_id;

    RETURN FOUND;
END;
$$ LANGUAGE plpgsql;

COMMENT ON FUNCTION restore_file(BIGINT) IS 'Restores a soft deleted file';

-- =================================================================================
-- VIEWS FOR ANALYTICS
-- =================================================================================

-- Active files summary
CREATE OR REPLACE VIEW v_file_summary AS
SELECT
    file_category,
    is_public,
    COUNT(*) AS file_count,
    SUM(file_size) AS total_size_bytes,
    ROUND(SUM(file_size)::NUMERIC / 1024 / 1024, 2) AS total_size_mb,
    MIN(upload_time) AS oldest_upload,
    MAX(upload_time) AS newest_upload,
    COUNT(*) FILTER (WHERE last_accessed IS NOT NULL) AS accessed_count,
    COUNT(*) FILTER (WHERE last_accessed IS NULL) AS never_accessed_count
FROM file_records
WHERE is_active = TRUE AND is_deleted = FALSE
GROUP BY file_category, is_public
ORDER BY file_category, is_public;

COMMENT ON VIEW v_file_summary IS 'Summary statistics of active files by category';

-- User file statistics
CREATE OR REPLACE VIEW v_user_file_stats AS
SELECT
    user_id,
    COUNT(*) AS total_files,
    SUM(file_size) AS total_storage_bytes,
    ROUND(SUM(file_size)::NUMERIC / 1024 / 1024, 2) AS total_storage_mb,
    MIN(upload_time) AS first_upload,
    MAX(upload_time) AS last_upload,
    COUNT(*) FILTER (WHERE file_category = 'PROFILE_PICTURE') AS profile_pictures,
    COUNT(*) FILTER (WHERE file_category = 'DOCUMENT') AS documents,
    COUNT(*) FILTER (WHERE file_category = 'APPLICATION_ATTACHMENT') AS application_files
FROM file_records
WHERE is_active = TRUE
    AND is_deleted = FALSE
    AND user_id IS NOT NULL
GROUP BY user_id
ORDER BY total_storage_bytes DESC;

COMMENT ON VIEW v_user_file_stats IS 'File storage statistics per user';

-- =================================================================================
-- INITIAL SAMPLE DATA (Development Only - COMMENT OUT IN PRODUCTION)
-- =================================================================================

-- Sample system files (learning modules)
-- INSERT INTO file_records (user_id, file_name, original_name, file_path, file_size, content_type, file_category, is_public)
-- VALUES
--     (NULL, 'intro_entrepreneurship_en.mp3', 'Intro to Entrepreneurship (English)', '/uploads/modules/intro/intro_en.mp3', 5242880, 'audio/mpeg', 'AUDIO_MODULE', TRUE),
--     (NULL, 'intro_entrepreneurship_lg.mp3', 'Intro to Entrepreneurship (Luganda)', '/uploads/modules/intro/intro_lg.mp3', 5242880, 'audio/mpeg', 'AUDIO_MODULE', TRUE);

-- =================================================================================
-- GRANTS (Security - Adjust for your database user)
-- =================================================================================

-- Grant appropriate permissions to application user
-- Uncomment and adjust for your database user
-- GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA public TO file_service_app;
-- GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA public TO file_service_app;
-- GRANT EXECUTE ON ALL FUNCTIONS IN SCHEMA public TO file_service_app;

-- =================================================================================
-- END OF MIGRATION
-- =================================================================================