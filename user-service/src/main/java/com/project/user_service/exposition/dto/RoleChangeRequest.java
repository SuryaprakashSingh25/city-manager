package com.project.user_service.exposition.dto;

import com.project.user_service.domain.model.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class RoleChangeRequest {
    private Role role;
    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
}

