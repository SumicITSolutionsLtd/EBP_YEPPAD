-- =================================================================================
-- Entrepreneurship Booster Platform - Maintenance Functions & Procedures
-- Version: 2.0.0
-- Description: Enhanced database maintenance procedures for PostgreSQL
-- =================================================================================

-- =================================================================================
-- CLEANUP PROCEDURES
-- =================================================================================

-- Function to clean expired tokens and sessions
CREATE OR REPLACE FUNCTION sp_clean_expired_tokens()
RETURNS TABLE(
    message TEXT,
    refresh_deleted BIGINT,
    password_deleted BIGINT,
    ussd_deleted BIGINT,
    total_deleted BIGINT,
    executed_at TIMESTAMP
) AS $$
DECLARE
    v_refresh_deleted BIGINT := 0;
    v_password_deleted BIGINT := 0;
    v_ussd_deleted BIGINT := 0;
    v_total_deleted BIGINT := 0;
BEGIN
    -- Delete expired and revoked refresh tokens
    DELETE FROM refresh_tokens
    WHERE expires_at < CURRENT_TIMESTAMP AND revoked = TRUE;
    GET DIAGNOSTICS v_refresh_deleted = ROW_COUNT;

    -- Delete old password reset tokens (used or expired older than 7 days)
    DELETE FROM password_reset_tokens
    WHERE (used = TRUE OR expires_at < CURRENT_TIMESTAMP)
    AND created_at < CURRENT_TIMESTAMP - INTERVAL '7 days';
    GET DIAGNOSTICS v_password_deleted = ROW_COUNT;

    -- Delete expired USSD sessions (older than 24 hours)
    DELETE FROM ussd_sessions
    WHERE expires_at < CURRENT_TIMESTAMP
    OR last_updated < CURRENT_TIMESTAMP - INTERVAL '24 hours';
    GET DIAGNOSTICS v_ussd_deleted = ROW_COUNT;

    v_total_deleted := v_refresh_deleted + v_password_deleted + v_ussd_deleted;

    RETURN QUERY SELECT
        'Cleanup completed successfully'::TEXT,
        v_refresh_deleted,
        v_password_deleted,
        v_ussd_deleted,
        v_total_deleted,
        CURRENT_TIMESTAMP;
END;
$$ LANGUAGE plpgsql;

COMMENT ON FUNCTION sp_clean_expired_tokens() IS 'Cleans up expired tokens and USSD sessions';

-- =================================================================================
-- STATISTICS FUNCTIONS
-- =================================================================================

-- Function to get database statistics
CREATE OR REPLACE FUNCTION get_database_statistics()
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

COMMENT ON FUNCTION get_database_statistics() IS 'Returns detailed table statistics including sizes';

-- =================================================================================
-- USER STATISTICS FUNCTIONS
-- =================================================================================

-- Function to get user registration statistics
CREATE OR REPLACE FUNCTION get_user_registration_stats(days_back INTEGER DEFAULT 30)
RETURNS TABLE(
    registration_date DATE,
    role user_role,
    user_count BIGINT,
    cumulative_count BIGINT
) AS $$
BEGIN
    RETURN QUERY
    WITH daily_counts AS (
        SELECT
            DATE(created_at) AS reg_date,
            role,
            COUNT(*)::BIGINT AS count
        FROM users
        WHERE created_at >= CURRENT_TIMESTAMP - (days_back || ' days')::INTERVAL
        AND is_deleted = FALSE
        GROUP BY DATE(created_at), role
    )
    SELECT
        reg_date AS registration_date,
        role,
        count AS user_count,
        SUM(count) OVER (PARTITION BY role ORDER BY reg_date)::BIGINT AS cumulative_count
    FROM daily_counts
    ORDER BY reg_date DESC, role;
END;
$$ LANGUAGE plpgsql;

COMMENT ON FUNCTION get_user_registration_stats(INTEGER) IS 'Returns user registration statistics with cumulative counts';

