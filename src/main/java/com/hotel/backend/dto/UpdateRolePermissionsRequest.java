package com.hotel.backend.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Map;

@Data
public class UpdateRolePermissionsRequest {
    @NotNull(message = "Danh sách quyền không được để trống")
    private Map<String, Boolean> permissions;
}
