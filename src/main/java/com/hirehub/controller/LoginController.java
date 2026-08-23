package com.hirehub.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.hirehub.model.UserAccount;
import com.hirehub.model.UserProfile;
import com.hirehub.repository.UserAccountRepository;
import com.hirehub.repository.UserProfileRepository;
import com.hirehub.util.PasswordUtil;

import jakarta.servlet.http.HttpSession;

@Controller
public class LoginController {

    private final UserAccountRepository accountRepository;
    private final UserProfileRepository profileRepository;

    public LoginController(UserAccountRepository accountRepository, UserProfileRepository profileRepository) {
        this.accountRepository = accountRepository;
        this.profileRepository = profileRepository;
    }

    @GetMapping("/login")
    public String loginPage(HttpSession session) {
        String email = (session != null) ? (String) session.getAttribute("userEmail") : null;
        if (email != null) {
            String role = (String) session.getAttribute("userRole");
            if ("Recruiter".equalsIgnoreCase(role)) return "redirect:/recruiter/dashboard";
            if ("Admin".equalsIgnoreCase(role)) return "redirect:/admin/dashboard";
            return "redirect:/dashboard";
        }
        return "login";
    }

    @PostMapping("/login")
    public String login(
            @RequestParam(required = false) String role,
            @RequestParam String email,
            @RequestParam String password,
            HttpSession session,
            Model model) {

        if (email == null || email.trim().isBlank() || password == null || password.isBlank()) {
            model.addAttribute("error", "Invalid email or password.");
            model.addAttribute("email", email);
            model.addAttribute("role", role);
            return "login";
        }

        String normalizedEmail = email.trim().toLowerCase();

        UserAccount account = accountRepository.findById(normalizedEmail).orElse(null);
        if (account == null && normalizedEmail.equals("admin")) {
            account = accountRepository.findById("admin@hirehub.com").orElse(null);
        }

        if (account == null || !PasswordUtil.matches(password, account.getPasswordHash())) {
            model.addAttribute("error", "Invalid email or password.");
            model.addAttribute("email", email);
            model.addAttribute("role", role);
            return "login";
        }

        if (!account.isEnabled()) {
            model.addAttribute("error", "Your account has been deactivated. Please contact support.");
            return "login";
        }

        session.setAttribute("userEmail", account.getEmail());
        session.setAttribute("userRole", account.getRole());
        session.setAttribute("userName", account.getName());

        UserProfile profile = profileRepository.findByEmail(normalizedEmail).orElse(null);
        if (profile != null) {
            session.setAttribute("userInitials", profile.getInitials());
            if (profile.getImageUrl() != null && !profile.getImageUrl().isEmpty()) {
                session.setAttribute("userProfileImage", profile.getImageUrl());
            }
        } else {
            String name = account.getName();
            String initials = "U";
            if (name != null && !name.trim().isEmpty()) {
                String[] parts = name.trim().split("\\s+");
                if (parts.length >= 2 && !parts[0].isEmpty() && !parts[parts.length - 1].isEmpty()) {
                    initials = (parts[0].substring(0, 1) + parts[parts.length - 1].substring(0, 1)).toUpperCase();
                } else {
                    initials = parts[0].substring(0, 1).toUpperCase();
                }
            }
            session.setAttribute("userInitials", initials);
        }

        if ("Recruiter".equalsIgnoreCase(account.getRole())) {
            return "redirect:/recruiter/dashboard";
        } else if ("Admin".equalsIgnoreCase(account.getRole())) {
            return "redirect:/admin/dashboard";
        }

        return "redirect:/dashboard";
    }

    @GetMapping("/logout")
    public String logoutGet(HttpSession session) {
        if (session != null) {
            session.invalidate();
        }
        return "redirect:/login";
    }

    @PostMapping("/logout")
    public String logout(HttpSession session) {
        if (session != null) {
            session.invalidate();
        }
        return "redirect:/login";
    }
}