-- Function to get active users count
CREATE OR REPLACE FUNCTION get_active_users_count()
RETURNS TABLE(
    role user_role,
    total_count BIGINT,
    active_count BIGINT,
    verified_count BIGINT,
    email_verified_count BIGINT,
    phone_verified_count BIGINT,
    active_percentage NUMERIC
) AS $$
BEGIN
    RETURN QUERY
    SELECT
        u.role,
        COUNT(*)::BIGINT AS total_count,
        COUNT(*) FILTER (WHERE is_active = TRUE)::BIGINT AS active_count,
        COUNT(*) FILTER (WHERE is_verified = TRUE)::BIGINT AS verified_count,
        COUNT(*) FILTER (WHERE email_verified = TRUE)::BIGINT AS email_verified_count,
        COUNT(*) FILTER (WHERE phone_verified = TRUE)::BIGINT AS phone_verified_count,
        ROUND((COUNT(*) FILTER (WHERE is_active = TRUE)::NUMERIC / NULLIF(COUNT(*), 0) * 100), 2) AS active_percentage
    FROM users u
    WHERE is_deleted = FALSE
    GROUP BY u.role
    ORDER BY u.role;
END;
$$ LANGUAGE plpgsql;

COMMENT ON FUNCTION get_active_users_count() IS 'Returns comprehensive user counts by role with verification status';

-- =================================================================================
-- OPPORTUNITY & APPLICATION STATISTICS
-- =================================================================================

-- Function to get opportunity statistics
CREATE OR REPLACE FUNCTION get_opportunity_stats()
RETURNS TABLE(
    opportunity_type opportunity_type,
    status opportunity_status,
    count BIGINT,
    avg_funding_amount NUMERIC,
    total_applications BIGINT,
    avg_applications_per_opportunity NUMERIC
) AS $$
BEGIN
    RETURN QUERY
    SELECT
        o.opportunity_type,
        o.status,
        COUNT(DISTINCT o.opportunity_id)::BIGINT AS count,
        ROUND(AVG(o.funding_amount), 2) AS avg_funding_amount,
        COUNT(a.application_id)::BIGINT AS total_applications,
        ROUND(COUNT(a.application_id)::NUMERIC / NULLIF(COUNT(DISTINCT o.opportunity_id), 0), 2) AS avg_applications_per_opportunity
    FROM opportunities o
    LEFT JOIN applications a ON o.opportunity_id = a.opportunity_id
    GROUP BY o.opportunity_type, o.status
    ORDER BY o.opportunity_type, o.status;
END;
$$ LANGUAGE plpgsql;

COMMENT ON FUNCTION get_opportunity_stats() IS 'Returns opportunity statistics with application metrics';

-- Function to get application conversion rates
CREATE OR REPLACE FUNCTION get_application_conversion_rates()
RETURNS TABLE(
    opportunity_type opportunity_type,
    total_applications BIGINT,
    pending_count BIGINT,
    under_review_count BIGINT,
    approved_count BIGINT,
    rejected_count BIGINT,
    approval_rate NUMERIC
) AS $$
BEGIN
    RETURN QUERY
    SELECT
        o.opportunity_type,
        COUNT(a.application_id)::BIGINT AS total_applications,
        COUNT(*) FILTER (WHERE a.status = 'PENDING')::BIGINT AS pending_count,
        COUNT(*) FILTER (WHERE a.status = 'UNDER_REVIEW')::BIGINT AS under_review_count,
        COUNT(*) FILTER (WHERE a.status = 'APPROVED')::BIGINT AS approved_count,
        COUNT(*) FILTER (WHERE a.status = 'REJECTED')::BIGINT AS rejected_count,
        ROUND(
            (COUNT(*) FILTER (WHERE a.status = 'APPROVED')::NUMERIC /
            NULLIF(COUNT(*) FILTER (WHERE a.status IN ('APPROVED', 'REJECTED')), 0) * 100),
            2
        ) AS approval_rate
    FROM opportunities o
    LEFT JOIN applications a ON o.opportunity_id = a.opportunity_id
    GROUP BY o.opportunity_type
    ORDER BY o.opportunity_type;
END;
$$ LANGUAGE plpgsql;

COMMENT ON FUNCTION get_application_conversion_rates() IS 'Returns application conversion rates by opportunity type';

-- =================================================================================
-- MENTORSHIP STATISTICS
-- =================================================================================

