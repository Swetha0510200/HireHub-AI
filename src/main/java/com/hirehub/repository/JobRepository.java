package com.hirehub.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.hirehub.model.Job;

@Repository
public interface JobRepository extends JpaRepository<Job, Long> {

    List<Job> findTop6ByIdNotOrderByCreatedAtDesc(Long id);

    List<Job> findByRecruiterEmailOrderByCreatedAtDesc(String recruiterEmail);

    List<Job> findByRecruiterEmailAndTypeIgnoreCaseOrderByCreatedAtDesc(String recruiterEmail, String type);

    long countByRecruiterEmail(String recruiterEmail);

    long countByRecruiterEmailAndActiveTrue(String recruiterEmail);

    long countByRecruiterEmailAndTypeIgnoreCase(String recruiterEmail, String type);

    List<Job> findByActiveTrueOrderByCreatedAtDesc();

    List<Job> findByStatusAndActiveTrueOrderByCreatedAtDesc(String status);

    List<Job> findByTypeIgnoreCaseAndActiveTrueOrderByCreatedAtDesc(String type);

    List<Job> findByTypeNotIgnoreCaseAndActiveTrueOrderByCreatedAtDesc(String type);

    List<Job> findAllByOrderByCreatedAtDesc();

    List<Job> findByStatusOrderByCreatedAtDesc(String status);

    long countByActiveTrue();

    long countByTypeIgnoreCase(String type);

    long countByTypeNotIgnoreCase(String type);

    long countByStatusIgnoreCase(String status);

    @Query("SELECT j FROM Job j WHERE j.active = true AND (" +
           "LOWER(j.title) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(j.company) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(j.location) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(j.skills) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(j.category) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(j.description) LIKE LOWER(CONCAT('%', :query, '%')))")
    List<Job> searchJobs(@Param("query") String query);

    @Query("SELECT j FROM Job j WHERE j.active = true AND LOWER(j.type) = 'internship' AND (" +
           "LOWER(j.title) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(j.company) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(j.location) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(j.skills) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(j.category) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(j.description) LIKE LOWER(CONCAT('%', :query, '%')))")
    List<Job> searchInternships(@Param("query") String query);
}

