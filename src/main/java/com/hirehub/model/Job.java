package com.hirehub.model;

import java.time.LocalDateTime;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "jobs")
public class Job {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String company;

    private String category; // "Software Development", "Data Science", "Artificial Intelligence", "Web Development", etc.
    private String location;
    private String type; // "Full-time", "Part-time", "Internship", "Contract"
    private String workMode; // "On-site", "Remote", "Hybrid"
    private String experience; // e.g. "Fresher", "1-3 Years", "3-5 Years", "5+ Years"
    private String salary;
    private String duration; // e.g. "3 Months", "6 Months" (for internships)
    private String stipend; // e.g. "₹25,000 / month" (for internships)
    private String jobCode; // e.g. "HH-JOB-101", "HH-INT-201"
    private String status = "Approved"; // "Approved", "Pending Approval", "Rejected", "Closed"

    @Column(columnDefinition = "TEXT")
    private String skills;

    @Column(columnDefinition = "LONGTEXT")
    private String description;

    @Column(columnDefinition = "TEXT")
    private String responsibilities;

    @Column(columnDefinition = "TEXT")
    private String requirements;

    @Column(columnDefinition = "TEXT")
    private String benefits;

    private String recruiterEmail;
    private String deadline;
    private Integer vacancies;
    private String applicationEmail;
    private boolean active = true;
    private LocalDateTime createdAt;

    public Job() {}

    public Job(String title, String company, String category, String location, String type, String workMode,
               String experience, String salary, String skills, String description, String recruiterEmail) {
        this.title = title;
        this.company = company;
        this.category = category;
        this.location = location;
        this.type = type;
        this.workMode = workMode;
        this.experience = experience;
        this.salary = salary;
        this.skills = skills;
        this.description = description;
        this.recruiterEmail = recruiterEmail;
        this.status = "Approved";
        this.active = true;
        this.createdAt = LocalDateTime.now();
    }

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (workMode == null || workMode.isBlank()) {
            workMode = (location != null && location.toLowerCase().contains("remote")) ? "Remote" : "On-site";
        }
        if (status == null || status.isBlank()) {
            status = "Approved";
        }
        if (category == null || category.isBlank()) {
            category = "Software Development";
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getCompany() { return company; }
    public void setCompany(String company) { this.company = company; }

    public String getCategory() { return category != null ? category : "Software Development"; }
    public void setCategory(String category) { this.category = category; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getWorkMode() { return workMode; }
    public void setWorkMode(String workMode) { this.workMode = workMode; }

    public String getExperience() { return experience; }
    public void setExperience(String experience) { this.experience = experience; }

    public String getSalary() { return salary; }
    public void setSalary(String salary) { this.salary = salary; }

    public String getDuration() { return duration; }
    public void setDuration(String duration) { this.duration = duration; }

    public String getStipend() { return stipend; }
    public void setStipend(String stipend) { this.stipend = stipend; }

    public String getJobCode() { return jobCode != null ? jobCode : (isInternship() ? "HH-INT-" + id : "HH-JOB-" + id); }
    public void setJobCode(String jobCode) { this.jobCode = jobCode; }

    public String getStatus() { return status != null ? status : "Approved"; }
    public void setStatus(String status) { this.status = status; }

    public String getSkills() { return skills; }
    public void setSkills(String skills) { this.skills = skills; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getResponsibilities() { return responsibilities; }
    public void setResponsibilities(String responsibilities) { this.responsibilities = responsibilities; }

    public String getRequirements() { return requirements; }
    public void setRequirements(String requirements) { this.requirements = requirements; }

    public String getBenefits() { return benefits; }
    public void setBenefits(String benefits) { this.benefits = benefits; }

    public String getRecruiterEmail() { return recruiterEmail; }
    public void setRecruiterEmail(String recruiterEmail) { this.recruiterEmail = recruiterEmail; }

    public String getDeadline() { return deadline; }
    public void setDeadline(String deadline) { this.deadline = deadline; }

    public Integer getVacancies() { return vacancies; }
    public void setVacancies(Integer vacancies) { this.vacancies = vacancies; }

    public String getApplicationEmail() { return applicationEmail; }
    public void setApplicationEmail(String applicationEmail) { this.applicationEmail = applicationEmail; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public boolean isInternship() {
        return (type != null && type.equalsIgnoreCase("Internship")) || (category != null && category.toLowerCase().contains("intern"));
    }

    public String getDisplayPay() {
        if (isInternship() && stipend != null && !stipend.isBlank()) {
            return stipend;
        }
        if (salary != null && !salary.isBlank()) {
            return salary;
        }
        return isInternship() ? "₹20,000 - ₹35,000 / month" : "₹6,00,000 - ₹12,00,000 / year";
    }

    public String getDisplayDurationOrExp() {
        if (isInternship()) {
            return (duration != null && !duration.isBlank()) ? duration : "3-6 Months";
        }
        return (experience != null && !experience.isBlank()) ? experience : "Fresher / 0-2 Years";
    }

    // Alias setters for form binding compatibility
    public void setJobTitle(String jobTitle) {
        if (this.title == null || this.title.isBlank()) {
            this.title = jobTitle;
        }
    }

    public void setCompanyName(String companyName) {
        if (this.company == null || this.company.isBlank()) {
            this.company = companyName;
        }
    }

    public void setEmploymentType(String employmentType) {
        if (this.type == null || this.type.isBlank()) {
            this.type = employmentType;
        }
    }

    public void setJobDescription(String jobDescription) {
        if (this.description == null || this.description.isBlank()) {
            this.description = jobDescription;
        }
    }
}

