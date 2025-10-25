-- ============================================================================
-- Youth Connect Uganda - Audit Fields, Performance & Analytics Enhancement
-- ============================================================================
-- Version: V2
-- Description: Adds audit trails, performance indexes, views, triggers, and procedures
-- ============================================================================

USE youth_connect_db;

-- ============================================================================
-- STEP 1: ADD AUDIT FIELDS TO PROFILE TABLES
-- ============================================================================

-- Youth Profiles
ALTER TABLE youth_profiles
ADD COLUMN IF NOT EXISTS created_by BIGINT NULL COMMENT 'User who created this profile',
ADD COLUMN IF NOT EXISTS updated_by BIGINT NULL COMMENT 'User who last updated',
ADD INDEX IF NOT EXISTS idx_youth_created_by (created_by),
ADD INDEX IF NOT EXISTS idx_youth_updated_by (updated_by);

-- Mentor Profiles
ALTER TABLE mentor_profiles
ADD COLUMN IF NOT EXISTS created_by BIGINT NULL,
ADD COLUMN IF NOT EXISTS updated_by BIGINT NULL,
ADD INDEX IF NOT EXISTS idx_mentor_created_by (created_by),
ADD INDEX IF NOT EXISTS idx_mentor_updated_by (updated_by);

-- NGO Profiles
ALTER TABLE ngo_profiles
ADD COLUMN IF NOT EXISTS created_by BIGINT NULL,
ADD COLUMN IF NOT EXISTS updated_by BIGINT NULL,
ADD INDEX IF NOT EXISTS idx_ngo_created_by (created_by),
ADD INDEX IF NOT EXISTS idx_ngo_updated_by (updated_by);

-- Funder Profiles
ALTER TABLE funder_profiles
ADD COLUMN IF NOT EXISTS created_by BIGINT NULL,
ADD COLUMN IF NOT EXISTS updated_by BIGINT NULL,
ADD INDEX IF NOT EXISTS idx_funder_created_by (created_by),
ADD INDEX IF NOT EXISTS idx_funder_updated_by (updated_by);

-- Service Provider Profiles
ALTER TABLE service_provider_profiles
ADD COLUMN IF NOT EXISTS created_by BIGINT NULL,
ADD COLUMN IF NOT EXISTS updated_by BIGINT NULL,
ADD INDEX IF NOT EXISTS idx_provider_created_by (created_by),
ADD INDEX IF NOT EXISTS idx_provider_updated_by (updated_by);

-- User Interests
ALTER TABLE user_interests
ADD COLUMN IF NOT EXISTS created_by BIGINT NULL,
ADD COLUMN IF NOT EXISTS updated_by BIGINT NULL,
ADD INDEX IF NOT EXISTS idx_interests_created_by (created_by);

-- ============================================================================
-- STEP 2: ADD SOFT DELETE FIELDS FOR DATA RETENTION
-- ============================================================================

-- Users table
ALTER TABLE users
ADD COLUMN IF NOT EXISTS is_deleted BOOLEAN DEFAULT FALSE AFTER is_verified,
ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP NULL AFTER is_deleted,
ADD INDEX IF NOT EXISTS idx_users_deleted (is_deleted, deleted_at);

-- All profile tables
ALTER TABLE youth_profiles
ADD COLUMN IF NOT EXISTS is_deleted BOOLEAN DEFAULT FALSE,
ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP NULL;

ALTER TABLE mentor_profiles
ADD COLUMN IF NOT EXISTS is_deleted BOOLEAN DEFAULT FALSE,
ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP NULL;

ALTER TABLE ngo_profiles
ADD COLUMN IF NOT EXISTS is_deleted BOOLEAN DEFAULT FALSE,
ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP NULL;

ALTER TABLE funder_profiles
ADD COLUMN IF NOT EXISTS is_deleted BOOLEAN DEFAULT FALSE,
ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP NULL;

ALTER TABLE service_provider_profiles
ADD COLUMN IF NOT EXISTS is_deleted BOOLEAN DEFAULT FALSE,
ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP NULL;

-- ============================================================================
-- STEP 3: CREATE ANALYTICS VIEWS
-- ============================================================================

-- View: Recent User Activity Summary (Last 30 Days)
CREATE OR REPLACE VIEW v_recent_user_activity AS
SELECT
    u.user_id,
    u.email,
    u.role,
    COALESCE(yp.first_name, mp.first_name, np.organisation_name,
             fp.funder_name, sp.provider_name) AS display_name,
    COUNT(ual.log_id) AS total_activities,
    COUNT(DISTINCT ual.activity_type) AS distinct_activity_types,
    MAX(ual.created_at) AS last_activity_time,
    COUNT(DISTINCT ual.session_id) AS session_count,
    COUNT(DISTINCT DATE(ual.created_at)) AS active_days
