package com.hirehub.model;

import java.time.LocalDateTime;
import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "resume_records")
public class ResumeRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String userEmail;

    private String fileName;
    private String contentType;

    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Column(columnDefinition = "LONGBLOB")
    private byte[] fileData;

    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Column(columnDefinition = "LONGTEXT")
    private String rawText;

    private Integer atsScore;
    private String resumeQuality;

    @Column(columnDefinition = "TEXT")
    private String summary;

    @Column(columnDefinition = "TEXT")
    private String skills;

    @Column(columnDefinition = "TEXT")
    private String strengths;

    @Column(columnDefinition = "TEXT")
    private String improvements;

    @Column(columnDefinition = "TEXT")
    private String lineFeedback;

    private Integer wordCount;
    private Integer skillCount;
    private Integer lineCount;
    private Integer quantifiedAchievements;
    private Integer actionVerbUsage;
    private Integer sectionScore;
    private Integer skillScore;
    private Integer achievementScore;
    private Integer lengthScore;
    private Integer atsStructureScore;

    private LocalDateTime uploadedAt;

    public ResumeRecord() {}

    @PrePersist
    public void prePersist() {
        if (uploadedAt == null) {
            uploadedAt = LocalDateTime.now();
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public String getContentType() { return contentType; }
    public void setContentType(String contentType) { this.contentType = contentType; }

    public byte[] getFileData() { return fileData; }
    public void setFileData(byte[] fileData) { this.fileData = fileData; }

    public String getRawText() { return rawText; }
    public void setRawText(String rawText) { this.rawText = rawText; }

    public Integer getAtsScore() { return atsScore; }
    public void setAtsScore(Integer atsScore) { this.atsScore = atsScore; }

    public String getResumeQuality() { return resumeQuality; }
    public void setResumeQuality(String resumeQuality) { this.resumeQuality = resumeQuality; }

    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }

    public String getSkills() { return skills; }
    public void setSkills(String skills) { this.skills = skills; }

    public String getStrengths() { return strengths; }
    public void setStrengths(String strengths) { this.strengths = strengths; }

    public String getImprovements() { return improvements; }
    public void setImprovements(String improvements) { this.improvements = improvements; }

    public String getLineFeedback() { return lineFeedback; }
    public void setLineFeedback(String lineFeedback) { this.lineFeedback = lineFeedback; }

    public Integer getWordCount() { return wordCount; }
    public void setWordCount(Integer wordCount) { this.wordCount = wordCount; }

    public Integer getSkillCount() { return skillCount; }
    public void setSkillCount(Integer skillCount) { this.skillCount = skillCount; }

    public Integer getLineCount() { return lineCount; }
    public void setLineCount(Integer lineCount) { this.lineCount = lineCount; }

    public Integer getQuantifiedAchievements() { return quantifiedAchievements; }
    public void setQuantifiedAchievements(Integer quantifiedAchievements) { this.quantifiedAchievements = quantifiedAchievements; }

    public Integer getActionVerbUsage() { return actionVerbUsage; }
    public void setActionVerbUsage(Integer actionVerbUsage) { this.actionVerbUsage = actionVerbUsage; }

    public Integer getSectionScore() { return sectionScore; }
    public void setSectionScore(Integer sectionScore) { this.sectionScore = sectionScore; }

    public Integer getSkillScore() { return skillScore; }
    public void setSkillScore(Integer skillScore) { this.skillScore = skillScore; }

    public Integer getAchievementScore() { return achievementScore; }
    public void setAchievementScore(Integer achievementScore) { this.achievementScore = achievementScore; }

    public Integer getLengthScore() { return lengthScore; }
    public void setLengthScore(Integer lengthScore) { this.lengthScore = lengthScore; }

    public Integer getAtsStructureScore() { return atsStructureScore; }
    public void setAtsStructureScore(Integer atsStructureScore) { this.atsStructureScore = atsStructureScore; }

    public LocalDateTime getUploadedAt() { return uploadedAt; }
    public void setUploadedAt(LocalDateTime uploadedAt) { this.uploadedAt = uploadedAt; }
}
