package com.hirehub.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.hirehub.model.UserProfile;

@Repository
public interface UserProfileRepository
        extends JpaRepository<UserProfile, String> {

    Optional<UserProfile> findByEmail(String email);
}