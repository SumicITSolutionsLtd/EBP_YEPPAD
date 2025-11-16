-- =================================================================================
-- FILE MANAGEMENT SERVICE - Maintenance Functions & Procedures
-- =================================================================================
-- Migration: V2__File_Maintenance_Functions.sql
-- Description: Maintenance procedures for file management service
-- Author: Douglas Kings Kato
-- Version: 1.0.0
-- Date: 2025-11-11
-- =================================================================================

-- =================================================================================
-- CLEANUP PROCEDURES
-- =================================================================================

-- Function to clean old soft-deleted files
CREATE OR REPLACE FUNCTION cleanup_old_deleted_files(days_to_keep INTEGER DEFAULT 30)
RETURNS TABLE(
    message TEXT,
    deleted_count BIGINT,
    freed_space_mb NUMERIC,
    executed_at TIMESTAMP
) AS $$
DECLARE
    v_deleted_count BIGINT;
    v_freed_space BIGINT;
BEGIN
    -- Calculate freed space before deletion
    SELECT COALESCE(SUM(file_size), 0) INTO v_freed_space
    FROM file_records
    WHERE is_deleted = TRUE
    AND deleted_at < CURRENT_TIMESTAMP - (days_to_keep || ' days')::INTERVAL;

    -- Delete files older than specified days
    DELETE FROM file_records
    WHERE is_deleted = TRUE
    AND deleted_at < CURRENT_TIMESTAMP - (days_to_keep || ' days')::INTERVAL;

    GET DIAGNOSTICS v_deleted_count = ROW_COUNT;

    RETURN QUERY SELECT
        FORMAT('Deleted %s files older than %s days', v_deleted_count, days_to_keep)::TEXT,
        v_deleted_count,
        ROUND(v_freed_space::NUMERIC / 1024 / 1024, 2),
        CURRENT_TIMESTAMP;
END;
$$ LANGUAGE plpgsql;

COMMENT ON FUNCTION cleanup_old_deleted_files(INTEGER)
    IS 'Permanently deletes soft-deleted files older than specified days';

-- Function to clean old access logs
CREATE OR REPLACE FUNCTION cleanup_old_access_logs(days_to_keep INTEGER DEFAULT 90)
RETURNS TABLE(
    message TEXT,
    deleted_count BIGINT,
    executed_at TIMESTAMP
) AS $$
DECLARE
    v_deleted_count BIGINT;
BEGIN
    DELETE FROM file_access_logs
    WHERE accessed_at < CURRENT_TIMESTAMP - (days_to_keep || ' days')::INTERVAL;

    GET DIAGNOSTICS v_deleted_count = ROW_COUNT;

    RETURN QUERY SELECT
        FORMAT('Deleted %s access log records older than %s days', v_deleted_count, days_to_keep)::TEXT,
        v_deleted_count,
        CURRENT_TIMESTAMP;
END;
$$ LANGUAGE plpgsql;

COMMENT ON FUNCTION cleanup_old_access_logs(INTEGER)
    IS 'Archives access logs older than specified days';

-- =================================================================================
-- STATISTICS FUNCTIONS
-- =================================================================================

-- Function to get database statistics
CREATE OR REPLACE FUNCTION get_file_database_statistics()
RETURNS TABLE(
    table_name TEXT,
    row_count BIGINT,
    size_mb NUMERIC,
    index_size_mb NUMERIC,
    total_size_mb NUMERIC
) AS $$
BEGIN
    RETURN QUERY
    SELECT
        schemaname || '.' || tablename AS table_name,
        n_live_tup AS row_count,
        ROUND((pg_table_size(schemaname||'.'||tablename)::NUMERIC / 1024 / 1024), 2) AS size_mb,
        ROUND((pg_indexes_size(schemaname||'.'||tablename)::NUMERIC / 1024 / 1024), 2) AS index_size_mb,
        ROUND((pg_total_relation_size(schemaname||'.'||tablename)::NUMERIC / 1024 / 1024), 2) AS total_size_mb
    FROM pg_stat_user_tables
    WHERE schemaname = 'public'
    ORDER BY pg_total_relation_size(schemaname||'.'||tablename) DESC;
END;
$$ LANGUAGE plpgsql;

COMMENT ON FUNCTION get_file_database_statistics()
    IS 'Returns detailed table statistics including sizes';

