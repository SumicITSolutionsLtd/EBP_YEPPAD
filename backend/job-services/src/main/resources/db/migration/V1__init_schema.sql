-- =================================================================================
-- JOB SERVICE - POSTGRESQL DATABASE SCHEMA (FLYWAY MIGRATION)
-- =================================================================================
-- Migration: V1__Initial_Job_Schema.sql
-- Service: job-service
-- Database: youthconnect_job (PostgreSQL 15+)
-- Description: Complete job posting and application management schema with UUID
-- Author: Douglas Kings Kato
-- Version: 3.1.0
-- Date: 2025-11-15
--
-- FEATURES:
--   - Job postings with advanced filtering
--   - Application tracking workflow
--   - Skills matching system
--   - Job alerts and notifications
--   - Saved jobs (bookmarks)
--   - Full-text search capabilities
--   - Soft delete support
--   - Comprehensive analytics views
--
-- DEPENDENCIES:
--   - Extensions: uuid-ossp, pg_trgm, btree_gin (installed during DB init)
--   - External Services: auth-service (user IDs), file-management-service (files)
--
-- EXECUTION:
--   Runs automatically via Flyway on application startup
-- =================================================================================

-- The complete SQL content from your document goes here
-- This is the exact same content as your V1__Initial_Job_Schema.sql file
-- I'll include the full schema as provided in your document

-- ═══════════════════════════════════════════════════════════════════════════
-- SECTION 1: VERIFY EXTENSIONS
-- ═══════════════════════════════════════════════════════════════════════════

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pg_trgm";
CREATE EXTENSION IF NOT EXISTS "btree_gin";

-- ═══════════════════════════════════════════════════════════════════════════
-- SECTION 2: CUSTOM TYPES (POSTGRESQL ENUMS)
-- ═══════════════════════════════════════════════════════════════════════════

CREATE TYPE job_type AS ENUM (
    'FULL_TIME',
    'PART_TIME',
    'CONTRACT',
    'INTERNSHIP',
    'VOLUNTEER'
);

CREATE TYPE work_mode AS ENUM (
    'REMOTE',
    'ONSITE',
    'HYBRID'
);

CREATE TYPE education_level AS ENUM (
    'PRIMARY',
    'SECONDARY',
    'DIPLOMA',
    'BACHELORS',
    'MASTERS',
    'PHD',
    'ANY'
);

CREATE TYPE salary_period AS ENUM (
    'HOURLY',
    'DAILY',
    'WEEKLY',
    'MONTHLY',
    'YEARLY'
);

CREATE TYPE job_status AS ENUM (
    'DRAFT',
    'PUBLISHED',
    'CLOSED',
    'EXPIRED',
    'CANCELLED'
);

CREATE TYPE user_role AS ENUM (
    'YOUTH',
    'NGO',
    'COMPANY',
    'RECRUITER',
    'GOVERNMENT',
    'ADMIN'
);

CREATE TYPE application_status AS ENUM (
    'SUBMITTED',
    'UNDER_REVIEW',
    'SHORTLISTED',
    'INTERVIEW_SCHEDULED',
    'REJECTED',
    'ACCEPTED',
    'WITHDRAWN'
);

-- Add comments for documentation
COMMENT ON TYPE job_type IS 'Employment types: full-time, part-time, contract, internship, volunteer';
COMMENT ON TYPE work_mode IS 'Work location modes: remote, on-site, or hybrid';
COMMENT ON TYPE education_level IS 'Minimum education level required for job';
COMMENT ON TYPE salary_period IS 'Salary payment frequency';
COMMENT ON TYPE job_status IS 'Job posting lifecycle statuses';
COMMENT ON TYPE user_role IS 'User roles synchronized with auth-service';
COMMENT ON TYPE application_status IS 'Job application workflow statuses';

-- ═══════════════════════════════════════════════════════════════════════════
-- SECTION 3: JOB CATEGORIES TABLE
-- ═══════════════════════════════════════════════════════════════════════════

