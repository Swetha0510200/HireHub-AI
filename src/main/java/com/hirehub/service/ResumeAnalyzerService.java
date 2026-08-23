package com.hirehub.service;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ResumeAnalyzerService {

    // ============================================================
    // 1. EXTRACT RESUME TEXT
    // ============================================================

    public String extractText(MultipartFile file) throws Exception {
        if (file == null || file.isEmpty()) {
            return "";
        }

        String fileName = file.getOriginalFilename();
        if (fileName == null) {
            throw new IllegalArgumentException("Please upload a valid resume file.");
        }

        String lowerName = fileName.toLowerCase();

        if (lowerName.endsWith(".docx")) {
            return extractDocxText(file.getInputStream());
        }

        if (lowerName.endsWith(".pdf")) {
            return extractPdfText(file.getInputStream());
        }

        if (lowerName.endsWith(".doc")) {
            return extractDocOrPlainText(file);
        }

        if (lowerName.endsWith(".txt")) {
            return new String(file.getBytes(), StandardCharsets.UTF_8);
        }

        throw new IllegalArgumentException("Please upload a PDF, DOC, DOCX, or TXT resume.");
    }

    private String extractDocxText(InputStream inputStream) throws Exception {
        StringBuilder text = new StringBuilder();
        try (XWPFDocument document = new XWPFDocument(inputStream)) {
            document.getParagraphs().forEach(paragraph -> {
                String paragraphText = paragraph.getText();
                if (paragraphText != null && !paragraphText.trim().isEmpty()) {
                    text.append(paragraphText.trim()).append("\n");
                }
            });

            for (XWPFTable table : document.getTables()) {
                table.getRows().forEach(row -> {
                    row.getTableCells().forEach(cell -> {
                        String cellText = cell.getText();
                        if (cellText != null && !cellText.trim().isEmpty()) {
                            text.append(cellText.trim()).append("\n");
                        }
                    });
                });
            }
        }
        return text.toString().trim();
    }

    private String extractPdfText(InputStream inputStream) throws Exception {
        try (PDDocument document = PDDocument.load(inputStream)) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document).trim();
        }
    }

    private String extractDocOrPlainText(MultipartFile file) {
        try {
            byte[] bytes = file.getBytes();
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                if ((b >= 32 && b <= 126) || b == 10 || b == 13 || b == 9) {
                    sb.append((char) b);
                }
            }
            return sb.toString().replaceAll("\\s+", " ").trim();
        } catch (Exception e) {
            return "";
        }
    }

    // ============================================================
    // 2. MAIN RESUME ANALYSIS
    // ============================================================

    public Map<String, Object> analyzeResume(String text) {
        Map<String, Object> result = new HashMap<>();

        if (text == null) {
            text = "";
        }

        String resume = text.replaceAll("\\r", "\n")
                            .replaceAll("[ \\t]+", " ")
                            .trim();

        String lowerResume = resume.toLowerCase();

        // 1. Basic Stats
        int wordCount = countWords(resume);
        int lineCount = countLines(resume);

        List<String> lines = Arrays.stream(resume.split("\\n"))
                .map(String::trim)
                .filter(line -> !line.isEmpty())
                .toList();

        // 2. Skill Detection (Industry Skill Base)
        String[] skillDatabase = {
            "java", "python", "c", "c++", "c#", "javascript", "typescript", "php", "ruby", "golang", "go", "rust",
            "html", "html5", "css", "css3", "react", "react.js", "angular", "vue", "vue.js", "node.js", "nodejs",
            "express", "express.js", "next.js", "tailwind", "bootstrap", "sass", "jquery",
            "spring", "spring boot", "spring data jpa", "hibernate", "jpa", "rest api", "restful api", "microservices",
            "django", "flask", "fastapi", "asp.net", "laravel",
            "sql", "mysql", "postgresql", "postgres", "mongodb", "oracle", "sqlite", "redis", "cassandra",
            "aws", "azure", "gcp", "google cloud", "docker", "kubernetes", "jenkins", "ci/cd", "terraform", "linux",
            "git", "github", "gitlab", "bitbucket", "jira", "maven", "gradle", "postman", "intellij", "vscode",
            "data structures", "algorithms", "oops", "system design", "dbms", "operating systems", "networking",
            "machine learning", "deep learning", "artificial intelligence", "nlp", "computer vision", "pandas",
            "numpy", "tensorflow", "pytorch", "scikit-learn", "keras", "data analysis", "tableau", "power bi",
            "agile", "scrum", "unit testing", "junit", "mockito", "problem solving", "leadership", "communication"
        };

        Set<String> detectedSkills = new LinkedHashSet<>();
        for (String skill : skillDatabase) {
            if (containsKeyword(lowerResume, skill)) {
                detectedSkills.add(formatSkillName(skill));
            }
        }

        // 3. Section Analysis
        boolean hasContact = containsEmail(lowerResume) || containsPhone(lowerResume);
        boolean hasSummary = containsAny(lowerResume, "summary", "objective", "profile", "about me", "professional summary");
        boolean hasEducation = containsAny(lowerResume, "education", "academic", "degree", "university", "college", "b.tech", "b.e", "btech", "mca", "bca", "bsc", "master");
        boolean hasExperience = containsAny(lowerResume, "experience", "work experience", "professional experience", "internship", "employment", "work history");
        boolean hasProjects = containsAny(lowerResume, "projects", "project", "key projects", "academic projects", "personal projects");
        boolean hasSkills = containsAny(lowerResume, "skills", "technical skills", "technologies", "tech stack", "tools & technologies", "competencies");
        boolean hasCertifications = containsAny(lowerResume, "certification", "certifications", "certified", "licenses", "courses");
        boolean hasAchievements = containsAny(lowerResume, "achievement", "achievements", "awards", "honors", "accomplishments", "publications");
        boolean hasLinks = containsAny(lowerResume, "linkedin", "github", "portfolio", "leetCode", "hackerrank");

        // Section Score (max 25)
        int sectionScore = 0;
        if (hasContact) sectionScore += 5;
        if (hasEducation) sectionScore += 4;
        if (hasExperience) sectionScore += 4;
        if (hasProjects) sectionScore += 4;
        if (hasSkills) sectionScore += 4;
        if (hasSummary) sectionScore += 2;
        if (hasCertifications || hasAchievements) sectionScore += 2;
        sectionScore = Math.min(sectionScore, 25);

        // Skill Score (max 20)
        int skillCount = detectedSkills.size();
        int skillScore;
        if (skillCount >= 12) skillScore = 20;
        else if (skillCount >= 9) skillScore = 17;
        else if (skillCount >= 6) skillScore = 14;
        else if (skillCount >= 4) skillScore = 10;
        else if (skillCount >= 2) skillScore = 6;
        else skillScore = 2;

        // Experience & Achievements Score (max 20)
        int achievementScore = 0;
        int quantifiedLines = countQuantifiedAchievements(lines);
        int actionVerbLines = countActionVerbLines(lines);

        if (quantifiedLines >= 5) achievementScore += 10;
        else if (quantifiedLines >= 3) achievementScore += 8;
        else if (quantifiedLines >= 1) achievementScore += 5;

        if (actionVerbLines >= 8) achievementScore += 10;
        else if (actionVerbLines >= 5) achievementScore += 8;
        else if (actionVerbLines >= 2) achievementScore += 5;
        achievementScore = Math.min(achievementScore, 20);

        // Length Score (max 15)
        int lengthScore;
        if (wordCount >= 350 && wordCount <= 950) lengthScore = 15;
        else if (wordCount >= 250 && wordCount < 350) lengthScore = 12;
        else if (wordCount >= 180) lengthScore = 9;
        else if (wordCount >= 100) lengthScore = 6;
        else lengthScore = 3;

        // ATS Structure Score (max 20)
        int atsStructureScore = 0;
        if (hasSkills) atsStructureScore += 4;
        if (hasProjects) atsStructureScore += 4;
        if (hasEducation) atsStructureScore += 4;
        if (hasExperience) atsStructureScore += 4;
        if (hasLinks) atsStructureScore += 2;
        if (hasContact) atsStructureScore += 2;
        atsStructureScore = Math.min(atsStructureScore, 20);

        // Final ATS Score (Sum out of 100)
        int atsScore = sectionScore + skillScore + achievementScore + lengthScore + atsStructureScore;
        atsScore = Math.max(10, Math.min(atsScore, 100));

        // Quality Classification
        String quality;
        if (atsScore >= 88) quality = "Outstanding";
        else if (atsScore >= 78) quality = "Excellent";
        else if (atsScore >= 68) quality = "Strong";
        else if (atsScore >= 55) quality = "Good";
        else if (atsScore >= 40) quality = "Needs Improvement";
        else quality = "Weak";

        // Summary
        String summary;
        if (atsScore >= 80) {
            summary = "This resume demonstrates strong ATS compatibility with well-structured sections, high-demand technical skills, and clear achievement metrics.";
        } else if (atsScore >= 65) {
            summary = "The resume has a solid technical foundation. Adding more quantified achievements, action verbs, and matching keywords will maximize interview callbacks.";
        } else if (atsScore >= 50) {
            summary = "The resume covers basic credentials but lacks essential ATS formatting, measurable outcomes, or detailed project technologies.";
        } else {
            summary = "The resume needs significant restructuring. Ensure proper headings for Skills, Projects, Education, and Experience, and include clear contact details.";
        }

        // Strengths
        List<String> strengths = new ArrayList<>();
        if (hasContact) strengths.add("Contact details and communication channels are clearly identifiable.");
        if (skillCount >= 6) strengths.add("Strong technical keyword coverage with " + skillCount + " detected skills.");
        else if (skillCount > 0) strengths.add(skillCount + " core technical skills detected.");
        if (hasProjects) strengths.add("Dedicated Projects section highlighting practical problem-solving.");
        if (hasExperience) strengths.add("Relevant professional or internship experience included.");
        if (hasEducation) strengths.add("Academic background and qualifications are well structured.");
        if (quantifiedLines > 0) strengths.add(quantifiedLines + " bullet point(s) contain measurable impact or metrics.");
        if (hasLinks) strengths.add("Professional profiles (LinkedIn/GitHub/Portfolio) are linked.");

        // Improvements
        List<String> improvements = new ArrayList<>();
        if (!hasContact) improvements.add("Add email address, phone number, and city/state location at the top.");
        if (!hasSummary) improvements.add("Add a 2-3 line professional summary highlighting your key focus and strengths.");
        if (!hasSkills) improvements.add("Organize technical skills under categorized headings (Languages, Frameworks, Databases, Tools).");
        if (skillCount < 6) improvements.add("Incorporate more industry-standard keywords related to your target role.");
        if (!hasProjects) improvements.add("Include at least 2 impactful projects with architecture, tools used, and live/demo links.");
        if (quantifiedLines < 3) improvements.add("Quantify your achievements using metrics (e.g. 'Improved speed by 35%', 'Served 500+ users').");
        if (actionVerbLines < 5) improvements.add("Begin bullet points with strong action verbs (Engineered, Architected, Optimized, Deployed).");
        if (!hasLinks) improvements.add("Add your GitHub and LinkedIn profile URLs for recruiter verification.");
        if (wordCount < 200) improvements.add("Your resume is brief. Elaborate on project implementations, responsibilities, and technologies.");

        if (improvements.isEmpty()) {
            improvements.add("Resume is well-optimized for ATS scanners. Keep tailoring keywords to specific job postings.");
        }

        // Line-by-line Feedback
        List<String> lineFeedback = new ArrayList<>();
        int lineNumber = 1;
        for (String line : lines) {
            if (line.length() < 3) {
                lineNumber++;
                continue;
            }

            String feedback;
            String lCase = line.toLowerCase();
            if (containsEmail(lCase) || containsPhone(lCase)) {
                feedback = "Line " + lineNumber + ": Contact information detected.";
            } else if (containsQuantification(line)) {
                feedback = "Line " + lineNumber + ": Strong measurable achievement detected.";
            } else if (containsActionVerb(line)) {
                feedback = "Line " + lineNumber + ": Action-oriented achievement statement detected.";
            } else if (containsAny(lCase, "education", "experience", "skills", "projects", "certifications", "achievements", "summary")) {
                feedback = "Line " + lineNumber + ": Resume section heading detected.";
            } else if (line.length() > 180) {
                feedback = "Line " + lineNumber + ": Consider breaking this long bullet point into concise statements.";
            } else {
                feedback = "Line " + lineNumber + ": Content detected and parsed for ATS scoring.";
            }
            lineFeedback.add(feedback);
            lineNumber++;
        }

        result.put("atsScore", atsScore);
        result.put("resumeQuality", quality);
        result.put("summary", summary);
        result.put("skills", new ArrayList<>(detectedSkills));
        result.put("strengths", strengths);
        result.put("improvements", improvements);
        result.put("lineFeedback", lineFeedback);
        result.put("wordCount", wordCount);
        result.put("lineCount", lineCount);
        result.put("skillCount", skillCount);
        result.put("quantifiedAchievements", quantifiedLines);
        result.put("actionVerbUsage", actionVerbLines);
        result.put("sectionScore", sectionScore);
        result.put("skillScore", skillScore);
        result.put("achievementScore", achievementScore);
        result.put("lengthScore", lengthScore);
        result.put("atsStructureScore", atsStructureScore);

        return result;
    }

    private String formatSkillName(String skill) {
        if ("javascript".equals(skill)) return "JavaScript";
        if ("typescript".equals(skill)) return "TypeScript";
        if ("html".equals(skill) || "html5".equals(skill)) return "HTML5";
        if ("css".equals(skill) || "css3".equals(skill)) return "CSS3";
        if ("spring boot".equals(skill)) return "Spring Boot";
        if ("spring data jpa".equals(skill)) return "Spring Data JPA";
        if ("rest api".equals(skill) || "restful api".equals(skill)) return "RESTful APIs";
        if ("mysql".equals(skill)) return "MySQL";
        if ("postgresql".equals(skill) || "postgres".equals(skill)) return "PostgreSQL";
        if ("mongodb".equals(skill)) return "MongoDB";
        if ("aws".equals(skill)) return "AWS";
        if ("gcp".equals(skill) || "google cloud".equals(skill)) return "Google Cloud";
        if ("oops".equals(skill)) return "OOP";
        if ("dbms".equals(skill)) return "DBMS";
        if ("ci/cd".equals(skill)) return "CI/CD";
        if ("nlp".equals(skill)) return "NLP";
        if ("ai".equals(skill) || "artificial intelligence".equals(skill)) return "Artificial Intelligence";
        if ("ml".equals(skill) || "machine learning".equals(skill)) return "Machine Learning";
        return Character.toUpperCase(skill.charAt(0)) + skill.substring(1);
    }

    private boolean containsKeyword(String text, String keyword) {
        if (keyword.contains(" ") || keyword.contains("+") || keyword.contains("#") || keyword.contains(".")) {
            return text.contains(keyword);
        }
        return Pattern.compile("\\b" + Pattern.quote(keyword) + "\\b", Pattern.CASE_INSENSITIVE).matcher(text).find();
    }

    private boolean containsAny(String text, String... values) {
        for (String value : values) {
            if (text.contains(value.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    private boolean containsEmail(String text) {
        return Pattern.compile("[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}", Pattern.CASE_INSENSITIVE).matcher(text).find();
    }

    private boolean containsPhone(String text) {
        return Pattern.compile("(\\+?\\d[\\d\\s().-]{8,}\\d)").matcher(text).find();
    }

    private int countWords(String text) {
        if (text == null || text.trim().isEmpty()) return 0;
        return text.trim().split("\\s+").length;
    }

    private int countLines(String text) {
        if (text == null || text.trim().isEmpty()) return 0;
        return (int) Arrays.stream(text.split("\\n"))
                .map(String::trim)
                .filter(line -> !line.isEmpty())
                .count();
    }

    private boolean containsQuantification(String line) {
        return Pattern.compile("(\\d+%|\\d+\\+|\\$\\d+|\\d+ users|\\d+ projects|\\d+ ms|\\d+ seconds|\\d+ hours|\\d+ days|\\d+x|\\d+ percent)", Pattern.CASE_INSENSITIVE).matcher(line).find();
    }

    private int countQuantifiedAchievements(List<String> lines) {
        int count = 0;
        for (String line : lines) {
            if (containsQuantification(line)) count++;
        }
        return count;
    }

    private boolean containsActionVerb(String line) {
        String[] verbs = {
            "developed", "designed", "implemented", "created", "built", "engineered",
            "optimized", "automated", "integrated", "deployed", "configured", "analyzed",
            "improved", "managed", "tested", "debugged", "maintained", "led", "architected",
            "spearheaded", "accelerated", "scaled", "delivered"
        };
        String lower = line.toLowerCase();
        for (String verb : verbs) {
            if (containsKeyword(lower, verb)) return true;
        }
        return false;
    }

    private int countActionVerbLines(List<String> lines) {
        int count = 0;
        for (String line : lines) {
            if (containsActionVerb(line)) count++;
        }
        return count;
    }
}