package com.project.user_service.application.port.in;

import com.project.user_service.domain.model.Role;
import com.project.user_service.exposition.dto.*;

import java.util.List;

public interface UserService {
    UserResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

    void verifyEmail(String token);

    void requestPasswordReset(ForgotPasswordRequest request);

    void resetPassword(ResetPasswordRequest request);

    AuthResponse refreshToken(RefreshTokenRequest request);

    UserResponse updateUserRole(String userId, Role newRole);

    List<UserResponse> getAllUsers();

    UserResponse getUserById(String id);

    List<UserResponse> getUsersByRole(String role);

}
