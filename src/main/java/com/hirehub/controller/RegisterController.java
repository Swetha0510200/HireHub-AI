package com.hirehub.controller;

import java.util.regex.Pattern;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.hirehub.model.Company;
import com.hirehub.model.UserAccount;
import com.hirehub.model.UserProfile;
import com.hirehub.repository.CompanyRepository;
import com.hirehub.repository.UserAccountRepository;
import com.hirehub.repository.UserProfileRepository;
import com.hirehub.util.PasswordUtil;

@Controller
public class RegisterController {

    private static final Pattern EMAIL_PATTERN = 
            Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,64}$");

    private final UserAccountRepository accountRepository;
    private final UserProfileRepository profileRepository;
    private final CompanyRepository companyRepository;

    public RegisterController(
            UserAccountRepository accountRepository,
            UserProfileRepository profileRepository,
            CompanyRepository companyRepository) {
        this.accountRepository = accountRepository;
        this.profileRepository = profileRepository;
        this.companyRepository = companyRepository;
    }

    @PostMapping("/register")
    public String register(
            @RequestParam(defaultValue = "student") String role,
            @RequestParam String name,
            @RequestParam String email,
            @RequestParam(required = false) String mobile,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String college,
            @RequestParam(required = false) String degree,
            @RequestParam(required = false) String qualification,
            @RequestParam(required = false) String specialization,
            @RequestParam(required = false) Integer graduationYear,
            @RequestParam(required = false) String companyName,
            @RequestParam(required = false) String designation,
            @RequestParam(required = false) String companyWebsite,
            @RequestParam(required = false) String companyLocation,
            @RequestParam(required = false) String companyDescription,
            @RequestParam String password,
            @RequestParam String confirmPassword,
            RedirectAttributes redirectAttributes) {

        if (name == null || name.trim().isBlank()) {
            redirectAttributes.addFlashAttribute("error", "Full name is required.");
            return "redirect:/register";
        }

        if (email == null || email.trim().isBlank()) {
            redirectAttributes.addFlashAttribute("error", "Email address is required.");
            return "redirect:/register";
        }

        String normalizedEmail = email.trim().toLowerCase();
        if (!EMAIL_PATTERN.matcher(normalizedEmail).matches()) {
            redirectAttributes.addFlashAttribute("error", "Please provide a valid email address.");
            return "redirect:/register";
        }

        if (password == null || password.length() < 6) {
            redirectAttributes.addFlashAttribute("error", "Password must be at least 6 characters.");
            return "redirect:/register";
        }

        if (!password.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("error", "Passwords do not match.");
            return "redirect:/register";
        }

        if (accountRepository.existsById(normalizedEmail)) {
            redirectAttributes.addFlashAttribute("error", "An account already exists with this email.");
            return "redirect:/register";
        }

        // Role mapping: "candidate" or "student" -> Student/Candidate, "recruiter" -> Recruiter
        String normalizedRole = ("recruiter".equalsIgnoreCase(role.trim())) ? "Recruiter" : "Student";

        String trimmedName = name.trim();
        String effectivePhone = (mobile != null && !mobile.isBlank()) ? mobile.trim() : ((phone != null && !phone.isBlank()) ? phone.trim() : null);
        String trimmedLocation = (location != null && !location.isBlank()) ? location.trim() : ((companyLocation != null && !companyLocation.isBlank()) ? companyLocation.trim() : null);
        String effectiveDegree = (degree != null && !degree.isBlank()) ? degree.trim() : ((qualification != null && !qualification.isBlank()) ? qualification.trim() : null);

        UserAccount account = new UserAccount(normalizedEmail, PasswordUtil.hash(password), trimmedName, normalizedRole);
        account.setMobile(effectivePhone);
        account.setLocation(trimmedLocation);
        account.setQualification(effectiveDegree);
        account.setGraduationYear(graduationYear);
        accountRepository.save(account);

        UserProfile profile = new UserProfile();
        profile.setEmail(normalizedEmail);
        profile.setName(trimmedName);
        profile.setRole(normalizedRole);
        profile.setLocation(trimmedLocation);
        profile.setPhone(effectivePhone);

        if ("Student".equals(normalizedRole) || "Candidate".equalsIgnoreCase(normalizedRole)) {
            profile.setCollege(college != null ? college.trim() : null);
            profile.setDegree(effectiveDegree);
            profile.setSpecialization(specialization != null ? specialization.trim() : null);
            profile.setGraduationYear(graduationYear);
            profile.setPreferredRole("Software Engineer");
        } else if ("Recruiter".equals(normalizedRole)) {
            String cName = (companyName != null && !companyName.isBlank()) ? companyName.trim() : "Recruitment Partner";
            String cWeb = (companyWebsite != null && !companyWebsite.isBlank()) ? companyWebsite.trim() : "";
            String cDesc = (companyDescription != null && !companyDescription.isBlank()) ? companyDescription.trim() : "Hiring company on HireHub.";
            profile.setCompanyName(cName);
            profile.setCompanyWebsite(cWeb);
            profile.setCompanyDescription(cDesc);
            profile.setSummary(designation != null ? designation.trim() : "Hiring Manager");
            profile.setRecruiterEmail(normalizedEmail);

            if (!companyRepository.existsByNameIgnoreCase(cName)) {
                Company company = new Company(cName, cWeb, "Growing Team", "Technology", trimmedLocation, normalizedEmail, cDesc);
                companyRepository.save(company);
            }
        }

        profileRepository.save(profile);

        redirectAttributes.addFlashAttribute("success", "Account created successfully! Please login to continue.");
        return "redirect:/login";
    }
}