-- Function to get mentorship session statistics
CREATE OR REPLACE FUNCTION get_mentorship_stats(days_back INTEGER DEFAULT 30)
RETURNS TABLE(
    status session_status,
    session_count BIGINT,
    unique_mentors BIGINT,
    unique_mentees BIGINT,
    avg_duration_minutes NUMERIC,
    total_hours NUMERIC
) AS $$
BEGIN
    RETURN QUERY
    SELECT
        ms.status,
        COUNT(*)::BIGINT AS session_count,
        COUNT(DISTINCT mentor_id)::BIGINT AS unique_mentors,
        COUNT(DISTINCT mentee_id)::BIGINT AS unique_mentees,
        ROUND(AVG(duration_minutes), 2) AS avg_duration_minutes,
        ROUND(SUM(duration_minutes)::NUMERIC / 60, 2) AS total_hours
    FROM mentorship_sessions ms
    WHERE created_at >= CURRENT_TIMESTAMP - (days_back || ' days')::INTERVAL
    GROUP BY ms.status
    ORDER BY session_count DESC;
END;
$$ LANGUAGE plpgsql;

COMMENT ON FUNCTION get_mentorship_stats(INTEGER) IS 'Returns mentorship session statistics';

-- =================================================================================
-- ARCHIVE OLD RECORDS FUNCTION
-- =================================================================================

-- Function to archive old audit trail records
CREATE OR REPLACE FUNCTION archive_old_audit_logs(days_to_keep INTEGER DEFAULT 90)
RETURNS TABLE(
    message TEXT,
    archived_count BIGINT,
    oldest_kept_date TIMESTAMP,
    executed_at TIMESTAMP
) AS $$
DECLARE
    v_deleted_count BIGINT;
    v_oldest_date TIMESTAMP;
BEGIN
    -- Get the oldest record that will be kept
    SELECT MIN(created_at) INTO v_oldest_date
    FROM audit_trail
    WHERE created_at >= CURRENT_TIMESTAMP - (days_to_keep || ' days')::INTERVAL;

    -- Delete audit logs older than specified days
    DELETE FROM audit_trail
    WHERE created_at < CURRENT_TIMESTAMP - (days_to_keep || ' days')::INTERVAL;

    GET DIAGNOSTICS v_deleted_count = ROW_COUNT;

    RETURN QUERY SELECT
        FORMAT('Archived %s audit log records older than %s days', v_deleted_count, days_to_keep)::TEXT,
        v_deleted_count,
        v_oldest_date,
        CURRENT_TIMESTAMP;
END;
$$ LANGUAGE plpgsql;

COMMENT ON FUNCTION archive_old_audit_logs(INTEGER) IS 'Archives audit logs older than specified days';

-- Function to archive old activity logs
CREATE OR REPLACE FUNCTION archive_old_activity_logs(days_to_keep INTEGER DEFAULT 180)
RETURNS TABLE(
    message TEXT,
    archived_count BIGINT,
    executed_at TIMESTAMP
) AS $$
DECLARE
    v_deleted_count BIGINT;
BEGIN
    DELETE FROM user_activity_logs
    WHERE created_at < CURRENT_TIMESTAMP - (days_to_keep || ' days')::INTERVAL;

    GET DIAGNOSTICS v_deleted_count = ROW_COUNT;

    RETURN QUERY SELECT
        FORMAT('Archived %s activity log records older than %s days', v_deleted_count, days_to_keep)::TEXT,
        v_deleted_count,
        CURRENT_TIMESTAMP;
END;
$$ LANGUAGE plpgsql;

COMMENT ON FUNCTION archive_old_activity_logs(INTEGER) IS 'Archives activity logs older than specified days';

-- =================================================================================
-- USER MANAGEMENT FUNCTIONS
-- =================================================================================

-- Function to soft delete user and cascade
CREATE OR REPLACE FUNCTION soft_delete_user(p_user_id BIGINT, p_deleted_by BIGINT)
RETURNS TABLE(
    success BOOLEAN,
    message TEXT,
    affected_records INTEGER
) AS $$
DECLARE
    v_affected INTEGER := 0;
