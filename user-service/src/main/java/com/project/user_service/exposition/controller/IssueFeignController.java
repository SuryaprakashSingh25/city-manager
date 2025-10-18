package com.project.user_service.exposition.controller;

import com.project.user_service.application.port.in.UserService;
import com.project.user_service.exposition.dto.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/internal/users")
@RequiredArgsConstructor
public class IssueFeignController {

    private final UserService userService;

    @GetMapping("/{id}")
    public UserResponse getUserById(@PathVariable("id") String id) {
        return userService.getUserById(id);
    }

    @GetMapping("/role/{role}")
    public List<UserResponse> getUsersByRole(@PathVariable("role") String role) {
        return userService.getUsersByRole(role);
    }

    public record UserDto(String id, String email, String role, String status) {}

}