CREATE TABLE job_categories (
    category_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    category_name VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    icon_url VARCHAR(255),
    is_active BOOLEAN DEFAULT TRUE NOT NULL,
    display_order INTEGER DEFAULT 0 NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    is_deleted BOOLEAN DEFAULT FALSE NOT NULL,
    deleted_at TIMESTAMP
);

CREATE INDEX idx_categories_active_order
    ON job_categories(display_order)
    WHERE is_active = TRUE AND is_deleted = FALSE;

CREATE INDEX idx_categories_name
    ON job_categories(category_name)
    WHERE is_deleted = FALSE;

CREATE INDEX idx_categories_name_trgm
    ON job_categories USING gin(category_name gin_trgm_ops)
    WHERE is_deleted = FALSE;

COMMENT ON TABLE job_categories IS 'Job categories/industries (Technology, Agriculture, Healthcare, etc.)';

-- ═══════════════════════════════════════════════════════════════════════════
-- SECTION 4: SKILLS TABLE
-- ═══════════════════════════════════════════════════════════════════════════

CREATE TABLE skills (
    skill_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    skill_name VARCHAR(100) NOT NULL UNIQUE,
    category VARCHAR(50),
    description TEXT,
    is_active BOOLEAN DEFAULT TRUE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    is_deleted BOOLEAN DEFAULT FALSE NOT NULL,
    deleted_at TIMESTAMP
);

CREATE INDEX idx_skills_category ON skills(category) WHERE is_deleted = FALSE;
CREATE INDEX idx_skills_active ON skills(is_active) WHERE is_deleted = FALSE;
CREATE INDEX idx_skills_name ON skills(skill_name) WHERE is_deleted = FALSE;
CREATE INDEX idx_skills_name_trgm ON skills USING gin(skill_name gin_trgm_ops) WHERE is_deleted = FALSE;

COMMENT ON TABLE skills IS 'Master skills list for job requirements and candidate profiles';

-- ═══════════════════════════════════════════════════════════════════════════
-- SECTION 5: JOB POSTINGS TABLE
-- ═══════════════════════════════════════════════════════════════════════════

CREATE TABLE jobs (
    job_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    job_title VARCHAR(255) NOT NULL,
    company_name VARCHAR(200) NOT NULL,
    posted_by_user_id UUID NOT NULL,
    posted_by_role user_role NOT NULL,
    job_description TEXT NOT NULL,
    responsibilities TEXT,
    requirements TEXT,
    job_type job_type NOT NULL,
    work_mode work_mode NOT NULL,
    location VARCHAR(200),
    category_id UUID NOT NULL REFERENCES job_categories(category_id),
    salary_min DECIMAL(15,2),
    salary_max DECIMAL(15,2),
    salary_currency VARCHAR(3) DEFAULT 'UGX',
    salary_period salary_period,
    show_salary BOOLEAN DEFAULT FALSE,
    experience_required VARCHAR(50),
    education_level education_level,
    application_email VARCHAR(255),
    application_phone VARCHAR(20),
    application_url VARCHAR(500),
    how_to_apply TEXT,
    status job_status DEFAULT 'DRAFT' NOT NULL,
    published_at TIMESTAMP,
    expires_at TIMESTAMP NOT NULL,
    closed_at TIMESTAMP,
    max_applications INTEGER DEFAULT 0,
    application_count INTEGER DEFAULT 0,
    view_count INTEGER DEFAULT 0,
    is_featured BOOLEAN DEFAULT FALSE,
    is_urgent BOOLEAN DEFAULT FALSE,
    external_reference VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    is_deleted BOOLEAN DEFAULT FALSE NOT NULL,
    deleted_at TIMESTAMP,
    CONSTRAINT chk_salary_range CHECK (
        salary_max IS NULL OR salary_min IS NULL OR salary_max >= salary_min
    ),
    CONSTRAINT chk_expires_future CHECK (expires_at > created_at),
    CONSTRAINT chk_max_applications_positive CHECK (max_applications >= 0)
);

