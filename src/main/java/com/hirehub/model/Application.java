package com.hirehub.model;

public class Application {

    private String jobTitle;
    private String company;
    private String location;
    private String appliedDate;
    private String status;
    private String salary;
    private int matchScore;

    public Application() {
    }

    public Application(String jobTitle, String company, String location,
            String appliedDate, String status, String salary, int matchScore) {
        this.jobTitle = jobTitle;
        this.company = company;
        this.location = location;
        this.appliedDate = appliedDate;
        this.status = status;
        this.salary = salary;
        this.matchScore = matchScore;
    }

    public String getJobTitle() {
        return jobTitle;
    }

    public void setJobTitle(String jobTitle) {
        this.jobTitle = jobTitle;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getAppliedDate() {
        return appliedDate;
    }

    public void setAppliedDate(String appliedDate) {
        this.appliedDate = appliedDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getSalary() {
        return salary;
    }

    public void setSalary(String salary) {
        this.salary = salary;
    }

    public int getMatchScore() {
        return matchScore;
    }

    public void setMatchScore(int matchScore) {
        this.matchScore = matchScore;
    }
}