-- Function to get file statistics by category
CREATE OR REPLACE FUNCTION get_file_stats_by_category()
RETURNS TABLE(
    category file_category,
    total_files BIGINT,
    active_files BIGINT,
    deleted_files BIGINT,
    public_files BIGINT,
    private_files BIGINT,
    total_size_mb NUMERIC,
    avg_file_size_kb NUMERIC,
    oldest_file TIMESTAMP,
    newest_file TIMESTAMP
) AS $$
BEGIN
    RETURN QUERY
    SELECT
        file_category AS category,
        COUNT(*)::BIGINT AS total_files,
        COUNT(*) FILTER (WHERE is_active = TRUE AND is_deleted = FALSE)::BIGINT AS active_files,
        COUNT(*) FILTER (WHERE is_deleted = TRUE)::BIGINT AS deleted_files,
        COUNT(*) FILTER (WHERE is_public = TRUE)::BIGINT AS public_files,
        COUNT(*) FILTER (WHERE is_public = FALSE)::BIGINT AS private_files,
        ROUND(SUM(file_size)::NUMERIC / 1024 / 1024, 2) AS total_size_mb,
        ROUND(AVG(file_size)::NUMERIC / 1024, 2) AS avg_file_size_kb,
        MIN(upload_time) AS oldest_file,
        MAX(upload_time) AS newest_file
    FROM file_records
    GROUP BY file_category
    ORDER BY file_category;
END;
$$ LANGUAGE plpgsql;

COMMENT ON FUNCTION get_file_stats_by_category()
    IS 'Returns comprehensive file statistics grouped by category';

-- Function to get user storage usage
CREATE OR REPLACE FUNCTION get_user_storage_usage(p_user_id UUID)
RETURNS TABLE(
    category file_category,
    file_count BIGINT,
    total_size_bytes BIGINT,
    total_size_mb NUMERIC,
    oldest_upload TIMESTAMP,
    newest_upload TIMESTAMP
) AS $$
BEGIN
    RETURN QUERY
    SELECT
        file_category AS category,
        COUNT(*)::BIGINT AS file_count,
        SUM(file_size)::BIGINT AS total_size_bytes,
        ROUND(SUM(file_size)::NUMERIC / 1024 / 1024, 2) AS total_size_mb,
        MIN(upload_time) AS oldest_upload,
        MAX(upload_time) AS newest_upload
    FROM file_records
    WHERE user_id = p_user_id
        AND is_active = TRUE
        AND is_deleted = FALSE
    GROUP BY file_category
    ORDER BY file_category;
END;
$$ LANGUAGE plpgsql;

COMMENT ON FUNCTION get_user_storage_usage(UUID)
    IS 'Returns storage usage breakdown for a specific user';

-- =================================================================================
-- FILE MANAGEMENT FUNCTIONS
-- =================================================================================

-- Function to find files not accessed in specified days
CREATE OR REPLACE FUNCTION find_inactive_files(days_inactive INTEGER DEFAULT 180)
RETURNS TABLE(
    file_id BIGINT,
    file_name VARCHAR,
    category file_category,
    file_size BIGINT,
    upload_time TIMESTAMP,
    last_accessed TIMESTAMP,
    days_since_access INTEGER
) AS $$
BEGIN
    RETURN QUERY
    SELECT
        fr.file_id,
        fr.file_name,
        fr.file_category AS category,
        fr.file_size,
        fr.upload_time,
        fr.last_accessed,
        EXTRACT(DAY FROM (CURRENT_TIMESTAMP - COALESCE(fr.last_accessed, fr.upload_time)))::INTEGER AS days_since_access
    FROM file_records fr
    WHERE fr.is_active = TRUE
        AND fr.is_deleted = FALSE
        AND (fr.last_accessed < CURRENT_TIMESTAMP - (days_inactive || ' days')::INTERVAL
            OR (fr.last_accessed IS NULL AND fr.upload_time < CURRENT_TIMESTAMP - (days_inactive || ' days')::INTERVAL))
    ORDER BY days_since_access DESC;
END;
$$ LANGUAGE plpgsql;

COMMENT ON FUNCTION find_inactive_files(INTEGER)
    IS 'Finds files that have not been accessed in specified days (candidates for cold storage)';

