package com.hirehub.controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.hirehub.dto.JobMatchDto;
import com.hirehub.model.Job;
import com.hirehub.model.JobApplication;
import com.hirehub.model.ResumeRecord;
import com.hirehub.model.SavedJob;
import com.hirehub.model.UserAccount;
import com.hirehub.model.UserProfile;
import com.hirehub.repository.JobApplicationRepository;
import com.hirehub.repository.JobRepository;
import com.hirehub.repository.ResumeRecordRepository;
import com.hirehub.repository.SavedJobRepository;
import com.hirehub.repository.UserAccountRepository;
import com.hirehub.repository.UserProfileRepository;
import com.hirehub.service.AiJobMatcherService;
import com.hirehub.service.NotificationService;
import com.hirehub.service.ResumeAnalyzerService;

import jakarta.servlet.http.HttpSession;

@Controller
public class JobActionController {

    private final JobRepository jobRepository;
    private final JobApplicationRepository applicationRepository;
    private final SavedJobRepository savedJobRepository;
    private final UserProfileRepository profileRepository;
    private final UserAccountRepository accountRepository;
    private final ResumeRecordRepository resumeRepository;
    private final ResumeAnalyzerService resumeAnalyzerService;
    private final AiJobMatcherService aiJobMatcherService;
    private final NotificationService notificationService;

    public JobActionController(
            JobRepository jobRepository,
            JobApplicationRepository applicationRepository,
            SavedJobRepository savedJobRepository,
            UserProfileRepository profileRepository,
            UserAccountRepository accountRepository,
            ResumeRecordRepository resumeRepository,
            ResumeAnalyzerService resumeAnalyzerService,
            AiJobMatcherService aiJobMatcherService,
            NotificationService notificationService) {
        this.jobRepository = jobRepository;
        this.applicationRepository = applicationRepository;
        this.savedJobRepository = savedJobRepository;
        this.profileRepository = profileRepository;
        this.accountRepository = accountRepository;
        this.resumeRepository = resumeRepository;
        this.resumeAnalyzerService = resumeAnalyzerService;
        this.aiJobMatcherService = aiJobMatcherService;
        this.notificationService = notificationService;
    }

    // =========================================================
    // 1. APPLICATION FORM (GET /apply/{jobId})
    // =========================================================

    @GetMapping("/apply/{jobId}")
    public String showApplicationForm(@PathVariable Long jobId, HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        String email = (String) session.getAttribute("userEmail");
        if (email == null) return "redirect:/login";

        Job job = jobRepository.findById(jobId).orElse(null);
        if (job == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Job not found.");
            return "redirect:/browse-jobs";
        }

        if (applicationRepository.existsByJobIdAndApplicantEmail(jobId, email)) {
            redirectAttributes.addFlashAttribute("infoMessage", "You have already applied for this position.");
            return "redirect:/applications";
        }

        UserProfile profile = profileRepository.findByEmail(email).orElse(new UserProfile());
        UserAccount account = accountRepository.findById(email).orElse(null);
        ResumeRecord latestResume = resumeRepository.findFirstByUserEmailOrderByUploadedAtDesc(email).orElse(null);

        model.addAttribute("job", job);
        model.addAttribute("profile", profile);
        model.addAttribute("account", account);
        model.addAttribute("latestResume", latestResume);
        model.addAttribute("userName", session.getAttribute("userName"));
        model.addAttribute("userRole", session.getAttribute("userRole"));

        return "apply";
    }

    // =========================================================
    // 2. SUBMIT APPLICATION (POST /apply/{jobId})
    // =========================================================