FROM users u
LEFT JOIN youth_profiles yp ON u.user_id = yp.user_id
LEFT JOIN mentor_profiles mp ON u.user_id = mp.user_id
LEFT JOIN ngo_profiles np ON u.user_id = np.user_id
LEFT JOIN funder_profiles fp ON u.user_id = fp.user_id
LEFT JOIN service_provider_profiles sp ON u.user_id = sp.user_id
LEFT JOIN user_activity_logs ual ON u.user_id = ual.user_id
    AND ual.created_at >= DATE_SUB(NOW(), INTERVAL 30 DAY)
WHERE u.is_active = TRUE AND (u.is_deleted = FALSE OR u.is_deleted IS NULL)
GROUP BY u.user_id, u.email, u.role, display_name;

-- View: Popular Interests Across Platform
CREATE OR REPLACE VIEW v_popular_interests AS
SELECT
    COALESCE(interest_tag, interest_name) AS interest,
    interest_category,
    COUNT(DISTINCT user_id) AS user_count,
    AVG(CASE interest_level
        WHEN 'HIGH' THEN 3
        WHEN 'MEDIUM' THEN 2
        WHEN 'LOW' THEN 1
        ELSE 2
    END) AS avg_interest_level,
    SUM(interaction_count) AS total_interactions,
    MAX(last_interaction) AS most_recent_interaction,
    COUNT(CASE WHEN is_primary = TRUE THEN 1 END) AS primary_interest_count
FROM user_interests
WHERE is_active = TRUE
GROUP BY interest, interest_category
ORDER BY user_count DESC, total_interactions DESC;

-- View: User Engagement Metrics
CREATE OR REPLACE VIEW v_user_engagement_metrics AS
SELECT
    u.user_id,
    u.email,
    u.role,
    u.created_at AS user_since,
    DATEDIFF(NOW(), u.created_at) AS days_since_registration,
    COUNT(DISTINCT DATE(ual.created_at)) AS active_days_last_30d,
    COUNT(ual.log_id) AS total_actions_last_30d,
    COUNT(DISTINCT ual.activity_type) AS unique_activity_types,
    AVG(ual.duration_seconds) AS avg_session_duration,
    COUNT(DISTINCT ual.target_id) AS unique_items_engaged,
    COUNT(DISTINCT ui.interest_tag) AS total_interests,
    MAX(ual.created_at) AS last_active_date,
    CASE
        WHEN MAX(ual.created_at) >= DATE_SUB(NOW(), INTERVAL 7 DAY) THEN 'ACTIVE'
        WHEN MAX(ual.created_at) >= DATE_SUB(NOW(), INTERVAL 30 DAY) THEN 'MODERATE'
        ELSE 'INACTIVE'
    END AS engagement_status
FROM users u
LEFT JOIN user_activity_logs ual ON u.user_id = ual.user_id
    AND ual.created_at >= DATE_SUB(NOW(), INTERVAL 30 DAY)
LEFT JOIN user_interests ui ON u.user_id = ui.user_id AND ui.is_active = TRUE
WHERE u.is_active = TRUE AND (u.is_deleted = FALSE OR u.is_deleted IS NULL)
GROUP BY u.user_id, u.email, u.role, u.created_at;

-- View: Activity Heatmap (Day/Hour Distribution)
CREATE OR REPLACE VIEW v_activity_heatmap AS
SELECT
    DAYNAME(created_at) AS day_of_week,
    HOUR(created_at) AS hour_of_day,
    COUNT(*) AS activity_count,
    COUNT(DISTINCT user_id) AS unique_users,
    COUNT(DISTINCT session_id) AS unique_sessions
FROM user_activity_logs
WHERE created_at >= DATE_SUB(NOW(), INTERVAL 30 DAY)
GROUP BY day_of_week, hour_of_day
ORDER BY
    FIELD(day_of_week, 'Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday', 'Sunday'),
    hour_of_day;

-- View: Interest Trends Over Time
CREATE OR REPLACE VIEW v_interest_trends AS
SELECT
    COALESCE(interest_tag, interest_name) AS interest,
    interest_category,
    DATE_FORMAT(created_at, '%Y-%m') AS month_year,
    COUNT(*) AS new_interests_added,
    AVG(confidence_score) AS avg_confidence,
    COUNT(CASE WHEN source = 'USER_SELECTED' THEN 1 END) AS user_selected_count,
    COUNT(CASE WHEN source = 'AI_INFERRED' THEN 1 END) AS ai_inferred_count,
    COUNT(CASE WHEN source = 'ACTIVITY_BASED' THEN 1 END) AS activity_based_count