-- Function to find duplicate files by name
CREATE OR REPLACE FUNCTION find_duplicate_files()
RETURNS TABLE(
    file_name VARCHAR,
    duplicate_count BIGINT,
    total_size_mb NUMERIC,
    user_ids UUID[]
) AS $$
BEGIN
    RETURN QUERY
    SELECT
        fr.file_name,
        COUNT(*)::BIGINT AS duplicate_count,
        ROUND(SUM(fr.file_size)::NUMERIC / 1024 / 1024, 2) AS total_size_mb,
        ARRAY_AGG(DISTINCT fr.user_id) FILTER (WHERE fr.user_id IS NOT NULL) AS user_ids
    FROM file_records fr
    WHERE fr.is_active = TRUE AND fr.is_deleted = FALSE
    GROUP BY fr.file_name
    HAVING COUNT(*) > 1
    ORDER BY duplicate_count DESC, total_size_mb DESC;
END;
$$ LANGUAGE plpgsql;

COMMENT ON FUNCTION find_duplicate_files()
    IS 'Finds duplicate files by filename (potential for deduplication)';

-- Function to get file access frequency
CREATE OR REPLACE FUNCTION get_file_access_frequency(days_back INTEGER DEFAULT 30)
RETURNS TABLE(
    file_id BIGINT,
    file_name VARCHAR,
    category file_category,
    access_count BIGINT,
    unique_users BIGINT,
    last_accessed TIMESTAMP
) AS $$
BEGIN
    RETURN QUERY
    SELECT
        fr.file_id,
        fr.file_name,
        fr.file_category AS category,
        COUNT(fal.log_id)::BIGINT AS access_count,
        COUNT(DISTINCT fal.user_id)::BIGINT AS unique_users,
        MAX(fal.accessed_at) AS last_accessed
    FROM file_records fr
    LEFT JOIN file_access_logs fal ON fr.file_id = fal.file_id
        AND fal.accessed_at >= CURRENT_TIMESTAMP - (days_back || ' days')::INTERVAL
    WHERE fr.is_active = TRUE AND fr.is_deleted = FALSE
    GROUP BY fr.file_id, fr.file_name, fr.file_category
    ORDER BY access_count DESC
    LIMIT 100;
END;
$$ LANGUAGE plpgsql;

COMMENT ON FUNCTION get_file_access_frequency(INTEGER)
    IS 'Returns most frequently accessed files in specified period';

-- =================================================================================
-- BULK OPERATIONS
-- =================================================================================

-- Function to bulk soft delete files by user
CREATE OR REPLACE FUNCTION bulk_delete_user_files(p_user_id UUID, p_category file_category DEFAULT NULL)
RETURNS TABLE(
    success BOOLEAN,
    message TEXT,
    deleted_count BIGINT
) AS $$
DECLARE
    v_deleted_count BIGINT;
BEGIN
    -- Soft delete files
    IF p_category IS NOT NULL THEN
        UPDATE file_records
        SET is_active = FALSE,
            is_deleted = TRUE,
            deleted_at = CURRENT_TIMESTAMP
        WHERE user_id = p_user_id
            AND file_category = p_category
            AND is_deleted = FALSE;
    ELSE
        UPDATE file_records
        SET is_active = FALSE,
            is_deleted = TRUE,
            deleted_at = CURRENT_TIMESTAMP
        WHERE user_id = p_user_id
            AND is_deleted = FALSE;
    END IF;

    GET DIAGNOSTICS v_deleted_count = ROW_COUNT;

    RETURN QUERY SELECT
        TRUE,
        FORMAT('Deleted %s files for user %s', v_deleted_count, p_user_id)::TEXT,
        v_deleted_count;
END;
$$ LANGUAGE plpgsql;

COMMENT ON FUNCTION bulk_delete_user_files(UUID, file_category)
    IS 'Bulk soft delete all files for a user (optionally filtered by category)';

-- Function to bulk restore files
CREATE OR REPLACE FUNCTION bulk_restore_user_files(p_user_id UUID, p_category file_category DEFAULT NULL)
RETURNS TABLE(
    success BOOLEAN,
    message TEXT,
    restored_count BIGINT
) AS $$
DECLARE
    v_restored_count BIGINT;
