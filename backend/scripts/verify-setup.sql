-- ═════════════════════════════════════════════════════════════════════════════
-- DATABASE VERIFICATION SCRIPT
-- ═════════════════════════════════════════════════════════════════════════════

\c youthconnect_user

\echo '═══════════════════════════════════════════════════════════════'
\echo 'DATABASE VERIFICATION REPORT'
\echo '═══════════════════════════════════════════════════════════════'
\echo ''

\echo '1. INSTALLED EXTENSIONS:'
\dx

\echo ''
\echo '2. ENUM TYPES:'
SELECT n.nspname as "Schema",
       t.typname as "Name"
FROM pg_type t
     LEFT JOIN pg_catalog.pg_namespace n ON n.oid = t.typnamespace
WHERE (t.typrelid = 0 OR (SELECT c.relkind = 'c' FROM pg_catalog.pg_class c WHERE c.oid = t.typrelid))
  AND NOT EXISTS(SELECT 1 FROM pg_catalog.pg_type el WHERE el.oid = t.typelem AND el.typarray = t.oid)
  AND n.nspname = 'public'
  AND t.typtype = 'e'
ORDER BY 1, 2;

\echo ''
\echo '3. TABLES:'
\dt

\echo ''
\echo '4. INDEXES:'
\di

\echo ''
\echo '5. FLYWAY SCHEMA HISTORY:'
SELECT installed_rank, version, description, type, script, success, installed_on
FROM flyway_schema_history
ORDER BY installed_rank;

\echo ''
\echo '6. TABLE ROW COUNTS:'
SELECT
    schemaname,
    tablename,
    n_live_tup AS row_count
FROM pg_stat_user_tables
WHERE schemaname = 'public'
ORDER BY tablename;

\echo ''
\echo '═══════════════════════════════════════════════════════════════'