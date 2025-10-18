package com.project.user_service.domain.model;

import java.time.Instant;
import java.util.UUID;

public class User {

    private final String id;
    private String email;
    private String passwordHash;
    private Role role;
    private Status status;
    private final Instant createdAt;

    private String verificationToken;
    private Instant verificationTokenExpiry;

    private String resetToken;
    private Instant resetTokenExpiry;

    public User(String email, String passwordHash, Role role){
        this.id= UUID.randomUUID().toString();
        this.email=email;
        this.passwordHash=passwordHash;
        this.role=role != null ? role : Role.CITIZEN;
        this.status=Status.PENDING_VERIFICATION;
        this.createdAt=Instant.now();
        this.verificationToken = UUID.randomUUID().toString();
        this.verificationTokenExpiry = Instant.now().plusSeconds(3600);
    }

    public void activate(String token){
        if (!token.equals(this.verificationToken) || Instant.now().isAfter(verificationTokenExpiry)) {
            throw new IllegalStateException("Invalid or expired verification token.");
        }
        this.status = Status.ACTIVE;
        this.verificationToken = null;
        this.verificationTokenExpiry = null;
    }

    public void requestPasswordReset(){
        if (this.status == Status.INACTIVE) {
            throw new IllegalStateException("Cannot reset password for inactive user.");
        }
        this.status = Status.RESET_REQUESTED;
        this.resetToken = UUID.randomUUID().toString();
        this.resetTokenExpiry = Instant.now().plusSeconds(900);
    }

    public void completePasswordReset(String token, String newPasswordHash) {
        if (this.resetToken == null || this.resetTokenExpiry == null) {
            throw new IllegalStateException("No reset request found for this user.");
        }
        if (!token.equals(this.resetToken) || Instant.now().isAfter(this.resetTokenExpiry)) {
            throw new IllegalStateException("Invalid or expired reset token.");
        }
        this.passwordHash = newPasswordHash;
        this.status = Status.ACTIVE;
        this.resetToken = null;
        this.resetTokenExpiry = null;
    }

    public void changeRole(Role newRole) {
        if (this.status != Status.ACTIVE) {
            throw new IllegalStateException("Only active users can have their role changed.");
        }
        if (newRole == null) {
            throw new IllegalArgumentException("Role cannot be null.");
        }
        this.role = newRole;
    }



    // getters only
    public String getId() { return id; }
    public String getEmail() { return email; }
    public String getPasswordHash() { return passwordHash; }
    public Role getRole() { return role; }
    public Status getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
    public String getVerificationToken() { return verificationToken; }
    public Instant getVerificationTokenExpiry() { return verificationTokenExpiry; }
    public String getResetToken() { return resetToken; }
    public Instant getResetTokenExpiry() { return resetTokenExpiry; }

}
