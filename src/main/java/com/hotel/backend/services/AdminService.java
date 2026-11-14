package com.hotel.backend.services;

import com.hotel.backend.dto.UserSummaryDto;
import com.hotel.backend.models.Role;
import com.hotel.backend.models.User;
import com.hotel.backend.repositories.RoleRepository;
import com.hotel.backend.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    /**
     * Trao quyền / gán roles cho một tài khoản người dùng
     * Chỉ SysAdmin có quyền thực hiện
     */
    public UserSummaryDto grantRolesToUser(String userId, Set<String> newRoles) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng với ID: " + userId));

        // Chuẩn hóa và kiểm tra sự tồn tại của role
        Set<String> formattedRoles = newRoles.stream()
                .map(r -> r.startsWith("ROLE_") ? r : "ROLE_" + r.toUpperCase())
                .collect(Collectors.toSet());

        for (String roleId : formattedRoles) {
            if (!roleRepository.existsById(roleId)) {
                throw new IllegalArgumentException("Vai trò không hợp lệ: " + roleId);
            }
        }

        user.setRoles(formattedRoles);
        user.setUpdatedAt(Instant.now());
        User saved = userRepository.save(user);

        return toSummaryDto(saved);
    }

    /**
     * Cấu hình quyền hạn chi tiết cho từng Role trong hệ thống
     * Chỉ SysAdmin có quyền cập nhật ma trận phân quyền của các Role
     */
    public Role updateRolePermissions(String roleId, Map<String, Boolean> newPermissions) {
        String formattedRoleId = roleId.startsWith("ROLE_") ? roleId : "ROLE_" + roleId.toUpperCase();

        Role role = roleRepository.findById(formattedRoleId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy vai trò: " + formattedRoleId));

        // Cập nhật ma trận permissions
        role.getPermissions().putAll(newPermissions);
        return roleRepository.save(role);
    }

    public List<UserSummaryDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::toSummaryDto)
                .collect(Collectors.toList());
    }

    public List<Role> getAllRoles() {
        return roleRepository.findAll();
    }

    public UserSummaryDto toggleUserActive(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng với ID: " + userId));

        // Không cho phép khóa tài khoản sysadmin hoặc bất kỳ tài khoản nào có quyền ROLE_SYSADMIN
        boolean isSysAdminAccount = "sysadmin".equalsIgnoreCase(user.getUsername())
                || (user.getRoles() != null && user.getRoles().contains("ROLE_SYSADMIN"))
                || "ROLE_SYSADMIN".equalsIgnoreCase(user.getRoleId());
        if (isSysAdminAccount && user.isActive()) {
            throw new IllegalArgumentException("Không thể khóa tài khoản quản trị cấp cao SysAdmin.");
        }

        user.setActive(!user.isActive());
        user.setUpdatedAt(Instant.now());
        User saved = userRepository.save(user);
        return toSummaryDto(saved);
    }

    private UserSummaryDto toSummaryDto(User user) {
        return UserSummaryDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .roles(user.getRoles())
                .customPermissions(user.getCustomPermissions())
                .active(user.isActive())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
