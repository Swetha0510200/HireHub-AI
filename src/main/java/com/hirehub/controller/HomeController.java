package com.hirehub.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.hirehub.model.Job;
import com.hirehub.repository.CompanyRepository;
import com.hirehub.repository.JobApplicationRepository;
import com.hirehub.repository.JobRepository;
import com.hirehub.repository.UserAccountRepository;

import jakarta.servlet.http.HttpSession;

@Controller
public class HomeController {

    private final JobRepository jobRepository;
    private final CompanyRepository companyRepository;
    private final UserAccountRepository accountRepository;
    private final JobApplicationRepository applicationRepository;

    public HomeController(JobRepository jobRepository,
                          CompanyRepository companyRepository,
                          UserAccountRepository accountRepository,
                          JobApplicationRepository applicationRepository) {
        this.jobRepository = jobRepository;
        this.companyRepository = companyRepository;
        this.accountRepository = accountRepository;
        this.applicationRepository = applicationRepository;
    }

    @GetMapping("/")
    public String home(HttpSession session, Model model) {
        String email = (String) session.getAttribute("userEmail");
        String role = (String) session.getAttribute("userRole");
        String userName = (String) session.getAttribute("userName");

        List<Job> allActive = jobRepository.findByActiveTrueOrderByCreatedAtDesc();
        if (allActive.isEmpty()) {
            allActive = jobRepository.findAllByOrderByCreatedAtDesc();
        }

        List<Job> featuredJobs = allActive.stream()
                .filter(j -> !j.isInternship())
                .limit(6)
                .collect(Collectors.toList());

        List<Job> featuredInternships = allActive.stream()
                .filter(Job::isInternship)
                .limit(6)
                .collect(Collectors.toList());

        long totalJobs = jobRepository.countByTypeNotIgnoreCase("Internship");
        long totalInternships = jobRepository.countByTypeIgnoreCase("Internship");
        long totalCompanies = companyRepository.count();
        long totalCandidates = accountRepository.countByRoleIgnoreCase("Student");
        long totalPlaced = applicationRepository.countByStatusIgnoreCase("Selected");

        model.addAttribute("featuredJobs", featuredJobs);
        model.addAttribute("featuredInternships", featuredInternships);
        model.addAttribute("totalJobs", totalJobs > 0 ? totalJobs : 15);
        model.addAttribute("totalInternships", totalInternships > 0 ? totalInternships : 12);
        model.addAttribute("totalCompanies", totalCompanies > 0 ? totalCompanies : 8);
        model.addAttribute("totalCandidates", totalCandidates > 0 ? totalCandidates : 120);
        model.addAttribute("totalPlaced", totalPlaced > 0 ? totalPlaced : 45);

        model.addAttribute("userEmail", email);
        model.addAttribute("userName", userName);
        model.addAttribute("userRole", role);

        return "index";
    }

    @GetMapping("/register")
    public String register(HttpSession session) {
        if (session.getAttribute("userEmail") != null) {
            return "redirect:/";
        }
        return "register";
    }
}