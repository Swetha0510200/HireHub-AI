# HireHub – AI-Powered Job & Internship Portal

**HireHub** is an enterprise-grade, full-stack recruitment and career intelligence platform designed for students, job seekers, enterprise recruiters, and university placement cells. Built with **Java 17**, **Spring Boot 3.5.x**, **Spring Data JPA / Hibernate**, **MySQL**, and **Thymeleaf with HTML5 & Vanilla JavaScript**, HireHub offers intelligent skill-based job/internship recommendations, real-time application timeline tracking, automated ATS resume scoring, recruiter applicant pipeline management, and administrative moderation.

---

## 🌟 Key Platform Highlights

1. **Intelligent Local AI Recommendation Engine**:
   - Calculates weighted candidate-to-opportunity fit using a local 4-factor formula:
     $$\text{Match Score} = (0.60 \times \text{Skill Match}) + (0.20 \times \text{Category Match}) + (0.10 \times \text{Location Match}) + (0.10 \times \text{Experience Match})$$
   - Generates granular insights including matched skills, missing skills, and confidence categories without requiring paid third-party APIs.

2. **Unified Job & Internship Ecosystem**:
   - Distinct pathways for full-time professional roles and student internships with stipend, duration, and academic eligibility criteria.
   - 1-Click application flow with automated resume attachment and ATS compatibility grading.

3. **Step-by-Step Application Timeline Stepper**:
   - Visual candidate progress tracking across stages:
     `Applied` $\rightarrow$ `Under Review` $\rightarrow$ `Shortlisted` $\rightarrow$ `Interview Scheduled` $\rightarrow$ `Selected / Hired` (or `Rejected`).
   - Real-time recruiter notes and status feedback.

4. **Automated ATS Resume Analyzer**:
   - Native parser evaluating word count, line count, detected tech skills, action verbs, quantified achievements, and ATS section structure, providing an overall ATS score (0–100) and actionable improvement recommendations.

5. **Recruiter Hiring & Pipeline Hub**:
   - Recruiter job & internship posting with custom requirements, candidate applicant review, resume downloads, 1-click status transitions, and interview scheduling with Google Meet links.

6. **Admin Moderation & Control Center**:
   - Platform analytics, account activation/deactivation, and 1-click moderation for job and internship listings.

---

## 🛠️ Technology Stack

| Layer | Technology |
|---|---|
| **Backend Framework** | Java 17, Spring Boot 3.5.x |
| **Persistence & ORM** | Spring Data JPA, Hibernate ORM |
| **Database** | MySQL Community Server 8.0+ |
| **Security & Hashing** | Spring Security Crypto (`BCryptPasswordEncoder`), Session Auth & Role Interceptors |
| **Template Engine** | Thymeleaf 3.x |
| **Frontend Technologies** | Semantic HTML5, Modern Responsive CSS3 (CSS Variables), Vanilla JavaScript (ES6+) |
| **Icons & Typography** | FontAwesome 6.6+, Google Fonts (Inter / Poppins) |
| **Build & Dependency Management** | Apache Maven 3.9+ |

---

## 🔐 Default Demo Accounts & Credentials

HireHub automatically seeds the MySQL database on startup with ready-to-test accounts:

| Role | Username / Email | Password | Details |
|---|---|---|---|
| **Super Admin** | `admin` (or `admin@hirehub.com`) | `admin123` | Platform oversight, job moderation, user activation |
| **Recruiter (Google)** | `recruiter@google.com` | `recruiter123` | Google India hiring recruiter |
| **Recruiter (Microsoft)** | `recruiter@microsoft.com` | `recruiter123` | Microsoft IDC hiring recruiter |
| **Recruiter (Amazon)** | `recruiter@amazon.com` | `recruiter123` | Amazon AWS recruiter |
| **Candidate (Java / Full Stack)** | `alex.johnson@example.com` | `student123` | B.Tech CS, IIT Madras, Java, Spring Boot, MySQL, Docker |
| **Candidate (AI / ML)** | `priya.sharma@example.com` | `student123` | B.Tech AI, BITS Pilani, Python, PyTorch, Deep Learning |
| **Candidate (Cloud / DevOps)** | `rahul.verma@example.com` | `student123` | B.Tech IT, NIT Trichy, AWS, Kubernetes, CI/CD |
| **Candidate (General Demo)** | `candidate@hirehub.com` | `candidate123` | Standard candidate test account |

