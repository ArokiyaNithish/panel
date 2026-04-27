# Campus Recruitment Portal Implementation Plan

Design and develop a web-based Campus Recruitment Portal with AI-enhanced features, role-based access, and analytical dashboards.

## User Review Required

> [!IMPORTANT]
> **Database Configuration**: I will configure the project to use **Microsoft SQL Server** (as requested for SSMS). You will need to provide your SQL Server connection details (URL, username, password) in `application.properties`.
> **Gemini API Key**: I will use **Gemini 2.5 Flash** for AI features. You will need a Google AI Studio API Key.
> **Email Configuration**: Automated emails will require SMTP details (e.g., Gmail App Password).

## Proposed Changes

### 1. Project Initialization
- Create `pom.xml` with dependencies: Web, JPA, Security, Thymeleaf, Mail, Validation, MSSQL JDBC Driver, Lombok.
- Set up standard directory structure (`src/main/java`, `src/main/resources`).

### 2. Backend - Core Entities (Spring Data JPA)
- `User`: Handles authentication and roles (STUDENT, EMPLOYER, ADMIN).
- `StudentProfile`: Extended details for students (GPA, Skills, Resume path).
- `Job`: Job postings by Employers.
- `Application`: Links Students to Jobs with status (PENDING, SHORTLISTED, REJECTED, HIRED).
- `Notification`: System alerts for users.
- `Interview`: Scheduling details (date, time, slot).

### 3. Backend - Security & Authentication
- Configure `SecurityConfig` with BCrypt password hashing.
- Role-based access control for URLs.
- Custom Login/Logout flows.

### 4. AI Integration (Gemini 2.5 Flash)
- **Resume Parsing**: Automatically extract skills, experience, and projects from uploaded resumes.
- **Job Matching**: AI-driven recommendation for students based on their skills and job requirements.
- **Candidate Analysis**: Help employers shortlist by comparing resumes against job descriptions.

### 5. Frontend - Modern UI (Thymeleaf + CSS)
- **Core Design**: Premium dark/light mode with glassmorphism and smooth transitions.
- **Student Dashboard**: Application tracker (visual progress bar), job search, profile management, interview calendar.
- **Employer Dashboard**: Job management, applicant lists with AI scores, interview scheduling interface.
- **Admin Dashboard**: Charts for recruitment stats, company approval workflow, student enrollment view.
- **Navigation**: Persistent notification bar and responsive sidebar.

### 6. Additional Features
- **File Upload**: Handle multipart resume uploads.
- **Email Service**: Send notifications for application status changes and interview invites.
- **YouTube Integration**: Embed related career/company videos on job pages.

## Verification Plan

### Automated Tests
- JUnit tests for Repository and Service layers.
- MockMvc tests for Controller endpoints.

### Manual Verification
- Testing the end-to-end flow: Student registration -> Resume upload -> Job application -> Employer shortlisting -> Interview scheduling.
- Verifying UI responsiveness and "WOW" factor.
