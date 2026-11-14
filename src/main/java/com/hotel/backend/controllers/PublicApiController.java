package com.hotel.backend.controllers;

import com.hotel.backend.config.UserPrincipal;
import com.hotel.backend.dto.ApiResponse;
import com.hotel.backend.dto.StaffRequest;
import com.hotel.backend.models.Booking;
import com.hotel.backend.models.Role;
import com.hotel.backend.models.Room;
import com.hotel.backend.models.Transaction;
import com.hotel.backend.models.User;
import com.hotel.backend.repositories.BookingRepository;
import com.hotel.backend.repositories.RoleRepository;
import com.hotel.backend.repositories.RoomRepository;
import com.hotel.backend.repositories.TransactionRepository;
import com.hotel.backend.repositories.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/public")
@RequiredArgsConstructor
public class PublicApiController {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final Map<String, Double> ADDON_FIXED_PRICES = Map.of(
            "Buffet Ăn Sáng Hoàng Gia", 450_000.0,
            "Đưa Đón Sân Bay", 450_000.0,
            "Đưa Đón Sân Bay Khứ Hồi", 450_000.0,
            "Lotus Spa", 600_000.0,
            "Lotus Spa VIP", 450_000.0
    );

    private final RoomRepository roomRepository;
    private final BookingRepository bookingRepository;
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    private String generateUniqueId(String prefix, Predicate<String> existsChecker) {
        for (int attempt = 0; attempt < 10; attempt++) {
            byte[] bytes = new byte[4];
            SECURE_RANDOM.nextBytes(bytes);
            StringBuilder sb = new StringBuilder(prefix);
            for (byte b : bytes) {
                sb.append(String.format("%02X", b));
            }
            String candidate = sb.toString();
            if (!existsChecker.test(candidate)) {
                return candidate;
            }
        }
        return prefix + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private boolean hasAnyAuthority(UserPrincipal user, String... authorities) {
        if (user == null || user.getAuthorities() == null) {
            return false;
        }
        Set<String> granted = user.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());
        for (String auth : authorities) {
            if (granted.contains(auth)) {
                return true;
            }
        }
        return false;
    }

    private boolean isStaffBookingHandler(UserPrincipal user) {
        return hasAnyAuthority(user,
                "ROLE_SYSADMIN", "ROLE_ADMIN", "ROLE_MANAGER", "ROLE_RECEPTIONIST", "ROLE_CASHIER",
                "manage_bookings", "room_checkin_checkout", "view_revenue", "manage_finances");
    }

    private boolean isSysAdminAccount(User user) {
        return "sysadmin".equalsIgnoreCase(user.getUsername())
                || "ROLE_SYSADMIN".equalsIgnoreCase(user.getRoleId())
                || (user.getRoles() != null && user.getRoles().contains("ROLE_SYSADMIN"));
    }

    private boolean isBookingOwnedBy(Booking booking, UserPrincipal principal, String userCccd) {
        if (booking == null || principal == null) {
            return false;
        }
        if (principal.getId() != null && principal.getId().equals(booking.getUserId())) {
            return true;
        }
        if (principal.getUsername() != null && principal.getUsername().equalsIgnoreCase(booking.getUsername())) {
            return true;
        }
        return userCccd != null && !userCccd.isBlank() && userCccd.equals(booking.getCccd());
    }

    @GetMapping("/rooms")
    public ResponseEntity<ApiResponse<List<Room>>> getRooms() {
        return ResponseEntity.ok(ApiResponse.ok(roomRepository.findAll()));
    }

    @PreAuthorize("hasAnyAuthority('ROLE_SYSADMIN', 'ROLE_ADMIN', 'manage_rooms', 'room_cleaning', 'room_checkin_checkout')")
    @PostMapping("/rooms")
    public ResponseEntity<ApiResponse<Room>> createOrUpdateRoom(
            @Valid @RequestBody Room room,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        boolean canManageCatalog = hasAnyAuthority(currentUser, "ROLE_SYSADMIN", "ROLE_ADMIN", "manage_rooms");
        var existingOpt = roomRepository.findById(room.getId());

        if (existingOpt.isEmpty() && !canManageCatalog) {
            throw new AccessDeniedException("Bạn không có quyền thêm phòng mới.");
        }

        if (existingOpt.isPresent() && !canManageCatalog) {
            // Staff without catalog permission can only update room occupancy/cleaning status
            Room existing = existingOpt.get();
            existing.setStatus(room.getStatus());
            existing.setCurrentGuest(room.getCurrentGuest());
            existing.setUpdatedAt(Instant.now());
            Room saved = roomRepository.save(existing);
            return ResponseEntity.ok(ApiResponse.ok("Cập nhật trạng thái phòng thành công", saved));
        }

        if (room.getCreatedAt() == null) {
            room.setCreatedAt(existingOpt.map(Room::getCreatedAt).orElseGet(Instant::now));
        }
        room.setUpdatedAt(Instant.now());
        Room saved = roomRepository.save(room);
        return ResponseEntity.ok(ApiResponse.ok("Lưu thông tin phòng thành công", saved));
    }

