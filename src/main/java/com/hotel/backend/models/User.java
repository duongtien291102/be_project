package com.hotel.backend.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "users")
public class User {

    @Id
    private String id;

    @Indexed(unique = true)
    private String username;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    private String fullName;

    private String name;

    private String roleId;

    private String roleName;

    private String department;

    private String avatar;

    private String shift;

    private String joinDate;

    private String notes;

    private String status;

    @Indexed(unique = true)
    private String email;

    private String phone;

    private String dob;

    private String gender;

    private String nationality;

    private String address;

    private String cccd;

    private String cccdDate;

    private String cccdPlace;

    private String cccdFront;

    private String cccdBack;

    private String lastLogin;

    @Builder.Default
    private Set<String> roles = new HashSet<>();

    @Builder.Default
    private Map<String, Boolean> customPermissions = new HashMap<>();

    @Builder.Default
    private boolean active = true;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;
}
