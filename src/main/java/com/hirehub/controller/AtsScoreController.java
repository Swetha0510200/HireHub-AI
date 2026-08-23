package com.hirehub.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.hirehub.model.ResumeRecord;
import com.hirehub.repository.ResumeRecordRepository;

import jakarta.servlet.http.HttpSession;

@Controller
public class AtsScoreController {

    private final ResumeRecordRepository resumeRepository;

    public AtsScoreController(ResumeRecordRepository resumeRepository) {
        this.resumeRepository = resumeRepository;
    }

    @GetMapping("/ats-score")
    public String atsScore(HttpSession session, Model model) {
        String email = (String) session.getAttribute("userEmail");
        if (email == null) return "redirect:/login";

        ResumeRecord record = resumeRepository
                .findFirstByUserEmailOrderByUploadedAtDesc(email)
                .orElse(null);

        int atsScore = record == null || record.getAtsScore() == null
                ? 0 : record.getAtsScore();

        int formattingScore = record == null || record.getLengthScore() == null
                ? 0 : Math.min(100, record.getLengthScore() * 100 / 15);

        int keywordScore = record == null || record.getAtsStructureScore() == null
                ? 0 : record.getAtsStructureScore() * 10;

        int skillScore = record == null || record.getSkillScore() == null
                ? 0 : record.getSkillScore() * 5;

        int experienceScore = record == null || record.getAchievementScore() == null
                ? 0 : record.getAchievementScore() * 5;

        int projectScore = record == null || record.getSectionScore() == null
                ? 0 : Math.min(100, record.getSectionScore() * 4);

        int educationScore = record == null || record.getSectionScore() == null
                ? 0 : Math.min(100, record.getSectionScore() * 4);

        model.addAttribute("atsScore", atsScore);
        model.addAttribute("formattingScore", formattingScore);
        model.addAttribute("keywordScore", keywordScore);
        model.addAttribute("skillScore", skillScore);
        model.addAttribute("experienceScore", experienceScore);
        model.addAttribute("projectScore", projectScore);
        model.addAttribute("educationScore", educationScore);
        model.addAttribute("hasResume", record != null);
        model.addAttribute("resumeName",
                record == null ? null : record.getFileName());
        model.addAttribute("userName", session.getAttribute("userName"));
        model.addAttribute("userRole", session.getAttribute("userRole"));

        return "ats-score";
    }
}
