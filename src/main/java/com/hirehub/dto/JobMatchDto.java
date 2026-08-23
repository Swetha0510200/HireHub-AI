package com.hirehub.dto;

import java.util.List;
import com.hirehub.model.Job;

public class JobMatchDto {
    private Job job;
    private int matchScore;
    private List<String> matchedSkills;
    private List<String> missingSkills;
    private String matchReason;
    private List<String> recommendedSkills;
    private boolean applied;
    private boolean saved;

    public JobMatchDto() {}

    public JobMatchDto(Job job, int matchScore, List<String> matchedSkills, List<String> missingSkills,
                       String matchReason, List<String> recommendedSkills, boolean applied, boolean saved) {
        this.job = job;
        this.matchScore = matchScore;
        this.matchedSkills = matchedSkills;
        this.missingSkills = missingSkills;
        this.matchReason = matchReason;
        this.recommendedSkills = recommendedSkills;
        this.applied = applied;
        this.saved = saved;
    }

    public Job getJob() { return job; }
    public void setJob(Job job) { this.job = job; }

    public int getMatchScore() { return matchScore; }
    public void setMatchScore(int matchScore) { this.matchScore = matchScore; }

    public List<String> getMatchedSkills() { return matchedSkills; }
    public void setMatchedSkills(List<String> matchedSkills) { this.matchedSkills = matchedSkills; }

    public List<String> getMissingSkills() { return missingSkills; }
    public void setMissingSkills(List<String> missingSkills) { this.missingSkills = missingSkills; }

    public String getMatchReason() { return matchReason; }
    public void setMatchReason(String matchReason) { this.matchReason = matchReason; }

    public List<String> getRecommendedSkills() { return recommendedSkills; }
    public void setRecommendedSkills(List<String> recommendedSkills) { this.recommendedSkills = recommendedSkills; }

    public boolean isApplied() { return applied; }
    public void setApplied(boolean applied) { this.applied = applied; }

    public boolean isSaved() { return saved; }
    public void setSaved(boolean saved) { this.saved = saved; }
}
