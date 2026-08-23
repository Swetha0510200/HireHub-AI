package com.hirehub.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import jakarta.servlet.http.HttpSession;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.hirehub.dto.JobMatchDto;
import com.hirehub.model.Interview;
import com.hirehub.model.Job;
import com.hirehub.model.JobApplication;
import com.hirehub.model.Notification;
import com.hirehub.model.ResumeRecord;
import com.hirehub.model.UserProfile;
import com.hirehub.repository.InterviewRepository;
import com.hirehub.repository.JobApplicationRepository;
import com.hirehub.repository.JobRepository;
import com.hirehub.repository.NotificationRepository;
import com.hirehub.repository.ResumeRecordRepository;
import com.hirehub.repository.SavedJobRepository;
import com.hirehub.repository.UserProfileRepository;
import com.hirehub.service.AiJobMatcherService;

@Controller
public class DashboardController {

    private final ResumeRecordRepository resumeRepository;
    private final JobRepository jobRepository;
    private final JobApplicationRepository applicationRepository;
    private final SavedJobRepository savedJobRepository;
    private final UserProfileRepository profileRepository;
    private final InterviewRepository interviewRepository;
    private final NotificationRepository notificationRepository;
    private final AiJobMatcherService aiJobMatcherService;

    public DashboardController(
            ResumeRecordRepository resumeRepository,
            JobRepository jobRepository,
            JobApplicationRepository applicationRepository,
            SavedJobRepository savedJobRepository,
            UserProfileRepository profileRepository,
            InterviewRepository interviewRepository,
            NotificationRepository notificationRepository,
            AiJobMatcherService aiJobMatcherService) {
        this.resumeRepository = resumeRepository;
        this.jobRepository = jobRepository;
        this.applicationRepository = applicationRepository;
        this.savedJobRepository = savedJobRepository;
        this.profileRepository = profileRepository;
        this.interviewRepository = interviewRepository;
        this.notificationRepository = notificationRepository;
        this.aiJobMatcherService = aiJobMatcherService;
    }

