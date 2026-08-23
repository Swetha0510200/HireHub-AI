package com.hirehub.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.hirehub.model.SavedJob;

@Repository
public interface SavedJobRepository extends JpaRepository<SavedJob, Long> {

    List<SavedJob> findByUserEmailOrderBySavedAtDesc(String userEmail);

    boolean existsByJobIdAndUserEmail(Long jobId, String userEmail);

    @Transactional
    void deleteByJobIdAndUserEmail(Long jobId, String userEmail);

    @Transactional
    void deleteByUserEmail(String userEmail);
}