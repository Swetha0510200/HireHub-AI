package com.hirehub.model;

import java.time.LocalDateTime;
import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
    name = "job_applications",
    uniqueConstraints = {
        @UniqueConstraint(
            columnNames = {"job_id", "applicant_email"}
        )
    }
)
public class JobApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "job_id", nullable = false)
    private Job job;

    @Column(name = "applicant_email", nullable = false)
    private String applicantEmail;

    private String applicantName;
    private String applicantPhone;

    @Column(columnDefinition = "TEXT")
    private String coverLetter;

    private String resumeFileName;
    private String resumeContentType;

    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Column(columnDefinition = "LONGBLOB")
    private byte[] resumeData;

    private String degree;
    private String university;
    private Integer graduationYear;

    @Column(columnDefinition = "TEXT")
    private String skills;
    private String experience;
    private String linkedIn;
    private String github;
    private String portfolio;

    private String applicationId; // e.g. "HH-APP-00001"
    private String expectedSalary;
    private String availability; // e.g. "Immediate", "15 Days", "1 Month"

    @Column(columnDefinition = "TEXT")
    private String recruiterRemarks;

    private Integer matchScore;
    private String status = "Applied"; // "Applied", "Under Review", "Shortlisted", "Interview", "Selected", "Rejected"

    private LocalDateTime appliedAt;
    private LocalDateTime updatedAt;

    public JobApplication() {}

    @PrePersist
    public void prePersist() {
        if (appliedAt == null) {
            appliedAt = LocalDateTime.now();
        }
        if (updatedAt == null) {
            updatedAt = LocalDateTime.now();
        }
        if (status == null || status.isBlank()) {
            status = "Applied";
        }
        if (applicationId == null || applicationId.isBlank()) {
            applicationId = "HH-APP-" + String.format("%05d", (int)(Math.random() * 90000) + 10000);
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getApplicationId() {
        if (applicationId == null || applicationId.isBlank()) {
            return "HH-APP-" + String.format("%05d", id != null ? id : 10001);
        }
        return applicationId;
    }
    public void setApplicationId(String applicationId) { this.applicationId = applicationId; }

    public Job getJob() { return job; }
    public void setJob(Job job) { this.job = job; }

    public String getApplicantEmail() { return applicantEmail; }
    public void setApplicantEmail(String applicantEmail) { this.applicantEmail = applicantEmail; }

    public String getApplicantName() { return applicantName; }
    public void setApplicantName(String applicantName) { this.applicantName = applicantName; }

    public String getApplicantPhone() { return applicantPhone; }
    public void setApplicantPhone(String applicantPhone) { this.applicantPhone = applicantPhone; }

    public String getCoverLetter() { return coverLetter; }
    public void setCoverLetter(String coverLetter) { this.coverLetter = coverLetter; }

    public String getResumeFileName() { return resumeFileName; }
    public void setResumeFileName(String resumeFileName) { this.resumeFileName = resumeFileName; }

    public String getResumeContentType() { return resumeContentType; }
    public void setResumeContentType(String resumeContentType) { this.resumeContentType = resumeContentType; }

    public byte[] getResumeData() { return resumeData; }
    public void setResumeData(byte[] resumeData) { this.resumeData = resumeData; }

    public String getDegree() { return degree; }
    public void setDegree(String degree) { this.degree = degree; }

    public String getUniversity() { return university; }
    public void setUniversity(String university) { this.university = university; }

    public String getCollege() { return university != null ? university : ""; }
    public void setCollege(String college) { this.university = college; }

    public Integer getGraduationYear() { return graduationYear; }
    public void setGraduationYear(Integer graduationYear) { this.graduationYear = graduationYear; }

    public String getSkills() { return skills; }
    public void setSkills(String skills) { this.skills = skills; }

    public String getExperience() { return experience; }
    public void setExperience(String experience) { this.experience = experience; }

    public String getExpectedSalary() { return expectedSalary; }
    public void setExpectedSalary(String expectedSalary) { this.expectedSalary = expectedSalary; }

    public String getAvailability() { return availability; }
    public void setAvailability(String availability) { this.availability = availability; }

    public String getRecruiterRemarks() { return recruiterRemarks; }
    public void setRecruiterRemarks(String recruiterRemarks) { this.recruiterRemarks = recruiterRemarks; }

    public String getLinkedIn() { return linkedIn; }
    public void setLinkedIn(String linkedIn) { this.linkedIn = linkedIn; }

    public String getGithub() { return github; }
    public void setGithub(String github) { this.github = github; }

    public String getPortfolio() { return portfolio; }
    public void setPortfolio(String portfolio) { this.portfolio = portfolio; }

    public Integer getMatchScore() { return matchScore != null ? matchScore : 80; }
    public void setMatchScore(Integer matchScore) { this.matchScore = matchScore; }

    public String getStatus() { return status != null ? status : "Applied"; }
    public void setStatus(String status) {
        this.status = status;
        this.updatedAt = LocalDateTime.now();
    }

    public LocalDateTime getAppliedAt() { return appliedAt; }
    public void setAppliedAt(LocalDateTime appliedAt) { this.appliedAt = appliedAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    // Stage order: 1: Applied, 2: Under Review, 3: Shortlisted, 4: Interview, 5: Selected, -1: Rejected
    public int getTimelineStage() {
        if ("Rejected".equalsIgnoreCase(status)) return -1;
        if ("Selected".equalsIgnoreCase(status)) return 5;
        if ("Interview".equalsIgnoreCase(status)) return 4;
        if ("Shortlisted".equalsIgnoreCase(status)) return 3;
        if ("Under Review".equalsIgnoreCase(status)) return 2;
        return 1; // "Applied"
    }

    public String getStatusBadgeClass() {
        if ("Selected".equalsIgnoreCase(status)) return "badge-selected";
        if ("Interview".equalsIgnoreCase(status)) return "badge-interview";
        if ("Shortlisted".equalsIgnoreCase(status)) return "badge-shortlisted";
        if ("Under Review".equalsIgnoreCase(status)) return "badge-review";
        if ("Rejected".equalsIgnoreCase(status)) return "badge-rejected";
        return "badge-applied";
    }
}