FROM user_interests
WHERE created_at >= DATE_SUB(NOW(), INTERVAL 6 MONTH)
GROUP BY interest, interest_category, month_year
ORDER BY month_year DESC, new_interests_added DESC;

-- ============================================================================
-- STEP 4: CREATE STORED PROCEDURES
-- ============================================================================

DELIMITER //

-- Procedure: Get User's Top Interests
DROP PROCEDURE IF EXISTS sp_get_user_top_interests//
CREATE PROCEDURE sp_get_user_top_interests(
    IN p_user_id BIGINT,
    IN p_limit INT,
    IN p_offset INT
)
BEGIN
    SELECT
        COALESCE(interest_tag, interest_name) AS interest,
        interest_level,
        interest_category,
        interaction_count,
        source,
        confidence_score,
        is_primary,
        last_interaction,
        created_at
    FROM user_interests
    WHERE user_id = p_user_id
    AND is_active = TRUE
    ORDER BY
        is_primary DESC,
        CASE interest_level
            WHEN 'HIGH' THEN 3
            WHEN 'MEDIUM' THEN 2
            WHEN 'LOW' THEN 1
        END DESC,
        interaction_count DESC,
        confidence_score DESC
    LIMIT p_limit OFFSET p_offset;
END//

-- Procedure: Get User Activity Summary
DROP PROCEDURE IF EXISTS sp_get_user_activity_summary//
CREATE PROCEDURE sp_get_user_activity_summary(
    IN p_user_id BIGINT,
    IN p_start_date DATE,
    IN p_end_date DATE
)
BEGIN
    SELECT
        activity_type,
        activity_category,
        COUNT(*) AS activity_count,
        COUNT(DISTINCT target_id) AS unique_targets,
        COUNT(DISTINCT DATE(created_at)) AS active_days,
        AVG(duration_seconds) AS avg_duration,
        MAX(created_at) AS last_activity,
        COUNT(CASE WHEN action_result = 'SUCCESS' THEN 1 END) AS successful_actions,
        COUNT(CASE WHEN action_result = 'FAILED' THEN 1 END) AS failed_actions
    FROM user_activity_logs
    WHERE user_id = p_user_id
    AND DATE(created_at) BETWEEN p_start_date AND p_end_date
    GROUP BY activity_type, activity_category
    ORDER BY activity_count DESC;
END//

-- Procedure: Track User Activity
DROP PROCEDURE IF EXISTS sp_track_user_activity//
CREATE PROCEDURE sp_track_user_activity(
    IN p_user_id BIGINT,
    IN p_activity_type VARCHAR(50),
    IN p_activity_category VARCHAR(50),
    IN p_target_id BIGINT,
    IN p_target_type VARCHAR(50),
    IN p_target_name VARCHAR(255),
    IN p_session_id VARCHAR(255),
    IN p_tags TEXT,
    IN p_metadata JSON
)
BEGIN
    DECLARE v_log_id BIGINT;

    INSERT INTO user_activity_logs (
        user_id,
        activity_type,
        activity_category,
        target_id,
        target_type,
        target_name,
        session_id,
        tags,
        metadata,
        device_type
    ) VALUES (
        p_user_id,
        p_activity_type,
        p_activity_category,
        p_target_id,
        p_target_type,
        p_target_name,
        p_session_id,
        p_tags,
        p_metadata,
        'WEB'
    );

    SET v_log_id = LAST_INSERT_ID();

    SELECT v_log_id AS log_id, 'Activity tracked successfully' AS message;
END//

-- Procedure: Bulk Add User Interests
DROP PROCEDURE IF EXISTS sp_bulk_add_user_interests//
CREATE PROCEDURE sp_bulk_add_user_interests(
    IN p_user_id BIGINT,
    IN p_interest_tags TEXT,
    IN p_interest_level ENUM('LOW', 'MEDIUM', 'HIGH'),
    IN p_source ENUM('USER_SELECTED', 'AI_INFERRED', 'ACTIVITY_BASED', 'SURVEY', 'IMPORTED'),
    IN p_created_by BIGINT
)
BEGIN
    DECLARE v_tag VARCHAR(50);
    DECLARE v_position INT DEFAULT 1;
    DECLARE v_comma_pos INT;

    WHILE v_position > 0 DO
        SET v_comma_pos = LOCATE(',', p_interest_tags, v_position);

        IF v_comma_pos = 0 THEN
            SET v_tag = TRIM(SUBSTRING(p_interest_tags, v_position));
            SET v_position = 0;
        ELSE
            SET v_tag = TRIM(SUBSTRING(p_interest_tags, v_position, v_comma_pos - v_position));
            SET v_position = v_comma_pos + 1;
        END IF;

        IF LENGTH(v_tag) > 0 THEN
            INSERT INTO user_interests (
                user_id,
                interest_tag,
                interest_name,
                interest_level,
                source,
                created_by
            ) VALUES (
                p_user_id,
                v_tag,
                v_tag,
                p_interest_level,
                p_source,
                p_created_by
            ) ON DUPLICATE KEY UPDATE
                interest_level = VALUES(interest_level),
                updated_at = CURRENT_TIMESTAMP,
                updated_by = p_created_by;
        END IF;
    END WHILE;

    SELECT CONCAT('Interests added successfully for user ', p_user_id) AS message;
