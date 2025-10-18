package com.project.user_service.application.service;

import com.project.user_service.application.port.in.UserService;
import com.project.user_service.application.port.out.MailEventPublisher;
import com.project.user_service.domain.model.Role;
import com.project.user_service.domain.model.Status;
import com.project.user_service.domain.model.User;
import com.project.user_service.domain.repository.UserRepository;
import com.project.user_service.exposition.dto.*;
import com.project.user_service.infrastructure.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final MailEventPublisher mailEventPublisher;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtService;

    @Override
    public UserResponse register(RegisterRequest request) {
        userRepository.findByEmail(request.getEmail())
                .ifPresent(u -> {
                    throw new IllegalArgumentException("Email already exists");
                });

        User user=new User(
                request.getEmail(),
                passwordEncoder.encode(request.getPassword()),
                Role.CITIZEN
        );

        User saved=userRepository.save(user);
        mailEventPublisher.publishUserRegistered(saved);

        return new UserResponse(saved.getId(), saved.getEmail(), saved.getRole().name(), saved.getStatus().name());

    }

    @Override
    public AuthResponse login(LoginRequest request) {
        User user=userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Invalid Credentials"));

        if (user.getStatus() == Status.PENDING_VERIFICATION) {   // or user.getEmailVerified(), depending on your entity
            throw new IllegalStateException("Please verify your email before logging in.");
        }

        if(!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())){
            throw new IllegalArgumentException("Invalid Credentials");
        }

        String accessToken= jwtService.generateAccessToken(user.getId(),user.getRole().name());
        String refreshToken= jwtService.generateRefreshToken(user.getId());

        return new AuthResponse(accessToken,refreshToken);
    }

    @Override
    public void verifyEmail(String token) {
        User user=userRepository.findByVerificationToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Invalid token"));
        user.activate(token);
        userRepository.save(user);
    }

    @Override
    public void requestPasswordReset(ForgotPasswordRequest request) {
        User user=userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        user.requestPasswordReset();
        userRepository.save(user);
        mailEventPublisher.publishPasswordResetRequested(user);
    }

    @Override
    public void resetPassword(ResetPasswordRequest request) {
        User user=userRepository.findByResetToken(request.getToken())
                .orElseThrow(() -> new IllegalArgumentException("Invalid reset token"));
        user.completePasswordReset(request.getToken(), passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    @Override
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        String id=jwtService.extractUserId(request.getRefreshToken());
        User user=userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if(!jwtService.validateToken(request.getRefreshToken())){
            throw new IllegalArgumentException("Invalid refresh token");
        }

        String accessToken= jwtService.generateAccessToken(user.getId(),user.getRole().name());
        String refreshToken= jwtService.generateRefreshToken(user.getId());

        return new AuthResponse(accessToken,refreshToken);
    }

    @Override
    public UserResponse updateUserRole(String userId,Role newRole){
        User user=userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        user.changeRole(newRole);
        User saved=userRepository.save(user);

        return new UserResponse(saved.getId(), saved.getEmail(), saved.getRole().name(), saved.getStatus().name());
    }

    @Override
    public List<UserResponse> getAllUsers(){
        return userRepository.findAll()
                .stream()
                .map(user -> new UserResponse(
                        user.getId(),
                        user.getEmail(),
                        user.getRole().name(),
                        user.getStatus().name()
                )).toList();
    }

    @Override
    public UserResponse getUserById(String id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return new UserResponse(user.getId(), user.getEmail(), user.getRole().name(), user.getStatus().name());
    }

    @Override
    public List<UserResponse> getUsersByRole(String role) {
        return userRepository.findAll().stream()
                .filter(user -> user.getRole().name().equalsIgnoreCase(role))
                .map(u -> new UserResponse(u.getId(), u.getEmail(), u.getRole().name(), u.getStatus().name()))
                .toList();
    }


}
