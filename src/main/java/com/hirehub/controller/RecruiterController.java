package com.hirehub.controller;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.hirehub.model.Company;
import com.hirehub.model.Interview;
import com.hirehub.model.Job;
import com.hirehub.model.JobApplication;
import com.hirehub.model.UserProfile;
import com.hirehub.repository.CompanyRepository;
import com.hirehub.repository.InterviewRepository;
import com.hirehub.repository.JobApplicationRepository;
import com.hirehub.repository.JobRepository;
import com.hirehub.repository.UserProfileRepository;
import com.hirehub.service.NotificationService;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/recruiter")
public class RecruiterController {

    private final JobRepository jobRepository;
    private final JobApplicationRepository applicationRepository;
    private final InterviewRepository interviewRepository;
    private final CompanyRepository companyRepository;
    private final UserProfileRepository profileRepository;
    private final NotificationService notificationService;

    public RecruiterController(
            JobRepository jobRepository,
            JobApplicationRepository applicationRepository,
            InterviewRepository interviewRepository,
            CompanyRepository companyRepository,
            UserProfileRepository profileRepository,
            NotificationService notificationService) {
        this.jobRepository = jobRepository;
        this.applicationRepository = applicationRepository;
        this.interviewRepository = interviewRepository;
        this.companyRepository = companyRepository;
        this.profileRepository = profileRepository;
        this.notificationService = notificationService;
    }

    // =========================================================
    // 1. RECRUITER DASHBOARD
    // =========================================================

    @GetMapping("/dashboard")
    public String recruiterDashboard(HttpSession session, Model model) {
        String email = (String) session.getAttribute("userEmail");
        if (email == null) return "redirect:/login";

        long totalJobs = jobRepository.countByRecruiterEmail(email);
        long activeJobs = jobRepository.countByRecruiterEmailAndActiveTrue(email);
        long totalApplicants = applicationRepository.countByJobRecruiterEmail(email);
        long shortlisted = applicationRepository.countByJobRecruiterEmailAndStatusIgnoreCase(email, "Shortlisted");
        long interviewCount = interviewRepository.countByRecruiterEmail(email);
        long selected = applicationRepository.countByJobRecruiterEmailAndStatusIgnoreCase(email, "Selected");

        List<Job> recruiterJobs = jobRepository.findByRecruiterEmailOrderByCreatedAtDesc(email);
        List<JobApplication> recentApplicants = applicationRepository.findByJobRecruiterEmailOrderByAppliedAtDesc(email);
        List<Interview> upcomingInterviews = interviewRepository.findByRecruiterEmailOrderByInterviewDateDesc(email);

        UserProfile profile = profileRepository.findByEmail(email).orElse(null);
        Company company = companyRepository.findFirstByRecruiterEmailOrderByCreatedAtDesc(email).orElse(null);

        model.addAttribute("totalJobs", totalJobs);
        model.addAttribute("activeJobs", activeJobs);
        model.addAttribute("totalApplicants", totalApplicants);
        model.addAttribute("shortlistedCount", shortlisted);
        model.addAttribute("interviewCount", interviewCount);
        model.addAttribute("selectedCount", selected);

        model.addAttribute("jobs", recruiterJobs);
        model.addAttribute("recentApplicants", recentApplicants.stream().limit(6).toList());
        model.addAttribute("upcomingInterviews", upcomingInterviews.stream().limit(4).toList());
        model.addAttribute("company", company);
        model.addAttribute("profile", profile);

        model.addAttribute("userEmail", email);
        model.addAttribute("userName", session.getAttribute("userName"));
        model.addAttribute("userRole", session.getAttribute("userRole"));

        return "recruiter-dashboard";
    }

    // =========================================================
    // 2. MANAGE POSTED JOBS
    // =========================================================

