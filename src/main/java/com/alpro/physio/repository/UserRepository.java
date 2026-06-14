package com.alpro.physio.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.alpro.physio.entity.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    // Optional: if you ever need to check by userId
    Optional<User> findByUserId(String userId);
}
