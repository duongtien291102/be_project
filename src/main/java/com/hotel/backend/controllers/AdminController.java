package com.hotel.backend.controllers;

import com.hotel.backend.dto.ApiResponse;
import com.hotel.backend.dto.GrantRoleRequest;
import com.hotel.backend.dto.UpdateRolePermissionsRequest;
import com.hotel.backend.dto.UserSummaryDto;
import com.hotel.backend.models.Role;
import com.hotel.backend.services.AdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    /**
     * Trao quyền / gán vai trò cho người dùng
     * CHỈ SYSADMIN MỚI CÓ QUYỀN
     */
    @PreAuthorize("hasRole('SYSADMIN')")
    @PostMapping("/roles/grant-to-user/{userId}")
    public ResponseEntity<ApiResponse<UserSummaryDto>> grantRolesToUser(
            @PathVariable String userId,
            @Valid @RequestBody GrantRoleRequest request) {
        UserSummaryDto updated = adminService.grantRolesToUser(userId, request.getRoles());
        return ResponseEntity.ok(ApiResponse.ok("SysAdmin đã trao quyền / cập nhật vai trò thành công cho người dùng", updated));
    }

    /**
     * Cập nhật chi tiết ma trận quyền hạn cho một Role
     * CHỈ SYSADMIN MỚI CÓ QUYỀN
     */
    @PreAuthorize("hasRole('SYSADMIN')")
    @PutMapping("/roles/{roleId}/permissions")
    public ResponseEntity<ApiResponse<Role>> updateRolePermissions(
            @PathVariable String roleId,
            @Valid @RequestBody UpdateRolePermissionsRequest request) {
        Role updated = adminService.updateRolePermissions(roleId, request.getPermissions());
        return ResponseEntity.ok(ApiResponse.ok("SysAdmin đã cập nhật quyền hạn cho vai trò: " + roleId, updated));
    }

    /**
     * Lấy danh sách tất cả các Role cùng cấu hình quyền hạn
     */
    @PreAuthorize("hasAnyRole('SYSADMIN', 'ADMIN')")
    @GetMapping("/roles")
    public ResponseEntity<ApiResponse<List<Role>>> getAllRoles() {
        return ResponseEntity.ok(ApiResponse.ok(adminService.getAllRoles()));
    }

    /**
     * Lấy danh sách tất cả tài khoản người dùng
     */
    @PreAuthorize("hasAnyRole('SYSADMIN', 'ADMIN')")
    @GetMapping("/users")
    public ResponseEntity<ApiResponse<List<UserSummaryDto>>> getAllUsers() {
        return ResponseEntity.ok(ApiResponse.ok(adminService.getAllUsers()));
    }

    /**
     * Khóa / Mở khóa tài khoản
     */
    @PreAuthorize("hasAnyRole('SYSADMIN', 'ADMIN')")
    @PatchMapping("/users/{userId}/toggle-status")
    public ResponseEntity<ApiResponse<UserSummaryDto>> toggleUserStatus(@PathVariable String userId) {
        UserSummaryDto updated = adminService.toggleUserActive(userId);
        return ResponseEntity.ok(ApiResponse.ok("Đã cập nhật trạng thái hoạt động của tài khoản", updated));
    }
}
