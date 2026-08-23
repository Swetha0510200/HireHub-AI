package com.hirehub.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.hirehub.dto.JobMatchDto;
import com.hirehub.model.Job;
import com.hirehub.model.ResumeRecord;
import com.hirehub.model.UserProfile;
import com.hirehub.repository.JobApplicationRepository;
import com.hirehub.repository.JobRepository;
import com.hirehub.repository.ResumeRecordRepository;
import com.hirehub.repository.SavedJobRepository;
import com.hirehub.repository.UserProfileRepository;

@Service
public class AiJobMatcherService {

    private final JobRepository jobRepository;
    private final ResumeRecordRepository resumeRepository;
    private final UserProfileRepository profileRepository;
    private final JobApplicationRepository applicationRepository;
    private final SavedJobRepository savedJobRepository;

    public AiJobMatcherService(JobRepository jobRepository,
                               ResumeRecordRepository resumeRepository,
                               UserProfileRepository profileRepository,
                               JobApplicationRepository applicationRepository,
                               SavedJobRepository savedJobRepository) {
        this.jobRepository = jobRepository;
        this.resumeRepository = resumeRepository;
        this.profileRepository = profileRepository;
        this.applicationRepository = applicationRepository;
        this.savedJobRepository = savedJobRepository;
    }

