-- =================================================================================
-- Job Service - PostgreSQL Database Schema with UUID
-- Version: 2.0.0
-- Description: Complete job service schema with UUID identifiers
-- =================================================================================

-- Enable UUID extension
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pg_trgm";  -- For text search

-- =================================================================================
-- SECTION 1: JOB CATEGORIES & INDUSTRIES
-- =================================================================================

CREATE TABLE job_categories (
    category_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    category_name VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    icon_url VARCHAR(255),
    is_active BOOLEAN DEFAULT TRUE NOT NULL,
    display_order INTEGER DEFAULT 0 NOT NULL,

    -- Audit fields
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,

    -- Soft delete
    is_deleted BOOLEAN DEFAULT FALSE NOT NULL,
    deleted_at TIMESTAMP
);

CREATE INDEX idx_categories_active_order ON job_categories(display_order) WHERE is_active = TRUE AND is_deleted = FALSE;
CREATE INDEX idx_categories_name ON job_categories(category_name) WHERE is_deleted = FALSE;

COMMENT ON TABLE job_categories IS 'Job categories/industries (Technology, Agriculture, etc.)';

-- =================================================================================
-- SECTION 2: JOB POSTINGS
-- =================================================================================

CREATE TYPE job_type AS ENUM ('FULL_TIME', 'PART_TIME', 'CONTRACT', 'INTERNSHIP', 'VOLUNTEER');
CREATE TYPE work_mode AS ENUM ('REMOTE', 'ONSITE', 'HYBRID');
CREATE TYPE education_level AS ENUM ('PRIMARY', 'SECONDARY', 'DIPLOMA', 'BACHELORS', 'MASTERS', 'PHD', 'ANY');
CREATE TYPE salary_period AS ENUM ('HOURLY', 'DAILY', 'WEEKLY', 'MONTHLY', 'YEARLY');
CREATE TYPE job_status AS ENUM ('DRAFT', 'PUBLISHED', 'CLOSED', 'EXPIRED', 'CANCELLED');
CREATE TYPE user_role AS ENUM ('YOUTH', 'NGO', 'COMPANY', 'RECRUITER', 'GOVERNMENT', 'ADMIN');

CREATE TABLE jobs (
    job_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),

    -- Basic Information
    job_title VARCHAR(255) NOT NULL,
    company_name VARCHAR(200) NOT NULL,
    posted_by_user_id UUID NOT NULL,
    posted_by_role user_role NOT NULL,

    -- Job Details
    job_description TEXT NOT NULL,
    responsibilities TEXT,
    requirements TEXT,

    -- Job Type & Location
    job_type job_type NOT NULL,
    work_mode work_mode NOT NULL,
    location VARCHAR(200),

    -- Category
    category_id UUID NOT NULL REFERENCES job_categories(category_id),

    -- Compensation
    salary_min DECIMAL(15,2),
    salary_max DECIMAL(15,2),
    salary_currency VARCHAR(3) DEFAULT 'UGX',
    salary_period salary_period,
    show_salary BOOLEAN DEFAULT FALSE,

    -- Experience & Education
    experience_required VARCHAR(50),
    education_level education_level,

    -- Application Details
    application_email VARCHAR(255),
    application_phone VARCHAR(20),
    application_url VARCHAR(500),
    how_to_apply TEXT,

    -- Job Status & Lifecycle
    status job_status DEFAULT 'DRAFT' NOT NULL,
    published_at TIMESTAMP,
    expires_at TIMESTAMP NOT NULL,
    closed_at TIMESTAMP,

    -- Limits & Tracking
    max_applications INTEGER DEFAULT 0,
    application_count INTEGER DEFAULT 0,
    view_count INTEGER DEFAULT 0,

    -- Metadata
    is_featured BOOLEAN DEFAULT FALSE,
    is_urgent BOOLEAN DEFAULT FALSE,

    -- Audit Fields
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,

    -- Soft Delete
    is_deleted BOOLEAN DEFAULT FALSE NOT NULL,
    deleted_at TIMESTAMP,

    CONSTRAINT chk_salary_range CHECK (salary_max IS NULL OR salary_min IS NULL OR salary_max >= salary_min),
    CONSTRAINT chk_time_order CHECK (expires_at > created_at)
);

