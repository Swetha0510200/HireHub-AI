package com.hirehub.controller;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.hirehub.model.Company;
import com.hirehub.model.Job;
import com.hirehub.repository.CompanyRepository;
import com.hirehub.repository.JobApplicationRepository;
import com.hirehub.repository.JobRepository;
import com.hirehub.repository.SavedJobRepository;
import com.hirehub.service.NotificationService;

import jakarta.servlet.http.HttpSession;

@Controller
public class JobController {

    private final JobRepository jobRepository;
    private final JobApplicationRepository applicationRepository;
    private final SavedJobRepository savedJobRepository;
    private final CompanyRepository companyRepository;
    private final NotificationService notificationService;

    public JobController(JobRepository jobRepository,
                         JobApplicationRepository applicationRepository,
                         SavedJobRepository savedJobRepository,
                         CompanyRepository companyRepository,
                         NotificationService notificationService) {
        this.jobRepository = jobRepository;
        this.applicationRepository = applicationRepository;
        this.savedJobRepository = savedJobRepository;
        this.companyRepository = companyRepository;
        this.notificationService = notificationService;
    }

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(String.class, new StringTrimmerEditor(true));
    }

    @GetMapping("/manage-jobs")
    public String manageJobsAlias(HttpSession session) {
        String role = (String) session.getAttribute("userRole");
        if ("Recruiter".equalsIgnoreCase(role) || "Admin".equalsIgnoreCase(role)) {
            return "redirect:/recruiter/jobs";
        }
        return "redirect:/browse-jobs";
    }

    @GetMapping("/applicants")
    public String applicantsAlias(HttpSession session) {
        String role = (String) session.getAttribute("userRole");
        if ("Recruiter".equalsIgnoreCase(role) || "Admin".equalsIgnoreCase(role)) {
            return "redirect:/recruiter/applicants";
        }
        return "redirect:/applications";
    }

    @GetMapping({"/post-job", "/jobs/post"})
    public String showPostJobForm(HttpSession session, Model model) {
        String email = (String) session.getAttribute("userEmail");
        if (email == null) return "redirect:/login";

        String role = (String) session.getAttribute("userRole");
        if (!"Recruiter".equalsIgnoreCase(role) && !"Admin".equalsIgnoreCase(role)) {
            return "redirect:/dashboard";
        }

        model.addAttribute("job", new Job());
        model.addAttribute("userEmail", email);
        model.addAttribute("userName", session.getAttribute("userName"));
        model.addAttribute("userRole", role);

        companyRepository.findFirstByRecruiterEmailOrderByCreatedAtDesc(email)
                .ifPresent(company -> model.addAttribute("defaultCompany", company.getName()));

        return "post-job";
    }

    @PostMapping({"/post-job", "/jobs/post"})
    public String postJob(
            @ModelAttribute("job") Job job,
            BindingResult bindingResult,
            @RequestParam(required = false) String jobTitle,
            @RequestParam(required = false) String companyName,
            @RequestParam(required = false) String employmentType,
            @RequestParam(required = false) String jobDescription,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        String email = (String) session.getAttribute("userEmail");
        if (email == null) return "redirect:/login";

        String role = (String) session.getAttribute("userRole");
        if (!"Recruiter".equalsIgnoreCase(role) && !"Admin".equalsIgnoreCase(role)) {
            return "redirect:/dashboard";
        }

        // Apply parameter fallbacks if alias form fields were used
        if ((job.getTitle() == null || job.getTitle().trim().isEmpty()) && jobTitle != null && !jobTitle.trim().isEmpty()) {
            job.setTitle(jobTitle.trim());
        }
        if ((job.getCompany() == null || job.getCompany().trim().isEmpty()) && companyName != null && !companyName.trim().isEmpty()) {
            job.setCompany(companyName.trim());
        }
        if ((job.getType() == null || job.getType().trim().isEmpty()) && employmentType != null && !employmentType.trim().isEmpty()) {
            job.setType(employmentType.trim());
        }
        if ((job.getDescription() == null || job.getDescription().trim().isEmpty()) && jobDescription != null && !jobDescription.trim().isEmpty()) {
            job.setDescription(jobDescription.trim());
        }

        // Form Validation
        if (job.getTitle() == null || job.getTitle().trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Job title is required.");
            return "redirect:/post-job";
        }
        if (job.getCompany() == null || job.getCompany().trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Company name is required.");
            return "redirect:/post-job";
        }
        if (job.getLocation() == null || job.getLocation().trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Job location is required.");
            return "redirect:/post-job";
        }

        try {
            job.setTitle(job.getTitle().trim());
            job.setCompany(job.getCompany().trim());
            job.setLocation(job.getLocation().trim());
            if (job.getType() != null) job.setType(job.getType().trim());
            if (job.getExperience() != null) job.setExperience(job.getExperience().trim());
            if (job.getSalary() != null) job.setSalary(job.getSalary().trim());
            if (job.getSkills() != null) job.setSkills(job.getSkills().trim());
            if (job.getDescription() != null) job.setDescription(job.getDescription().trim());
            if (job.getDeadline() != null) job.setDeadline(job.getDeadline().trim());

            job.setRecruiterEmail(email);
            if (job.getApplicationEmail() == null || job.getApplicationEmail().isBlank()) {
                job.setApplicationEmail(email);
            }
            job.setActive(true);
            job.setCreatedAt(LocalDateTime.now());

            Job savedJob = jobRepository.save(job);

            // Ensure company entity exists in MySQL
            if (job.getCompany() != null && !companyRepository.existsByNameIgnoreCase(job.getCompany())) {
                companyRepository.save(new Company(job.getCompany(), "", "Growing Company", "Technology", job.getLocation(), email, "Company profile created on HireHub."));
            }

            notificationService.sendNotification(email, "Job Published Successfully",
                    "Your job posting '" + savedJob.getTitle() + "' is now live for all candidates on HireHub AI.", "SYSTEM", "/browse-jobs");

            redirectAttributes.addFlashAttribute("successMessage", "Job published successfully! It is now live in Browse Jobs.");
            return "redirect:/browse-jobs";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to publish job: " + e.getMessage());
            return "redirect:/post-job";
        }
    }

    @GetMapping({"/browse-jobs", "/jobs", "/candidate/jobs"})
    public String browseJobs(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String mode,
            @RequestParam(required = false) String experience,
            @RequestParam(required = false) String location,
            HttpSession session,
            Model model) {

        String email = (String) session.getAttribute("userEmail");
        if (email == null) return "redirect:/login";

        List<Job> allJobs;
        if (search != null && !search.isBlank()) {
            allJobs = jobRepository.searchJobs(search.trim());
        } else {
            allJobs = jobRepository.findByActiveTrueOrderByCreatedAtDesc();
            if (allJobs.isEmpty()) {
                allJobs = jobRepository.findAllByOrderByCreatedAtDesc();
            }
        }

        // Apply filters
        List<Job> filtered = allJobs.stream().filter(j -> {
            if (category != null && !category.isBlank() && !category.equalsIgnoreCase("all")) {
                if (j.getCategory() == null || !j.getCategory().toLowerCase().contains(category.toLowerCase())) return false;
            }
            if (type != null && !type.isBlank() && !type.equalsIgnoreCase("all")) {
                if (j.getType() == null || !j.getType().toLowerCase().contains(type.toLowerCase())) return false;
            }
            if (mode != null && !mode.isBlank() && !mode.equalsIgnoreCase("all")) {
                if (j.getWorkMode() == null || !j.getWorkMode().toLowerCase().contains(mode.toLowerCase())) return false;
            }
            if (experience != null && !experience.isBlank() && !experience.equalsIgnoreCase("all")) {
                if (j.getExperience() == null || !j.getExperience().toLowerCase().contains(experience.toLowerCase())) return false;
            }
            if (location != null && !location.isBlank() && !location.equalsIgnoreCase("all")) {
                if (j.getLocation() == null || !j.getLocation().toLowerCase().contains(location.toLowerCase())) return false;
            }
            return true;
        }).collect(Collectors.toList());

        // Track saved and applied job IDs for the logged in student
        Set<Long> savedJobIds = new HashSet<>();
        savedJobRepository.findByUserEmailOrderBySavedAtDesc(email)
                .forEach(sj -> savedJobIds.add(sj.getJob().getId()));

        Set<Long> appliedJobIds = new HashSet<>();
        applicationRepository.findByApplicantEmailOrderByAppliedAtDesc(email)
                .forEach(app -> appliedJobIds.add(app.getJob().getId()));

        model.addAttribute("jobs", filtered);
        model.addAttribute("savedJobIds", savedJobIds);
        model.addAttribute("appliedJobIds", appliedJobIds);
        model.addAttribute("totalJobCount", filtered.size());
        model.addAttribute("searchQuery", search);
        model.addAttribute("selectedCategory", category);
        model.addAttribute("selectedType", type);
        model.addAttribute("selectedMode", mode);
        model.addAttribute("selectedExperience", experience);
        model.addAttribute("selectedLocation", location);
        model.addAttribute("userName", session.getAttribute("userName"));
        model.addAttribute("userRole", session.getAttribute("userRole"));

        return "jobs";
    }

    @GetMapping("/jobs/{id}")
    public String jobDetails(
            @PathVariable Long id,
            HttpSession session,
            Model model) {

        String email = (String) session.getAttribute("userEmail");
        if (email == null) return "redirect:/login";

        Job job = jobRepository.findById(id).orElse(null);
        if (job == null) return "redirect:/browse-jobs";

        boolean hasApplied = applicationRepository.existsByJobIdAndApplicantEmail(id, email);
        boolean hasSaved = savedJobRepository.existsByJobIdAndUserEmail(id, email);

        model.addAttribute("job", job);
        model.addAttribute("hasApplied", hasApplied);
        model.addAttribute("hasSaved", hasSaved);
        model.addAttribute("recommendedJobs", jobRepository.findTop6ByIdNotOrderByCreatedAtDesc(id));
        model.addAttribute("userName", session.getAttribute("userName"));
        model.addAttribute("userRole", session.getAttribute("userRole"));

        return "job-details";
    }

    @GetMapping({"/internships", "/candidate/internships"})
    public String browseInternships(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String domain,
            @RequestParam(required = false) String duration,
            @RequestParam(required = false) String mode,
            @RequestParam(required = false) String location,
            HttpSession session,
            Model model) {

        String email = (String) session.getAttribute("userEmail");
        if (email == null) return "redirect:/login";

        List<Job> allInternships;
        if (search != null && !search.isBlank()) {
            allInternships = jobRepository.searchInternships(search.trim());
        } else {
            allInternships = jobRepository.findByTypeIgnoreCaseAndActiveTrueOrderByCreatedAtDesc("Internship");
            if (allInternships.isEmpty()) {
                allInternships = jobRepository.findByActiveTrueOrderByCreatedAtDesc().stream()
                        .filter(Job::isInternship)
                        .collect(Collectors.toList());
            }
        }

        // Apply filters
        List<Job> filtered = allInternships.stream().filter(j -> {
            if (domain != null && !domain.isBlank() && !domain.equalsIgnoreCase("all")) {
                if (j.getCategory() == null || !j.getCategory().toLowerCase().contains(domain.toLowerCase())) return false;
            }
            if (duration != null && !duration.isBlank() && !duration.equalsIgnoreCase("all")) {
                if (j.getDuration() == null || !j.getDuration().toLowerCase().contains(duration.toLowerCase())) return false;
            }
            if (mode != null && !mode.isBlank() && !mode.equalsIgnoreCase("all")) {
                if (j.getWorkMode() == null || !j.getWorkMode().toLowerCase().contains(mode.toLowerCase())) return false;
            }
            if (location != null && !location.isBlank() && !location.equalsIgnoreCase("all")) {
                if (j.getLocation() == null || !j.getLocation().toLowerCase().contains(location.toLowerCase())) return false;
            }
            return true;
        }).collect(Collectors.toList());

        Set<Long> savedJobIds = new HashSet<>();
        savedJobRepository.findByUserEmailOrderBySavedAtDesc(email)
                .forEach(sj -> savedJobIds.add(sj.getJob().getId()));

        Set<Long> appliedJobIds = new HashSet<>();
        applicationRepository.findByApplicantEmailOrderByAppliedAtDesc(email)
                .forEach(app -> appliedJobIds.add(app.getJob().getId()));

        model.addAttribute("internships", filtered);
        model.addAttribute("savedJobIds", savedJobIds);
        model.addAttribute("appliedJobIds", appliedJobIds);
        model.addAttribute("totalInternshipCount", filtered.size());
        model.addAttribute("searchQuery", search);
        model.addAttribute("selectedDomain", domain);
        model.addAttribute("selectedDuration", duration);
        model.addAttribute("selectedMode", mode);
        model.addAttribute("selectedLocation", location);
        model.addAttribute("userName", session.getAttribute("userName"));
        model.addAttribute("userRole", session.getAttribute("userRole"));

        return "internships";
    }

    @GetMapping("/internships/{id}")
    public String internshipDetails(
            @PathVariable Long id,
            HttpSession session,
            Model model) {

        String email = (String) session.getAttribute("userEmail");
        if (email == null) return "redirect:/login";

        Job internship = jobRepository.findById(id).orElse(null);
        if (internship == null) return "redirect:/internships";

        boolean hasApplied = applicationRepository.existsByJobIdAndApplicantEmail(id, email);
        boolean hasSaved = savedJobRepository.existsByJobIdAndUserEmail(id, email);

        model.addAttribute("internship", internship);
        model.addAttribute("job", internship);
        model.addAttribute("hasApplied", hasApplied);
        model.addAttribute("hasSaved", hasSaved);
        model.addAttribute("recommendedInternships", jobRepository.findTop6ByIdNotOrderByCreatedAtDesc(id));
        model.addAttribute("userName", session.getAttribute("userName"));
        model.addAttribute("userRole", session.getAttribute("userRole"));

        return "internship-details";
    }

    @GetMapping({"/post-internship", "/recruiter/internships/create"})
    public String showPostInternshipForm(HttpSession session, Model model) {
        String email = (String) session.getAttribute("userEmail");
        if (email == null) return "redirect:/login";

        String role = (String) session.getAttribute("userRole");
        if (!"Recruiter".equalsIgnoreCase(role) && !"Admin".equalsIgnoreCase(role)) {
            return "redirect:/dashboard";
        }

        Job internship = new Job();
        internship.setType("Internship");
        model.addAttribute("internship", internship);
        model.addAttribute("job", internship);
        model.addAttribute("userEmail", email);
        model.addAttribute("userName", session.getAttribute("userName"));
        model.addAttribute("userRole", role);

        companyRepository.findFirstByRecruiterEmailOrderByCreatedAtDesc(email)
                .ifPresent(company -> model.addAttribute("defaultCompany", company.getName()));

        return "create_internship";
    }

    @PostMapping({"/post-internship", "/recruiter/internships/create"})
    public String postInternship(
            @ModelAttribute("internship") Job internship,
            BindingResult bindingResult,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        String email = (String) session.getAttribute("userEmail");
        if (email == null) return "redirect:/login";

        String role = (String) session.getAttribute("userRole");
        if (!"Recruiter".equalsIgnoreCase(role) && !"Admin".equalsIgnoreCase(role)) {
            return "redirect:/dashboard";
        }

        if (internship.getTitle() == null || internship.getTitle().trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Internship title is required.");
            return "redirect:/post-internship";
        }
        if (internship.getCompany() == null || internship.getCompany().trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Company name is required.");
            return "redirect:/post-internship";
        }

        try {
            internship.setTitle(internship.getTitle().trim());
            internship.setCompany(internship.getCompany().trim());
            internship.setType("Internship");
            if (internship.getLocation() == null || internship.getLocation().isBlank()) internship.setLocation("Remote");
            if (internship.getDuration() == null || internship.getDuration().isBlank()) internship.setDuration("3-6 Months");
            if (internship.getStipend() == null || internship.getStipend().isBlank()) internship.setStipend("₹25,000 / month");
            internship.setRecruiterEmail(email);
            internship.setActive(true);
            internship.setStatus("Approved");
            internship.setCreatedAt(LocalDateTime.now());

            Job saved = jobRepository.save(internship);

            if (internship.getCompany() != null && !companyRepository.existsByNameIgnoreCase(internship.getCompany())) {
                companyRepository.save(new Company(internship.getCompany(), "", "Growing Company", "Technology", internship.getLocation(), email, "Company profile created on HireHub."));
            }

            notificationService.sendNotification(email, "Internship Published Successfully",
                    "Your internship opportunity '" + saved.getTitle() + "' is now live on HireHub AI.", "SYSTEM", "/internships");

            redirectAttributes.addFlashAttribute("successMessage", "Internship opportunity published successfully!");
            return "redirect:/internships";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to publish internship: " + e.getMessage());
            return "redirect:/post-internship";
        }
    }
}

