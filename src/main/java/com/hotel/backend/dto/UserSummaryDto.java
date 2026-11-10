package com.hotel.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSummaryDto {
    private String id;
    private String username;
    private String fullName;
    private String email;
    private String phone;
    private Set<String> roles;
    private Map<String, Boolean> customPermissions;
    private boolean active;
    private Instant createdAt;
}