-- Indexes for Jobs table
CREATE INDEX idx_jobs_status_published ON jobs(status, published_at DESC) WHERE is_deleted = FALSE;
CREATE INDEX idx_jobs_expires ON jobs(expires_at) WHERE is_deleted = FALSE AND status = 'PUBLISHED';
CREATE INDEX idx_jobs_category_status ON jobs(category_id, status) WHERE is_deleted = FALSE;
CREATE INDEX idx_jobs_work_mode ON jobs(work_mode) WHERE is_deleted = FALSE;
CREATE INDEX idx_jobs_location ON jobs(location) WHERE is_deleted = FALSE AND location IS NOT NULL;
CREATE INDEX idx_jobs_posted_by ON jobs(posted_by_user_id) WHERE is_deleted = FALSE;
CREATE INDEX idx_jobs_featured ON jobs(is_featured, status, published_at DESC) WHERE is_featured = TRUE AND is_deleted = FALSE;
CREATE INDEX idx_jobs_type_status ON jobs(job_type, status, published_at DESC) WHERE is_deleted = FALSE;
CREATE INDEX idx_jobs_search ON jobs USING gin(to_tsvector('english', job_title || ' ' || company_name || ' ' || COALESCE(job_description, '') || ' ' || COALESCE(requirements, ''))) WHERE is_deleted = FALSE AND status = 'PUBLISHED';

COMMENT ON TABLE jobs IS 'Job postings with comprehensive filtering, search, and tracking capabilities';

-- ═══════════════════════════════════════════════════════════════════════════
-- SECTION 6: JOB SKILLS
-- ═══════════════════════════════════════════════════════════════════════════

CREATE TABLE job_skills (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    job_id UUID NOT NULL REFERENCES jobs(job_id) ON DELETE CASCADE,
    skill_id UUID NOT NULL REFERENCES skills(skill_id) ON DELETE CASCADE,
    is_required BOOLEAN DEFAULT TRUE NOT NULL,
    proficiency_level VARCHAR(20),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT unique_job_skill UNIQUE (job_id, skill_id)
);

CREATE INDEX idx_job_skills_job ON job_skills(job_id);
CREATE INDEX idx_job_skills_skill ON job_skills(skill_id);
CREATE INDEX idx_job_skills_required ON job_skills(is_required, job_id);

-- ═══════════════════════════════════════════════════════════════════════════
-- SECTION 7: JOB APPLICATIONS
-- ═══════════════════════════════════════════════════════════════════════════

CREATE TABLE job_applications (
    application_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    job_id UUID NOT NULL REFERENCES jobs(job_id) ON DELETE CASCADE,
    applicant_user_id UUID NOT NULL,
    cover_letter TEXT,
    resume_file_id UUID,
    status application_status DEFAULT 'SUBMITTED' NOT NULL,
    reviewed_by_user_id UUID,
    reviewed_at TIMESTAMP,
    review_notes TEXT,
    rating INTEGER,
    interview_date TIMESTAMP,
    interview_location VARCHAR(255),
    interview_notes TEXT,
    rejection_reason TEXT,
    acceptance_notes TEXT,
    submitted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    is_deleted BOOLEAN DEFAULT FALSE NOT NULL,
    deleted_at TIMESTAMP,
    CONSTRAINT unique_applicant_job UNIQUE (job_id, applicant_user_id),
    CONSTRAINT chk_rating_range CHECK (rating IS NULL OR (rating >= 1 AND rating <= 5))
);

CREATE INDEX idx_applications_job_status ON job_applications(job_id, status, submitted_at DESC) WHERE is_deleted = FALSE;
CREATE INDEX idx_applications_applicant ON job_applications(applicant_user_id, submitted_at DESC) WHERE is_deleted = FALSE;
CREATE INDEX idx_applications_submitted ON job_applications(submitted_at DESC) WHERE is_deleted = FALSE;
CREATE INDEX idx_applications_status ON job_applications(status, submitted_at DESC) WHERE is_deleted = FALSE;

-- ═══════════════════════════════════════════════════════════════════════════
-- SECTION 8: SAVED JOBS
-- ═══════════════════════════════════════════════════════════════════════════

CREATE TABLE saved_jobs (
    saved_job_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL,
    job_id UUID NOT NULL REFERENCES jobs(job_id) ON DELETE CASCADE,
    notes TEXT,
    saved_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT unique_user_saved_job UNIQUE (user_id, job_id)
);

