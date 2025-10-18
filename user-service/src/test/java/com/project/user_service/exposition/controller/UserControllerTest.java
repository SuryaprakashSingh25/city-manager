package com.project.user_service.exposition.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.user_service.application.port.in.UserService;
import com.project.user_service.config.TestSecurityConfig;
import com.project.user_service.exposition.dto.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = UserController.class,
        excludeFilters = {
                @ComponentScan.Filter(
                        type = FilterType.ASSIGNABLE_TYPE,
                        classes = {
                                com.project.user_service.infrastructure.security.SecurityConfig.class,
                                com.project.user_service.infrastructure.security.JwtAuthenticationFilter.class
                        }
                )
        }
)
@Import(TestSecurityConfig.class)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;
    private UserResponse userResponse;
    private AuthResponse authResponse;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequest("john@example.com", "password");
        loginRequest = new LoginRequest("john@example.com", "password");
        userResponse = new UserResponse("1", "john@example.com", "CITIZEN", "IN_PROGRESS");
        authResponse = new AuthResponse("accessToken", "refreshToken");
    }

    @Test
    void testRegister() throws Exception {
        Mockito.when(userService.register(any(RegisterRequest.class))).thenReturn(userResponse);

        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("john@example.com"));
    }

    @Test
    void testLogin() throws Exception {
        Mockito.when(userService.login(any(LoginRequest.class))).thenReturn(authResponse);

        mockMvc.perform(post("/api/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("accessToken"));
    }

    @Test
    void testVerifyEmail() throws Exception {
        mockMvc.perform(get("/api/users/verify").param("token", "xyz"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Email verified successfully"));

        Mockito.verify(userService).verifyEmail("xyz");
    }

    @Test
    void testForgotPassword() throws Exception {
        ForgotPasswordRequest req = new ForgotPasswordRequest("john@example.com");

        mockMvc.perform(post("/api/users/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Password reset link sent to your email"));

        Mockito.verify(userService).requestPasswordReset(any(ForgotPasswordRequest.class));
    }

    @Test
    void testResetPassword() throws Exception {
        ResetPasswordRequest req = new ResetPasswordRequest("token", "newPassword");

        mockMvc.perform(post("/api/users/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Password has been reset successfully"));

        Mockito.verify(userService).resetPassword(any(ResetPasswordRequest.class));
    }

    @Test
    void testRefreshToken() throws Exception {
        RefreshTokenRequest req = new RefreshTokenRequest("refreshToken");
        Mockito.when(userService.refreshToken(any(RefreshTokenRequest.class))).thenReturn(authResponse);

        mockMvc.perform(post("/api/users/refresh-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.refreshToken").value("refreshToken"));
    }

    @Test
    void testGetAllUsers() throws Exception {
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken("admin", "password",
                        List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));

        SecurityContextHolder.getContext().setAuthentication(auth);

        mockMvc.perform(get("/api/users/allUsers"))
                .andExpect(status().isOk());
    }
}
