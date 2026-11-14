package com.hotel.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    private String token;
    @Builder.Default
    private String type = "Bearer";
    private String id;
    private String username;
    private String fullName;
    private String name;
    private String email;
    private String phone;
    private String roleId;
    private String roleName;
    private String status;
    private String avatar;
    private String dob;
    private String gender;
    private String nationality;
    private String address;
    private String cccd;
    private String cccdDate;
    private String cccdPlace;
    private String cccdFront;
    private String cccdBack;
    private Set<String> roles;
    private Map<String, Boolean> effectivePermissions;
}
