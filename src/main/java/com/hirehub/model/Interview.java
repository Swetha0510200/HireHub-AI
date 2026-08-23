package com.hirehub.model;

import java.time.LocalDateTime;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "interviews")
public class Interview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String candidateEmail;

    private String candidateName;
    private String recruiterEmail;

    @ManyToOne
    @JoinColumn(name = "job_id")
    private Job job;

    @ManyToOne
    @JoinColumn(name = "application_id")
    private JobApplication application;

    private String company;
    private String role;
    private String interviewDate;
    private String interviewTime;
    private String location;
    private String interviewType; // "Technical", "HR Round", "Live Coding", "Video Interview"
    private String meetingUrl;

    @Column(length = 3000)
    private String notes;

    private String status; // "Scheduled", "Completed", "Cancelled"
    private LocalDateTime createdAt;

    public Interview() {}

    public Interview(String candidateEmail, String candidateName, String recruiterEmail,
                     Job job, JobApplication application, String company, String role,
                     String interviewDate, String interviewTime, String interviewType,
                     String meetingUrl, String notes, String status) {
        this.candidateEmail = candidateEmail;
        this.candidateName = candidateName;
        this.recruiterEmail = recruiterEmail;
        this.job = job;
        this.application = application;
        this.company = company;
        this.role = role;
        this.interviewDate = interviewDate;
        this.interviewTime = interviewTime;
        this.interviewType = interviewType;
        this.meetingUrl = meetingUrl;
        this.notes = notes;
        this.status = status;
        this.createdAt = LocalDateTime.now();
    }

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (status == null || status.isBlank()) {
            status = "Scheduled";
        }
        if (company == null && job != null) {
            company = job.getCompany();
        }
        if (role == null && job != null) {
            role = job.getTitle();
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCandidateEmail() { return candidateEmail; }
    public void setCandidateEmail(String candidateEmail) { this.candidateEmail = candidateEmail; }

    public String getCandidateName() { return candidateName; }
    public void setCandidateName(String candidateName) { this.candidateName = candidateName; }

    public String getRecruiterEmail() { return recruiterEmail; }
    public void setRecruiterEmail(String recruiterEmail) { this.recruiterEmail = recruiterEmail; }

    public Job getJob() { return job; }
    public void setJob(Job job) { this.job = job; }

    public JobApplication getApplication() { return application; }
    public void setApplication(JobApplication application) { this.application = application; }

    public String getCompany() { return company; }
    public void setCompany(String company) { this.company = company; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getDate() { return interviewDate; }
    public void setDate(String date) { this.interviewDate = date; }

    public String getInterviewDate() { return interviewDate; }
    public void setInterviewDate(String interviewDate) { this.interviewDate = interviewDate; }

    public String getTime() { return interviewTime; }
    public void setTime(String time) { this.interviewTime = time; }

    public String getInterviewTime() { return interviewTime; }
    public void setInterviewTime(String interviewTime) { this.interviewTime = interviewTime; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getInterviewType() { return interviewType; }
    public void setInterviewType(String interviewType) { this.interviewType = interviewType; }

    public String getMeetingUrl() { return meetingUrl; }
    public void setMeetingUrl(String meetingUrl) { this.meetingUrl = meetingUrl; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
