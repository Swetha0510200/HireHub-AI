package com.hirehub.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.hirehub.model.Interview;
import com.hirehub.repository.InterviewRepository;

import jakarta.servlet.http.HttpSession;

@Controller
public class InterviewController {

    private final InterviewRepository interviewRepository;

    public InterviewController(InterviewRepository interviewRepository) {
        this.interviewRepository = interviewRepository;
    }

    @GetMapping("/interviews")
    public String interviews(HttpSession session, Model model) {
        String email = (String) session.getAttribute("userEmail");
        if (email == null) {
            return "redirect:/login";
        }

        String role = (String) session.getAttribute("userRole");
        List<Interview> interviews;
        if ("Recruiter".equalsIgnoreCase(role)) {
            interviews = interviewRepository.findByRecruiterEmailOrderByInterviewDateDesc(email);
        } else {
            interviews = interviewRepository.findByCandidateEmailOrderByInterviewDateDesc(email);
        }

        model.addAttribute("interviews", interviews);
        model.addAttribute("totalInterviews", interviews.size());
        model.addAttribute("userName", session.getAttribute("userName"));
        model.addAttribute("userRole", role != null ? role : "Student");

        return "interviews";
    }
}
