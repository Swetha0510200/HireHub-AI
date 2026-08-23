package com.hirehub.controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.hirehub.model.ResumeRecord;
import com.hirehub.model.UserAccount;
import com.hirehub.model.UserProfile;
import com.hirehub.repository.ResumeRecordRepository;
import com.hirehub.repository.UserAccountRepository;
import com.hirehub.repository.UserProfileRepository;
import com.hirehub.service.ResumeAnalyzerService;

import jakarta.servlet.http.HttpSession;

@Controller
public class ProfileController {

    private final UserProfileRepository profileRepository;
    private final UserAccountRepository accountRepository;
    private final ResumeAnalyzerService resumeAnalyzerService;
    private final ResumeRecordRepository resumeRepository;

    public ProfileController(
            UserProfileRepository profileRepository,
            UserAccountRepository accountRepository,
            ResumeAnalyzerService resumeAnalyzerService,
            ResumeRecordRepository resumeRepository) {
        this.profileRepository = profileRepository;
        this.accountRepository = accountRepository;
        this.resumeAnalyzerService = resumeAnalyzerService;
        this.resumeRepository = resumeRepository;
    }

    // =========================================================
    // PROFILE PAGE (VIEW / EDIT)
    // =========================================================

    @GetMapping({"/profile", "/candidate/profile"})
    public String profile(
            @RequestParam(name = "edit", required = false) String edit,
            HttpSession session,
            Model model) {

        String email = (String) session.getAttribute("userEmail");
        if (email == null) {
            return "redirect:/login";
        }

        String normalizedEmail = email.trim().toLowerCase();

        // Retrieve existing UserProfile or construct from UserAccount
        UserProfile profile = profileRepository.findByEmail(normalizedEmail).orElseGet(() -> {
            UserProfile newProfile = new UserProfile();
            newProfile.setEmail(normalizedEmail);
            return newProfile;
        });

        // Ensure baseline fields are populated from UserAccount if missing
        accountRepository.findById(normalizedEmail).ifPresent(account -> {
            if (profile.getName() == null || profile.getName().isBlank()) {
                profile.setName(account.getName());
            }
            if (profile.getRole() == null || profile.getRole().isBlank()) {
                profile.setRole(account.getRole());
            }
            if (profile.getPhone() == null || profile.getPhone().isBlank()) {
                profile.setPhone(account.getMobile());
            }
            if (profile.getLocation() == null || profile.getLocation().isBlank()) {
                profile.setLocation(account.getLocation());
            }
        });

        if (profile.getRole() == null || profile.getRole().isBlank()) {
            profile.setRole((String) session.getAttribute("userRole"));
        }

        // Save profile if new
        if (profileRepository.findByEmail(normalizedEmail).isEmpty()) {
            profileRepository.save(profile);
        }

        boolean isEdit = "true".equalsIgnoreCase(edit);

        model.addAttribute("profile", profile);
        model.addAttribute("isEdit", isEdit);
        model.addAttribute("userEmail", normalizedEmail);
        model.addAttribute("userName", profile.getName() != null ? profile.getName() : session.getAttribute("userName"));
        model.addAttribute("userRole", profile.getRole() != null ? profile.getRole() : session.getAttribute("userRole"));

        resumeRepository.findFirstByUserEmailOrderByUploadedAtDesc(normalizedEmail)
                .ifPresent(record -> {
                    model.addAttribute("resumeRecord", record);
                    model.addAttribute("atsScore", record.getAtsScore());
                });

        return "profile";
    }

    // =========================================================
    // EDIT PROFILE ALIAS
    // =========================================================

    @GetMapping({"/profile/edit", "/candidate/profile/edit"})
    public String editProfile(HttpSession session) {
        if (session.getAttribute("userEmail") == null) {
            return "redirect:/login";
        }
        return "redirect:/profile?edit=true";
    }

    // =========================================================
    // UPLOAD PROFILE PHOTO / IMAGE
    // =========================================================