    @GetMapping("/jobs")
    public String manageJobs(HttpSession session, Model model) {
        String email = (String) session.getAttribute("userEmail");
        if (email == null) return "redirect:/login";

        List<Job> jobs = jobRepository.findByRecruiterEmailOrderByCreatedAtDesc(email);

        model.addAttribute("jobs", jobs);
        model.addAttribute("totalJobs", jobs.size());
        model.addAttribute("activeJobs", jobs.stream().filter(Job::isActive).count());
        model.addAttribute("userName", session.getAttribute("userName"));
        model.addAttribute("userRole", session.getAttribute("userRole"));

        return "recruiter-jobs";
    }

    @PostMapping("/jobs/{id}/toggle-status")
    public String toggleJobStatus(@PathVariable Long id, HttpSession session, RedirectAttributes redirectAttributes) {
        String email = (String) session.getAttribute("userEmail");
        if (email == null) return "redirect:/login";

        Job job = jobRepository.findById(id).orElse(null);
        if (job != null && email.equalsIgnoreCase(job.getRecruiterEmail())) {
            job.setActive(!job.isActive());
            jobRepository.save(job);
            redirectAttributes.addFlashAttribute("successMessage", "Job status updated to " + (job.isActive() ? "Active" : "Inactive") + ".");
        }
        return "redirect:/recruiter/jobs";
    }

    @PostMapping("/jobs/{id}/delete")
    public String deleteJob(@PathVariable Long id, HttpSession session, RedirectAttributes redirectAttributes) {
        String email = (String) session.getAttribute("userEmail");
        if (email == null) return "redirect:/login";

        Job job = jobRepository.findById(id).orElse(null);
        if (job != null && email.equalsIgnoreCase(job.getRecruiterEmail())) {
            jobRepository.delete(job);
            redirectAttributes.addFlashAttribute("successMessage", "Job posting removed successfully.");
        }
        return "redirect:/recruiter/jobs";
    }

    // =========================================================
    // 3. APPLICANT MANAGEMENT (/recruiter/applicants & /recruiter/applications)
    // =========================================================

    @GetMapping({"/applicants", "/applications"})
    public String viewAllApplicants(
            @RequestParam(required = false) Long jobId,
            HttpSession session,
            Model model) {

        String email = (String) session.getAttribute("userEmail");
        if (email == null) return "redirect:/login";

        List<JobApplication> applications;
        if (jobId != null) {
            applications = applicationRepository.findByJobIdOrderByAppliedAtDesc(jobId);
            jobRepository.findById(jobId).ifPresent(j -> model.addAttribute("selectedJob", j));
        } else {
            applications = applicationRepository.findByJobRecruiterEmailOrderByAppliedAtDesc(email);
        }

        List<Job> recruiterJobs = jobRepository.findByRecruiterEmailOrderByCreatedAtDesc(email);

        model.addAttribute("applications", applications);
        model.addAttribute("recruiterJobs", recruiterJobs);
        model.addAttribute("selectedJobId", jobId);
        model.addAttribute("userName", session.getAttribute("userName"));
        model.addAttribute("userRole", session.getAttribute("userRole"));

        return "recruiter-applicants";
    }

    @GetMapping("/applications/{id}")
    public String viewApplicantDetail(
            @PathVariable Long id,
            HttpSession session,
            Model model,
            RedirectAttributes redirectAttributes) {

        String email = (String) session.getAttribute("userEmail");
        if (email == null) return "redirect:/login";

        JobApplication application = applicationRepository.findById(id).orElse(null);
        if (application == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Application not found.");
            return "redirect:/recruiter/applicants";
        }

        UserProfile candidateProfile = profileRepository.findByEmail(application.getApplicantEmail()).orElse(null);

        model.addAttribute("application", application);
        model.addAttribute("candidateProfile", candidateProfile);
        model.addAttribute("job", application.getJob());
        model.addAttribute("userName", session.getAttribute("userName"));
        model.addAttribute("userRole", session.getAttribute("userRole"));

        return "candidate-view";
    }

