package com.hirehub.model;

import java.util.ArrayList;
import java.util.List;

public class ResumeAnalysis {

    private int atsScore;

    private int formatting;

    private int keywords;

    private int skills;

    private int education;

    private int projects;

    private List<String> foundSkills = new ArrayList<>();

    private List<String> missingSkills = new ArrayList<>();

    private List<String> suggestions = new ArrayList<>();

    private String summary;

    public ResumeAnalysis() {
    }

    public int getAtsScore() {
        return atsScore;
    }

    public void setAtsScore(int atsScore) {
        this.atsScore = atsScore;
    }

    public int getFormatting() {
        return formatting;
    }

    public void setFormatting(int formatting) {
        this.formatting = formatting;
    }

    public int getKeywords() {
        return keywords;
    }

    public void setKeywords(int keywords) {
        this.keywords = keywords;
    }

    public int getSkills() {
        return skills;
    }

    public void setSkills(int skills) {
        this.skills = skills;
    }

    public int getEducation() {
        return education;
    }

    public void setEducation(int education) {
        this.education = education;
    }

    public int getProjects() {
        return projects;
    }

    public void setProjects(int projects) {
        this.projects = projects;
    }

    public List<String> getFoundSkills() {
        return foundSkills;
    }

    public void setFoundSkills(List<String> foundSkills) {
        this.foundSkills = foundSkills;
    }

    public List<String> getMissingSkills() {
        return missingSkills;
    }

    public void setMissingSkills(List<String> missingSkills) {
        this.missingSkills = missingSkills;
    }

    public List<String> getSuggestions() {
        return suggestions;
    }

    public void setSuggestions(List<String> suggestions) {
        this.suggestions = suggestions;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

}