    @PostMapping({"/profile/photo/upload", "/profile/image/upload"})
    public String uploadProfilePhoto(
            @RequestParam("photo") MultipartFile photo,
            HttpSession session) {

        String email = (String) session.getAttribute("userEmail");
        if (email == null) return "redirect:/login";
        String normalizedEmail = email.trim().toLowerCase();

        if (photo == null || photo.isEmpty()) {
            return "redirect:/profile?error=Please+select+a+photo+to+upload";
        }

        String contentType = photo.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            return "redirect:/profile?error=Only+valid+image+files+(PNG,+JPG,+WEBP)+are+allowed";
        }

        try {
            String base64Image = "data:" + contentType + ";base64," + java.util.Base64.getEncoder().encodeToString(photo.getBytes());
            UserProfile profile = profileRepository.findByEmail(normalizedEmail).orElseGet(() -> {
                UserProfile p = new UserProfile();
                p.setEmail(normalizedEmail);
                return p;
            });

            profile.setImageUrl(base64Image);
            profileRepository.save(profile);

            session.setAttribute("userProfileImage", base64Image);
            session.setAttribute("userInitials", profile.getInitials());
            if (profile.getName() != null && !profile.getName().isBlank()) {
                session.setAttribute("userName", profile.getName());
            }

            return "redirect:/profile?success=Profile+photo+updated+successfully";
        } catch (Exception e) {
            return "redirect:/profile?error=Failed+to+upload+profile+photo";
        }
    }

    // =========================================================
    // SAVE STUDENT / GENERAL PROFILE
    // =========================================================

    @PostMapping({"/profile/student/save", "/profile/save"})
    public String saveStudentProfile(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String college,
            @RequestParam(required = false) String degree,
            @RequestParam(required = false) String specialization,
            @RequestParam(required = false) Integer graduationYear,
            @RequestParam(required = false) String skills,
            @RequestParam(required = false) String experienceLevel,
            @RequestParam(required = false) String preferredRole,
            @RequestParam(required = false) String preferredLocation,
            @RequestParam(required = false) String linkedIn,
            @RequestParam(required = false) String github,
            @RequestParam(required = false) String careerObjective,
            @RequestParam(required = false) String summary,
            @RequestParam(required = false) MultipartFile resume,
            @RequestParam(name = "profilePhoto", required = false) MultipartFile profilePhoto,
            @RequestParam(name = "photo", required = false) MultipartFile photo,
            HttpSession session) {

        String email = (String) session.getAttribute("userEmail");
        if (email == null) return "redirect:/login";
        String normalizedEmail = email.trim().toLowerCase();

        UserProfile profile = profileRepository.findByEmail(normalizedEmail)
                .orElseGet(() -> {
                    UserProfile p = new UserProfile();
                    p.setEmail(normalizedEmail);
                    return p;
                });

        // Set Role
        profile.setRole("Student");

        // Update Text Fields
        if (name != null && !name.isBlank()) profile.setName(name.trim());
        if (phone != null) profile.setPhone(phone.trim());
        if (location != null) profile.setLocation(location.trim());
        if (college != null) profile.setCollege(college.trim());
        if (degree != null) profile.setDegree(degree.trim());
        if (specialization != null) profile.setSpecialization(specialization.trim());
        if (graduationYear != null) profile.setGraduationYear(graduationYear);
        if (skills != null) profile.setSkills(skills.trim());
        if (experienceLevel != null) profile.setExperienceLevel(experienceLevel.trim());
        if (preferredRole != null) profile.setPreferredRole(preferredRole.trim());
        if (preferredLocation != null) profile.setPreferredLocation(preferredLocation.trim());
        if (linkedIn != null) profile.setLinkedIn(linkedIn.trim());
        if (github != null) profile.setGithub(github.trim());

        if (careerObjective != null && !careerObjective.isBlank()) {
            profile.setSummary(careerObjective.trim());
        } else if (summary != null && !summary.isBlank()) {
            profile.setSummary(summary.trim());
        }

        // Sync with UserAccount
        accountRepository.findById(normalizedEmail).ifPresent(account -> {
            if (profile.getName() != null && !profile.getName().isBlank()) {
                account.setName(profile.getName());
            }
            if (profile.getPhone() != null && !profile.getPhone().isBlank()) {
                account.setMobile(profile.getPhone());
            }
            if (profile.getLocation() != null && !profile.getLocation().isBlank()) {
                account.setLocation(profile.getLocation());
            }
            accountRepository.save(account);
        });

        // Photo Upload Handling (Keep existing if not provided)
        MultipartFile activePhoto = (profilePhoto != null && !profilePhoto.isEmpty()) ? profilePhoto : photo;
        if (activePhoto != null && !activePhoto.isEmpty()) {
            String contentType = activePhoto.getContentType();
            if (contentType != null && contentType.startsWith("image/")) {
                try {
                    String base64Image = "data:" + contentType + ";base64," + java.util.Base64.getEncoder().encodeToString(activePhoto.getBytes());
                    profile.setImageUrl(base64Image);
                    session.setAttribute("userProfileImage", base64Image);
                } catch (Exception ignored) {}
            }
        }

        // Save Profile to Database
        profileRepository.save(profile);

        // Update Session Information
        if (profile.getName() != null && !profile.getName().isBlank()) {
            session.setAttribute("userName", profile.getName());
        }
        session.setAttribute("userInitials", profile.getInitials());
        if (profile.getImageUrl() != null && !profile.getImageUrl().isEmpty()) {
            session.setAttribute("userProfileImage", profile.getImageUrl());
        }

        // Resume Upload & Analysis Handling
        if (resume != null && !resume.isEmpty()) {
            try {
                String originalName = resume.getOriginalFilename();
                String lower = originalName == null ? "" : originalName.toLowerCase();

                if (lower.endsWith(".pdf") || lower.endsWith(".doc") || lower.endsWith(".docx") || lower.endsWith(".txt")) {
                    String text = resumeAnalyzerService.extractText(resume);
                    if (text != null && !text.isBlank()) {
                        Map<String, Object> result = resumeAnalyzerService.analyzeResume(text);

                        ResumeRecord record = new ResumeRecord();
                        record.setUserEmail(normalizedEmail);
                        record.setFileName(originalName);
                        record.setContentType(resume.getContentType());
                        record.setFileData(resume.getBytes());
                        record.setUploadedAt(LocalDateTime.now());
                        record.setAtsScore(asInt(result.get("atsScore")));
                        record.setResumeQuality(String.valueOf(result.get("resumeQuality")));
                        record.setSummary(String.valueOf(result.get("summary")));
                        record.setSkills(join(result.get("skills")));
                        record.setStrengths(join(result.get("strengths")));
                        record.setImprovements(join(result.get("improvements")));
                        record.setWordCount(asInt(result.get("wordCount")));
                        record.setSkillCount(asInt(result.get("skillCount")));
                        record.setLineCount(asInt(result.get("lineCount")));
                        record.setQuantifiedAchievements(asInt(result.get("quantifiedAchievements")));
                        record.setActionVerbUsage(asInt(result.get("actionVerbUsage")));
                        record.setSectionScore(asInt(result.get("sectionScore")));
                        record.setSkillScore(asInt(result.get("skillScore")));
                        record.setAchievementScore(asInt(result.get("achievementScore")));
                        record.setLengthScore(asInt(result.get("lengthScore")));
                        record.setAtsStructureScore(asInt(result.get("atsStructureScore")));

                        resumeRepository.save(record);
                    }
                }
            } catch (Exception ignored) {}
        }

        return "redirect:/profile?success=Profile+saved+successfully";
    }

    // =========================================================
    // SAVE RECRUITER PROFILE
    // =========================================================

    @PostMapping("/profile/recruiter/save")
    public String saveRecruiterProfile(
            @RequestParam(required = false) String recruiterName,
            @RequestParam(required = false) String recruiterContact,
            @RequestParam(required = false) String recruiterEmail,
            @RequestParam(required = false) String companyName,
            @RequestParam(required = false) String companyWebsite,
            @RequestParam(required = false) String companySize,
            @RequestParam(required = false) String industry,
            @RequestParam(required = false) String hiringLocation,
            @RequestParam(required = false) String hiringTimeline,
            @RequestParam(required = false) String salaryRange,
            @RequestParam(required = false) String preferredSkills,
            @RequestParam(required = false) String companyDescription,
            @RequestParam(name = "profilePhoto", required = false) MultipartFile profilePhoto,
            @RequestParam(name = "photo", required = false) MultipartFile photo,
            HttpSession session) {

        String email = (String) session.getAttribute("userEmail");
        if (email == null) return "redirect:/login";
        String normalizedEmail = email.trim().toLowerCase();

        UserProfile profile = profileRepository.findByEmail(normalizedEmail)
                .orElseGet(() -> {
                    UserProfile p = new UserProfile();
                    p.setEmail(normalizedEmail);
                    return p;
                });

        profile.setRole("Recruiter");

        if (recruiterName != null && !recruiterName.isBlank()) profile.setName(recruiterName.trim());
        if (recruiterContact != null) {
            profile.setRecruiterContact(recruiterContact.trim());
            profile.setPhone(recruiterContact.trim());
        }
        if (recruiterEmail != null && !recruiterEmail.isBlank()) profile.setRecruiterEmail(recruiterEmail.trim());
        if (companyName != null) profile.setCompanyName(companyName.trim());
        if (companyWebsite != null) profile.setCompanyWebsite(companyWebsite.trim());
        if (companySize != null) profile.setCompanySize(companySize.trim());
        if (industry != null) profile.setIndustry(industry.trim());
        if (hiringLocation != null) {
            profile.setHiringLocation(hiringLocation.trim());
            profile.setHiringLocations(hiringLocation.trim());
            profile.setLocation(hiringLocation.trim());
        }
        if (hiringTimeline != null) profile.setHiringTimeline(hiringTimeline.trim());
        if (salaryRange != null) profile.setSalaryRange(salaryRange.trim());
        if (preferredSkills != null) profile.setPreferredSkills(preferredSkills.trim());
        if (companyDescription != null) {
            profile.setCompanyDescription(companyDescription.trim());
            profile.setSummary(companyDescription.trim());
        }

        // Sync with UserAccount
        accountRepository.findById(normalizedEmail).ifPresent(account -> {
            if (profile.getName() != null && !profile.getName().isBlank()) {
                account.setName(profile.getName());
            }
            if (profile.getPhone() != null && !profile.getPhone().isBlank()) {
                account.setMobile(profile.getPhone());
            }
            if (profile.getLocation() != null && !profile.getLocation().isBlank()) {
                account.setLocation(profile.getLocation());
            }
            accountRepository.save(account);
        });

        // Photo Upload Handling
        MultipartFile activePhoto = (profilePhoto != null && !profilePhoto.isEmpty()) ? profilePhoto : photo;
        if (activePhoto != null && !activePhoto.isEmpty()) {
            String contentType = activePhoto.getContentType();
            if (contentType != null && contentType.startsWith("image/")) {
                try {
                    String base64Image = "data:" + contentType + ";base64," + java.util.Base64.getEncoder().encodeToString(activePhoto.getBytes());
                    profile.setImageUrl(base64Image);
                    session.setAttribute("userProfileImage", base64Image);
                } catch (Exception ignored) {}
            }
        }

        profileRepository.save(profile);

        if (profile.getName() != null && !profile.getName().isBlank()) {
            session.setAttribute("userName", profile.getName());
        }
        session.setAttribute("userInitials", profile.getInitials());
        if (profile.getImageUrl() != null && !profile.getImageUrl().isEmpty()) {
            session.setAttribute("userProfileImage", profile.getImageUrl());
        }

        return "redirect:/profile?success=Recruiter+profile+saved+successfully";
    }

    // =========================================================
    // GENERAL PROFILE EDIT POST
    // =========================================================

    @PostMapping("/profile/edit")
    public String saveGeneralProfile(
            UserProfile submitted,
            @RequestParam(name = "profilePhoto", required = false) MultipartFile profilePhoto,
            @RequestParam(name = "photo", required = false) MultipartFile photo,
            @RequestParam(name = "resume", required = false) MultipartFile resume,
            HttpSession session) {

        String email = (String) session.getAttribute("userEmail");
        if (email == null) return "redirect:/login";
        String normalizedEmail = email.trim().toLowerCase();

        UserProfile profile = profileRepository.findByEmail(normalizedEmail)
                .orElseGet(() -> {
                    UserProfile p = new UserProfile();
                    p.setEmail(normalizedEmail);
                    return p;
                });

        if (submitted.getName() != null && !submitted.getName().isBlank()) profile.setName(submitted.getName().trim());
        if (submitted.getPhone() != null) profile.setPhone(submitted.getPhone().trim());
        if (submitted.getLocation() != null) profile.setLocation(submitted.getLocation().trim());
        if (submitted.getRole() != null && !submitted.getRole().isBlank()) profile.setRole(submitted.getRole().trim());
        if (submitted.getLinkedIn() != null) profile.setLinkedIn(submitted.getLinkedIn().trim());
        if (submitted.getGithub() != null) profile.setGithub(submitted.getGithub().trim());
        if (submitted.getSummary() != null) profile.setSummary(submitted.getSummary().trim());
        if (submitted.getCollege() != null) profile.setCollege(submitted.getCollege().trim());
        if (submitted.getDegree() != null) profile.setDegree(submitted.getDegree().trim());
        if (submitted.getSpecialization() != null) profile.setSpecialization(submitted.getSpecialization().trim());
        if (submitted.getGraduationYear() != null) profile.setGraduationYear(submitted.getGraduationYear());
        if (submitted.getSkills() != null) profile.setSkills(submitted.getSkills().trim());
        if (submitted.getExperienceLevel() != null) profile.setExperienceLevel(submitted.getExperienceLevel().trim());
        if (submitted.getPreferredRole() != null) profile.setPreferredRole(submitted.getPreferredRole().trim());
        if (submitted.getPreferredLocation() != null) profile.setPreferredLocation(submitted.getPreferredLocation().trim());

        if (submitted.getCompanyName() != null) profile.setCompanyName(submitted.getCompanyName().trim());
        if (submitted.getCompanyWebsite() != null) profile.setCompanyWebsite(submitted.getCompanyWebsite().trim());
        if (submitted.getCompanySize() != null) profile.setCompanySize(submitted.getCompanySize().trim());
        if (submitted.getIndustry() != null) profile.setIndustry(submitted.getIndustry().trim());
        if (submitted.getCompanyDescription() != null) profile.setCompanyDescription(submitted.getCompanyDescription().trim());
        if (submitted.getOpenRoles() != null) profile.setOpenRoles(submitted.getOpenRoles().trim());
        if (submitted.getHiringLocation() != null) profile.setHiringLocation(submitted.getHiringLocation().trim());
        if (submitted.getHiringLocations() != null) profile.setHiringLocations(submitted.getHiringLocations().trim());
        if (submitted.getHiringTimeline() != null) profile.setHiringTimeline(submitted.getHiringTimeline().trim());
        if (submitted.getSalaryRange() != null) profile.setSalaryRange(submitted.getSalaryRange().trim());
        if (submitted.getPreferredSkills() != null) profile.setPreferredSkills(submitted.getPreferredSkills().trim());
        if (submitted.getRecruiterContact() != null) profile.setRecruiterContact(submitted.getRecruiterContact().trim());
        if (submitted.getRecruiterEmail() != null) profile.setRecruiterEmail(submitted.getRecruiterEmail().trim());
        if (submitted.getIdealCandidateProfile() != null) profile.setIdealCandidateProfile(submitted.getIdealCandidateProfile().trim());

        // Photo Upload Handling
        MultipartFile activePhoto = (profilePhoto != null && !profilePhoto.isEmpty()) ? profilePhoto : photo;
        if (activePhoto != null && !activePhoto.isEmpty()) {
            String contentType = activePhoto.getContentType();
            if (contentType != null && contentType.startsWith("image/")) {
                try {
                    String base64Image = "data:" + contentType + ";base64," + java.util.Base64.getEncoder().encodeToString(activePhoto.getBytes());
                    profile.setImageUrl(base64Image);
                    session.setAttribute("userProfileImage", base64Image);
                } catch (Exception ignored) {}
            }
        }

        profileRepository.save(profile);

        // Sync with UserAccount
        accountRepository.findById(normalizedEmail).ifPresent(account -> {
            if (profile.getName() != null && !profile.getName().isBlank()) {
                account.setName(profile.getName());
            }
            if (profile.getPhone() != null && !profile.getPhone().isBlank()) {
                account.setMobile(profile.getPhone());
            }
            if (profile.getLocation() != null && !profile.getLocation().isBlank()) {
                account.setLocation(profile.getLocation());
            }
            accountRepository.save(account);
        });

        session.setAttribute("userName", profile.getName());
        session.setAttribute("userInitials", profile.getInitials());
        if (profile.getImageUrl() != null && !profile.getImageUrl().isEmpty()) {
            session.setAttribute("userProfileImage", profile.getImageUrl());
        }

        // Resume Upload Handling
        if (resume != null && !resume.isEmpty()) {
            try {
                String originalName = resume.getOriginalFilename();
                String lower = originalName == null ? "" : originalName.toLowerCase();
                if (lower.endsWith(".pdf") || lower.endsWith(".doc") || lower.endsWith(".docx") || lower.endsWith(".txt")) {
                    String text = resumeAnalyzerService.extractText(resume);
                    if (text != null && !text.isBlank()) {
                        Map<String, Object> result = resumeAnalyzerService.analyzeResume(text);

                        ResumeRecord record = new ResumeRecord();
                        record.setUserEmail(normalizedEmail);
                        record.setFileName(originalName);
                        record.setContentType(resume.getContentType());
                        record.setFileData(resume.getBytes());
                        record.setUploadedAt(LocalDateTime.now());
                        record.setAtsScore(asInt(result.get("atsScore")));
                        record.setResumeQuality(String.valueOf(result.get("resumeQuality")));
                        record.setSummary(String.valueOf(result.get("summary")));
                        record.setSkills(join(result.get("skills")));
                        record.setStrengths(join(result.get("strengths")));
                        record.setImprovements(join(result.get("improvements")));
                        record.setWordCount(asInt(result.get("wordCount")));
                        record.setSkillCount(asInt(result.get("skillCount")));
                        record.setLineCount(asInt(result.get("lineCount")));
                        record.setQuantifiedAchievements(asInt(result.get("quantifiedAchievements")));
                        record.setActionVerbUsage(asInt(result.get("actionVerbUsage")));
                        record.setSectionScore(asInt(result.get("sectionScore")));
                        record.setSkillScore(asInt(result.get("skillScore")));
                        record.setAchievementScore(asInt(result.get("achievementScore")));
                        record.setLengthScore(asInt(result.get("lengthScore")));
                        record.setAtsStructureScore(asInt(result.get("atsStructureScore")));

                        resumeRepository.save(record);
                    }
                }
            } catch (Exception ignored) {}
        }

        return "redirect:/profile?success=Profile+updated+successfully";
    }

    private int asInt(Object value) {
        return value instanceof Number ? ((Number) value).intValue() : 0;
    }

    private String join(Object value) {
        if (value instanceof List<?> list) {
            return String.join("||", list.stream().map(Object::toString).toList());
        }
        return value == null ? "" : value.toString();
    }
}