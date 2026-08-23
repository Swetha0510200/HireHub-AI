package com.hirehub.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.hirehub.model.ResumeRecord;

@Repository
public interface ResumeRecordRepository extends JpaRepository<ResumeRecord, Long> {

    Optional<ResumeRecord> findFirstByUserEmailOrderByUploadedAtDesc(String userEmail);

    long countByUserEmail(String userEmail);
}