END//

-- Procedure: Get Recommended Connections
DROP PROCEDURE IF EXISTS sp_get_recommended_connections//
CREATE PROCEDURE sp_get_recommended_connections(
    IN p_user_id BIGINT,
    IN p_limit INT
)
BEGIN
    SELECT
        u.user_id,
        u.email,
        u.role,
        COALESCE(yp.first_name, mp.first_name) AS first_name,
        COALESCE(yp.last_name, mp.last_name) AS last_name,
        COUNT(DISTINCT ui2.interest_tag) AS shared_interests,
        GROUP_CONCAT(DISTINCT ui2.interest_tag ORDER BY ui2.interest_tag) AS common_tags
    FROM user_interests ui1
    INNER JOIN user_interests ui2 ON ui1.interest_tag = ui2.interest_tag
    INNER JOIN users u ON ui2.user_id = u.user_id
    LEFT JOIN youth_profiles yp ON u.user_id = yp.user_id
    LEFT JOIN mentor_profiles mp ON u.user_id = mp.user_id
    WHERE ui1.user_id = p_user_id
    AND ui2.user_id != p_user_id
    AND ui1.is_active = TRUE
    AND ui2.is_active = TRUE
    AND u.is_active = TRUE
    AND (u.is_deleted = FALSE OR u.is_deleted IS NULL)
    GROUP BY u.user_id, u.email, u.role, first_name, last_name
    ORDER BY shared_interests DESC, u.created_at DESC
    LIMIT p_limit;
END//

-- Procedure: Generate Engagement Report
DROP PROCEDURE IF EXISTS sp_generate_engagement_report//
CREATE PROCEDURE sp_generate_engagement_report(
    IN p_start_date DATE,
    IN p_end_date DATE
)
BEGIN
    SELECT
        u.role,
        COUNT(DISTINCT u.user_id) AS total_users,
        COUNT(DISTINCT ual.user_id) AS active_users,
        ROUND(COUNT(DISTINCT ual.user_id) / COUNT(DISTINCT u.user_id) * 100, 2) AS engagement_rate,
        COUNT(ual.log_id) AS total_activities,
        ROUND(COUNT(ual.log_id) / NULLIF(COUNT(DISTINCT ual.user_id), 0), 2) AS avg_activities_per_user,
        COUNT(DISTINCT ual.session_id) AS total_sessions
    FROM users u
    LEFT JOIN user_activity_logs ual ON u.user_id = ual.user_id
        AND DATE(ual.created_at) BETWEEN p_start_date AND p_end_date
    WHERE u.is_active = TRUE AND (u.is_deleted = FALSE OR u.is_deleted IS NULL)
    GROUP BY u.role
    ORDER BY total_users DESC;
END//

DELIMITER ;

-- ============================================================================
-- STEP 5: CREATE TRIGGERS
-- ============================================================================

DELIMITER //

-- Trigger: Update Interest Interactions
DROP TRIGGER IF EXISTS trg_update_interest_interaction//
CREATE TRIGGER trg_update_interest_interaction
AFTER INSERT ON user_activity_logs
FOR EACH ROW
BEGIN
    IF NEW.tags IS NOT NULL THEN
        UPDATE user_interests ui
        SET
            ui.interaction_count = ui.interaction_count + 1,
            ui.last_interaction = NEW.created_at,
            ui.updated_at = CURRENT_TIMESTAMP
        WHERE ui.user_id = NEW.user_id
        AND ui.is_active = TRUE
        AND FIND_IN_SET(ui.interest_tag, REPLACE(NEW.tags, ' ', '')) > 0;

        -- Auto-adjust interest level based on interactions
        UPDATE user_interests
        SET
            interest_level = CASE
                WHEN interaction_count >= 50 THEN 'HIGH'
                WHEN interaction_count >= 20 THEN 'MEDIUM'
                ELSE 'LOW'
            END,
            updated_at = CURRENT_TIMESTAMP
        WHERE user_id = NEW.user_id
        AND is_active = TRUE
        AND source IN ('ACTIVITY_BASED', 'AI_INFERRED');
    END IF;