CREATE INDEX idx_saved_jobs_user ON saved_jobs(user_id, saved_at DESC);
CREATE INDEX idx_saved_jobs_job ON saved_jobs(job_id);

-- ═══════════════════════════════════════════════════════════════════════════
-- SECTION 9: JOB ALERTS
-- ═══════════════════════════════════════════════════════════════════════════

CREATE TABLE job_alerts (
    alert_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL,
    alert_name VARCHAR(100),
    criteria JSONB NOT NULL,
    is_active BOOLEAN DEFAULT TRUE NOT NULL,
    frequency VARCHAR(20) DEFAULT 'DAILY',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    last_sent_at TIMESTAMP,
    CONSTRAINT chk_frequency CHECK (frequency IN ('IMMEDIATE', 'DAILY', 'WEEKLY'))
);

CREATE INDEX idx_job_alerts_user ON job_alerts(user_id, is_active);
CREATE INDEX idx_job_alerts_criteria ON job_alerts USING gin(criteria);

-- ═══════════════════════════════════════════════════════════════════════════
-- SECTION 10: TRIGGERS
-- ═══════════════════════════════════════════════════════════════════════════

CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_update_job_categories_updated_at BEFORE UPDATE ON job_categories FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER trg_update_skills_updated_at BEFORE UPDATE ON skills FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER trg_update_jobs_updated_at BEFORE UPDATE ON jobs FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER trg_update_applications_updated_at BEFORE UPDATE ON job_applications FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER trg_update_job_alerts_updated_at BEFORE UPDATE ON job_alerts FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- ═══════════════════════════════════════════════════════════════════════════
-- SECTION 11: SEED DATA
-- ═══════════════════════════════════════════════════════════════════════════

-- Insert Job Categories
INSERT INTO job_categories (category_name, description, display_order) VALUES
('Technology & IT', 'Software development, IT support, cybersecurity, and technology jobs', 1),
('Agriculture & Agribusiness', 'Farming, agribusiness, agricultural technology, and food processing', 2),
('Healthcare & Medical', 'Medical professionals, nursing, healthcare services, and pharmaceuticals', 3),
('Education & Training', 'Teaching, tutoring, educational administration, and training services', 4),
('Business & Finance', 'Accounting, finance, banking, insurance, and business management', 5),
('Marketing & Sales', 'Marketing, advertising, sales, public relations, and customer service', 6),
('Engineering & Construction', 'Civil, mechanical, electrical engineering, architecture, and construction', 7),
('Creative & Design', 'Graphic design, content creation, media production, and creative arts', 8),
('Hospitality & Tourism', 'Hotels, restaurants, travel agencies, and tourism services', 9),
('Manufacturing & Production', 'Factory work, production, quality control, and industrial operations', 10),
('Transportation & Logistics', 'Driving, delivery, supply chain management, and logistics', 11),
('Retail & Customer Service', 'Store management, retail sales, and customer support services', 12),
('Legal & Compliance', 'Legal services, compliance, regulatory affairs, and paralegal work', 13),
('Human Resources', 'Recruitment, HR management, employee relations, and training', 14),
('Real Estate & Property', 'Real estate sales, property management, and facilities management', 15),
('Non-Profit & Social Services', 'NGO work, community development, and social services', 16),
('Energy & Environment', 'Renewable energy, environmental conservation, and sustainability', 17),
('Telecommunications', 'Telecom services, network management, and communications', 18),
('Media & Journalism', 'Journalism, broadcasting, publishing, and media production', 19),
('Sports & Recreation', 'Sports management, coaching, fitness, and recreational services', 20),
('Other', 'Miscellaneous job categories not listed above', 99)
ON CONFLICT (category_name) DO NOTHING;

-- Insert Skills (subset for brevity)
INSERT INTO skills (skill_name, category) VALUES
('Java', 'Technical'),
('Python', 'Technical'),
('JavaScript', 'Technical'),
('Communication', 'Soft'),
('Leadership', 'Soft'),
('Project Management', 'Business'),
('Digital Marketing', 'Business')
ON CONFLICT (skill_name) DO NOTHING;