---

## 🚀 Quickstart & Setup Guide

### 1. Prerequisites
- **Java Development Kit (JDK)**: Version 17 or higher (`java -version`).
- **MySQL Database Server**: Running locally on port `3306` with user `root` (or configured credentials).
- **Apache Maven**: Version 3.8+ (or use the included wrapper).

### 2. Database Configuration
By default, HireHub connects to MySQL at `jdbc:mysql://127.0.0.1:3306/hirehub?createDatabaseIfNotExist=true`.
To adjust credentials, edit `src/main/resources/application.properties`:
```properties
spring.datasource.url=jdbc:mysql://127.0.0.1:3306/hirehub?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
spring.datasource.username=root
spring.datasource.password=
spring.jpa.hibernate.ddl-auto=update
```

### 3. Build & Run Application
From the project root directory:

```bash
# Using Maven Wrapper (Windows PowerShell)
.\mvnw.cmd clean spring-boot:run

# Or using Maven directly
mvn clean spring-boot:run
```

### 4. Access the Platform
Open your browser and navigate to:
```
http://localhost:8080
```

---

## 📂 Project Structure

```
hirehub/
├── pom.xml
├── README.md
├── src/
│   ├── main/
│   │   ├── java/com/hirehub/
│   │   │   ├── HireHubApplication.java
│   │   │   ├── config/
│   │   │   │   ├── AuthInterceptor.java
│   │   │   │   ├── DataInitializer.java
│   │   │   │   ├── GlobalExceptionHandler.java
│   │   │   │   └── WebMvcConfig.java
│   │   │   ├── controller/
│   │   │   │   ├── AdminController.java
│   │   │   │   ├── DashboardController.java
│   │   │   │   ├── HomeController.java
│   │   │   │   ├── JobActionController.java
│   │   │   │   ├── JobController.java
│   │   │   │   ├── LoginController.java
│   │   │   │   ├── NotificationController.java
│   │   │   │   ├── ProfileController.java
│   │   │   │   ├── RecruiterController.java
│   │   │   │   ├── RegisterController.java
│   │   │   │   └── ResumeController.java
│   │   │   ├── dto/
│   │   │   │   └── JobMatchDto.java
│   │   │   ├── model/
│   │   │   │   ├── Company.java
│   │   │   │   ├── Interview.java
│   │   │   │   ├── Job.java
│   │   │   │   ├── JobApplication.java
│   │   │   │   ├── Notification.java
│   │   │   │   ├── ResumeRecord.java
│   │   │   │   ├── SavedJob.java
│   │   │   │   ├── UserAccount.java
│   │   │   │   └── UserProfile.java
│   │   │   ├── repository/
│   │   │   │   ├── CompanyRepository.java
│   │   │   │   ├── InterviewRepository.java
│   │   │   │   ├── JobApplicationRepository.java
│   │   │   │   ├── JobRepository.java
│   │   │   │   ├── NotificationRepository.java
│   │   │   │   ├── ResumeRecordRepository.java
│   │   │   │   ├── SavedJobRepository.java
│   │   │   │   ├── UserAccountRepository.java
│   │   │   │   └── UserProfileRepository.java
│   │   │   ├── service/
│   │   │   │   ├── AiJobMatcherService.java
│   │   │   │   ├── NotificationService.java
│   │   │   │   └── ResumeAnalyzerService.java
│   │   │   └── util/
│   │   │       └── PasswordUtil.java
│   │   └── resources/
│   │       ├── application.properties
│   │       ├── static/
│   │       │   ├── css/
│   │       │   │   ├── style.css
│   │       │   │   ├── dashboard.css
│   │       │   │   ├── jobs.css
│   │       │   │   ├── applications.css
│   │       │   │   ├── ai-job-match.css
│   │       │   │   └── post-job.css
│   │       │   └── js/
│   │       │       ├── main.js
│   │       │       ├── auth.js
│   │       │       ├── candidate.js
│   │       │       ├── recruiter.js
│   │       │       └── admin.js
│   │       └── templates/
│   │           ├── index.html
│   │           ├── login.html
│   │           ├── register.html
│   │           ├── dashboard.html
│   │           ├── jobs.html
│   │           ├── job-details.html
│   │           ├── internships.html
│   │           ├── internship-details.html
│   │           ├── recommendations.html
│   │           ├── applications.html
│   │           ├── saved-jobs.html
│   │           ├── profile.html
│   │           ├── post-job.html
│   │           ├── create_internship.html
│   │           ├── candidate-view.html
│   │           ├── recruiter-dashboard.html
│   │           ├── recruiter-jobs.html
│   │           ├── recruiter-applicants.html
│   │           ├── admin-dashboard.html
│   │           ├── admin-jobs.html
│   │           ├── admin-users.html
│   │           ├── resume-analyzer.html
│   │           ├── ats-score.html
│   │           ├── interviews.html
│   │           └── error.html
```

