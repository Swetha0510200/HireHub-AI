# HireHub AI

### AI-Powered Job & Internship Recruitment Platform

HireHub AI is a modern, full-stack recruitment platform designed to connect job seekers, students, recruiters, and administrators through an intelligent and streamlined hiring ecosystem.

The platform combines traditional job and internship management with AI-assisted resume analysis and skill-based job matching to help candidates discover relevant opportunities and help recruiters manage applications efficiently.

---

## 🚀 Project Overview

HireHub AI provides a centralized platform where candidates can:

- Create and manage professional profiles
- Upload and analyze resumes
- Receive ATS-based resume scores
- Discover jobs and internships
- Get AI-powered job recommendations
- Save interesting job opportunities
- Apply for jobs
- Track application progress
- View scheduled interviews

Recruiters can:

- Create and manage company profiles
- Post jobs and internships
- Edit and delete job postings
- View applicants
- Manage application statuses
- Schedule interviews

Administrators can:

- Manage users
- Manage recruiters
- Manage jobs and internships
- Monitor applications
- Access platform statistics

---

LIVE DEMO 

   https://hirehub-ai-system.onrender.com

   https://drive.google.com/file/d/1cOWu5OFKWEEdP88MbegqXU3GmUGEmJ8w/view?usp=drive_link
   
## ✨ Key Features

### 👨‍💼 Candidate Features

- Candidate registration and login
- Professional profile management
- Resume upload
- Resume text extraction
- ATS resume scoring
- Skill identification
- Resume strengths and improvement insights
- Job search and filtering
- Internship discovery
- Job details and application
- Saved jobs
- Application tracking
- Interview tracking
- AI-powered job matching
- Candidate dashboard

### 🏢 Recruiter Features

- Recruiter registration and login
- Company profile management
- Job posting
- Internship posting
- Job editing and deletion
- Applicant management
- Application status updates
- Interview scheduling
- Recruiter dashboard

### 🛡️ Admin Features

- Admin dashboard
- User management
- Recruiter management
- Job management
- Application monitoring
- Platform statistics

### 🤖 AI Features

#### AI Job Match

HireHub AI uses a skill-based recommendation algorithm to compare candidate information with job requirements.

The matching process considers factors such as:

- Technical skills
- Candidate profile information
- Education
- Experience
- Resume information
- Job-required skills

The system generates a compatibility score to help candidates identify relevant opportunities.

#### ATS Resume Analyzer

The resume analyzer extracts information from uploaded resumes and evaluates them using multiple criteria, including:

- Resume structure
- Skills
- Sections
- Achievements
- Resume length
- ATS compatibility
- Contact information
- Education
- Experience
- Projects
- Certifications

The system generates an ATS score and provides resume insights.

---

## 🛠️ Technology Stack

### Backend

- Java 17
- Spring Boot 3.5.4
- Spring MVC
- Spring Data JPA
- Hibernate
- Maven

### Frontend

- HTML5
- CSS3
- JavaScript
- Thymeleaf
- Font Awesome
- Responsive UI

### Database

- MySQL
- MySQL Connector/J

### Resume Processing

- Apache PDFBox
- Apache POI

### Development Tools

- Git
- GitHub
- Visual Studio Code
- Maven Wrapper

---

## 🏗️ Project Architecture


HireHub-AI
│
├── src
│   ├── main
│   │   ├── java
│   │   │   └── com.hirehub
│   │   │       ├── config
│   │   │       ├── controller
│   │   │       ├── dto
│   │   │       ├── model
│   │   │       ├── repository
│   │   │       ├── service
│   │   │       └── util
│   │   │
│   │   └── resources
│   │       ├── static
│   │       │   ├── css
│   │       │   └── js
│   │       │
│   │       ├── templates
│   │       └── application.properties
│   │
│   └── test
│
├── database
│   └── create-hirehub.sql
│
├── pom.xml
├── mvnw
├── mvnw.cmd
├── README.md
└── .gitignore
