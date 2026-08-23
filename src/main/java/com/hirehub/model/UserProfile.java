package com.hirehub.model;

import java.time.LocalDateTime;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity
@Table(name = "user_profiles")
public class UserProfile {

    @Id
    private String email;

    private String name;
    private String phone;
    private String role; // "Student", "Recruiter", "Admin"
    private String location;
    private String linkedIn;
    private String github;
    private String portfolio;

    @Column(columnDefinition = "TEXT")
    private String summary;

    @Column(columnDefinition = "LONGTEXT")
    private String imageUrl;

    // Student specific fields
    private String college;
    private String degree;
    private String specialization;
    private Integer graduationYear;
    private String cgpa;
    private String currentTitle;
    private String experienceLevel;
    private String totalExperience;

    @Column(columnDefinition = "TEXT")
    private String skills;

    @Column(columnDefinition = "TEXT")
    private String certifications;

    @Column(columnDefinition = "TEXT")
    private String projects;

    @Column(columnDefinition = "TEXT")
    private String experienceDetails;

    @Column(columnDefinition = "TEXT")
    private String educationDetails;

    private String resumeFilename;

    private String preferredRole;
    private String preferredLocation;

    // Recruiter specific fields
    private String companyName;
    private String companyWebsite;
    private String companySize;
    private String industry;

    @Column(columnDefinition = "TEXT")
    private String companyDescription;

    private String openRoles;
    private String hiringLocation;
    private String hiringLocations;
    private String hiringTimeline;
    private String salaryRange;

    @Column(columnDefinition = "TEXT")
    private String preferredSkills;
    private String recruiterContact;
    private String recruiterEmail;

    @Column(columnDefinition = "TEXT")
    private String idealCandidateProfile;

    private LocalDateTime updatedAt;

    public UserProfile() {}

    @PrePersist
    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getLinkedIn() { return linkedIn; }
    public void setLinkedIn(String linkedIn) { this.linkedIn = linkedIn; }

    public String getGithub() { return github; }
    public void setGithub(String github) { this.github = github; }

    public String getPortfolio() { return portfolio; }
    public void setPortfolio(String portfolio) { this.portfolio = portfolio; }

    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getCollege() { return college; }
    public void setCollege(String college) { this.college = college; }

    public String getDegree() { return degree; }
    public void setDegree(String degree) { this.degree = degree; }

    public String getSpecialization() { return specialization; }
    public void setSpecialization(String specialization) { this.specialization = specialization; }

    public Integer getGraduationYear() { return graduationYear; }
    public void setGraduationYear(Integer graduationYear) { this.graduationYear = graduationYear; }

    public String getCurrentTitle() { return currentTitle; }
    public void setCurrentTitle(String currentTitle) { this.currentTitle = currentTitle; }

    public String getExperienceLevel() { return experienceLevel; }
    public void setExperienceLevel(String experienceLevel) { this.experienceLevel = experienceLevel; }

    public String getTotalExperience() { return totalExperience; }
    public void setTotalExperience(String totalExperience) { this.totalExperience = totalExperience; }

    public String getSkills() { return skills; }
    public void setSkills(String skills) { this.skills = skills; }

    public String getCertifications() { return certifications; }
    public void setCertifications(String certifications) { this.certifications = certifications; }

    public String getProjects() { return projects; }
    public void setProjects(String projects) { this.projects = projects; }

    public String getPreferredRole() { return preferredRole; }
    public void setPreferredRole(String preferredRole) { this.preferredRole = preferredRole; }

    public String getPreferredLocation() { return preferredLocation; }
    public void setPreferredLocation(String preferredLocation) { this.preferredLocation = preferredLocation; }

    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }

    public String getCompanyWebsite() { return companyWebsite; }
    public void setCompanyWebsite(String companyWebsite) { this.companyWebsite = companyWebsite; }

    public String getCompanySize() { return companySize; }
    public void setCompanySize(String companySize) { this.companySize = companySize; }

    public String getIndustry() { return industry; }
    public void setIndustry(String industry) { this.industry = industry; }

    public String getCompanyDescription() { return companyDescription; }
    public void setCompanyDescription(String companyDescription) { this.companyDescription = companyDescription; }

    public String getOpenRoles() { return openRoles; }
    public void setOpenRoles(String openRoles) { this.openRoles = openRoles; }

    public String getHiringLocation() { return hiringLocation; }
    public void setHiringLocation(String hiringLocation) { this.hiringLocation = hiringLocation; }

    public String getHiringLocations() { return hiringLocations; }
    public void setHiringLocations(String hiringLocations) { this.hiringLocations = hiringLocations; }

    public String getHiringTimeline() { return hiringTimeline; }
    public void setHiringTimeline(String hiringTimeline) { this.hiringTimeline = hiringTimeline; }

    public String getSalaryRange() { return salaryRange; }
    public void setSalaryRange(String salaryRange) { this.salaryRange = salaryRange; }

    public String getPreferredSkills() { return preferredSkills; }
    public void setPreferredSkills(String preferredSkills) { this.preferredSkills = preferredSkills; }

    public String getRecruiterContact() { return recruiterContact; }
    public void setRecruiterContact(String recruiterContact) { this.recruiterContact = recruiterContact; }

    public String getRecruiterEmail() { return recruiterEmail; }
    public void setRecruiterEmail(String recruiterEmail) { this.recruiterEmail = recruiterEmail; }

    public String getIdealCandidateProfile() { return idealCandidateProfile; }
    public void setIdealCandidateProfile(String idealCandidateProfile) { this.idealCandidateProfile = idealCandidateProfile; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public String getCgpa() { return cgpa; }
    public void setCgpa(String cgpa) { this.cgpa = cgpa; }

    public String getExperienceDetails() { return experienceDetails; }
    public void setExperienceDetails(String experienceDetails) { this.experienceDetails = experienceDetails; }

    public String getEducationDetails() { return educationDetails; }
    public void setEducationDetails(String educationDetails) { this.educationDetails = educationDetails; }

    public String getResumeFilename() { return resumeFilename; }
    public void setResumeFilename(String resumeFilename) { this.resumeFilename = resumeFilename; }

    public int getProfileCompletion() {
        int score = 0;
        if (name != null && !name.isBlank()) score += 15;
        if (email != null && !email.isBlank()) score += 10;
        if (phone != null && !phone.isBlank()) score += 10;
        if (location != null && !location.isBlank()) score += 10;
        if (skills != null && !skills.isBlank()) score += 20;
        if ((degree != null && !degree.isBlank()) || (college != null && !college.isBlank())) score += 15;
        if ((experienceDetails != null && !experienceDetails.isBlank()) || (summary != null && !summary.isBlank())) score += 10;
        if (resumeFilename != null && !resumeFilename.isBlank()) score += 10;
        return Math.min(100, Math.max(20, score));
    }

    public String getInitials() {
        if (name == null || name.trim().isEmpty()) {
            if (email != null && !email.trim().isEmpty()) {
                return email.substring(0, 1).toUpperCase();
            }
            return "U";
        }
        String[] parts = name.trim().split("\\s+");
        if (parts.length >= 2 && !parts[0].isEmpty() && !parts[parts.length - 1].isEmpty()) {
            return (parts[0].substring(0, 1) + parts[parts.length - 1].substring(0, 1)).toUpperCase();
        }
        return parts[0].substring(0, 1).toUpperCase();
    }
}