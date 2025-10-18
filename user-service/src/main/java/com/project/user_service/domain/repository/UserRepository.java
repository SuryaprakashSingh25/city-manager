package com.project.user_service.domain.repository;

import com.project.user_service.domain.model.Role;
import com.project.user_service.domain.model.User;

import java.util.List;
import java.util.Optional;

public interface UserRepository {
    User save(User user);

    Optional<User> findByEmail(String email);

    Optional<User> findById(String id);

    Optional<User> findByVerificationToken(String token);

    Optional<User> findByResetToken(String token);

    boolean existsByRole(Role role);

    List<User> findAll();
}