    public List<JobMatchDto> matchJobsForUser(String userEmail) {
        List<Job> allJobs = jobRepository.findByActiveTrueOrderByCreatedAtDesc();
        if (allJobs.isEmpty()) {
            allJobs = jobRepository.findAllByOrderByCreatedAtDesc();
        }

        Optional<UserProfile> profileOpt = profileRepository.findByEmail(userEmail);
        Optional<ResumeRecord> resumeOpt = resumeRepository.findFirstByUserEmailOrderByUploadedAtDesc(userEmail);

        UserProfile profile = profileOpt.orElse(null);
        ResumeRecord resume = resumeOpt.orElse(null);

        // Gather all candidate skills from both Profile and Resume
        Set<String> candidateSkills = new LinkedHashSet<>();
        if (profile != null && profile.getSkills() != null && !profile.getSkills().isBlank()) {
            candidateSkills.addAll(extractSkillTokens(profile.getSkills()));
        }
        if (resume != null && resume.getSkills() != null && !resume.getSkills().isBlank()) {
            candidateSkills.addAll(extractSkillTokens(resume.getSkills()));
        }

        String candidateRole = profile != null && profile.getPreferredRole() != null ? profile.getPreferredRole() : "";
        String candidateSummary = resume != null && resume.getSummary() != null ? resume.getSummary() : (profile != null && profile.getSummary() != null ? profile.getSummary() : "");
        String candidateDegree = profile != null && profile.getDegree() != null ? profile.getDegree() : "";
        String candidateExp = profile != null && profile.getExperienceLevel() != null ? profile.getExperienceLevel() : "";

        List<JobMatchDto> results = new ArrayList<>();

        for (Job job : allJobs) {
            List<String> requiredJobSkills = extractSkillTokens(job.getSkills());
            List<String> matched = new ArrayList<>();
            List<String> missing = new ArrayList<>();

            for (String reqSkill : requiredJobSkills) {
                if (containsSkill(candidateSkills, reqSkill)) {
                    matched.add(reqSkill);
                } else {
                    missing.add(reqSkill);
                }
            }

            // Scoring Formula (60% Skill + 20% Category + 10% Location + 10% Experience)
            // 1. Skill Match (60%)
            double skillScore = 0.0;
            if (!requiredJobSkills.isEmpty()) {
                double matchRatio = (double) matched.size() / requiredJobSkills.size();
                skillScore = matchRatio * 60.0;
            } else {
                skillScore = candidateSkills.isEmpty() ? 30.0 : 45.0;
            }

            // 2. Category / Role Track Match (20%)
            double categoryScore = 0.0;
            String jobCategory = job.getCategory() != null ? job.getCategory().toLowerCase() : "";
            String jobTitle = job.getTitle() != null ? job.getTitle().toLowerCase() : "";
            if (!candidateRole.isBlank()) {
                String cRole = candidateRole.toLowerCase();
                if (jobTitle.contains(cRole) || cRole.contains(jobTitle) || jobCategory.contains(cRole)) {
                    categoryScore = 20.0;
                } else {
                    for (String word : cRole.split("\\s+")) {
                        if (word.length() > 2 && (jobTitle.contains(word) || jobCategory.contains(word))) {
                            categoryScore += 10.0;
                        }
                    }
                    categoryScore = Math.min(categoryScore, 20.0);
                }
            } else {
                categoryScore = 12.0; // baseline if candidate hasn't set role
            }

            // 3. Location Match (10%)
            double locationScore = 5.0;
            String candidateLoc = profile != null && profile.getLocation() != null ? profile.getLocation().toLowerCase() : "";
            String jobLoc = job.getLocation() != null ? job.getLocation().toLowerCase() : "";
            if ("Remote".equalsIgnoreCase(job.getWorkMode()) || jobLoc.contains("remote")) {
                locationScore = 10.0;
            } else if (!candidateLoc.isBlank() && !jobLoc.isBlank()) {
                if (jobLoc.contains(candidateLoc) || candidateLoc.contains(jobLoc)) {
                    locationScore = 10.0;
                }
            }

            // 4. Experience Match (10%)
            double expScore = 5.0;
            if (job.getExperience() != null && candidateExp != null && !candidateExp.isBlank()) {
                if (job.getExperience().equalsIgnoreCase(candidateExp) ||
                    (job.getExperience().toLowerCase().contains("fresher") && candidateExp.toLowerCase().contains("fresher"))) {
                    expScore = 10.0;
                } else {
                    expScore = 7.0;
                }
            } else {
                expScore = 8.0;
            }

            int totalScore = (int) Math.round(skillScore + categoryScore + locationScore + expScore);
            totalScore = Math.max(25, Math.min(totalScore, 98));

            // Generate Match Explanation
            String matchReason;
            if (matched.size() >= 3) {
                matchReason = "High match (" + totalScore + "%): Aligns with " + matched.size() + " of your skills (" +
                              String.join(", ", matched.stream().limit(3).toList()) + ") and career domain.";
            } else if (!matched.isEmpty()) {
                matchReason = "Good match (" + totalScore + "%): Matches skills in " + String.join(", ", matched) +
                              ". Bridging " + (missing.isEmpty() ? "suggested skills" : missing.get(0)) + " will boost interview callbacks.";
            } else if (categoryScore > 10) {
                matchReason = "Role fit (" + totalScore + "%): Fits your preferred specialization in " + job.getCategory() +
                              ". Add " + (missing.isEmpty() ? "core skills" : String.join(", ", missing.stream().limit(2).toList())) + " to improve score.";
            } else {
                matchReason = "Opportunity fit (" + totalScore + "%): Active hiring demand in " + job.getCompany() + " (" + job.getLocation() + ").";
            }

            List<String> recommendedSkills = missing.stream().limit(3).collect(Collectors.toList());

            boolean isApplied = applicationRepository.existsByJobIdAndApplicantEmail(job.getId(), userEmail);
            boolean isSaved = savedJobRepository.existsByJobIdAndUserEmail(job.getId(), userEmail);

            results.add(new JobMatchDto(
                    job, totalScore, matched, missing, matchReason, recommendedSkills, isApplied, isSaved
            ));
        }

        // Sort descending by calculated matchScore
        results.sort((a, b) -> Integer.compare(b.getMatchScore(), a.getMatchScore()));
        return results;
    }

    private List<String> extractSkillTokens(String skillsStr) {
        if (skillsStr == null || skillsStr.isBlank()) return Collections.emptyList();
        String[] parts = skillsStr.split("[,|/•\n\r]+");
        List<String> list = new ArrayList<>();
        for (String p : parts) {
            String trimmed = p.trim();
            if (!trimmed.isEmpty() && !trimmed.equalsIgnoreCase("and") && !trimmed.equalsIgnoreCase("or")) {
                list.add(trimmed);
            }
        }
        return list;
    }

    private boolean containsSkill(Set<String> candidateSkills, String targetSkill) {
        String target = targetSkill.toLowerCase().trim();
        for (String cand : candidateSkills) {
            String c = cand.toLowerCase().trim();
            if (c.equals(target) || c.contains(target) || target.contains(c)) {
                return true;
            }
        }
        return false;
    }
}
