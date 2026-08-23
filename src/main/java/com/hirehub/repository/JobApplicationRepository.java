package com.hirehub.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.hirehub.model.JobApplication;

@Repository
public interface JobApplicationRepository extends JpaRepository<JobApplication, Long> {

    List<JobApplication> findByApplicantEmailOrderByAppliedAtDesc(String applicantEmail);

    boolean existsByJobIdAndApplicantEmail(Long jobId, String applicantEmail);

    long countByApplicantEmail(String applicantEmail);

    long countByApplicantEmailAndStatusIgnoreCase(String applicantEmail, String status);

    List<JobApplication> findByJobIdOrderByAppliedAtDesc(Long jobId);

    List<JobApplication> findByJobRecruiterEmailOrderByAppliedAtDesc(String recruiterEmail);

    long countByJobRecruiterEmail(String recruiterEmail);

    long countByJobRecruiterEmailAndStatusIgnoreCase(String recruiterEmail, String status);

    long countByStatusIgnoreCase(String status);

    List<JobApplication> findAllByOrderByAppliedAtDesc();

    @Query("SELECT COUNT(a) FROM JobApplication a WHERE a.job.id = :jobId")
    long countByJobId(@Param("jobId") Long jobId);
}