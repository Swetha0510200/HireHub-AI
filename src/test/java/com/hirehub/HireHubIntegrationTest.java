package com.hirehub;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;

import com.hirehub.dto.JobMatchDto;
import com.hirehub.model.Interview;
import com.hirehub.model.Job;
import com.hirehub.model.JobApplication;
import com.hirehub.model.ResumeRecord;
import com.hirehub.model.SavedJob;
import com.hirehub.model.UserAccount;
import com.hirehub.model.UserProfile;
import com.hirehub.repository.CompanyRepository;
import com.hirehub.repository.InterviewRepository;
import com.hirehub.repository.JobApplicationRepository;
import com.hirehub.repository.JobRepository;
import com.hirehub.repository.ResumeRecordRepository;
import com.hirehub.repository.SavedJobRepository;
import com.hirehub.repository.UserAccountRepository;
import com.hirehub.repository.UserProfileRepository;
import com.hirehub.service.AiJobMatcherService;
import com.hirehub.service.ResumeAnalyzerService;
import com.hirehub.util.PasswordUtil;

@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class HireHubIntegrationTest {

    @Autowired
    private UserAccountRepository accountRepository;

    @Autowired
    private UserProfileRepository profileRepository;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private JobApplicationRepository applicationRepository;

    @Autowired
    private SavedJobRepository savedJobRepository;

    @Autowired
    private InterviewRepository interviewRepository;

    @Autowired
    private ResumeRecordRepository resumeRecordRepository;

    @Autowired
    private ResumeAnalyzerService resumeAnalyzerService;

    @Autowired
    private AiJobMatcherService aiJobMatcherService;

    private static final String TEST_STUDENT_EMAIL = "test.student.e2e@hirehub.com";
    private static final String TEST_RECRUITER_EMAIL = "test.recruiter.e2e@hirehub.com";
    private static final String TEST_PASSWORD = "SecurePassword2026!";
    private static Long postedJobId;
    private static Long createdApplicationId;

    @Test
    @Order(1)
    public void testMySQLConnectionAndContext() {
        assertNotNull(accountRepository, "Account repository must be injected and connected to MySQL");
        assertNotNull(profileRepository, "Profile repository must be injected and connected to MySQL");
        assertNotNull(jobRepository, "Job repository must be injected and connected to MySQL");
        assertNotNull(applicationRepository, "Application repository must be injected and connected to MySQL");
        assertNotNull(savedJobRepository, "SavedJob repository must be injected and connected to MySQL");
        assertNotNull(interviewRepository, "Interview repository must be injected and connected to MySQL");
        assertNotNull(resumeRecordRepository, "ResumeRecord repository must be injected and connected to MySQL");
    }

    @Test
    @Order(2)
    public void testStudentAndRecruiterRegistrationInMySQL() {
        // Clean up test data to guarantee test idempotency
        interviewRepository.findByCandidateEmailOrderByInterviewDateDesc(TEST_STUDENT_EMAIL)
                .forEach(interviewRepository::delete);
        applicationRepository.findByApplicantEmailOrderByAppliedAtDesc(TEST_STUDENT_EMAIL)
                .forEach(applicationRepository::delete);
        savedJobRepository.deleteByUserEmail(TEST_STUDENT_EMAIL);
        resumeRecordRepository.findFirstByUserEmailOrderByUploadedAtDesc(TEST_STUDENT_EMAIL)
                .ifPresent(resumeRecordRepository::delete);

        if (accountRepository.existsById(TEST_STUDENT_EMAIL)) accountRepository.deleteById(TEST_STUDENT_EMAIL);
        if (profileRepository.existsById(TEST_STUDENT_EMAIL)) profileRepository.deleteById(TEST_STUDENT_EMAIL);
        if (accountRepository.existsById(TEST_RECRUITER_EMAIL)) accountRepository.deleteById(TEST_RECRUITER_EMAIL);
        if (profileRepository.existsById(TEST_RECRUITER_EMAIL)) profileRepository.deleteById(TEST_RECRUITER_EMAIL);

        // 1. Student Registration
        String hashedStudentPassword = PasswordUtil.hash(TEST_PASSWORD);
        UserAccount student = new UserAccount(TEST_STUDENT_EMAIL, hashedStudentPassword, "Swetha R", "Student");
        student.setLocation("Bengaluru");
        student.setMobile("+91 9876543210");
        accountRepository.save(student);

        UserProfile studentProfile = new UserProfile();
        studentProfile.setEmail(TEST_STUDENT_EMAIL);
        studentProfile.setName("Swetha R");
        studentProfile.setRole("Student");
        studentProfile.setLocation("Bengaluru");
        studentProfile.setCollege("NIT Trichy");
        studentProfile.setDegree("B.Tech Computer Science");
        studentProfile.setGraduationYear(2026);
        studentProfile.setSkills("Java, Spring Boot, MySQL, REST API, React");
        profileRepository.save(studentProfile);

        // 2. Recruiter Registration
        String hashedRecruiterPassword = PasswordUtil.hash(TEST_PASSWORD);
        UserAccount recruiter = new UserAccount(TEST_RECRUITER_EMAIL, hashedRecruiterPassword, "Arun Kumar", "Recruiter");
        recruiter.setLocation("Hyderabad");
        accountRepository.save(recruiter);

        UserProfile recruiterProfile = new UserProfile();
        recruiterProfile.setEmail(TEST_RECRUITER_EMAIL);
        recruiterProfile.setName("Arun Kumar");
        recruiterProfile.setRole("Recruiter");
        recruiterProfile.setCompanyName("Nexus Innovations");
        recruiterProfile.setCompanyWebsite("https://nexusinnovations.com");
        profileRepository.save(recruiterProfile);

        // 3. Confirm in MySQL
        Optional<UserAccount> loadedStudent = accountRepository.findById(TEST_STUDENT_EMAIL);
        assertTrue(loadedStudent.isPresent(), "Student account must be saved in MySQL");
        assertEquals("Student", loadedStudent.get().getRole());
        assertTrue(PasswordUtil.matches(TEST_PASSWORD, loadedStudent.get().getPasswordHash()), "BCrypt verification must succeed");
        assertFalse(PasswordUtil.matches("WrongPassword", loadedStudent.get().getPasswordHash()), "BCrypt verification must reject wrong password");

        Optional<UserAccount> loadedRecruiter = accountRepository.findById(TEST_RECRUITER_EMAIL);
        assertTrue(loadedRecruiter.isPresent(), "Recruiter account must be saved in MySQL");
        assertEquals("Recruiter", loadedRecruiter.get().getRole());
    }

    @Test
    @Order(3)
    public void testRecruiterPostJobAndBrowseJobs() {
        Job job = new Job();
        job.setTitle("Senior Full Stack Java Engineer");
        job.setCompany("Nexus Innovations");
        job.setLocation("Bengaluru / Remote");
        job.setType("Full Time");
        job.setWorkMode("Hybrid");
        job.setExperience("2-4 Years");
        job.setSalary("₹18-24 LPA");
        job.setSkills("Java, Spring Boot, MySQL, REST API, Docker");
        job.setDescription("Looking for a skilled Java developer to architect high-throughput microservices on Spring Boot.");
        job.setRecruiterEmail(TEST_RECRUITER_EMAIL);
        job.setApplicationEmail(TEST_RECRUITER_EMAIL);
        job.setDeadline("2026-12-31");
        job.setVacancies(4);
        job.setActive(true);
        job.setCreatedAt(LocalDateTime.now());

        Job savedJob = jobRepository.save(job);
        assertNotNull(savedJob.getId(), "Job must be persisted in MySQL with an ID");
        postedJobId = savedJob.getId();

        List<Job> activeJobs = jobRepository.findByActiveTrueOrderByCreatedAtDesc();
        assertFalse(activeJobs.isEmpty(), "Browse jobs must return real active jobs from MySQL");
        assertTrue(activeJobs.stream().anyMatch(j -> j.getId().equals(postedJobId)), "Newly posted job must appear in browse jobs");
    }

    @Test
    @Order(4)
    public void testStudentApplicationAndDuplicatePrevention() {
        assertNotNull(postedJobId, "Posted job ID must be available");
        Job job = jobRepository.findById(postedJobId).orElseThrow();

        JobApplication app = new JobApplication();
        app.setJob(job);
        app.setApplicantEmail(TEST_STUDENT_EMAIL);
        app.setApplicantName("Swetha R");
        app.setApplicantPhone("+91 9876543210");
        app.setDegree("B.Tech Computer Science");
        app.setUniversity("NIT Trichy");
        app.setSkills("Java, Spring Boot, MySQL, REST API");
        app.setCoverLetter("Excited to apply with solid backend and database design experience.");
        app.setMatchScore(92);
        app.setStatus("Applied");
        app.setAppliedAt(LocalDateTime.now());

        JobApplication savedApp = applicationRepository.save(app);
        assertNotNull(savedApp.getId(), "Application must be stored in MySQL");
        createdApplicationId = savedApp.getId();

        // Duplicate prevention check
        boolean exists = applicationRepository.existsByJobIdAndApplicantEmail(job.getId(), TEST_STUDENT_EMAIL);
        assertTrue(exists, "System must detect existing application to prevent duplicate submissions");

        List<JobApplication> candidateApps = applicationRepository.findByApplicantEmailOrderByAppliedAtDesc(TEST_STUDENT_EMAIL);
        assertFalse(candidateApps.isEmpty(), "Student application list must retrieve MySQL records");
        assertEquals("Applied", candidateApps.get(0).getStatus());
    }

    @Test
    @Order(5)
    public void testSavedJobsAndUnsaveFlow() {
        assertNotNull(postedJobId);
        Job job = jobRepository.findById(postedJobId).orElseThrow();

        // Save job
        SavedJob savedJob = new SavedJob(TEST_STUDENT_EMAIL, job);
        savedJobRepository.save(savedJob);

        assertTrue(savedJobRepository.existsByJobIdAndUserEmail(job.getId(), TEST_STUDENT_EMAIL), "Saved job must exist in MySQL");
        List<SavedJob> savedList = savedJobRepository.findByUserEmailOrderBySavedAtDesc(TEST_STUDENT_EMAIL);
        assertFalse(savedList.isEmpty(), "Saved jobs list must not be empty");

        // Unsave job
        savedJobRepository.deleteByJobIdAndUserEmail(job.getId(), TEST_STUDENT_EMAIL);
        assertFalse(savedJobRepository.existsByJobIdAndUserEmail(job.getId(), TEST_STUDENT_EMAIL), "Saved job must be removed from MySQL after unsave");
    }

    @Test
    @Order(6)
    public void testProfilePhotoAndInitials() {
        UserProfile profile = profileRepository.findByEmail(TEST_STUDENT_EMAIL).orElseThrow();
        assertEquals("SR", profile.getInitials(), "Initials for 'Swetha R' must be 'SR'");

        String sampleBase64 = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg==";
        profile.setImageUrl(sampleBase64);
        profileRepository.save(profile);

        UserProfile reloaded = profileRepository.findByEmail(TEST_STUDENT_EMAIL).orElseThrow();
        assertEquals(sampleBase64, reloaded.getImageUrl(), "Profile image base64 data must persist in MySQL");
    }

    @Test
    @Order(7)
    public void testResumeAnalysisAndAtsScoring() throws Exception {
        String sampleResume = """
                Swetha R
                Email: swetha.r@gmail.com | Phone: +91 9876543210 | Location: Bengaluru, India
                LinkedIn: https://linkedin.com/in/swetha-r | GitHub: https://github.com/swetha-r

                PROFESSIONAL SUMMARY
                Passionate Software Engineer with hands-on experience building scalable backend microservices, REST APIs, and database solutions using Java, Spring Boot, and MySQL.

                TECHNICAL SKILLS
                Languages: Java, SQL, JavaScript, HTML5, CSS3
                Frameworks: Spring Boot, Spring Data JPA, Hibernate, REST API, Microservices
                Databases & Tools: MySQL, PostgreSQL, Docker, Git, GitHub, Maven, Postman
                Concepts: OOP, Data Structures, Algorithms, System Design

                WORK EXPERIENCE & PROJECTS
                Java Backend Developer Intern - Tech Solutions (2025 - Present)
                - Developed and deployed high-performance RESTful APIs using Spring Boot and MySQL, improving query latency by 35%.
                - Designed and optimized database schemas with Spring Data JPA for 5000+ daily active users.
                - Automated integration testing using JUnit and Mockito, achieving 90% test coverage.

                EDUCATION
                B.Tech in Computer Science and Engineering - NIT Trichy (2022 - 2026)
                CGPA: 8.9/10
                """;

        MockMultipartFile file = new MockMultipartFile("resume", "swetha_resume.txt", "text/plain", sampleResume.getBytes(StandardCharsets.UTF_8));
        String extracted = resumeAnalyzerService.extractText(file);
        assertNotNull(extracted);
        assertFalse(extracted.isBlank());

        Map<String, Object> analysis = resumeAnalyzerService.analyzeResume(extracted);
        assertNotNull(analysis.get("atsScore"), "ATS Score must be calculated from real resume");
        int atsScore = (Integer) analysis.get("atsScore");
        assertTrue(atsScore >= 50 && atsScore <= 100, "ATS Score must be in realistic range");

        ResumeRecord record = new ResumeRecord();
        record.setUserEmail(TEST_STUDENT_EMAIL);
        record.setFileName("swetha_resume.txt");
        record.setContentType("text/plain");
        record.setFileData(file.getBytes());
        record.setAtsScore(atsScore);
        record.setResumeQuality((String) analysis.get("resumeQuality"));
        record.setSummary((String) analysis.get("summary"));
        record.setSkills(String.join("||", (List<String>) analysis.get("skills")));
        record.setStrengths(String.join("||", (List<String>) analysis.get("strengths")));
        record.setImprovements(String.join("||", (List<String>) analysis.get("improvements")));
        record.setWordCount((Integer) analysis.get("wordCount"));
        record.setSkillCount((Integer) analysis.get("skillCount"));
        record.setLineCount((Integer) analysis.get("lineCount"));
        record.setSectionScore((Integer) analysis.get("sectionScore"));
        record.setSkillScore((Integer) analysis.get("skillScore"));
        record.setAchievementScore((Integer) analysis.get("achievementScore"));
        record.setLengthScore((Integer) analysis.get("lengthScore"));
        record.setAtsStructureScore((Integer) analysis.get("atsStructureScore"));

        ResumeRecord savedRecord = resumeRecordRepository.save(record);
        assertNotNull(savedRecord.getId(), "Resume analysis must be stored in MySQL");

        Optional<ResumeRecord> latestResume = resumeRecordRepository.findFirstByUserEmailOrderByUploadedAtDesc(TEST_STUDENT_EMAIL);
        assertTrue(latestResume.isPresent(), "Saved resume must be retrievable from MySQL");
        assertEquals(atsScore, latestResume.get().getAtsScore());
    }

    @Test
    @Order(8)
    public void testAiJobMatchingEngine() {
        List<JobMatchDto> matches = aiJobMatcherService.matchJobsForUser(TEST_STUDENT_EMAIL);
        assertFalse(matches.isEmpty(), "AI Job Match must compare against real MySQL jobs");

        JobMatchDto topMatch = matches.get(0);
        assertTrue(topMatch.getMatchScore() >= 15 && topMatch.getMatchScore() <= 98, "Match score must be properly calculated");
        assertNotNull(topMatch.getMatchReason(), "Match reason must be generated");
        assertFalse(topMatch.getMatchedSkills().isEmpty(), "Matched skills should detect overlapping skills (Java, Spring Boot, MySQL)");
    }

    @Test
    @Order(9)
    public void testRecruiterScheduleInterviewAndCandidateTracking() {
        assertNotNull(createdApplicationId);
        JobApplication app = applicationRepository.findById(createdApplicationId).orElseThrow();

        Interview interview = new Interview();
        interview.setCandidateEmail(app.getApplicantEmail());
        interview.setCandidateName(app.getApplicantName());
        interview.setRecruiterEmail(TEST_RECRUITER_EMAIL);
        interview.setJob(app.getJob());
        interview.setApplication(app);
        interview.setCompany(app.getJob().getCompany());
        interview.setRole(app.getJob().getTitle());
        interview.setInterviewDate("2026-09-15");
        interview.setInterviewTime("02:30 PM IST");
        interview.setInterviewType("Technical Round 1");
        interview.setMeetingUrl("https://meet.google.com/xyz-abcd-efg");
        interview.setNotes("Be ready with live Java/Spring environment.");
        interview.setStatus("Scheduled");

        Interview savedInterview = interviewRepository.save(interview);
        assertNotNull(savedInterview.getId(), "Interview must be saved to MySQL");

        List<Interview> candidateInterviews = interviewRepository.findByCandidateEmailOrderByInterviewDateDesc(TEST_STUDENT_EMAIL);
        assertFalse(candidateInterviews.isEmpty(), "Candidate must see scheduled interviews from MySQL");
        assertEquals("Technical Round 1", candidateInterviews.get(0).getInterviewType());
        assertEquals("https://meet.google.com/xyz-abcd-efg", candidateInterviews.get(0).getMeetingUrl());
    }

    @Test
    @Order(10)
    public void testCompletePersistenceAcrossReLogin() {
        // Re-query all entities from MySQL to confirm 100% database persistence
        Optional<UserAccount> studentAccount = accountRepository.findById(TEST_STUDENT_EMAIL);
        assertTrue(studentAccount.isPresent());
        assertTrue(PasswordUtil.matches(TEST_PASSWORD, studentAccount.get().getPasswordHash()));

        Optional<UserProfile> studentProfile = profileRepository.findByEmail(TEST_STUDENT_EMAIL);
        assertTrue(studentProfile.isPresent());
        assertEquals("NIT Trichy", studentProfile.get().getCollege());

        List<JobApplication> apps = applicationRepository.findByApplicantEmailOrderByAppliedAtDesc(TEST_STUDENT_EMAIL);
        assertEquals(1, apps.size());

        List<Interview> interviews = interviewRepository.findByCandidateEmailOrderByInterviewDateDesc(TEST_STUDENT_EMAIL);
        assertEquals(1, interviews.size());

        Optional<ResumeRecord> resume = resumeRecordRepository.findFirstByUserEmailOrderByUploadedAtDesc(TEST_STUDENT_EMAIL);
        assertTrue(resume.isPresent());
    }

    @Test
    @Order(11)
    public void testProfileUpdateInPlaceAndPublishJobInAiMatch() {
        long profileCountBefore = profileRepository.count();

        // 1. Update existing profile (must NOT create duplicate record)
        UserProfile profile = profileRepository.findByEmail(TEST_STUDENT_EMAIL).orElseThrow();
        profile.setSpecialization("Cloud & Distributed Systems");
        profile.setPreferredRole("Principal Java Architect");
        profile.setSkills("Java 17, Spring Boot 3, MySQL 8, Docker, Kubernetes, AWS");
        profileRepository.save(profile);

        long profileCountAfter = profileRepository.count();
        assertEquals(profileCountBefore, profileCountAfter, "Updating profile must NOT create duplicate records in MySQL");

        UserProfile reloaded = profileRepository.findByEmail(TEST_STUDENT_EMAIL).orElseThrow();
        assertEquals("Cloud & Distributed Systems", reloaded.getSpecialization());
        assertEquals("Principal Java Architect", reloaded.getPreferredRole());
        assertEquals("NIT Trichy", reloaded.getCollege(), "Existing fields like college must be retained");
        assertNotNull(reloaded.getImageUrl(), "Existing profile photo must be retained");

        // 2. Recruiter posts a second job with new skills
        Job job2 = new Job();
        job2.setTitle("Cloud Backend Architect");
        job2.setCompany("Nexus Innovations");
        job2.setLocation("Bengaluru");
        job2.setType("Full Time");
        job2.setWorkMode("Remote");
        job2.setExperience("3-5 Years");
        job2.setSalary("₹25-35 LPA");
        job2.setSkills("Java, Spring Boot, Kubernetes, AWS");
        job2.setDescription("Lead backend architecture for cloud-native microservices.");
        job2.setRecruiterEmail(TEST_RECRUITER_EMAIL);
        job2.setActive(true);
        job2.setCreatedAt(LocalDateTime.now());

        Job savedJob2 = jobRepository.save(job2);
        assertNotNull(savedJob2.getId());

        // 3. Confirm newly published job is visible in Browse Jobs
        List<Job> activeJobs = jobRepository.findByActiveTrueOrderByCreatedAtDesc();
        assertTrue(activeJobs.stream().anyMatch(j -> j.getId().equals(savedJob2.getId())), "Newly published job must appear in Browse Jobs");

        // 4. Confirm newly published job is available in AI Job Match
        List<JobMatchDto> matches = aiJobMatcherService.matchJobsForUser(TEST_STUDENT_EMAIL);
        assertTrue(matches.stream().anyMatch(m -> m.getJob().getId().equals(savedJob2.getId())), "Newly published job must appear in AI Job Match");
    }
}