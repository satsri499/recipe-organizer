package com.example.demo.repository;

import com.example.demo.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    // Find user by email — used for login
    Optional<User> findByEmail(String email);

    // Check if email already exists — used for registration
    boolean existsByEmail(String email);
}