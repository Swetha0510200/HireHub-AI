package com.hirehub.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.hirehub.dto.JobMatchDto;
import com.hirehub.model.Notification;
import com.hirehub.model.ResumeRecord;
import com.hirehub.model.UserAccount;
import com.hirehub.model.UserProfile;
import com.hirehub.repository.NotificationRepository;
import com.hirehub.repository.ResumeRecordRepository;
import com.hirehub.repository.UserAccountRepository;
import com.hirehub.repository.UserProfileRepository;
import com.hirehub.service.AiJobMatcherService;
import com.hirehub.service.NotificationService;
import com.hirehub.util.PasswordUtil;

import jakarta.servlet.http.HttpSession;

@Controller
public class UtilityPageController {

    private final AiJobMatcherService aiJobMatcherService;
    private final ResumeRecordRepository resumeRepository;
    private final UserAccountRepository accountRepository;
    private final UserProfileRepository profileRepository;
    private final NotificationRepository notificationRepository;
    private final NotificationService notificationService;

    public UtilityPageController(
            AiJobMatcherService aiJobMatcherService,
            ResumeRecordRepository resumeRepository,
            UserAccountRepository accountRepository,
            UserProfileRepository profileRepository,
            NotificationRepository notificationRepository,
            NotificationService notificationService) {
        this.aiJobMatcherService = aiJobMatcherService;
        this.resumeRepository = resumeRepository;
        this.accountRepository = accountRepository;
        this.profileRepository = profileRepository;
        this.notificationRepository = notificationRepository;
        this.notificationService = notificationService;
    }

    @GetMapping("/ai-job-match")
    public String aiJobMatch(HttpSession session, Model model) {
        String email = (String) session.getAttribute("userEmail");
        if (email == null) return "redirect:/login";

        List<JobMatchDto> matchedJobs = aiJobMatcherService.matchJobsForUser(email);
        ResumeRecord latestResume = resumeRepository.findFirstByUserEmailOrderByUploadedAtDesc(email).orElse(null);
        UserProfile profile = profileRepository.findByEmail(email).orElse(null);

        model.addAttribute("matchedJobs", matchedJobs);
        model.addAttribute("hasResume", latestResume != null);
        model.addAttribute("latestResume", latestResume);
        model.addAttribute("profile", profile);
        model.addAttribute("topMatchScore", matchedJobs.isEmpty() ? 0 : matchedJobs.get(0).getMatchScore());
        model.addAttribute("userName", session.getAttribute("userName"));
        model.addAttribute("userRole", session.getAttribute("userRole"));

        return "ai-job-match";
    }

    @GetMapping("/notifications")
    public String notifications(HttpSession session, Model model) {
        String email = (String) session.getAttribute("userEmail");
        if (email == null) return "redirect:/login";

        List<Notification> list = notificationService.getUserNotifications(email);
        model.addAttribute("notifications", list);
        model.addAttribute("unreadCount", notificationService.getUnreadCount(email));
        model.addAttribute("userName", session.getAttribute("userName"));
        model.addAttribute("userRole", session.getAttribute("userRole"));

        return "notifications";
    }

    @PostMapping("/notifications/mark-read")
    public String markNotificationsRead(HttpSession session) {
        String email = (String) session.getAttribute("userEmail");
        if (email == null) return "redirect:/login";

        notificationService.markAllAsRead(email);
        return "redirect:/notifications";
    }

    @GetMapping("/settings")
    public String settings(HttpSession session, Model model) {
        String email = (String) session.getAttribute("userEmail");
        if (email == null) return "redirect:/login";

        UserAccount account = accountRepository.findById(email).orElse(null);
        UserProfile profile = profileRepository.findByEmail(email).orElse(new UserProfile());

        model.addAttribute("account", account);
        model.addAttribute("profile", profile);
        model.addAttribute("userName", session.getAttribute("userName"));
        model.addAttribute("userRole", session.getAttribute("userRole"));
        return "settings";
    }

    @PostMapping("/settings/preferences")
    public String updatePreferences(
            @RequestParam(defaultValue = "false") boolean jobAlertsEnabled,
            @RequestParam(defaultValue = "false") boolean emailUpdatesEnabled,
            @RequestParam(defaultValue = "false") boolean interviewRemindersEnabled,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        String email = (String) session.getAttribute("userEmail");
        if (email == null) return "redirect:/login";

        UserAccount account = accountRepository.findById(email).orElse(null);
        if (account == null) return "redirect:/login";

        account.setJobAlertsEnabled(jobAlertsEnabled);
        account.setEmailUpdatesEnabled(emailUpdatesEnabled);
        account.setInterviewRemindersEnabled(interviewRemindersEnabled);
        accountRepository.save(account);

        redirectAttributes.addFlashAttribute("successMessage", "Preferences saved successfully.");
        return "redirect:/settings";
    }

    @PostMapping("/settings/password")
    public String changePassword(
            @RequestParam String currentPassword,
            @RequestParam String newPassword,
            @RequestParam String confirmPassword,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        String email = (String) session.getAttribute("userEmail");
        if (email == null) return "redirect:/login";

        if (newPassword.length() < 6 || !newPassword.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("errorMessage", "New passwords must match and contain at least 6 characters.");
            return "redirect:/settings";
        }

        UserAccount account = accountRepository.findById(email).orElse(null);
        if (account == null || !PasswordUtil.matches(currentPassword, account.getPasswordHash())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Current password is incorrect.");
            return "redirect:/settings";
        }

        account.setPasswordHash(PasswordUtil.hash(newPassword));
        accountRepository.save(account);
        redirectAttributes.addFlashAttribute("successMessage", "Password changed successfully.");
        return "redirect:/settings";
    }
}