CREATE INDEX idx_jobs_status_published ON jobs(status, published_at DESC) WHERE is_deleted = FALSE;
CREATE INDEX idx_jobs_expires ON jobs(expires_at) WHERE is_deleted = FALSE;
CREATE INDEX idx_jobs_category_status ON jobs(category_id, status) WHERE is_deleted = FALSE;
CREATE INDEX idx_jobs_work_mode ON jobs(work_mode) WHERE is_deleted = FALSE;
CREATE INDEX idx_jobs_location ON jobs(location) WHERE is_deleted = FALSE;
CREATE INDEX idx_jobs_posted_by ON jobs(posted_by_user_id) WHERE is_deleted = FALSE;
CREATE INDEX idx_jobs_featured ON jobs(is_featured, status) WHERE is_featured = TRUE AND is_deleted = FALSE;

-- Full-text search index
CREATE INDEX idx_jobs_search ON jobs USING gin(to_tsvector('english', job_title || ' ' || company_name || ' ' || COALESCE(job_description, '')));

COMMENT ON TABLE jobs IS 'Job postings with UUID identifiers';

-- =================================================================================
-- SECTION 3: JOB APPLICATIONS
-- =================================================================================

CREATE TYPE application_status AS ENUM ('SUBMITTED', 'UNDER_REVIEW', 'SHORTLISTED', 'INTERVIEW_SCHEDULED', 'REJECTED', 'ACCEPTED', 'WITHDRAWN');

CREATE TABLE job_applications (
    application_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    job_id UUID NOT NULL REFERENCES jobs(job_id) ON DELETE CASCADE,
    applicant_user_id UUID NOT NULL,

    -- Application Content
    cover_letter TEXT,
    resume_file_id UUID,  -- Reference to file-management-service

    -- Status Tracking
    status application_status DEFAULT 'SUBMITTED' NOT NULL,

    -- Review Information
    reviewed_by_user_id UUID,
    reviewed_at TIMESTAMP,
    review_notes TEXT,

    -- Interview Details
    interview_date TIMESTAMP,
    interview_location VARCHAR(255),
    interview_notes TEXT,

    -- Timestamps
    submitted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,

    -- Soft Delete
    is_deleted BOOLEAN DEFAULT FALSE NOT NULL,
    deleted_at TIMESTAMP,

    CONSTRAINT unique_applicant_job UNIQUE (job_id, applicant_user_id)
);

CREATE INDEX idx_applications_job_status ON job_applications(job_id, status) WHERE is_deleted = FALSE;
CREATE INDEX idx_applications_applicant ON job_applications(applicant_user_id) WHERE is_deleted = FALSE;
CREATE INDEX idx_applications_submitted ON job_applications(submitted_at DESC) WHERE is_deleted = FALSE;

COMMENT ON TABLE job_applications IS 'Job applications with UUID identifiers';

-- =================================================================================
-- SECTION 4: TRIGGERS FOR UPDATED_AT
-- =================================================================================

CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER update_job_categories_updated_at BEFORE UPDATE ON job_categories
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_jobs_updated_at BEFORE UPDATE ON jobs
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_applications_updated_at BEFORE UPDATE ON job_applications
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- =================================================================================
-- SECTION 5: SEED DATA
-- =================================================================================

-- Sample Job Categories
INSERT INTO job_categories (category_name, description, display_order) VALUES
('Technology & IT', 'Software development, IT support, and tech jobs', 1),
('Agriculture & Agribusiness', 'Farming, agribusiness, and agricultural technology', 2),
('Healthcare & Medical', 'Medical, nursing, and healthcare services', 3),
('Education & Training', 'Teaching, tutoring, and educational services', 4),
('Business & Finance', 'Accounting, finance, and business management', 5),
('Marketing & Sales', 'Marketing, sales, and customer service', 6),
('Engineering & Construction', 'Civil, mechanical, electrical engineering', 7),
('Creative & Design', 'Graphic design, content creation, media', 8),
('Hospitality & Tourism', 'Hotels, restaurants, travel services', 9),
('Other', 'Miscellaneous job categories', 99);

-- =================================================================================
-- END OF SCHEMA
-- =================================================================================