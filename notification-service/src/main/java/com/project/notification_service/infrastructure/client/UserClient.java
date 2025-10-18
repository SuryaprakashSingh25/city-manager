package com.project.notification_service.infrastructure.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(name = "user-service")
public interface UserClient {

    @GetMapping("/api/internal/users/{id}")
    UserDto getUserById(@PathVariable("id") String id);

    @GetMapping("/api/internal/users/role/{role}")
    List<UserDto> getUsersByRole(@PathVariable("role") String role);

    record UserDto(String id, String email, String role, String status){}

}