BEGIN
    -- Check if user exists
    IF NOT EXISTS (SELECT 1 FROM users WHERE user_id = p_user_id) THEN
        RETURN QUERY SELECT FALSE, 'User not found'::TEXT, 0;
        RETURN;
    END IF;

    -- Soft delete user
    UPDATE users
    SET is_deleted = TRUE,
        deleted_at = CURRENT_TIMESTAMP,
        is_active = FALSE,
        updated_by = p_deleted_by
    WHERE user_id = p_user_id;
    GET DIAGNOSTICS v_affected = ROW_COUNT;

    -- Cascade soft delete to profiles
    UPDATE youth_profiles SET is_deleted = TRUE, deleted_at = CURRENT_TIMESTAMP WHERE user_id = p_user_id;
    UPDATE mentor_profiles SET is_deleted = TRUE, deleted_at = CURRENT_TIMESTAMP WHERE user_id = p_user_id;
    UPDATE ngo_profiles SET is_deleted = TRUE, deleted_at = CURRENT_TIMESTAMP WHERE user_id = p_user_id;
    UPDATE funder_profiles SET is_deleted = TRUE, deleted_at = CURRENT_TIMESTAMP WHERE user_id = p_user_id;
    UPDATE service_provider_profiles SET is_deleted = TRUE, deleted_at = CURRENT_TIMESTAMP WHERE user_id = p_user_id;

    -- Deactivate user interests
    UPDATE user_interests SET is_active = FALSE WHERE user_id = p_user_id;

    -- Log the action
    INSERT INTO audit_trail (user_id, service_name, action_type, entity_type, entity_id, description, status)
    VALUES (p_deleted_by, 'user-service', 'SOFT_DELETE_USER', 'USER', p_user_id,
            FORMAT('User %s soft deleted', p_user_id), 'SUCCESS');

    RETURN QUERY SELECT TRUE, FORMAT('User %s successfully soft deleted', p_user_id)::TEXT, v_affected;
END;
$$ LANGUAGE plpgsql;

COMMENT ON FUNCTION soft_delete_user(BIGINT, BIGINT) IS 'Soft deletes a user and cascades to related records';

-- Function to restore soft deleted user
CREATE OR REPLACE FUNCTION restore_deleted_user(p_user_id BIGINT, p_restored_by BIGINT)
RETURNS TABLE(
    success BOOLEAN,
    message TEXT
) AS $$
BEGIN
    -- Check if user exists and is deleted
    IF NOT EXISTS (SELECT 1 FROM users WHERE user_id = p_user_id AND is_deleted = TRUE) THEN
        RETURN QUERY SELECT FALSE, 'User not found or not deleted'::TEXT;
        RETURN;
    END IF;

    -- Restore user
    UPDATE users
    SET is_deleted = FALSE,
        deleted_at = NULL,
        is_active = TRUE,
        updated_by = p_restored_by
    WHERE user_id = p_user_id;

    -- Restore profiles
    UPDATE youth_profiles SET is_deleted = FALSE, deleted_at = NULL WHERE user_id = p_user_id;
    UPDATE mentor_profiles SET is_deleted = FALSE, deleted_at = NULL WHERE user_id = p_user_id;
    UPDATE ngo_profiles SET is_deleted = FALSE, deleted_at = NULL WHERE user_id = p_user_id;
    UPDATE funder_profiles SET is_deleted = FALSE, deleted_at = NULL WHERE user_id = p_user_id;
    UPDATE service_provider_profiles SET is_deleted = FALSE, deleted_at = NULL WHERE user_id = p_user_id;

    -- Reactivate user interests
    UPDATE user_interests SET is_active = TRUE WHERE user_id = p_user_id;

    -- Log the action
    INSERT INTO audit_trail (user_id, service_name, action_type, entity_type, entity_id, description, status)
    VALUES (p_restored_by, 'user-service', 'RESTORE_USER', 'USER', p_user_id,
            FORMAT('User %s restored', p_user_id), 'SUCCESS');

    RETURN QUERY SELECT TRUE, FORMAT('User %s successfully restored', p_user_id)::TEXT;
END;
$$ LANGUAGE plpgsql;

COMMENT ON FUNCTION restore_deleted_user(BIGINT, BIGINT) IS 'Restores a soft deleted user and related records';

-- =================================================================================
-- INTEREST MANAGEMENT FUNCTIONS
-- =================================================================================

-- Function to bulk add/update user interests
CREATE OR REPLACE FUNCTION bulk_upsert_user_interests(
    p_user_id BIGINT,
    p_interest_tags TEXT[],
    p_interest_level interest_level DEFAULT 'MEDIUM',
    p_source interest_source DEFAULT 'USER_SELECTED',
    p_created_by BIGINT DEFAULT NULL
)
RETURNS TABLE(
    success BOOLEAN,
    message TEXT,
    affected_count INTEGER
) AS $$
DECLARE
    v_tag TEXT;
    v_count INTEGER := 0;