END//

-- Trigger: Audit Youth Profile Updates
DROP TRIGGER IF EXISTS trg_audit_youth_profile_update//
CREATE TRIGGER trg_audit_youth_profile_update
AFTER UPDATE ON youth_profiles
FOR EACH ROW
BEGIN
    INSERT INTO audit_trail (
        user_id,
        action_type,
        entity_type,
        entity_id,
        description,
        status
    ) VALUES (
        NEW.updated_by,
        'UPDATE_PROFILE',
        'YOUTH_PROFILE',
        NEW.profile_id,
        CONCAT('Youth profile updated for user_id: ', NEW.user_id),
        'SUCCESS'
    );
END//

-- Trigger: Audit Mentor Profile Updates
DROP TRIGGER IF EXISTS trg_audit_mentor_profile_update//
CREATE TRIGGER trg_audit_mentor_profile_update
AFTER UPDATE ON mentor_profiles
FOR EACH ROW
BEGIN
    INSERT INTO audit_trail (
        user_id,
        action_type,
        entity_type,
        entity_id,
        description,
        status
    ) VALUES (
        NEW.updated_by,
        'UPDATE_PROFILE',
        'MENTOR_PROFILE',
        NEW.mentor_profile_id,
        CONCAT('Mentor profile updated for user_id: ', NEW.user_id),
        'SUCCESS'
    );
END//

-- Trigger: Soft Delete Cascade
DROP TRIGGER IF EXISTS trg_soft_delete_user_cascade//
CREATE TRIGGER trg_soft_delete_user_cascade
AFTER UPDATE ON users
FOR EACH ROW
BEGIN
    IF NEW.is_deleted = TRUE AND (OLD.is_deleted = FALSE OR OLD.is_deleted IS NULL) THEN
        UPDATE youth_profiles
        SET is_deleted = TRUE, deleted_at = NEW.deleted_at
        WHERE user_id = NEW.user_id;

        UPDATE mentor_profiles
        SET is_deleted = TRUE, deleted_at = NEW.deleted_at
        WHERE user_id = NEW.user_id;

        UPDATE ngo_profiles
        SET is_deleted = TRUE, deleted_at = NEW.deleted_at
        WHERE user_id = NEW.user_id;

        UPDATE funder_profiles
        SET is_deleted = TRUE, deleted_at = NEW.deleted_at
        WHERE user_id = NEW.user_id;

        UPDATE service_provider_profiles
        SET is_deleted = TRUE, deleted_at = NEW.deleted_at
        WHERE user_id = NEW.user_id;

        UPDATE user_interests
        SET is_active = FALSE
        WHERE user_id = NEW.user_id;
    END IF;
END//

DELIMITER ;

-- ============================================================================
-- VERIFICATION QUERIES
-- ============================================================================

-- Verify audit fields added
SELECT
    'Audit Fields Added' AS Status,
    TABLE_NAME,
    COLUMN_NAME
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = DATABASE()
AND COLUMN_NAME IN ('created_by', 'updated_by', 'is_deleted', 'deleted_at')
ORDER BY TABLE_NAME, COLUMN_NAME;

-- Verify views created
SELECT
    'Views Created' AS Status,
    TABLE_NAME AS view_name
FROM INFORMATION_SCHEMA.TABLES
WHERE TABLE_SCHEMA = DATABASE()
AND TABLE_TYPE = 'VIEW'
ORDER BY TABLE_NAME;

-- Verify stored procedures created
SELECT
    'Stored Procedures Created' AS Status,
    ROUTINE_NAME AS procedure_name
FROM INFORMATION_SCHEMA.ROUTINES
WHERE ROUTINE_SCHEMA = DATABASE()
AND ROUTINE_TYPE = 'PROCEDURE'
ORDER BY ROUTINE_NAME;

-- Verify triggers created
SELECT
    'Triggers Created' AS Status,
    TRIGGER_NAME,
    EVENT_OBJECT_TABLE,
    ACTION_TIMING,
    EVENT_MANIPULATION
FROM INFORMATION_SCHEMA.TRIGGERS
WHERE TRIGGER_SCHEMA = DATABASE()
ORDER BY EVENT_OBJECT_TABLE, TRIGGER_NAME;

-- Success message
SELECT '✓ Audit fields, views, procedures, and triggers added successfully!' AS Result;