    @PostMapping({"/apply/{jobId}", "/jobs/{jobId}/apply"})
    public String submitApplication(
            @PathVariable Long jobId,
            @RequestParam(required = false) String applicantName,
            @RequestParam(required = false) String applicantPhone,
            @RequestParam(required = false) String coverLetter,
            @RequestParam(required = false) String degree,
            @RequestParam(required = false) String university,
            @RequestParam(required = false) Integer graduationYear,
            @RequestParam(required = false) String skills,
            @RequestParam(required = false) String experience,
            @RequestParam(required = false) String linkedIn,
            @RequestParam(required = false) String github,
            @RequestParam(required = false) String portfolio,
            @RequestParam(name = "resumeFile", required = false) MultipartFile resumeFile,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        String email = (String) session.getAttribute("userEmail");
        if (email == null) return "redirect:/login";

        Job job = jobRepository.findById(jobId).orElse(null);
        if (job == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Job not found.");
            return "redirect:/browse-jobs";
        }

        if (applicationRepository.existsByJobIdAndApplicantEmail(jobId, email)) {
            redirectAttributes.addFlashAttribute("infoMessage", "You have already applied for this job.");
            return "redirect:/applications";
        }

        UserProfile profile = profileRepository.findByEmail(email).orElse(null);

        JobApplication application = new JobApplication();
        application.setJob(job);
        application.setApplicantEmail(email);
        application.setApplicantName(applicantName != null && !applicantName.isBlank() ? applicantName : (String) session.getAttribute("userName"));
        application.setApplicantPhone(applicantPhone);
        application.setCoverLetter(coverLetter);
        application.setDegree(degree != null && !degree.isBlank() ? degree : (profile != null ? profile.getDegree() : null));
        application.setUniversity(university != null && !university.isBlank() ? university : (profile != null ? profile.getCollege() : null));
        application.setGraduationYear(graduationYear != null ? graduationYear : (profile != null ? profile.getGraduationYear() : null));
        application.setSkills(skills != null && !skills.isBlank() ? skills : (profile != null ? profile.getSkills() : null));
        application.setExperience(experience != null && !experience.isBlank() ? experience : (profile != null ? profile.getExperienceLevel() : null));
        application.setLinkedIn(linkedIn != null && !linkedIn.isBlank() ? linkedIn : (profile != null ? profile.getLinkedIn() : null));
        application.setGithub(github != null && !github.isBlank() ? github : (profile != null ? profile.getGithub() : null));
        application.setPortfolio(portfolio != null && !portfolio.isBlank() ? portfolio : (profile != null ? profile.getPortfolio() : null));
        application.setStatus("Applied");

        // Handle Resume Upload
        if (resumeFile != null && !resumeFile.isEmpty()) {
            try {
                application.setResumeFileName(resumeFile.getOriginalFilename());
                application.setResumeContentType(resumeFile.getContentType());
                application.setResumeData(resumeFile.getBytes());

                // Also analyze and store in resume records
                String text = resumeAnalyzerService.extractText(resumeFile);
                if (text != null && !text.isBlank()) {
                    Map<String, Object> res = resumeAnalyzerService.analyzeResume(text);
                    ResumeRecord rec = new ResumeRecord();
                    rec.setUserEmail(email);
                    rec.setFileName(resumeFile.getOriginalFilename());
                    rec.setContentType(resumeFile.getContentType());
                    rec.setFileData(resumeFile.getBytes());
                    rec.setAtsScore((Integer) res.get("atsScore"));
                    rec.setResumeQuality((String) res.get("resumeQuality"));
                    rec.setSummary((String) res.get("summary"));
                    rec.setSkills(String.join("||", (List<String>) res.get("skills")));
                    rec.setStrengths(String.join("||", (List<String>) res.get("strengths")));
                    rec.setImprovements(String.join("||", (List<String>) res.get("improvements")));
                    rec.setWordCount((Integer) res.get("wordCount"));
                    rec.setSkillCount((Integer) res.get("skillCount"));
                    rec.setLineCount((Integer) res.get("lineCount"));
                    rec.setSectionScore((Integer) res.get("sectionScore"));
                    rec.setSkillScore((Integer) res.get("skillScore"));
                    rec.setAchievementScore((Integer) res.get("achievementScore"));
                    rec.setLengthScore((Integer) res.get("lengthScore"));
                    rec.setAtsStructureScore((Integer) res.get("atsStructureScore"));
                    resumeRepository.save(rec);

                    application.setMatchScore((Integer) res.get("atsScore"));
                }
            } catch (Exception e) {
                // Resume parsing shouldn't crash application submission
            }
        } else {
            // Attach existing resume from profile if available
            resumeRepository.findFirstByUserEmailOrderByUploadedAtDesc(email).ifPresent(rec -> {
                application.setResumeFileName(rec.getFileName());
                application.setResumeContentType(rec.getContentType());
                application.setResumeData(rec.getFileData());
                application.setMatchScore(rec.getAtsScore());
            });
        }

        if (application.getMatchScore() == null) {
            List<JobMatchDto> matches = aiJobMatcherService.matchJobsForUser(email);
            int calculatedScore = matches.stream()
                    .filter(m -> m.getJob() != null && m.getJob().getId() != null && m.getJob().getId().equals(job.getId()))
                    .findFirst()
                    .map(JobMatchDto::getMatchScore)
                    .orElse(70);
            application.setMatchScore(calculatedScore);
        }

        applicationRepository.save(application);

        // Send real notifications
        notificationService.sendNotification(email, "Application Submitted",
                "Your application for " + job.getTitle() + " at " + job.getCompany() + " has been successfully submitted.", "APPLICATION", "/applications");

        if (job.getRecruiterEmail() != null) {
            notificationService.sendNotification(job.getRecruiterEmail(), "New Candidate Application",
                    application.getApplicantName() + " applied for " + job.getTitle() + ".", "APPLICATION", "/recruiter/applicants");
        }

        redirectAttributes.addFlashAttribute("successMessage", "Application submitted successfully for " + job.getTitle() + "!");
        return "redirect:/applications";
    }

