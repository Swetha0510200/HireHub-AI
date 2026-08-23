package com.hirehub.model;

import java.time.LocalDateTime;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "user_accounts")
public class UserAccount {

    @Id
    private String email;

    private String passwordHash;
    private String name;
    private String role; // "Student", "Recruiter", "Admin"
    private String mobile;
    private String location;
    private String qualification;
    private Integer graduationYear;
    private boolean jobAlertsEnabled = true;
    private boolean emailUpdatesEnabled = true;
    private boolean interviewRemindersEnabled = true;
    private boolean enabled = true;
    private LocalDateTime createdAt;

    public UserAccount() {}

    public UserAccount(String email, String passwordHash, String name, String role) {
        this.email = email;
        this.passwordHash = passwordHash;
        this.name = name;
        this.role = role;
        this.createdAt = LocalDateTime.now();
    }

    public UserAccount(String email, String passwordHash, String name, String role, String phone, String location, String qualification, Integer graduationYear) {
        this.email = email;
        this.passwordHash = passwordHash;
        this.name = name;
        this.role = role;
        this.mobile = phone;
        this.location = location;
        this.qualification = qualification;
        this.graduationYear = graduationYear;
        this.createdAt = LocalDateTime.now();
    }

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getMobile() { return mobile; }
    public void setMobile(String mobile) { this.mobile = mobile; }

    public String getPhone() { return mobile != null ? mobile : ""; }
    public void setPhone(String phone) { this.mobile = phone; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getQualification() { return qualification; }
    public void setQualification(String qualification) { this.qualification = qualification; }

    public Integer getGraduationYear() { return graduationYear; }
    public void setGraduationYear(Integer graduationYear) { this.graduationYear = graduationYear; }

    public boolean isJobAlertsEnabled() { return jobAlertsEnabled; }
    public void setJobAlertsEnabled(boolean jobAlertsEnabled) { this.jobAlertsEnabled = jobAlertsEnabled; }

    public boolean isEmailUpdatesEnabled() { return emailUpdatesEnabled; }
    public void setEmailUpdatesEnabled(boolean emailUpdatesEnabled) { this.emailUpdatesEnabled = emailUpdatesEnabled; }

    public boolean isInterviewRemindersEnabled() { return interviewRemindersEnabled; }
    public void setInterviewRemindersEnabled(boolean interviewRemindersEnabled) { this.interviewRemindersEnabled = interviewRemindersEnabled; }

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public boolean isCandidate() {
        return "Student".equalsIgnoreCase(role) || "Candidate".equalsIgnoreCase(role);
    }

    public boolean isRecruiter() {
        return "Recruiter".equalsIgnoreCase(role);
    }

    public boolean isAdmin() {
        return "Admin".equalsIgnoreCase(role);
    }
}

