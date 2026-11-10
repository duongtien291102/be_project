package com.hotel.backend.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.Set;

@Data
public class GrantRoleRequest {
    @NotEmpty(message = "Danh sách vai trò không được để trống")
    private Set<String> roles; // e.g. ["ROLE_ADMIN"], ["ROLE_MANAGER"]
}
