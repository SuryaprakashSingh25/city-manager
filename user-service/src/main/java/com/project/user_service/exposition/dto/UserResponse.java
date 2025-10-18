package com.project.user_service.exposition.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserResponse {
    private String id;
    private String email;
    private String role;
    private String status;
}