---

## 🌐 Application Routing Guide

### Public Endpoints
- `GET /` — Public landing page with search, statistics, featured jobs, and top internships.
- `GET /login`, `POST /login` — Authentication with role routing and BCrypt verification.
- `GET /register`, `POST /register` — Candidate and recruiter account creation.
- `GET /logout` — Session termination.

### Candidate Endpoints
- `GET /dashboard`, `GET /candidate/dashboard` — Candidate dashboard with summary cards & quick actions.
- `GET /jobs`, `GET /browse-jobs`, `GET /candidate/jobs` — Job search with keyword, category, mode, and salary filters.
- `GET /jobs/{id}` — Job details with 1-click apply and bookmarking.
- `GET /internships`, `GET /candidate/internships` — Student internship search with domain and duration filters.
- `GET /internships/{id}` — Internship details and application submission.
- `GET /recommendations`, `GET /candidate/recommendations`, `GET /ai-job-match` — AI skill-based matching portal.
- `GET /applications`, `GET /candidate/applications` — Applications tracking with live stepper timeline.
- `GET /saved-jobs`, `GET /candidate/saved` — Bookmarked jobs and internships.
- `GET /profile`, `GET /candidate/profile` — Profile view and editing with photo & resume uploads.
- `GET /resume-analyzer`, `POST /resume-analyzer` — ATS resume diagnostic breakdown.

### Recruiter Endpoints
- `GET /recruiter/dashboard` — Recruiter analytics dashboard and quick metrics.
- `GET /post-job`, `POST /post-job` — Full-time job posting form.
- `GET /post-internship`, `POST /post-internship` — Student internship creation form.
- `GET /recruiter/jobs` — Manage and toggle active status of posted opportunities.
- `GET /recruiter/applicants`, `GET /recruiter/applications` — View applicants by job, filter by status.
- `GET /recruiter/applications/{id}` — Detailed application view with candidate profile and status actions.
- `GET /recruiter/candidate/{email}` — Candidate profile viewer.
- `POST /recruiter/applications/{id}/status` — Update candidate status (`Under Review`, `Shortlisted`, `Interview`, `Selected`, `Rejected`) with recruiter remarks.
- `GET /recruiter/applications/{id}/resume` — Stream candidate PDF resume.
- `POST /recruiter/schedule-interview` — Schedule candidate interview with Google Meet room.

### Admin Endpoints
- `GET /admin`, `GET /admin/dashboard` — System-wide analytics and platform overview.
- `GET /admin/jobs` — Moderate jobs (Approve, Reject, Delete).
- `GET /admin/internships` — Moderate internships (Approve, Reject, Delete).
- `GET /admin/users` — Moderate candidate and recruiter user accounts (Activate, Deactivate, Delete).

---

## 🛡️ Security & Role Enforcement

HireHub utilizes a centralized `AuthInterceptor` enforcing:
1. **Unauthenticated Redirects**: Any unauthenticated request to protected routes redirects cleanly to `/login`.
2. **Recruiter Isolation**: Normal candidates cannot access `/recruiter/**`, `/post-job`, or applicant data.
3. **Candidate Isolation**: Recruiters are routed to their respective dashboards.
4. **Admin Protection**: Only users with the `Admin` role can access `/admin/**` moderation endpoints.
5. **Secure Cryptography**: Passwords are encrypted using salted BCrypt before database persistence.

---

## 📄 License & Attribution
Developed for **HireHub – AI-Powered Job & Internship Portal** © 2026. Built with modern Java enterprise best practices.
