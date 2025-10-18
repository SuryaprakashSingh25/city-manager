package com.project.user_service.exposition.controller;

import com.project.user_service.application.port.in.UserService;
import com.project.user_service.exposition.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@RequestBody RegisterRequest request) {
        return ResponseEntity.ok(userService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(userService.login(request));
    }

    @GetMapping("/verify")
    public ResponseEntity<Map<String, String>> verifyEmail(@RequestParam("token") String token) {
        userService.verifyEmail(token);
        return ResponseEntity.ok(Map.of("message", "Email verified successfully"));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, String>> forgotPassword(@RequestBody ForgotPasswordRequest request) {
        userService.requestPasswordReset(request);
        return ResponseEntity.ok(Map.of("message", "Password reset link sent to your email"));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, String>> resetPassword(@RequestBody ResetPasswordRequest request) {
        userService.resetPassword(request);
        return ResponseEntity.ok(Map.of("message", "Password has been reset successfully"));
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<AuthResponse> refreshToken(@RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(userService.refreshToken(request));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/allUsers/{userId}/role")
    public ResponseEntity<?> changeUserRole(
            @PathVariable("userId") String userId,
            @RequestBody RoleChangeRequest request) {

        userService.updateUserRole(userId, request.getRole());
        return ResponseEntity.ok("Role updated successfully");
    }

    @GetMapping("/allUsers")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        List<UserResponse> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

}
