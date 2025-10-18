package com.project.user_service.infrastructure.repository;

import com.project.user_service.domain.model.Role;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface UserMongoRepository extends MongoRepository<UserDocument, String> {
    Optional<UserDocument> findByEmail(String email);
    Optional<UserDocument> findByVerificationToken(String token);
    Optional<UserDocument> findByResetToken(String token);
    boolean existsByRole(Role role);
}
