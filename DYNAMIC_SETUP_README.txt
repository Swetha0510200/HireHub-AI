HireHub AI - Dynamic Version
=============================

This version keeps the existing HTML/CSS theme and changes the data flow to real
database-backed data.

MAIN DYNAMIC FLOWS
------------------
1. Register -> user_accounts + user_profiles in MySQL.
2. Login -> validates the stored password hash.
3. Post Job -> saves Job in MySQL -> redirects to Browse Jobs.
4. Browse Jobs -> reads every posted Job from MySQL.
5. Apply Now -> saves a real JobApplication.
6. Save Job -> saves a real SavedJob.
7. Applications -> reads real JobApplication records.
8. Resume Analyzer -> accepts PDF/DOC/DOCX, extracts text, calculates the ATS
   analysis, stores the uploaded file and analysis in MySQL.
9. Dashboard ATS score -> reads the latest saved ResumeRecord.
10. ATS Score page -> reads the same latest ResumeRecord.
11. Profile resume upload -> also stores and analyzes the resume.
12. Resume download -> serves the actual uploaded file from the database.

IMPORTANT
---------
The CSS files were intentionally left unchanged.

REPLACE / ADD THESE FILES
-------------------------
src/main/java/com/hirehub/model/Job.java
src/main/java/com/hirehub/model/UserAccount.java
src/main/java/com/hirehub/model/ResumeRecord.java

src/main/java/com/hirehub/repository/JobRepository.java
src/main/java/com/hirehub/repository/UserAccountRepository.java
src/main/java/com/hirehub/repository/ResumeRecordRepository.java

src/main/java/com/hirehub/util/PasswordUtil.java

src/main/java/com/hirehub/controller/LoginController.java
src/main/java/com/hirehub/controller/RegisterController.java
src/main/java/com/hirehub/controller/JobController.java
src/main/java/com/hirehub/controller/JobActionController.java
src/main/java/com/hirehub/controller/DashboardController.java
src/main/java/com/hirehub/controller/AtsScoreController.java
src/main/java/com/hirehub/controller/ResumeAnalyzerController.java
src/main/java/com/hirehub/controller/ProfileController.java
src/main/java/com/hirehub/controller/InterviewController.java
src/main/java/com/hirehub/controller/UtilityPageController.java

src/main/java/com/hirehub/service/ResumeAnalyzerService.java

src/main/resources/templates/register.html
src/main/resources/templates/login.html
src/main/resources/templates/jobs.html
src/main/resources/templates/job-details.html
src/main/resources/templates/applications.html
src/main/resources/templates/saved-jobs.html
src/main/resources/templates/ats-score.html
src/main/resources/templates/resume-analyzer.html

pom.xml
src/main/resources/application.properties

DATABASE
--------
Create the database once:

CREATE DATABASE IF NOT EXISTS hirehub;

Do NOT manually create the tables. Hibernate will create/update them because:
spring.jpa.hibernate.ddl-auto=update

RUN
---
From:
HireHub/hirehub

PowerShell:
.\mvnw.cmd spring-boot:run

Then:
http://localhost:8080/

FIRST TEST
----------
1. Create a Student account.
2. Login.
3. Upload a real PDF/DOC/DOCX resume.
4. Confirm the ATS score appears.
5. Open Dashboard and confirm the same ATS score appears.
6. Create/login as Recruiter.
7. Open Post a Job.
8. Publish a job.
9. Open Browse Jobs.
10. The newly posted job must appear from MySQL.
11. Login as Student and Apply Now.
12. Open Applications and confirm the real application appears.

NOTE
----
If XAMPP MySQL is not running, the application cannot connect to the database.
The project uses:
host: 127.0.0.1
port: 3306
database: hirehub
username: root
password: blank
