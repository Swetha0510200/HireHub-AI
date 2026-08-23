package com.hirehub.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.hirehub.model.Interview;

@Repository
public interface InterviewRepository extends JpaRepository<Interview, Long> {

    List<Interview> findByCandidateEmailOrderByInterviewDateDesc(String candidateEmail);

    List<Interview> findByRecruiterEmailOrderByInterviewDateDesc(String recruiterEmail);

    long countByCandidateEmail(String candidateEmail);

    long countByRecruiterEmail(String recruiterEmail);

    List<Interview> findAllByOrderByInterviewDateDesc();
}
