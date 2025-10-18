package com.project.user_service.domain.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    private User user;

    @BeforeEach
    void setUp() {
        user = new User("test@example.com", "hashedPassword", Role.CITIZEN);
    }

    // ---------------- activate() ----------------
    @Test
    void activate_withValidToken_shouldActivateUser() {
        String token = user.getVerificationToken();
        user.activate(token);
        assertEquals(Status.ACTIVE, user.getStatus());
        assertNull(user.getVerificationToken());
        assertNull(user.getVerificationTokenExpiry());
    }

    @Test
    void activate_withInvalidToken_shouldThrow() {
        String invalidToken = "wrong-token";
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> user.activate(invalidToken));
        assertEquals("Invalid or expired verification token.", exception.getMessage());
    }

    @Test
    void requestPasswordReset_InactiveUser_shouldThrow() throws Exception {
        // Set user status to INACTIVE using reflection
        var statusField = User.class.getDeclaredField("status");
        statusField.setAccessible(true);
        statusField.set(user, Status.INACTIVE);

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> user.requestPasswordReset());
        assertEquals("Cannot reset password for inactive user.", exception.getMessage());
    }


    // ---------------- requestPasswordReset() ----------------
    @Test
    void requestPasswordReset_shouldSetStatusAndToken() {
        user.activate(user.getVerificationToken()); // make ACTIVE
        user.requestPasswordReset();

        assertEquals(Status.RESET_REQUESTED, user.getStatus());
        assertNotNull(user.getResetToken());
        assertNotNull(user.getResetTokenExpiry());
    }

    @Test
    void requestPasswordReset_onInactiveUser_shouldThrow() throws Exception {
        User user = new User("test@example.com", "hashed", Role.CITIZEN);

        // Set status to INACTIVE
        var statusField = User.class.getDeclaredField("status");
        statusField.setAccessible(true);
        statusField.set(user, Status.INACTIVE);

        // Now call and assert
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> user.requestPasswordReset());

        assertEquals("Cannot reset password for inactive user.", exception.getMessage());
    }


    // ---------------- completePasswordReset() ----------------
    @Test
    void completePasswordReset_withValidToken_shouldResetPassword() {
        user.activate(user.getVerificationToken());
        user.requestPasswordReset();
        String resetToken = user.getResetToken();

        user.completePasswordReset(resetToken, "newHashedPassword");

        assertEquals(Status.ACTIVE, user.getStatus());
        assertEquals("newHashedPassword", user.getPasswordHash());
        assertNull(user.getResetToken());
        assertNull(user.getResetTokenExpiry());
    }

    @Test
    void completePasswordReset_withoutRequest_shouldThrow() {
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> user.completePasswordReset("any-token", "newHash"));
        assertEquals("No reset request found for this user.", exception.getMessage());
    }

    @Test
    void completePasswordReset_withInvalidToken_shouldThrow() {
        user.activate(user.getVerificationToken());
        user.requestPasswordReset();
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> user.completePasswordReset("wrong-token", "newHash"));
        assertEquals("Invalid or expired reset token.", exception.getMessage());
    }

    @Test
    void completePasswordReset_withExpiredToken_shouldThrow() throws Exception {
        User user = new User("test@example.com", "hashedPassword", Role.CITIZEN);

        // Step 1: request password reset
        user.requestPasswordReset();

        // Step 2: set expiry to past using reflection
        var expiryField = User.class.getDeclaredField("resetTokenExpiry");
        expiryField.setAccessible(true);
        expiryField.set(user, Instant.now().minusSeconds(1)); // expired

        // Step 3: call completePasswordReset and assert exception
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> user.completePasswordReset(user.getResetToken(), "newHash"));

        assertEquals("Invalid or expired reset token.", exception.getMessage());
    }


    // ---------------- changeRole() ----------------
    @Test
    void changeRole_withActiveUser_shouldChangeRole() {
        user.activate(user.getVerificationToken());
        user.changeRole(Role.ADMIN);
        assertEquals(Role.ADMIN, user.getRole());
    }

    @Test
    void changeRole_withNullRole_shouldThrow() {
        user.activate(user.getVerificationToken());
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> user.changeRole(null));
        assertEquals("Role cannot be null.", exception.getMessage());
    }

    @Test
    void changeRole_withNonActiveUser_shouldThrow() {
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> user.changeRole(Role.ADMIN));
        assertEquals("Only active users can have their role changed.", exception.getMessage());
    }
}