    @PreAuthorize("hasAnyAuthority('ROLE_SYSADMIN', 'ROLE_ADMIN', 'manage_rooms')")
    @DeleteMapping("/rooms/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteRoom(@PathVariable String id) {
        roomRepository.deleteById(id);
        return ResponseEntity.ok(ApiResponse.ok("Đã xóa phòng thành công", null));
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/bookings")
    public ResponseEntity<ApiResponse<List<Booking>>> getBookings(@AuthenticationPrincipal UserPrincipal currentUser) {
        if (isStaffBookingHandler(currentUser)) {
            return ResponseEntity.ok(ApiResponse.ok(bookingRepository.findAll()));
        }

        String userCccd = userRepository.findById(currentUser.getId())
                .map(User::getCccd)
                .orElse(null);

        List<Booking> ownBookings = bookingRepository.findAll().stream()
                .filter(b -> isBookingOwnedBy(b, currentUser, userCccd))
                .toList();

        return ResponseEntity.ok(ApiResponse.ok(ownBookings));
    }

    @PreAuthorize("hasAnyAuthority('ROLE_SYSADMIN', 'ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_RECEPTIONIST', 'ROLE_CASHIER', 'ROLE_USER', 'manage_bookings', 'create_booking', 'room_checkin_checkout')")
    @PostMapping("/bookings")
    public ResponseEntity<ApiResponse<Booking>> createBooking(
            @Valid @RequestBody Booking booking,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        Room room = roomRepository.findById(booking.getRoomId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phòng với mã: " + booking.getRoomId()));

        boolean isStaff = isStaffBookingHandler(currentUser);
        if (!isStaff && !"Trống".equals(room.getStatus())) {
            throw new IllegalArgumentException("Phòng này hiện không còn trống để đặt.");
        }

        long nights;
        try {
            LocalDate checkInDate = LocalDate.parse(booking.getCheckIn());
            LocalDate checkOutDate = LocalDate.parse(booking.getCheckOut());
            if (checkOutDate.isBefore(checkInDate)) {
                throw new IllegalArgumentException("Ngày trả phòng phải sau ngày nhận phòng.");
            }
            nights = Math.max(1L, ChronoUnit.DAYS.between(checkInDate, checkOutDate));
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException("Định dạng ngày nhận/trả phòng không hợp lệ (YYYY-MM-DD).");
        }

        double addonTotal = 0.0;
        if (booking.getAddons() != null) {
            for (String addon : booking.getAddons()) {
                if ("Buffet Sáng Michelin".equals(addon)) {
                    addonTotal += 200_000.0 * nights;
                } else {
                    addonTotal += ADDON_FIXED_PRICES.getOrDefault(addon, 0.0);
                }
            }
        }

        double minibar = (isStaff && booking.getMinibar() != null) ? Math.max(0.0, booking.getMinibar()) : 0.0;
        double serverTotalAmount = (room.getPrice() * nights) + addonTotal + minibar;
        double requestedDeposit = booking.getDepositAmount() != null ? booking.getDepositAmount() : 0.0;
        double clampedDeposit = Math.min(Math.max(0.0, requestedDeposit), serverTotalAmount);

        if (booking.getId() == null || booking.getId().isBlank() || bookingRepository.existsById(booking.getId())) {
            booking.setId(generateUniqueId("BK-", bookingRepository::existsById));
        }

        if (!isStaff) {
            booking.setUserId(currentUser.getId());
            booking.setUsername(currentUser.getUsername());
            booking.setStatus("Chờ check-in");
            booking.setPaidAmount(0.0);
            booking.setRemainingAmount(serverTotalAmount);
        } else {
            double requestedPaid = booking.getPaidAmount() != null ? booking.getPaidAmount() : 0.0;
            double clampedPaid = Math.min(Math.max(0.0, requestedPaid), serverTotalAmount);
            booking.setPaidAmount(clampedPaid);
            booking.setRemainingAmount(Math.max(0.0, serverTotalAmount - clampedPaid));
            if (booking.getStatus() == null || booking.getStatus().isBlank()) {
                booking.setStatus("Chờ check-in");
            }
        }

        booking.setRoomName(room.getName());
        booking.setNights((int) nights);
        booking.setMinibar(minibar);
        booking.setTotalAmount(serverTotalAmount);
        booking.setDepositAmount(clampedDeposit);
        if (booking.getCreatedAtInstant() == null) {
            booking.setCreatedAtInstant(Instant.now());
        }

        Booking saved = bookingRepository.save(booking);

        if ("Đã check-in".equals(saved.getStatus())) {
            room.setStatus("Đang thuê");
            room.setCurrentGuest(saved.getCustomerName());
            room.setUpdatedAt(Instant.now());
            roomRepository.save(room);
        } else if ("Chờ check-in".equals(saved.getStatus())) {
            room.setStatus("Đã đặt");
            room.setCurrentGuest(saved.getCustomerName());
            room.setUpdatedAt(Instant.now());
            roomRepository.save(room);
        }

        return ResponseEntity.ok(ApiResponse.ok("Đặt phòng thành công", saved));
    }

    @PreAuthorize("isAuthenticated()")
    @PutMapping("/bookings/{id}/status")
    public ResponseEntity<ApiResponse<Booking>> updateBookingStatus(
            @PathVariable String id,
            @RequestParam String status,
            @RequestParam(required = false) Double paidAmount,
            @RequestParam(required = false) Double remainingAmount,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        return bookingRepository.findById(id).map(booking -> {
            boolean isStaff = isStaffBookingHandler(currentUser);

            if (!isStaff) {
                String userCccd = userRepository.findById(currentUser.getId())
                        .map(User::getCccd)
                        .orElse(null);
                if (!isBookingOwnedBy(booking, currentUser, userCccd)) {
                    throw new AccessDeniedException("Bạn không có quyền thao tác trên đơn đặt phòng của khách hàng khác.");
                }
                if (!"Đã hủy".equals(status)) {
                    throw new AccessDeniedException("Khách hàng chỉ có thể tự hủy đơn đặt phòng của mình.");
                }
            } else {
                if ("Đã hủy".equals(status) && !hasAnyAuthority(currentUser, "ROLE_SYSADMIN", "ROLE_ADMIN", "ROLE_MANAGER", "cancel_booking", "manage_bookings")) {
                    throw new AccessDeniedException("Bạn không có quyền hủy đơn đặt phòng.");
                }
                if (("Đã check-in".equals(status) || "Đã hoàn thành".equals(status))
                        && !hasAnyAuthority(currentUser, "ROLE_SYSADMIN", "ROLE_ADMIN", "ROLE_MANAGER", "room_checkin_checkout", "manage_bookings", "manage_finances")) {
                    throw new AccessDeniedException("Bạn không có quyền check-in / trả phòng.");
                }
            }

            booking.setStatus(status);
            double total = booking.getTotalAmount() != null ? booking.getTotalAmount() : 0.0;
            if (isStaff) {
                if ("Đã hoàn thành".equals(status)) {
                    booking.setPaidAmount(total);
                    booking.setRemainingAmount(0.0);
                } else {
                    if (paidAmount != null) {
                        double clampedPaid = Math.min(Math.max(0.0, paidAmount), total);
                        booking.setPaidAmount(clampedPaid);
                        booking.setRemainingAmount(Math.max(0.0, total - clampedPaid));
                    } else if (remainingAmount != null) {
                        booking.setRemainingAmount(Math.max(0.0, Math.min(remainingAmount, total)));
                    }
                }
            }
            bookingRepository.save(booking);

            if (booking.getRoomId() != null) {
                roomRepository.findById(booking.getRoomId()).ifPresent(room -> {
                    if ("Đã check-in".equals(status)) {
                        room.setStatus("Đang thuê");
                        room.setCurrentGuest(booking.getCustomerName());
                    } else if ("Đã hoàn thành".equals(status)) {
                        room.setStatus("Đang dọn");
                        room.setCurrentGuest(null);
                    } else if ("Đã hủy".equals(status) && "Đã đặt".equals(room.getStatus())) {
                        room.setStatus("Trống");
                        room.setCurrentGuest(null);
                    }
                    room.setUpdatedAt(Instant.now());
                    roomRepository.save(room);
                });
            }

            return ResponseEntity.ok(ApiResponse.ok("Cập nhật trạng thái thành công", booking));
        }).orElse(ResponseEntity.notFound().build());
    }

    @PreAuthorize("hasAnyAuthority('ROLE_SYSADMIN', 'ROLE_ADMIN', 'cancel_booking', 'manage_bookings')")
    @DeleteMapping("/bookings/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteBooking(@PathVariable String id) {
        bookingRepository.deleteById(id);
        return ResponseEntity.ok(ApiResponse.ok("Đã xóa đơn đặt phòng", null));
    }

    @PreAuthorize("hasAnyAuthority('ROLE_SYSADMIN', 'ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_CASHIER', 'manage_finances', 'view_revenue')")
    @GetMapping("/transactions")
    public ResponseEntity<ApiResponse<List<Transaction>>> getTransactions() {
        return ResponseEntity.ok(ApiResponse.ok(transactionRepository.findAll()));
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/transactions")
    public ResponseEntity<ApiResponse<Transaction>> createTransaction(
            @Valid @RequestBody Transaction tx,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        boolean hasFinanceAuthority = hasAnyAuthority(currentUser,
                "ROLE_SYSADMIN", "ROLE_ADMIN", "ROLE_MANAGER", "ROLE_RECEPTIONIST", "ROLE_CASHIER",
                "manage_finances", "room_checkin_checkout");

        if (!hasFinanceAuthority) {
            if (tx.getBookingId() == null || tx.getBookingId().isBlank()) {
                throw new AccessDeniedException("Chỉ nhân viên thu ngân mới được tạo giao dịch thủ công.");
            }
            Booking linkedBooking = bookingRepository.findById(tx.getBookingId())
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn đặt phòng liên kết."));
            String userCccd = userRepository.findById(currentUser.getId())
                    .map(User::getCccd)
                    .orElse(null);
            if (!isBookingOwnedBy(linkedBooking, currentUser, userCccd)) {
                throw new AccessDeniedException("Bạn không có quyền ghi nhận giao dịch cho đơn của khách hàng khác.");
            }
            // Self-reported online VietQR transfers must be verified by staff before counting as completed
            tx.setMethod("VietQR");
            tx.setStatus("Chờ xác nhận");
            double maxAllowed = linkedBooking.getTotalAmount() != null ? linkedBooking.getTotalAmount() : 0.0;
            tx.setAmount(Math.min(Math.max(0.0, tx.getAmount()), maxAllowed));
            tx.setStaff("Cổng Online VietQR");
        } else if (tx.getStatus() == null || tx.getStatus().isBlank()) {
            tx.setStatus("Đã hoàn tất");
        }

        if (tx.getId() == null || tx.getId().isBlank() || transactionRepository.existsById(tx.getId())) {
            tx.setId(generateUniqueId("TX-", transactionRepository::existsById));
        }
        if (tx.getCreatedAtInstant() == null) {
            tx.setCreatedAtInstant(Instant.now());
        }
        Transaction saved = transactionRepository.save(tx);
        return ResponseEntity.ok(ApiResponse.ok("Tạo giao dịch thu chi thành công", saved));
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/roles")
    public ResponseEntity<ApiResponse<List<Role>>> getRoles() {
        return ResponseEntity.ok(ApiResponse.ok(roleRepository.findAll()));
    }

    @PreAuthorize("hasAnyAuthority('ROLE_SYSADMIN', 'ROLE_ADMIN', 'config_permissions', 'grant_permissions')")
    @PutMapping("/roles/{id}")
    public ResponseEntity<ApiResponse<Role>> updateRole(
            @PathVariable String id,
            @RequestBody Role role,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        if ("ROLE_SYSADMIN".equalsIgnoreCase(id)) {
            boolean isSysAdmin = hasAnyAuthority(currentUser, "ROLE_SYSADMIN");
            var existingSysAdminRole = roleRepository.findById("ROLE_SYSADMIN");
            if (!isSysAdmin) {
                return existingSysAdminRole
                        .map(r -> ResponseEntity.ok(ApiResponse.ok("Giữ nguyên quyền SysAdmin mặc định", r)))
                        .orElseThrow(() -> new AccessDeniedException("Chỉ SysAdmin mới được sửa vai trò ROLE_SYSADMIN."));
            }
            if (role.getPermissions() != null) {
                role.getPermissions().put("config_permissions", true);
                role.getPermissions().put("manage_staff", true);
                role.getPermissions().put("manage_roles", true);
                role.getPermissions().put("grant_permissions", true);
            }
        }
        role.setId(id);
        Role saved = roleRepository.save(role);
        return ResponseEntity.ok(ApiResponse.ok("Cập nhật vai trò phân quyền thành công", saved));
    }

    @PreAuthorize("hasAnyAuthority('ROLE_SYSADMIN', 'ROLE_ADMIN', 'ROLE_MANAGER', 'view_staff', 'manage_staff', 'config_permissions')")
    @GetMapping("/staff")
    public ResponseEntity<ApiResponse<List<User>>> getStaff() {
        List<User> staffOnly = userRepository.findAll().stream()
                .filter(u -> {
                    boolean isOnlyUser = u.getRoles() != null && !u.getRoles().isEmpty()
                            && u.getRoles().stream().allMatch("ROLE_USER"::equals);
                    return !isOnlyUser && !"ROLE_USER".equals(u.getRoleId());
                })
                .toList();
        return ResponseEntity.ok(ApiResponse.ok(staffOnly));
    }

    @PreAuthorize("hasAnyAuthority('ROLE_SYSADMIN', 'ROLE_ADMIN', 'manage_staff')")
    @PostMapping("/staff")
    public ResponseEntity<ApiResponse<User>> saveStaff(
            @Valid @RequestBody StaffRequest staffReq,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        boolean callerIsSysAdmin = hasAnyAuthority(currentUser, "ROLE_SYSADMIN");
        String requestedRoleId = staffReq.getRoleId().trim();
        String mappedRole = requestedRoleId.startsWith("ROLE_")
                ? requestedRoleId.toUpperCase()
                : "ROLE_" + requestedRoleId.toUpperCase();

        if ("ROLE_SYSADMIN".equals(mappedRole) && !callerIsSysAdmin) {
            throw new AccessDeniedException("Chỉ tài khoản SysAdmin mới được phép gán vai trò ROLE_SYSADMIN.");
        }

        if (staffReq.getId() != null && !staffReq.getId().isBlank()) {
            var existingOpt = userRepository.findById(staffReq.getId());
            if (existingOpt.isPresent()) {
                User existing = existingOpt.get();
                if (isSysAdminAccount(existing) && !callerIsSysAdmin) {
                    throw new AccessDeniedException("Bạn không có quyền chỉnh sửa tài khoản SysAdmin.");
                }
                if (isSysAdminAccount(existing) && !"ROLE_SYSADMIN".equals(mappedRole)) {
                    throw new IllegalArgumentException("Không thể hạ cấp vai trò của tài khoản SysAdmin.");
                }

                String resolvedName = staffReq.getName().trim();
                existing.setName(resolvedName);
                existing.setFullName(staffReq.getFullName() != null && !staffReq.getFullName().isBlank()
                        ? staffReq.getFullName().trim() : resolvedName);

                if (staffReq.getUsername() != null && !staffReq.getUsername().isBlank()) {
                    existing.setUsername(staffReq.getUsername().trim());
                }
                if (staffReq.getEmail() != null && !staffReq.getEmail().isBlank()) {
                    existing.setEmail(staffReq.getEmail().trim());
                }
                if (staffReq.getPhone() != null) {
                    existing.setPhone(staffReq.getPhone().trim());
                }

                existing.setRoleId(requestedRoleId);
                existing.setRoles(isSysAdminAccount(existing)
                        ? Set.of("ROLE_SYSADMIN", "ROLE_ADMIN")
                        : Set.of(mappedRole));

                String resolvedRoleName = roleRepository.findById(mappedRole)
                        .map(Role::getName)
                        .orElse(staffReq.getRoleName() != null ? staffReq.getRoleName() : requestedRoleId);
                existing.setRoleName(resolvedRoleName);

                if (staffReq.getStatus() != null && !staffReq.getStatus().isBlank()) {
                    if (isSysAdminAccount(existing) && "locked".equalsIgnoreCase(staffReq.getStatus())) {
                        throw new IllegalArgumentException("Không thể khóa tài khoản SysAdmin.");
                    }
                    existing.setStatus(staffReq.getStatus());
                    existing.setActive(!"locked".equalsIgnoreCase(staffReq.getStatus()));
                } else if (staffReq.getActive() != null) {
                    if (isSysAdminAccount(existing) && !staffReq.getActive()) {
                        throw new IllegalArgumentException("Không thể khóa tài khoản SysAdmin.");
                    }
                    existing.setActive(staffReq.getActive());
                    existing.setStatus(staffReq.getActive() ? "active" : "locked");
                }

                if (staffReq.getPassword() != null && !staffReq.getPassword().isBlank()) {
                    existing.setPassword(passwordEncoder.encode(staffReq.getPassword()));
                }
                existing.setUpdatedAt(Instant.now());
                User saved = userRepository.save(existing);
                return ResponseEntity.ok(ApiResponse.ok("Cập nhật thông tin nhân sự thành công", saved));
            }
        }

        if (staffReq.getPassword() == null || staffReq.getPassword().isBlank()) {
            throw new IllegalArgumentException("Vui lòng nhập mật khẩu (tối thiểu 6 ký tự) cho nhân viên mới.");
        }

        String staffId = (staffReq.getId() != null && !staffReq.getId().isBlank() && !userRepository.existsById(staffReq.getId()))
                ? staffReq.getId().trim()
                : generateUniqueId("NV-", userRepository::existsById);

        String username = (staffReq.getUsername() != null && !staffReq.getUsername().isBlank())
                ? staffReq.getUsername().trim()
                : ((staffReq.getEmail() != null && !staffReq.getEmail().isBlank())
                        ? staffReq.getEmail().trim()
                        : staffId.toLowerCase());

        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("Tên đăng nhập nhân viên đã tồn tại trong hệ thống.");
        }
        if (staffReq.getEmail() != null && !staffReq.getEmail().isBlank() && userRepository.existsByEmail(staffReq.getEmail().trim())) {
            throw new IllegalArgumentException("Email nhân viên đã tồn tại trong hệ thống.");
        }

        String resolvedName = staffReq.getName().trim();
        String resolvedRoleName = roleRepository.findById(mappedRole)
                .map(Role::getName)
                .orElse(staffReq.getRoleName() != null ? staffReq.getRoleName() : requestedRoleId);
        boolean isActive = staffReq.getStatus() != null
                ? !"locked".equalsIgnoreCase(staffReq.getStatus())
                : (staffReq.getActive() == null || staffReq.getActive());

        User newStaff = User.builder()
                .id(staffId)
                .username(username)
                .password(passwordEncoder.encode(staffReq.getPassword()))
                .name(resolvedName)
                .fullName(staffReq.getFullName() != null && !staffReq.getFullName().isBlank() ? staffReq.getFullName().trim() : resolvedName)
                .email(staffReq.getEmail() != null ? staffReq.getEmail().trim() : null)
                .phone(staffReq.getPhone() != null ? staffReq.getPhone().trim() : null)
                .roleId(requestedRoleId)
                .roleName(resolvedRoleName)
                .roles(Set.of(mappedRole))
                .status(isActive ? "active" : "locked")
                .active(isActive)
                .lastLogin(staffReq.getLastLogin() != null ? staffReq.getLastLogin() : "Chưa đăng nhập")
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        User saved = userRepository.save(newStaff);
        return ResponseEntity.ok(ApiResponse.ok("Lưu thông tin nhân sự thành công", saved));
    }

    @PreAuthorize("hasAnyAuthority('ROLE_SYSADMIN', 'ROLE_ADMIN', 'manage_staff')")
    @PatchMapping("/staff/{id}/toggle-status")
    public ResponseEntity<ApiResponse<User>> toggleStaffStatus(@PathVariable String id) {
        return userRepository.findById(id).map(user -> {
            if (isSysAdminAccount(user) && user.isActive()) {
                throw new IllegalArgumentException("Không thể tạm khóa tài khoản SysAdmin.");
            }
            boolean newActive = !user.isActive();
            user.setActive(newActive);
            user.setStatus(newActive ? "active" : "locked");
            user.setUpdatedAt(Instant.now());
            userRepository.save(user);
            return ResponseEntity.ok(ApiResponse.ok("Cập nhật trạng thái nhân sự thành công", user));
        }).orElse(ResponseEntity.notFound().build());
    }

    @PreAuthorize("hasAnyAuthority('ROLE_SYSADMIN', 'ROLE_ADMIN', 'manage_staff')")
    @DeleteMapping("/staff/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteStaff(@PathVariable String id) {
        User target = userRepository.findById(id).orElse(null);
        if (target == null) {
            return ResponseEntity.notFound().build();
        }
        if (isSysAdminAccount(target)) {
            throw new IllegalArgumentException("Không thể xóa tài khoản quản trị cấp cao SysAdmin.");
        }
        userRepository.deleteById(id);
        return ResponseEntity.ok(ApiResponse.ok("Đã xóa nhân viên thành công", null));
    }
}
