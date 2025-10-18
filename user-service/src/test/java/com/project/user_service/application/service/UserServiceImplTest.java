package com.project.user_service.application.service;

import com.project.user_service.application.port.out.MailEventPublisher;
import com.project.user_service.domain.model.Role;
import com.project.user_service.domain.model.Status;
import com.project.user_service.domain.model.User;
import com.project.user_service.domain.repository.UserRepository;
import com.project.user_service.exposition.dto.*;
import com.project.user_service.infrastructure.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock private UserRepository userRepository;
    @Mock private MailEventPublisher mailEventPublisher;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtUtil jwtUtil;

    @InjectMocks
    private UserServiceImpl userService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User("test@example.com", "encodedPassword", Role.CITIZEN);
    }

    // ------------------ REGISTER ------------------
    @Test
    void register_success() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);

        UserResponse response = userService.register(new RegisterRequest("test@example.com", "password"));

        assertEquals(user.getEmail(), response.getEmail());
        assertEquals(user.getRole().name(), response.getRole());
        verify(mailEventPublisher, times(1)).publishUserRegistered(any(User.class));
    }

    @Test
    void register_emailAlreadyExists_throwsException() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> userService.register(new RegisterRequest("test@example.com", "password")));

        assertEquals("Email already exists", ex.getMessage());
    }

    // ------------------ LOGIN ------------------
    @Test
    void login_success() {
        user.activate(user.getVerificationToken());
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        when(jwtUtil.generateAccessToken(anyString(), anyString())).thenReturn("accessToken");
        when(jwtUtil.generateRefreshToken(anyString())).thenReturn("refreshToken");

        AuthResponse response = userService.login(new LoginRequest("test@example.com", "password"));

        assertEquals("accessToken", response.getAccessToken());
        assertEquals("refreshToken", response.getRefreshToken());
    }

    @Test
    void login_invalidCredentials_throwsException() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> userService.login(new LoginRequest("test@example.com", "password")));

        assertEquals("Invalid Credentials", ex.getMessage());
    }

    @Test
    void login_pendingVerification_throwsException() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> userService.login(new LoginRequest("test@example.com", "password")));

        assertEquals("Please verify your email before logging in.", ex.getMessage());
    }

    // ------------------ VERIFY EMAIL ------------------
    @Test
    void verifyEmail_success() {
        User user = new User("email@test.com", "hashedPassword", Role.CITIZEN);
        String token = user.getVerificationToken();

        when(userRepository.findByVerificationToken(token))
                .thenReturn(Optional.of(user));

        assertDoesNotThrow(() -> userService.verifyEmail(token));
    }


    @Test
    void verifyEmail_invalidToken_throwsException() {
        when(userRepository.findByVerificationToken(anyString())).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> userService.verifyEmail("invalidToken"));

        assertEquals("Invalid token", ex.getMessage());
    }

    // ------------------ PASSWORD RESET ------------------
    @Test
    void requestPasswordReset_success() {
        user.activate(user.getVerificationToken());
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        assertDoesNotThrow(() -> userService.requestPasswordReset(new ForgotPasswordRequest("test@example.com")));
        verify(mailEventPublisher, times(1)).publishPasswordResetRequested(any(User.class));
    }

    @Test
    void requestPasswordReset_userNotFound_throwsException() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> userService.requestPasswordReset(new ForgotPasswordRequest("test@example.com")));

        assertEquals("User not found", ex.getMessage());
    }

    @Test
    void resetPassword_success() {
        user.activate(user.getVerificationToken());
        user.requestPasswordReset();
        when(userRepository.findByResetToken(anyString())).thenReturn(Optional.of(user));
        when(passwordEncoder.encode(anyString())).thenReturn("newHash");
        when(userRepository.save(any(User.class))).thenReturn(user);

        assertDoesNotThrow(() -> userService.resetPassword(new ResetPasswordRequest(user.getResetToken(), "newPassword")));
        assertEquals(Status.ACTIVE, user.getStatus());
    }

    @Test
    void resetPassword_invalidToken_throwsException() {
        when(userRepository.findByResetToken(anyString())).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> userService.resetPassword(new ResetPasswordRequest("invalidToken", "newPassword")));

        assertEquals("Invalid reset token", ex.getMessage());
    }

    // ------------------ REFRESH TOKEN ------------------
    @Test
    void refreshToken_success() {
        user.activate(user.getVerificationToken());
        when(jwtUtil.extractUserId(anyString())).thenReturn(user.getId());
        when(userRepository.findById(anyString())).thenReturn(Optional.of(user));
        when(jwtUtil.validateToken(anyString())).thenReturn(true);
        when(jwtUtil.generateAccessToken(anyString(), anyString())).thenReturn("accessToken");
        when(jwtUtil.generateRefreshToken(anyString())).thenReturn("refreshToken");

        AuthResponse response = userService.refreshToken(new RefreshTokenRequest("refreshToken"));
        assertEquals("accessToken", response.getAccessToken());
        assertEquals("refreshToken", response.getRefreshToken());
    }

    @Test
    void refreshToken_invalidToken_throwsException() {
        when(jwtUtil.extractUserId(anyString())).thenReturn(user.getId());
        when(userRepository.findById(anyString())).thenReturn(Optional.of(user));
        when(jwtUtil.validateToken(anyString())).thenReturn(false);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> userService.refreshToken(new RefreshTokenRequest("refreshToken")));

        assertEquals("Invalid refresh token", ex.getMessage());
    }

    // ------------------ UPDATE USER ROLE ------------------
    @Test
    void updateUserRole_success() {
        user.activate(user.getVerificationToken());
        when(userRepository.findById(anyString())).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        UserResponse response = userService.updateUserRole(user.getId(), Role.ADMIN);
        assertEquals(Role.ADMIN.name(), response.getRole());
    }

    @Test
    void updateUserRole_userNotFound_throwsException() {
        when(userRepository.findById(anyString())).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> userService.updateUserRole("invalidId", Role.ADMIN));

        assertEquals("User not found", ex.getMessage());
    }

    // ------------------ GET USERS ------------------
    @Test
    void getAllUsers_success() {
        when(userRepository.findAll()).thenReturn(List.of(user));

        List<UserResponse> users = userService.getAllUsers();
        assertEquals(1, users.size());
        assertEquals(user.getEmail(), users.get(0).getEmail());
    }

    @Test
    void getUserById_success() {
        when(userRepository.findById(anyString())).thenReturn(Optional.of(user));

        UserResponse response = userService.getUserById(user.getId());
        assertEquals(user.getEmail(), response.getEmail());
    }

    @Test
    void getUserById_notFound_throwsException() {
        when(userRepository.findById(anyString())).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> userService.getUserById("invalidId"));
        assertEquals("User not found", ex.getMessage());
    }

    @Test
    void getUsersByRole_success() {
        user.activate(user.getVerificationToken());
        when(userRepository.findAll()).thenReturn(List.of(user));

        List<UserResponse> users = userService.getUsersByRole("CITIZEN");
        assertEquals(1, users.size());
        assertEquals(user.getEmail(), users.get(0).getEmail());
    }
}