BEGIN
    -- Restore files
    IF p_category IS NOT NULL THEN
        UPDATE file_records
        SET is_active = TRUE,
            is_deleted = FALSE,
            deleted_at = NULL
        WHERE user_id = p_user_id
            AND file_category = p_category
            AND is_deleted = TRUE;
    ELSE
        UPDATE file_records
        SET is_active = TRUE,
            is_deleted = FALSE,
            deleted_at = NULL
        WHERE user_id = p_user_id
            AND is_deleted = TRUE;
    END IF;

    GET DIAGNOSTICS v_restored_count = ROW_COUNT;

    RETURN QUERY SELECT
        TRUE,
        FORMAT('Restored %s files for user %s', v_restored_count, p_user_id)::TEXT,
        v_restored_count;
END;
$$ LANGUAGE plpgsql;

COMMENT ON FUNCTION bulk_restore_user_files(UUID, file_category)
    IS 'Bulk restore soft-deleted files for a user';

-- =================================================================================
-- MAINTENANCE SCHEDULER FUNCTION
-- =================================================================================

-- Function to perform routine maintenance
CREATE OR REPLACE FUNCTION perform_file_maintenance()
RETURNS TABLE(
    operation TEXT,
    status TEXT,
    details TEXT,
    executed_at TIMESTAMP
) AS $$
DECLARE
    v_result RECORD;
BEGIN
    -- Clean old deleted files (30+ days)
    FOR v_result IN SELECT * FROM cleanup_old_deleted_files(30) LOOP
        RETURN QUERY SELECT
            'Clean Deleted Files'::TEXT,
            'SUCCESS'::TEXT,
            v_result.message,
            v_result.executed_at;
    END LOOP;

    -- Clean old access logs (90+ days)
    FOR v_result IN SELECT * FROM cleanup_old_access_logs(90) LOOP
        RETURN QUERY SELECT
            'Clean Access Logs'::TEXT,
            'SUCCESS'::TEXT,
            v_result.message,
            v_result.executed_at;
    END LOOP;

    -- Vacuum analyze
    EXECUTE 'VACUUM ANALYZE file_records';
    RETURN QUERY SELECT
        'Vacuum Analyze'::TEXT,
        'SUCCESS'::TEXT,
        'Completed vacuum analyze on file_records'::TEXT,
        CURRENT_TIMESTAMP;

    -- Update statistics
    EXECUTE 'ANALYZE';
    RETURN QUERY SELECT
        'Update Statistics'::TEXT,
        'SUCCESS'::TEXT,
        'Completed database statistics update'::TEXT,
        CURRENT_TIMESTAMP;

    RETURN;
END;
$$ LANGUAGE plpgsql;

COMMENT ON FUNCTION perform_file_maintenance()
    IS 'Performs routine file database maintenance tasks';

-- =================================================================================
-- REPORTING FUNCTIONS
-- =================================================================================

-- Function to generate daily file report
CREATE OR REPLACE FUNCTION get_daily_file_report(report_date DATE DEFAULT CURRENT_DATE)
RETURNS TABLE(
    metric_name TEXT,
    metric_value TEXT
) AS $$
BEGIN
    RETURN QUERY
    -- Total files uploaded today
    SELECT
        'Files Uploaded Today'::TEXT AS metric_name,
        COUNT(*)::TEXT AS metric_value
    FROM file_records
    WHERE DATE(upload_time) = report_date

    UNION ALL

    -- Total storage used today (MB)
    SELECT
        'Storage Used Today (MB)'::TEXT,
        ROUND(SUM(file_size)::NUMERIC / 1024 / 1024, 2)::TEXT
    FROM file_records
    WHERE DATE(upload_time) = report_date

    UNION ALL

    -- Total active files
    SELECT
        'Total Active Files'::TEXT,
        COUNT(*)::TEXT
    FROM file_records
    WHERE is_active = TRUE AND is_deleted = FALSE

    UNION ALL

    -- Total storage used (GB)
    SELECT
        'Total Storage Used (GB)'::TEXT,
        ROUND(SUM(file_size)::NUMERIC / 1024 / 1024 / 1024, 2)::TEXT
    FROM file_records
    WHERE is_active = TRUE AND is_deleted = FALSE

    UNION ALL

    -- Files accessed today
    SELECT
        'Files Accessed Today'::TEXT,
        COUNT(DISTINCT file_id)::TEXT
    FROM file_access_logs
    WHERE DATE(accessed_at) = report_date;
END;
$$ LANGUAGE plpgsql;

COMMENT ON FUNCTION get_daily_file_report(DATE)
    IS 'Generates daily file management report';

-- =================================================================================
-- END OF MAINTENANCE FUNCTIONS
-- =================================================================================