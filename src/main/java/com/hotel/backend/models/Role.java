package com.hotel.backend.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.HashMap;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "roles")
public class Role {

    @Id
    private String id; // e.g. "ROLE_SYSADMIN", "ROLE_ADMIN", "ROLE_USER"

    private String name; // e.g. "Quản trị viên hệ thống (SysAdmin)"

    private String description;

    private String badgeColor;

    @Builder.Default
    private Map<String, Boolean> permissions = new HashMap<>();
}