    // =========================================================
    // 2B. INTERNSHIP APPLICATION (POST /internships/{id}/apply)
    // =========================================================

    @PostMapping({"/internships/{id}/apply", "/apply/internship/{id}"})
    public String submitInternshipApplication(
            @PathVariable Long id,
            @RequestParam(required = false) String applicantName,
            @RequestParam(required = false) String applicantPhone,
            @RequestParam(required = false) String coverLetter,
            @RequestParam(required = false) String availability,
            @RequestParam(required = false) String college,
            @RequestParam(required = false) String university,
            @RequestParam(required = false) Integer graduationYear,
            @RequestParam(name = "resumeFile", required = false) MultipartFile resumeFile,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        String email = (String) session.getAttribute("userEmail");
        if (email == null) return "redirect:/login";

        Job internship = jobRepository.findById(id).orElse(null);
        if (internship == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Internship not found.");
            return "redirect:/internships";
        }

        if (applicationRepository.existsByJobIdAndApplicantEmail(id, email)) {
            redirectAttributes.addFlashAttribute("infoMessage", "You have already applied for this internship.");
            return "redirect:/applications";
        }

        UserProfile profile = profileRepository.findByEmail(email).orElse(null);

        JobApplication application = new JobApplication();
        application.setJob(internship);
        application.setApplicantEmail(email);
        application.setApplicantName(applicantName != null && !applicantName.isBlank() ? applicantName : (String) session.getAttribute("userName"));
        application.setApplicantPhone(applicantPhone);
        application.setCoverLetter(coverLetter);
        application.setAvailability(availability != null && !availability.isBlank() ? availability : "Immediate");
        application.setUniversity(college != null && !college.isBlank() ? college : (university != null && !university.isBlank() ? university : (profile != null ? profile.getCollege() : null)));
        application.setGraduationYear(graduationYear != null ? graduationYear : (profile != null ? profile.getGraduationYear() : null));
        application.setSkills(profile != null ? profile.getSkills() : null);
        application.setStatus("Applied");

        if (resumeFile != null && !resumeFile.isEmpty()) {
            try {
                application.setResumeFileName(resumeFile.getOriginalFilename());
                application.setResumeContentType(resumeFile.getContentType());
                application.setResumeData(resumeFile.getBytes());
            } catch (Exception ignored) {}
        } else {
            resumeRepository.findFirstByUserEmailOrderByUploadedAtDesc(email).ifPresent(rec -> {
                application.setResumeFileName(rec.getFileName());
                application.setResumeContentType(rec.getContentType());
                application.setResumeData(rec.getFileData());
                application.setMatchScore(rec.getAtsScore());
            });
        }

        if (application.getMatchScore() == null) {
            application.setMatchScore(85);
        }

        applicationRepository.save(application);

        notificationService.sendNotification(email, "Internship Application Submitted",
                "Your application for " + internship.getTitle() + " at " + internship.getCompany() + " has been submitted.", "APPLICATION", "/applications");

        if (internship.getRecruiterEmail() != null) {
            notificationService.sendNotification(internship.getRecruiterEmail(), "New Internship Applicant",
                    application.getApplicantName() + " applied for " + internship.getTitle() + ".", "APPLICATION", "/recruiter/applicants");
        }

        redirectAttributes.addFlashAttribute("successMessage", "Application submitted successfully for " + internship.getTitle() + "!");
        return "redirect:/applications";
    }

    // =========================================================
    // 3. APPLICATIONS TRACKING (/applications & /candidate/applications)
    // =========================================================

    @GetMapping({"/applications", "/candidate/applications"})
    public String viewApplications(HttpSession session, Model model) {
        String email = (String) session.getAttribute("userEmail");
        if (email == null) return "redirect:/login";

        List<JobApplication> applications = applicationRepository.findByApplicantEmailOrderByAppliedAtDesc(email);

        long appliedCount = applicationRepository.countByApplicantEmailAndStatusIgnoreCase(email, "Applied");
        long reviewCount = applicationRepository.countByApplicantEmailAndStatusIgnoreCase(email, "Under Review");
        long shortlistedCount = applicationRepository.countByApplicantEmailAndStatusIgnoreCase(email, "Shortlisted");
        long interviewCount = applicationRepository.countByApplicantEmailAndStatusIgnoreCase(email, "Interview");
        long selectedCount = applicationRepository.countByApplicantEmailAndStatusIgnoreCase(email, "Selected");
        long rejectedCount = applicationRepository.countByApplicantEmailAndStatusIgnoreCase(email, "Rejected");

        model.addAttribute("applications", applications);
        model.addAttribute("totalApplications", applications.size());
        model.addAttribute("appliedCount", appliedCount);
        model.addAttribute("reviewCount", reviewCount);
        model.addAttribute("shortlistedCount", shortlistedCount);
        model.addAttribute("interviewCount", interviewCount);
        model.addAttribute("selectedCount", selectedCount);
        model.addAttribute("rejectedCount", rejectedCount);
        model.addAttribute("userName", session.getAttribute("userName"));
        model.addAttribute("userRole", session.getAttribute("userRole"));

        return "applications";
    }