    @GetMapping({"/dashboard", "/candidate/dashboard", "/candidate"})
    public String dashboard(HttpSession session, Model model) {
        String email = (String) session.getAttribute("userEmail");
        if (email == null) return "redirect:/login";

        String role = (String) session.getAttribute("userRole");
        if ("Recruiter".equalsIgnoreCase(role)) return "redirect:/recruiter/dashboard";
        if ("Admin".equalsIgnoreCase(role)) return "redirect:/admin/dashboard";

        model.addAttribute("userEmail", email);
        model.addAttribute("userRole", role);
        model.addAttribute("userName", session.getAttribute("userName"));

        // 1. Resume & ATS Data
        ResumeRecord resume = resumeRepository
                .findFirstByUserEmailOrderByUploadedAtDesc(email)
                .orElse(null);

        int atsScore = (resume == null || resume.getAtsScore() == null) ? 0 : resume.getAtsScore();
        model.addAttribute("atsScore", atsScore);
        model.addAttribute("resumeQuality", resume == null ? "Not Analyzed" : resume.getResumeQuality());
        model.addAttribute("summary", resume == null ? null : resume.getSummary());
        model.addAttribute("skillCount", resume == null || resume.getSkillCount() == null ? 0 : resume.getSkillCount());
        model.addAttribute("skills", resume == null ? Collections.emptyList() : split(resume.getSkills()));
        model.addAttribute("improvements", resume == null ? Collections.emptyList() : split(resume.getImprovements()));

        // 2. Real Counts
        long applicationsCount = applicationRepository.countByApplicantEmail(email);
        long pendingCount = applicationRepository.countByApplicantEmailAndStatusIgnoreCase(email, "Applied") +
                            applicationRepository.countByApplicantEmailAndStatusIgnoreCase(email, "Under Review");
        long shortlistedCount = applicationRepository.countByApplicantEmailAndStatusIgnoreCase(email, "Shortlisted");
        long interviewCount = applicationRepository.countByApplicantEmailAndStatusIgnoreCase(email, "Interview");
        long selectedCount = applicationRepository.countByApplicantEmailAndStatusIgnoreCase(email, "Selected");
        long rejectedCount = applicationRepository.countByApplicantEmailAndStatusIgnoreCase(email, "Rejected");
        long savedCount = savedJobRepository.findByUserEmailOrderBySavedAtDesc(email).size();
        long unreadNotifications = notificationRepository.countByRecipientEmailAndReadFalse(email);

        model.addAttribute("applicationsCount", applicationsCount);
        model.addAttribute("totalApplications", applicationsCount);
        model.addAttribute("pendingApplicationsCount", pendingCount);
        model.addAttribute("shortlistedApplicationsCount", shortlistedCount);
        model.addAttribute("interviewCount", interviewCount);
        model.addAttribute("selectedApplicationsCount", selectedCount);
        model.addAttribute("rejectedApplicationsCount", rejectedCount);
        model.addAttribute("savedJobCount", savedCount);
        model.addAttribute("savedCount", savedCount);
        model.addAttribute("unreadNotificationCount", unreadNotifications);
        model.addAttribute("notificationCount", unreadNotifications);

        // 3. AI Job Match Score & Ranked Recommendations
        List<JobMatchDto> matchedJobs = aiJobMatcherService.matchJobsForUser(email);
        int bestMatchScore = matchedJobs.isEmpty() ? 0 : matchedJobs.get(0).getMatchScore();
        model.addAttribute("jobMatchScore", bestMatchScore);
        model.addAttribute("matchedJobs", matchedJobs.stream().limit(6).collect(Collectors.toList()));

        List<Job> recommendedJobs = matchedJobs.stream()
                .limit(6)
                .map(JobMatchDto::getJob)
                .collect(Collectors.toList());
        model.addAttribute("recommendedJobs", recommendedJobs);

        // 4. Recent Applications
        List<JobApplication> recentApplications = applicationRepository
                .findByApplicantEmailOrderByAppliedAtDesc(email);
        model.addAttribute("recentApplications", recentApplications.stream().limit(5).toList());

        // 5. Upcoming Interview
        List<Interview> candidateInterviews = interviewRepository
                .findByCandidateEmailOrderByInterviewDateDesc(email);
        Interview upcomingInterview = candidateInterviews.isEmpty() ? null : candidateInterviews.get(0);
        model.addAttribute("upcomingInterview", upcomingInterview);

        // 6. User Profile & Completion Calculation
        UserProfile profile = profileRepository.findByEmail(email).orElse(null);
        String profileTitle = profile == null ? "Job Seeker" : (profile.getCurrentTitle() != null ? profile.getCurrentTitle() : profile.getPreferredRole());
        if (profileTitle == null || profileTitle.isBlank()) profileTitle = "Software Engineering Candidate";

        int completion = calculateProfileCompletion(profile, resume);

        // 7. Notifications
        List<Notification> userNotifications = notificationRepository.findByRecipientEmailOrderByCreatedAtDesc(email);
        model.addAttribute("notifications", userNotifications.stream().limit(5).map(Notification::getMessage).toList());

        // 8. Skill score map for UI bars
        Map<String, Integer> skillScores = new HashMap<>();
        if (resume != null && resume.getSkills() != null) {
            int baseScore = (resume.getAtsScore() != null ? resume.getAtsScore() : 80);
            int idx = 0;
            for (String sk : split(resume.getSkills())) {
                int sc = Math.max(50, Math.min(98, baseScore - (idx * 3)));
                skillScores.put(sk, sc);
                idx++;
            }
        }
        model.addAttribute("skillScores", skillScores);

        // 9. Recent Platform Activities
        List<String> activities = new ArrayList<>();
        if (resume != null) activities.add("Resume analyzed: ATS score " + atsScore + "% (" + resume.getResumeQuality() + ")");
        if (applicationsCount > 0) activities.add(applicationsCount + " live application(s) actively tracking");
        if (interviewCount > 0) activities.add(interviewCount + " interview(s) scheduled on your calendar");
        if (savedCount > 0) activities.add(savedCount + " opportunity(s) bookmarked in saved jobs");
        if (profile != null && profile.getSkills() != null) activities.add("Profile skills updated with " + profile.getSkills().split(",").length + " competencies");

        if (activities.isEmpty()) {
            activities.add("Welcome to HireHub AI! Upload your resume to unlock real-time ATS scoring & AI Job Matching.");
        }

        model.addAttribute("profileTitle", profileTitle);
        model.addAttribute("profileImage", profile == null ? null : profile.getImageUrl());
        model.addAttribute("userInitials", profile == null ? "U" : profile.getInitials());
        model.addAttribute("profileCompletion", completion);
        model.addAttribute("recentActivities", activities);

        return "dashboard";
    }

    @GetMapping("/api/dashboard/summary")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getDashboardSummary(HttpSession session) {
        String email = (String) session.getAttribute("userEmail");
        Map<String, Object> summary = new HashMap<>();
        if (email == null) {
            summary.put("status", "offline");
            return ResponseEntity.ok(summary);
        }

        ResumeRecord resume = resumeRepository.findFirstByUserEmailOrderByUploadedAtDesc(email).orElse(null);
        long appCount = applicationRepository.countByApplicantEmail(email);
        long ivCount = interviewRepository.countByCandidateEmail(email);

        summary.put("status", "online");
        summary.put("atsScore", resume != null && resume.getAtsScore() != null ? resume.getAtsScore() : 0);
        summary.put("applicationsCount", appCount);
        summary.put("interviewCount", ivCount);

        return ResponseEntity.ok(summary);
    }

    private List<String> split(String value) {
        if (value == null || value.isBlank()) return List.of();
        return Arrays.stream(value.split("\\|\\|"))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .toList();
    }

    private int calculateProfileCompletion(UserProfile profile, ResumeRecord resume) {
        if (profile == null && resume == null) return 10;
        int total = 10;
        if (profile != null) {
            if (profile.getName() != null && !profile.getName().isBlank()) total += 15;
            if (profile.getLocation() != null && !profile.getLocation().isBlank()) total += 10;
            if (profile.getSkills() != null && !profile.getSkills().isBlank()) total += 15;
            if (profile.getPreferredRole() != null && !profile.getPreferredRole().isBlank()) total += 10;
            if (profile.getCollege() != null && !profile.getCollege().isBlank()) total += 10;
            if (profile.getDegree() != null && !profile.getDegree().isBlank()) total += 10;
            if (profile.getLinkedIn() != null && !profile.getLinkedIn().isBlank()) total += 5;
            if (profile.getGithub() != null && !profile.getGithub().isBlank()) total += 5;
        }
        if (resume != null) total += 10;
        return Math.min(total, 100);
    }
}
