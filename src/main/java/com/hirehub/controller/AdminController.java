package com.hirehub.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.hirehub.model.Company;
import com.hirehub.model.Job;
import com.hirehub.model.JobApplication;
import com.hirehub.model.UserAccount;
import com.hirehub.repository.CompanyRepository;
import com.hirehub.repository.JobApplicationRepository;
import com.hirehub.repository.JobRepository;
import com.hirehub.repository.UserAccountRepository;
import com.hirehub.repository.UserProfileRepository;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final UserAccountRepository accountRepository;
    private final UserProfileRepository profileRepository;
    private final CompanyRepository companyRepository;
    private final JobRepository jobRepository;
    private final JobApplicationRepository applicationRepository;

    public AdminController(
            UserAccountRepository accountRepository,
            UserProfileRepository profileRepository,
            CompanyRepository companyRepository,
            JobRepository jobRepository,
            JobApplicationRepository applicationRepository) {
        this.accountRepository = accountRepository;
        this.profileRepository = profileRepository;
        this.companyRepository = companyRepository;
        this.jobRepository = jobRepository;
        this.applicationRepository = applicationRepository;
    }

    @GetMapping({"", "/dashboard"})
    public String adminDashboard(HttpSession session, Model model) {
        String email = (String) session.getAttribute("userEmail");
        if (email == null) return "redirect:/login";

        long totalUsers = accountRepository.count();
        long totalStudents = accountRepository.countByRoleIgnoreCase("Student");
        long totalRecruiters = accountRepository.countByRoleIgnoreCase("Recruiter");
        long totalCompanies = companyRepository.count();
        long totalJobs = jobRepository.countByTypeNotIgnoreCase("Internship");
        long totalInternships = jobRepository.countByTypeIgnoreCase("Internship");
        long activeJobs = jobRepository.countByActiveTrue();
        long totalApplications = applicationRepository.count();
        long selectedCandidates = applicationRepository.countByStatusIgnoreCase("Selected");
        long pendingApprovals = jobRepository.countByStatusIgnoreCase("Pending Approval");

        List<UserAccount> users = accountRepository.findAllByOrderByCreatedAtDesc();
        List<Job> jobs = jobRepository.findAllByOrderByCreatedAtDesc();
        List<Company> companies = companyRepository.findAllByOrderByNameAsc();
        List<JobApplication> applications = applicationRepository.findAllByOrderByAppliedAtDesc();

        model.addAttribute("totalUsers", totalUsers);
        model.addAttribute("totalStudents", totalStudents);
        model.addAttribute("totalCandidates", totalStudents);
        model.addAttribute("totalRecruiters", totalRecruiters);
        model.addAttribute("totalCompanies", totalCompanies);
        model.addAttribute("totalJobs", totalJobs);
        model.addAttribute("totalInternships", totalInternships);
        model.addAttribute("activeJobs", activeJobs);
        model.addAttribute("totalApplications", totalApplications);
        model.addAttribute("selectedCandidates", selectedCandidates);
        model.addAttribute("pendingApprovals", pendingApprovals);

        model.addAttribute("users", users);
        model.addAttribute("jobs", jobs);
        model.addAttribute("companies", companies);
        model.addAttribute("applications", applications);

        model.addAttribute("userEmail", email);
        model.addAttribute("userName", session.getAttribute("userName"));
        model.addAttribute("userRole", session.getAttribute("userRole"));

        return "admin-dashboard";
    }

    @GetMapping("/jobs")
    public String adminJobs(
            @RequestParam(required = false) String status,
            HttpSession session,
            Model model) {

        String email = (String) session.getAttribute("userEmail");
        if (email == null) return "redirect:/login";

        List<Job> jobs;
        if (status != null && !status.isBlank() && !status.equalsIgnoreCase("all")) {
            jobs = jobRepository.findByStatusOrderByCreatedAtDesc(status).stream()
                    .filter(j -> !j.isInternship())
                    .toList();
        } else {
            jobs = jobRepository.findAllByOrderByCreatedAtDesc().stream()
                    .filter(j -> !j.isInternship())
                    .toList();
        }

        model.addAttribute("jobs", jobs);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("userName", session.getAttribute("userName"));
        model.addAttribute("userRole", session.getAttribute("userRole"));

        return "admin-jobs";
    }

    @GetMapping("/internships")
    public String adminInternships(
            @RequestParam(required = false) String status,
            HttpSession session,
            Model model) {

        String email = (String) session.getAttribute("userEmail");
        if (email == null) return "redirect:/login";

        List<Job> internships;
        if (status != null && !status.isBlank() && !status.equalsIgnoreCase("all")) {
            internships = jobRepository.findByStatusOrderByCreatedAtDesc(status).stream()
                    .filter(Job::isInternship)
                    .toList();
        } else {
            internships = jobRepository.findAllByOrderByCreatedAtDesc().stream()
                    .filter(Job::isInternship)
                    .toList();
        }

        model.addAttribute("internships", internships);
        model.addAttribute("jobs", internships);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("userName", session.getAttribute("userName"));
        model.addAttribute("userRole", session.getAttribute("userRole"));

        return "admin-jobs";
    }

    @PostMapping("/jobs/{id}/approve")
    public String approveJob(@PathVariable Long id, HttpSession session, RedirectAttributes redirectAttributes) {
        if (!"Admin".equalsIgnoreCase((String) session.getAttribute("userRole"))) return "redirect:/login";

        jobRepository.findById(id).ifPresent(j -> {
            j.setStatus("Approved");
            j.setActive(true);
            jobRepository.save(j);
            redirectAttributes.addFlashAttribute("successMessage", "Opportunity approved successfully.");
        });

        return "redirect:/admin/dashboard";
    }

    @PostMapping("/jobs/{id}/reject")
    public String rejectJob(@PathVariable Long id, HttpSession session, RedirectAttributes redirectAttributes) {
        if (!"Admin".equalsIgnoreCase((String) session.getAttribute("userRole"))) return "redirect:/login";

        jobRepository.findById(id).ifPresent(j -> {
            j.setStatus("Rejected");
            j.setActive(false);
            jobRepository.save(j);
            redirectAttributes.addFlashAttribute("successMessage", "Opportunity rejected.");
        });

        return "redirect:/admin/dashboard";
    }

    @GetMapping("/users")
    public String adminUsers(HttpSession session, Model model) {
        String email = (String) session.getAttribute("userEmail");
        if (email == null) return "redirect:/login";

        List<UserAccount> candidates = accountRepository.findByRoleIgnoreCase("Student");
        List<UserAccount> recruiters = accountRepository.findByRoleIgnoreCase("Recruiter");

        model.addAttribute("candidates", candidates);
        model.addAttribute("recruiters", recruiters);
        model.addAttribute("totalCandidates", candidates.size());
        model.addAttribute("totalRecruiters", recruiters.size());
        model.addAttribute("userName", session.getAttribute("userName"));
        model.addAttribute("userRole", session.getAttribute("userRole"));

        return "admin-users";
    }

    @GetMapping("/applications")
    public String adminApplications(HttpSession session, Model model) {
        String email = (String) session.getAttribute("userEmail");
        if (email == null) return "redirect:/login";

        List<JobApplication> applications = applicationRepository.findAllByOrderByAppliedAtDesc();

        model.addAttribute("applications", applications);
        model.addAttribute("totalApplications", applications.size());
        model.addAttribute("userName", session.getAttribute("userName"));
        model.addAttribute("userRole", session.getAttribute("userRole"));

        return "admin-dashboard";
    }

    @PostMapping("/users/{email}/toggle")
    public String toggleUserStatus(@PathVariable String email, HttpSession session, RedirectAttributes redirectAttributes) {
        if (!"Admin".equalsIgnoreCase((String) session.getAttribute("userRole"))) {
            return "redirect:/login";
        }

        accountRepository.findById(email).ifPresent(account -> {
            account.setEnabled(!account.isEnabled());
            accountRepository.save(account);
            redirectAttributes.addFlashAttribute("successMessage", "User account " + (account.isEnabled() ? "activated" : "deactivated") + ".");
        });

        return "redirect:/admin/dashboard";
    }

    @PostMapping("/users/{email}/delete")
    public String deleteUser(@PathVariable String email, HttpSession session, RedirectAttributes redirectAttributes) {
        if (!"Admin".equalsIgnoreCase((String) session.getAttribute("userRole"))) {
            return "redirect:/login";
        }

        accountRepository.deleteById(email);
        profileRepository.deleteById(email);
        redirectAttributes.addFlashAttribute("successMessage", "User deleted successfully.");
        return "redirect:/admin/dashboard";
    }

    @PostMapping("/jobs/{id}/delete")
    public String deleteJobAdmin(@PathVariable Long id, HttpSession session, RedirectAttributes redirectAttributes) {
        if (!"Admin".equalsIgnoreCase((String) session.getAttribute("userRole"))) {
            return "redirect:/login";
        }

        jobRepository.deleteById(id);
        redirectAttributes.addFlashAttribute("successMessage", "Job posting removed by Admin.");
        return "redirect:/admin/dashboard";
    }
}
