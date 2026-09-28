package com.salessavvy.repository;

import com.salessavvy.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Method name becomes SQL: WHERE email = ?
    Optional<User> findByEmail(String email);

    // SQL: WHERE email = ? AND enabled = true
    Optional<User> findByEmailAndEnabledTrue(String email);

    boolean existsByEmail(String email);
}
