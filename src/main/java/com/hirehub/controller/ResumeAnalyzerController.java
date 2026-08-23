package com.hirehub.controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.hirehub.model.ResumeRecord;
import com.hirehub.repository.ResumeRecordRepository;
import com.hirehub.service.ResumeAnalyzerService;

import jakarta.servlet.http.HttpSession;

@Controller
public class ResumeAnalyzerController {

    private final ResumeAnalyzerService resumeAnalyzerService;
    private final ResumeRecordRepository resumeRepository;

    public ResumeAnalyzerController(
            ResumeAnalyzerService resumeAnalyzerService,
            ResumeRecordRepository resumeRepository) {
        this.resumeAnalyzerService = resumeAnalyzerService;
        this.resumeRepository = resumeRepository;
    }

    @GetMapping("/resume-analyzer")
    public String resumeAnalyzer(HttpSession session, Model model) {
        String email = (String) session.getAttribute("userEmail");
        if (email == null) return "redirect:/login";

        addLatestAnalysis(email, model);
        addUser(session, model);
        return "resume-analyzer";
    }

    @PostMapping("/analyze-resume")
    public String analyzeResume(
            @RequestParam("resume") MultipartFile resume,
            HttpSession session,
            Model model) {

        String email = (String) session.getAttribute("userEmail");
        if (email == null) return "redirect:/login";

        try {
            if (resume == null || resume.isEmpty()) {
                model.addAttribute("error", "Please select a resume file.");
                addLatestAnalysis(email, model);
                addUser(session, model);
                return "resume-analyzer";
            }

            String originalName = resume.getOriginalFilename();
            if (originalName == null ||
                    !(originalName.toLowerCase().endsWith(".pdf")
                    || originalName.toLowerCase().endsWith(".doc")
                    || originalName.toLowerCase().endsWith(".docx"))) {
                model.addAttribute("error", "Only PDF, DOC and DOCX resumes are supported.");
                addUser(session, model);
                return "resume-analyzer";
            }

            String resumeText = resumeAnalyzerService.extractText(resume);

            if (resumeText == null || resumeText.trim().isEmpty()) {
                model.addAttribute("error",
                        "The file was uploaded but no readable text was found.");
                addUser(session, model);
                return "resume-analyzer";
            }

            Map<String, Object> result =
                    resumeAnalyzerService.analyzeResume(resumeText);

            ResumeRecord record = new ResumeRecord();
            record.setUserEmail(email);
            record.setFileName(originalName);
            record.setContentType(
                    resume.getContentType() == null
                            ? MediaType.APPLICATION_OCTET_STREAM_VALUE
                            : resume.getContentType());
            record.setFileData(resume.getBytes());
            record.setUploadedAt(LocalDateTime.now());

            record.setAtsScore(asInt(result.get("atsScore")));
            record.setResumeQuality(asString(result.get("resumeQuality")));
            record.setSummary(asString(result.get("summary")));
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

            putResultInModel(model, result, originalName);
            addUser(session, model);
            model.addAttribute("resumeId", record.getId());

            return "resume-analyzer";

        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error",
                    "Unable to analyze resume: " + e.getMessage());
            addLatestAnalysis(email, model);
            addUser(session, model);
            return "resume-analyzer";
        }
    }

    @GetMapping("/resume/{id}/download")
    public ResponseEntity<byte[]> downloadResume(
            @PathVariable Long id,
            HttpSession session) {

        String email = (String) session.getAttribute("userEmail");
        if (email == null) {
            return ResponseEntity.status(302)
                    .header(HttpHeaders.LOCATION, "/login")
                    .build();
        }

        ResumeRecord record = resumeRepository.findById(id).orElse(null);

        if (record == null || !email.equals(record.getUserEmail())) {
            return ResponseEntity.notFound().build();
        }

        MediaType type;
        try {
            type = MediaType.parseMediaType(record.getContentType());
        } catch (Exception e) {
            type = MediaType.APPLICATION_OCTET_STREAM;
        }

        return ResponseEntity.ok()
                .contentType(type)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + safeFileName(record.getFileName()) + "\"")
                .body(record.getFileData());
    }

    private void addLatestAnalysis(String email, Model model) {
        resumeRepository.findFirstByUserEmailOrderByUploadedAtDesc(email)
                .ifPresent(record -> {
                    model.addAttribute("analysis", record);
                    model.addAttribute("resumeName", record.getFileName());
                    model.addAttribute("atsScore", record.getAtsScore());
                    model.addAttribute("resumeQuality", record.getResumeQuality());
                    model.addAttribute("summary", record.getSummary());
                    model.addAttribute("skills",
                            split(record.getSkills()));
                    model.addAttribute("strengths",
                            split(record.getStrengths()));
                    model.addAttribute("improvements",
                            split(record.getImprovements()));
                    model.addAttribute("wordCount", record.getWordCount());
                    model.addAttribute("skillCount", record.getSkillCount());
                    model.addAttribute("lineCount", record.getLineCount());
                    model.addAttribute("quantifiedAchievements",
                            record.getQuantifiedAchievements());
                    model.addAttribute("actionVerbUsage",
                            record.getActionVerbUsage());
                    model.addAttribute("sectionScore", record.getSectionScore());
                    model.addAttribute("skillScore", record.getSkillScore());
                    model.addAttribute("achievementScore",
                            record.getAchievementScore());
                    model.addAttribute("lengthScore", record.getLengthScore());
                    model.addAttribute("atsStructureScore",
                            record.getAtsStructureScore());
                    model.addAttribute("resumeId", record.getId());
                });
    }

    private void putResultInModel(
            Model model, Map<String, Object> result, String resumeName) {

        model.addAttribute("analysis", result);
        model.addAttribute("resumeName", resumeName);

        for (String key : List.of(
                "atsScore", "resumeQuality", "summary", "skills", "strengths",
                "improvements", "wordCount", "skillCount", "lineCount",
                "quantifiedAchievements", "actionVerbUsage", "sectionScore",
                "skillScore", "achievementScore", "lengthScore",
                "atsStructureScore", "lineFeedback")) {
            model.addAttribute(key, result.get(key));
        }
    }

    private void addUser(HttpSession session, Model model) {
        model.addAttribute("userEmail", session.getAttribute("userEmail"));
        model.addAttribute("userRole", session.getAttribute("userRole"));
        model.addAttribute("userName", session.getAttribute("userName"));
    }

    private int asInt(Object value) {
        return value instanceof Number ? ((Number) value).intValue() : 0;
    }

    private String asString(Object value) {
        return value == null ? "" : value.toString();
    }

    private String join(Object value) {
        if (value instanceof List<?> list) {
            return String.join("||",
                    list.stream().map(Object::toString).toList());
        }
        return asString(value);
    }

    private List<String> split(String value) {
        if (value == null || value.isBlank()) return List.of();
        return List.of(value.split("\\|\\|"));
    }

    private String safeFileName(String name) {
        if (name == null || name.isBlank()) return "resume";
        return name.replaceAll("[\\\\/:*?\"<>|]", "_");
    }
}
