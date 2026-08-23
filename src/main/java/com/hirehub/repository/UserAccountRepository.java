package com.hirehub.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.hirehub.model.UserAccount;

@Repository
public interface UserAccountRepository extends JpaRepository<UserAccount, String> {
    List<UserAccount> findByRoleIgnoreCase(String role);
    long countByRoleIgnoreCase(String role);
    List<UserAccount> findAllByOrderByCreatedAtDesc();
}