    // =========================================================
    // 4. SAVED JOBS (/saved-jobs & /candidate/saved)
    // =========================================================

    @GetMapping({"/saved-jobs", "/candidate/saved", "/saved"})
    public String savedJobs(HttpSession session, Model model) {
        String email = (String) session.getAttribute("userEmail");
        if (email == null) return "redirect:/login";

        List<SavedJob> savedJobs = savedJobRepository.findByUserEmailOrderBySavedAtDesc(email);
        List<JobMatchDto> matches = aiJobMatcherService.matchJobsForUser(email);
        int bestMatch = matches.isEmpty() ? 0 : matches.get(0).getMatchScore();

        model.addAttribute("savedJobs", savedJobs);
        model.addAttribute("savedCount", savedJobs.size());
        model.addAttribute("appliedCount", applicationRepository.countByApplicantEmail(email));
        model.addAttribute("aiMatchScore", bestMatch);
        model.addAttribute("userName", session.getAttribute("userName"));
        model.addAttribute("userRole", session.getAttribute("userRole"));

        return "saved-jobs";
    }

    @PostMapping({"/jobs/{id}/save", "/internships/{id}/save"})
    public String saveJob(@PathVariable Long id, HttpSession session, RedirectAttributes redirectAttributes) {
        String email = (String) session.getAttribute("userEmail");
        if (email == null) return "redirect:/login";

        Job job = jobRepository.findById(id).orElse(null);
        if (job == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Opportunity not found.");
            return "redirect:/browse-jobs";
        }

        if (!savedJobRepository.existsByJobIdAndUserEmail(id, email)) {
            savedJobRepository.save(new SavedJob(email, job));
            redirectAttributes.addFlashAttribute("successMessage", (job.isInternship() ? "Internship" : "Job") + " bookmarked successfully.");
        } else {
            redirectAttributes.addFlashAttribute("infoMessage", "This opportunity is already bookmarked.");
        }

        return "redirect:/saved-jobs";
    }

    @PostMapping({"/jobs/{id}/unsave", "/internships/{id}/unsave", "/saved-jobs/{id}/delete"})
    public String unsaveJob(@PathVariable Long id, HttpSession session, RedirectAttributes redirectAttributes) {
        String email = (String) session.getAttribute("userEmail");
        if (email == null) return "redirect:/login";

        savedJobRepository.findById(id).ifPresentOrElse(
                savedJobRepository::delete,
                () -> savedJobRepository.deleteByJobIdAndUserEmail(id, email)
        );

        redirectAttributes.addFlashAttribute("successMessage", "Removed from saved bookmarks.");
        return "redirect:/saved-jobs";
    }

    // =========================================================
    // 5. AI RECOMMENDATIONS (/candidate/recommendations)
    // =========================================================

    @GetMapping({"/recommendations", "/candidate/recommendations"})
    public String viewRecommendations(HttpSession session, Model model) {
        String email = (String) session.getAttribute("userEmail");
        if (email == null) return "redirect:/login";

        List<JobMatchDto> matches = aiJobMatcherService.matchJobsForUser(email);
        UserProfile profile = profileRepository.findByEmail(email).orElse(null);

        model.addAttribute("matches", matches);
        model.addAttribute("totalMatches", matches.size());
        model.addAttribute("candidateSkills", profile != null && profile.getSkills() != null ? profile.getSkills() : "Java, Python, SQL");
        model.addAttribute("userName", session.getAttribute("userName"));
        model.addAttribute("userRole", session.getAttribute("userRole"));

        return "recommendations";
    }

    // =========================================================
    // 6. RESUME DOWNLOAD (/applications/{id}/resume)
    // =========================================================

    @GetMapping("/applications/{id}/resume")
    public ResponseEntity<byte[]> downloadApplicationResume(@PathVariable Long id, HttpSession session) {
        String email = (String) session.getAttribute("userEmail");
        if (email == null) return ResponseEntity.status(401).build();

        JobApplication application = applicationRepository.findById(id).orElse(null);
        if (application == null || application.getResumeData() == null) {
            return ResponseEntity.notFound().build();
        }

        String filename = application.getResumeFileName() != null ? application.getResumeFileName() : "resume.pdf";
        String contentType = application.getResumeContentType() != null ? application.getResumeContentType() : "application/pdf";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType(contentType))
                .body(application.getResumeData());
    }
}
