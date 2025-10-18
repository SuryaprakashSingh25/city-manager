package com.project.user_service.infrastructure.repository;

import com.project.user_service.domain.model.Role;
import com.project.user_service.domain.model.Status;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "users")
public class UserDocument {
    @Id
    private String id;
    private String email;
    private String passwordHash;
    private Role role;
    private Status status;
    private Instant createdAt;
    private String verificationToken;
    private Instant verificationTokenExpiry;
    private String resetToken;
    private Instant resetTokenExpiry;
}
