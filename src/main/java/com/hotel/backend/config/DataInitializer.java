package com.hotel.backend.config;

import com.hotel.backend.models.Role;
import com.hotel.backend.models.User;
import com.hotel.backend.repositories.RoleRepository;
import com.hotel.backend.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.sysadmin.username:sysadmin}")
    private String sysadminUsername;

    @Value("${app.sysadmin.password:Sysadmin@123}")
    private String sysadminPassword;

    @Value("${app.sysadmin.email:sysadmin@grandhotel.vn}")
    private String sysadminEmail;

    @Value("${app.sysadmin.fullName:Hệ Thống Quản Trị Cấp Cao (SysAdmin)}")
    private String sysadminFullName;

    @Override
    public void run(String... args) {
        initRoles();
        initSysAdmin();
    }

    private static Map<String, Boolean> makeMap(Object... entries) {
        Map<String, Boolean> map = new HashMap<>();
        for (int i = 0; i < entries.length; i += 2) {
            map.put((String) entries[i], (Boolean) entries[i + 1]);
        }
        return map;
    }

    private void initRoles() {
        // 1. SYSADMIN - Toàn quyền quản trị và trao quyền cho tất cả các role khác
        createRoleIfNotFound("ROLE_SYSADMIN", "Quản Trị Viên Hệ Thống (SysAdmin)",
                "Toàn quyền quản trị hệ thống, cấp phát quyền hạn và phân công vai trò cho mọi tài khoản",
                "#b91c1c", makeMap(
                        "manage_roles", true,
                        "grant_permissions", true,
                        "view_kpi", true,
                        "view_revenue", true,
                        "manage_rooms", true,
                        "room_checkin_checkout", true,
                        "room_cleaning", true,
                        "manage_bookings", true,
                        "create_booking", true,
                        "cancel_booking", true,
                        "manage_finances", true,
                        "view_staff", true,
                        "manage_staff", true,
                        "config_permissions", true,
                        "system_reset", true
                ));

        // 2. ADMIN - Giám Đốc / Tổng Quản Lý
        createRoleIfNotFound("ROLE_ADMIN", "Giám Đốc / Tổng Quản Lý",
                "Toàn quyền điều hành hoạt động kinh doanh khách sạn",
                "#dc2626", makeMap(
                        "view_kpi", true,
                        "view_revenue", true,
                        "manage_rooms", true,
                        "room_checkin_checkout", true,
                        "room_cleaning", true,
                        "manage_bookings", true,
                        "create_booking", true,
                        "cancel_booking", true,
                        "manage_finances", true,
                        "view_staff", true,
                        "manage_staff", true,
                        "config_permissions", true,
                        "system_reset", true
                ));

        // 3. MANAGER - Quản Lý Lễ Tân
        createRoleIfNotFound("ROLE_MANAGER", "Quản Lý Lễ Tân",
                "Quản lý hoạt động sảnh, buồng phòng và nhân sự trực ban",
                "#d97706", makeMap(
                        "view_kpi", true,
                        "view_revenue", true,
                        "manage_rooms", true,
                        "room_checkin_checkout", true,
                        "room_cleaning", true,
                        "manage_bookings", true,
                        "create_booking", true,
                        "cancel_booking", true,
                        "manage_finances", true,
                        "view_staff", true,
                        "manage_staff", false,
                        "config_permissions", false,
                        "system_reset", false
                ));

        // 4. RECEPTIONIST - Nhân Viên Lễ Tân
        createRoleIfNotFound("ROLE_RECEPTIONIST", "Nhân Viên Lễ Tân",
                "Thực hiện đặt phòng, check-in, check-out và thu tiền khách lẻ",
                "#2563eb", makeMap(
                        "view_kpi", true,
                        "view_revenue", false,
                        "manage_rooms", false,
                        "room_checkin_checkout", true,
                        "room_cleaning", false,
                        "manage_bookings", true,
                        "create_booking", true,
                        "cancel_booking", false,
                        "manage_finances", true,
                        "view_staff", false,
                        "manage_staff", false,
                        "config_permissions", false,
                        "system_reset", false
                ));

        // 5. CASHIER - Kế Toán / Thủ Quỹ
        createRoleIfNotFound("ROLE_CASHIER", "Kế Toán / Thủ Quỹ",
                "Kiểm soát dòng tiền, quyết toán đơn đặt và báo cáo thu chi",
                "#7c3aed", makeMap(
                        "view_kpi", true,
                        "view_revenue", true,
                        "manage_rooms", false,
                        "room_checkin_checkout", true,
                        "room_cleaning", false,
                        "manage_bookings", true,
                        "create_booking", false,
                        "cancel_booking", false,
                        "manage_finances", true,
                        "view_staff", false,
                        "manage_staff", false,
                        "config_permissions", false,
                        "system_reset", false
                ));

        // 6. HOUSEKEEPING - Buồng Phòng
        createRoleIfNotFound("ROLE_HOUSEKEEPING", "Quản Lý Buồng Phòng",
                "Cập nhật trạng thái vệ sinh phòng sẵn sàng đón khách",
                "#059669", makeMap(
                        "view_kpi", false,
                        "view_revenue", false,
                        "manage_rooms", false,
                        "room_checkin_checkout", false,
                        "room_cleaning", true,
                        "manage_bookings", false,
                        "create_booking", false,
                        "cancel_booking", false,
                        "manage_finances", false,
                        "view_staff", false,
                        "manage_staff", false,
                        "config_permissions", false,
                        "system_reset", false
                ));

        // 7. USER - Khách hàng thông thường (Role mặc định khi đăng ký tài khoản)
        createRoleIfNotFound("ROLE_USER", "Khách Hàng (User)",
                "Quyền hạn cơ bản dành cho tài khoản người dùng đăng ký thông thường",
                "#64748b", makeMap(
                        "view_rooms", true,
                        "create_booking", true,
                        "view_my_bookings", true
                ));
    }

    private void createRoleIfNotFound(String roleId, String name, String desc, String badgeColor, Map<String, Boolean> perms) {
        if (!roleRepository.existsById(roleId)) {
            Role role = Role.builder()
                    .id(roleId)
                    .name(name)
                    .description(desc)
                    .badgeColor(badgeColor)
                    .permissions(new HashMap<>(perms))
                    .build();
            roleRepository.save(role);
            log.info("Initialized role: {}", roleId);
        }
    }

    private void initSysAdmin() {
        if (!userRepository.existsByUsername(sysadminUsername)) {
            User sysadmin = User.builder()
                    .username(sysadminUsername)
                    .password(passwordEncoder.encode(sysadminPassword))
                    .fullName(sysadminFullName)
                    .email(sysadminEmail)
                    .phone("0900000000")
                    .roles(Set.of("ROLE_SYSADMIN", "ROLE_ADMIN"))
                    .active(true)
                    .createdAt(Instant.now())
                    .updatedAt(Instant.now())
                    .build();

            userRepository.save(sysadmin);
            log.info("=================================================================");
            log.info("TÀI KHOẢN SYSADMIN ĐÃ ĐƯỢC KHỞI TẠO TỰ ĐỘNG THÀNH CÔNG:");
            log.info("Username: {}", sysadminUsername);
            log.info("Password: {}", sysadminPassword);
            log.info("Roles   : ROLE_SYSADMIN, ROLE_ADMIN");
            log.info("Quyền   : Toàn quyền cấu hình và trao quyền cho mọi role khác");
            log.info("=================================================================");
        }
    }
}