BEGIN
    -- Loop through each interest tag
    FOREACH v_tag IN ARRAY p_interest_tags
    LOOP
        -- Trim and check if not empty
        v_tag := TRIM(v_tag);
        IF LENGTH(v_tag) > 0 THEN
            INSERT INTO user_interests (
                user_id, interest_tag, interest_name, interest_level, source, created_by
            ) VALUES (
                p_user_id, v_tag, v_tag, p_interest_level, p_source, p_created_by
            )
            ON CONFLICT (user_id, interest_tag)
            DO UPDATE SET
                interest_level = EXCLUDED.interest_level,
                updated_at = CURRENT_TIMESTAMP,
                updated_by = p_created_by,
                is_active = TRUE;

            v_count := v_count + 1;
        END IF;
    END LOOP;

    RETURN QUERY SELECT TRUE, FORMAT('Successfully processed %s interests', v_count)::TEXT, v_count;
END;
$$ LANGUAGE plpgsql;

COMMENT ON FUNCTION bulk_upsert_user_interests(BIGINT, TEXT[], interest_level, interest_source, BIGINT)
    IS 'Bulk insert or update user interests';

-- =================================================================================
-- UTILITY FUNCTIONS
-- =================================================================================

-- Function to validate Ugandan phone number
CREATE OR REPLACE FUNCTION is_valid_ugandan_phone(phone TEXT)
RETURNS BOOLEAN AS $$
BEGIN
    RETURN phone ~ '^\+?256[0-9]{9}$|^0[0-9]{9}$';
END;
$$ LANGUAGE plpgsql IMMUTABLE;

COMMENT ON FUNCTION is_valid_ugandan_phone(TEXT) IS 'Validates Ugandan phone number format';

-- Function to format Ugandan phone number to international format
CREATE OR REPLACE FUNCTION format_ugandan_phone(phone TEXT)
RETURNS TEXT AS $$
DECLARE
    v_cleaned TEXT;
BEGIN
    -- Remove any spaces or special characters
    v_cleaned := REGEXP_REPLACE(phone, '[^0-9+]', '', 'g');

    -- If starts with 0, replace with +256
    IF LEFT(v_cleaned, 1) = '0' THEN
        RETURN '+256' || SUBSTRING(v_cleaned FROM 2);
    END IF;

    -- If starts with 256, add +
    IF LEFT(v_cleaned, 3) = '256' THEN
        RETURN '+' || v_cleaned;
    END IF;

    -- If already starts with +256, return as is
    IF LEFT(v_cleaned, 4) = '+256' THEN
        RETURN v_cleaned;
    END IF;

    -- Invalid format
    RETURN NULL;
END;
$$ LANGUAGE plpgsql IMMUTABLE;

COMMENT ON FUNCTION format_ugandan_phone(TEXT) IS 'Formats Ugandan phone number to international format (+256...)';

-- Function to generate user display name
CREATE OR REPLACE FUNCTION get_user_display_name(p_user_id BIGINT)
RETURNS TEXT AS $$
DECLARE
    v_display_name TEXT;
BEGIN
    SELECT display_name INTO v_display_name
    FROM user_complete_profiles
    WHERE user_id = p_user_id;

    RETURN COALESCE(v_display_name, 'Unknown User');
END;
$$ LANGUAGE plpgsql STABLE;

COMMENT ON FUNCTION get_user_display_name(BIGINT) IS 'Returns formatted display name for a user';

-- =================================================================================
-- VACUUM & MAINTENANCE FUNCTION
-- =================================================================================

-- Function to perform routine maintenance
CREATE OR REPLACE FUNCTION perform_routine_maintenance()
RETURNS TABLE(
    operation TEXT,
    status TEXT,
    details TEXT
) AS $$
BEGIN
    -- Clean expired tokens
    PERFORM sp_clean_expired_tokens();
    RETURN QUERY SELECT 'Clean Expired Tokens'::TEXT, 'SUCCESS'::TEXT, 'Completed'::TEXT;

    -- Vacuum analyze all tables
    EXECUTE 'VACUUM ANALYZE';
    RETURN QUERY SELECT 'Vacuum Analyze'::TEXT, 'SUCCESS'::TEXT, 'Completed'::TEXT;

    -- Update statistics
    EXECUTE 'ANALYZE';
    RETURN QUERY SELECT 'Update Statistics'::TEXT, 'SUCCESS'::TEXT, 'Completed'::TEXT;

    RETURN;
END;
$$ LANGUAGE plpgsql;

COMMENT ON FUNCTION perform_routine_maintenance() IS 'Performs routine database maintenance tasks';

-- =================================================================================
-- END OF MAINTENANCE FUNCTIONS
-- =================================================================================