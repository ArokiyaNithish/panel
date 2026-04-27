-- V1__init.sql

CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    full_name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT false,
    approved BOOLEAN NOT NULL DEFAULT false,
    phone VARCHAR(255),
    otp VARCHAR(10),
    otp_expiry TIMESTAMP,
    last_login_at TIMESTAMP,
    last_login_ip VARCHAR(255)
);

CREATE TABLE student_profiles (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    user_id UUID NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    department VARCHAR(255),
    degree VARCHAR(255),
    year VARCHAR(50),
    roll_number VARCHAR(255),
    cgpa DOUBLE PRECISION,
    skills TEXT,
    technical_skills TEXT,
    experience TEXT,
    projects TEXT,
    certifications TEXT,
    summary TEXT,
    resume_path VARCHAR(255),
    linkedin_url VARCHAR(255),
    github_url VARCHAR(255)
);

CREATE TABLE jobs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    employer_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    title VARCHAR(200) NOT NULL,
    description TEXT NOT NULL,
    skills_required TEXT,
    location VARCHAR(255),
    category VARCHAR(255),
    experience_level VARCHAR(255),
    salary_range VARCHAR(255),
    package_offered VARCHAR(255),
    openings INTEGER,
    min_cgpa DOUBLE PRECISION,
    eligible_departments VARCHAR(255),
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    youtube_video_url TEXT,
    posted_at TIMESTAMP,
    deadline TIMESTAMP
);

CREATE TABLE applications (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    student_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    job_id UUID NOT NULL REFERENCES jobs(id) ON DELETE CASCADE,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    cover_letter TEXT,
    ai_match_score DOUBLE PRECISION,
    assessment_score INTEGER,
    employer_notes TEXT,
    applied_at TIMESTAMP,
    last_updated TIMESTAMP
);

CREATE TABLE interviews (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    application_id UUID NOT NULL UNIQUE REFERENCES applications(id) ON DELETE CASCADE,
    interview_date DATE NOT NULL,
    interview_time TIME NOT NULL,
    slot VARCHAR(255),
    venue VARCHAR(255),
    meet_link VARCHAR(255),
    interviewer_name VARCHAR(255),
    notes TEXT,
    status VARCHAR(50) NOT NULL DEFAULT 'SCHEDULED',
    reminder_sent BOOLEAN NOT NULL DEFAULT false
);

CREATE TABLE assessment_questions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    job_id UUID NOT NULL REFERENCES jobs(id) ON DELETE CASCADE,
    question_text TEXT NOT NULL,
    optiona VARCHAR(255) NOT NULL,
    optionb VARCHAR(255) NOT NULL,
    optionc VARCHAR(255) NOT NULL,
    optiond VARCHAR(255) NOT NULL,
    correct_option VARCHAR(255) NOT NULL
);

CREATE TABLE notifications (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    title VARCHAR(255),
    message TEXT,
    link VARCHAR(255),
    is_read BOOLEAN NOT NULL DEFAULT false
);

CREATE TABLE employer_profiles (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    user_id UUID NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    company_name VARCHAR(255),
    website VARCHAR(255),
    industry VARCHAR(255),
    description TEXT,
    logo_path VARCHAR(255),
    address VARCHAR(255)
);

CREATE TABLE admin_profiles (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    user_id UUID NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    department VARCHAR(255),
    admin_level VARCHAR(255)
);

CREATE TABLE audit_logs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    user_id UUID REFERENCES users(id) ON DELETE SET NULL,
    action VARCHAR(255) NOT NULL,
    details TEXT,
    ip_address VARCHAR(255)
);