    @GetMapping("/candidate/{candidateEmail}")
    public String viewCandidateProfile(
            @PathVariable String candidateEmail,
            HttpSession session,
            Model model,
            RedirectAttributes redirectAttributes) {

        String email = (String) session.getAttribute("userEmail");
        if (email == null) return "redirect:/login";

        UserProfile profile = profileRepository.findByEmail(candidateEmail.trim().toLowerCase()).orElse(null);
        if (profile == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Candidate profile not found.");
            return "redirect:/recruiter/applicants";
        }

        List<JobApplication> candidateApps = applicationRepository.findByApplicantEmailOrderByAppliedAtDesc(candidateEmail);

        model.addAttribute("candidateProfile", profile);
        model.addAttribute("candidateApps", candidateApps);
        model.addAttribute("userName", session.getAttribute("userName"));
        model.addAttribute("userRole", session.getAttribute("userRole"));

        return "candidate-view";
    }

    @GetMapping("/internships")
    public String manageInternships(HttpSession session, Model model) {
        String email = (String) session.getAttribute("userEmail");
        if (email == null) return "redirect:/login";

        List<Job> internships = jobRepository.findByRecruiterEmailAndTypeIgnoreCaseOrderByCreatedAtDesc(email, "Internship");

        model.addAttribute("internships", internships);
        model.addAttribute("jobs", internships);
        model.addAttribute("totalInternships", internships.size());
        model.addAttribute("activeInternships", internships.stream().filter(Job::isActive).count());
        model.addAttribute("userName", session.getAttribute("userName"));
        model.addAttribute("userRole", session.getAttribute("userRole"));

        return "recruiter-jobs";
    }

    @GetMapping("/jobs/{id}/edit")
    public String editJobForm(@PathVariable Long id, HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        String email = (String) session.getAttribute("userEmail");
        if (email == null) return "redirect:/login";

        Job job = jobRepository.findById(id).orElse(null);
        if (job == null || (!email.equalsIgnoreCase(job.getRecruiterEmail()) && !"Admin".equalsIgnoreCase((String) session.getAttribute("userRole")))) {
            redirectAttributes.addFlashAttribute("errorMessage", "Job not found or unauthorized.");
            return "redirect:/recruiter/jobs";
        }

        model.addAttribute("job", job);
        model.addAttribute("userEmail", email);
        model.addAttribute("userName", session.getAttribute("userName"));
        model.addAttribute("userRole", session.getAttribute("userRole"));

        return "post-job";
    }

    @PostMapping("/jobs/{id}/edit")
    public String updateJob(
            @PathVariable Long id,
            @RequestParam String title,
            @RequestParam String company,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String workMode,
            @RequestParam(required = false) String experience,
            @RequestParam(required = false) String salary,
            @RequestParam(required = false) String skills,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) String responsibilities,
            @RequestParam(required = false) String requirements,
            @RequestParam(required = false) String benefits,
            @RequestParam(required = false) String deadline,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        String email = (String) session.getAttribute("userEmail");
        if (email == null) return "redirect:/login";

        Job job = jobRepository.findById(id).orElse(null);
        if (job != null && (email.equalsIgnoreCase(job.getRecruiterEmail()) || "Admin".equalsIgnoreCase((String) session.getAttribute("userRole")))) {
            job.setTitle(title.trim());
            job.setCompany(company.trim());
            if (category != null) job.setCategory(category.trim());
            if (location != null) job.setLocation(location.trim());
            if (type != null) job.setType(type.trim());
            if (workMode != null) job.setWorkMode(workMode.trim());
            if (experience != null) job.setExperience(experience.trim());
            if (salary != null) job.setSalary(salary.trim());
            if (skills != null) job.setSkills(skills.trim());
            if (description != null) job.setDescription(description.trim());
            if (responsibilities != null) job.setResponsibilities(responsibilities.trim());
            if (requirements != null) job.setRequirements(requirements.trim());
            if (benefits != null) job.setBenefits(benefits.trim());
            if (deadline != null) job.setDeadline(deadline.trim());

            jobRepository.save(job);
            redirectAttributes.addFlashAttribute("successMessage", "Posting updated successfully.");
        }

