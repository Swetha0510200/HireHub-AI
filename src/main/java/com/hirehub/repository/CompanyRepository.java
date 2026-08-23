package com.hirehub.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.hirehub.model.Company;

@Repository
public interface CompanyRepository extends JpaRepository<Company, Long> {
    Optional<Company> findFirstByRecruiterEmail(String recruiterEmail);
    Optional<Company> findFirstByRecruiterEmailOrderByCreatedAtDesc(String recruiterEmail);
    List<Company> findAllByOrderByNameAsc();
    boolean existsByNameIgnoreCase(String name);
}