        return "redirect:/recruiter/jobs";
    }

    @PostMapping("/applications/{id}/status")
    public String updateApplicationStatus(
            @PathVariable Long id,
            @RequestParam String status,
            @RequestParam(required = false) String remarks,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        String email = (String) session.getAttribute("userEmail");
        if (email == null) return "redirect:/login";

        JobApplication application = applicationRepository.findById(id).orElse(null);
        if (application != null && (email.equalsIgnoreCase(application.getJob().getRecruiterEmail()) || "Admin".equalsIgnoreCase((String) session.getAttribute("userRole")))) {
            application.setStatus(status);
            if (remarks != null && !remarks.isBlank()) {
                application.setRecruiterRemarks(remarks.trim());
            }
            applicationRepository.save(application);

            // Notify candidate
            notificationService.sendNotification(
                    application.getApplicantEmail(),
                    "Application Status: " + status,
                    "Your application for " + application.getJob().getTitle() + " at " + application.getJob().getCompany() + " has been updated to: " + status + (remarks != null && !remarks.isBlank() ? " (" + remarks + ")" : ""),
                    "STATUS",
                    "/applications"
            );

            redirectAttributes.addFlashAttribute("successMessage", "Candidate status updated to " + status + ".");
        }
        return "redirect:/recruiter/applicants";
    }

    @GetMapping("/applications/{id}/resume")
    public ResponseEntity<byte[]> downloadApplicantResume(
            @PathVariable Long id,
            HttpSession session) {

        String email = (String) session.getAttribute("userEmail");
        if (email == null) return ResponseEntity.status(302).header(HttpHeaders.LOCATION, "/login").build();

        JobApplication app = applicationRepository.findById(id).orElse(null);
        if (app == null || app.getResumeData() == null) {
            return ResponseEntity.notFound().build();
        }

        MediaType mediaType;
        try {
            mediaType = MediaType.parseMediaType(app.getResumeContentType());
        } catch (Exception e) {
            mediaType = MediaType.APPLICATION_OCTET_STREAM;
        }

        String fileName = app.getResumeFileName() != null ? app.getResumeFileName() : "candidate_resume.pdf";

        return ResponseEntity.ok()
                .contentType(mediaType)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                .body(app.getResumeData());
    }

    // =========================================================
    // 4. SCHEDULE INTERVIEW
    // =========================================================

    @PostMapping("/schedule-interview")
    public String scheduleInterview(
            @RequestParam Long applicationId,
            @RequestParam String interviewDate,
            @RequestParam String interviewTime,
            @RequestParam String interviewType,
            @RequestParam(required = false) String meetingUrl,
            @RequestParam(required = false) String notes,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        String email = (String) session.getAttribute("userEmail");
        if (email == null) return "redirect:/login";

        JobApplication application = applicationRepository.findById(applicationId).orElse(null);
        if (application == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Application not found.");
            return "redirect:/recruiter/applicants";
        }

        Interview interview = new Interview();
        interview.setCandidateEmail(application.getApplicantEmail());
        interview.setCandidateName(application.getApplicantName());
        interview.setRecruiterEmail(email);
        interview.setJob(application.getJob());
        interview.setApplication(application);
        interview.setCompany(application.getJob().getCompany());
        interview.setRole(application.getJob().getTitle());
        interview.setInterviewDate(interviewDate);
        interview.setInterviewTime(interviewTime);
        interview.setInterviewType(interviewType);
        interview.setMeetingUrl(meetingUrl != null && !meetingUrl.isBlank() ? meetingUrl : "https://meet.google.com/hirehub-room");
        interview.setNotes(notes);
        interview.setStatus("Scheduled");

        interviewRepository.save(interview);

        // Update application status to Interview
        application.setStatus("Interview");
        applicationRepository.save(application);

        // Notify candidate
        notificationService.sendNotification(
                application.getApplicantEmail(),
                "Interview Scheduled!",
                application.getJob().getCompany() + " scheduled your " + interviewType + " on " + interviewDate + " at " + interviewTime + ".",
                "INTERVIEW",
                "/interviews"
        );

        redirectAttributes.addFlashAttribute("successMessage", "Interview scheduled successfully with " + application.getApplicantName() + "!");
        return "redirect:/recruiter/applicants";
    }
}
