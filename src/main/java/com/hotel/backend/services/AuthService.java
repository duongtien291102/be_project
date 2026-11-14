package com.hotel.backend.services;

import com.hotel.backend.config.JwtTokenProvider;
import com.hotel.backend.dto.AuthResponse;
import com.hotel.backend.dto.LoginRequest;
import com.hotel.backend.dto.ProfileUpdateRequest;
import com.hotel.backend.dto.RegisterRequest;
import com.hotel.backend.models.User;
import com.hotel.backend.repositories.RoleRepository;
import com.hotel.backend.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final DateTimeFormatter LOGIN_TIME_FMT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm").withZone(ZoneId.of("Asia/Ho_Chi_Minh"));

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;

    public AuthResponse login(LoginRequest request) {
        String input = request.getUsernameOrEmail() != null ? request.getUsernameOrEmail().trim() : "";

        // Tìm kiếm theo username, email, phone hoặc cccd
        User user = userRepository.findByUsernameOrEmail(input, input)
                .or(() -> userRepository.findByPhone(input))
                .or(() -> userRepository.findByCccd(input))
                .orElseThrow(() -> new BadCredentialsException("Tài khoản hoặc mật khẩu không chính xác"));

        if (!user.isActive() || "locked".equalsIgnoreCase(user.getStatus())) {
            throw new BadCredentialsException("Tài khoản của bạn đã bị khóa hoặc vô hiệu hóa.");
        }

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(user.getUsername(), request.getPassword())
        );

        user.setLastLogin(LOGIN_TIME_FMT.format(Instant.now()));
        userRepository.save(user);

        String token = tokenProvider.generateToken(authentication);
        return buildAuthResponse(user, token);
    }

    public AuthResponse register(RegisterRequest request) {
        String username = request.getUsername().trim();
        String email = request.getEmail().trim();
        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("Tên đăng nhập đã tồn tại trong hệ thống.");
        }
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Địa chỉ email đã được đăng ký.");
        }

        String fullName = request.getFullName().trim();
        String nowStr = LOGIN_TIME_FMT.format(Instant.now());

        // Quy định nghiệp vụ: Tài khoản tạo ra bình thường mặc định có quyền ROLE_USER
        User newUser = User.builder()
                .username(username)
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(fullName)
                .name(fullName)
                .email(email)
                .phone(request.getPhone())
                .dob(request.getDob())
                .gender(request.getGender())
                .nationality(request.getNationality())
                .address(request.getAddress())
                .cccd(request.getCccd())
                .cccdDate(request.getCccdDate())
                .cccdPlace(request.getCccdPlace())
                .cccdFront(request.getCccdFront())
                .cccdBack(request.getCccdBack())
                .avatar(request.getAvatar() != null ? request.getAvatar() : fullName.substring(0, 1).toUpperCase())
                .roleId("ROLE_USER")
                .roleName("Khách Hàng (User)")
                .status("active")
                .lastLogin(nowStr)
                .roles(Set.of("ROLE_USER"))
                .active(true)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        User savedUser = userRepository.save(newUser);

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, request.getPassword())
        );

        String token = tokenProvider.generateToken(authentication);
        return buildAuthResponse(savedUser, token);
    }

    public AuthResponse getCurrentUser(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy thông tin người dùng."));

        return buildAuthResponse(user, null);
    }

    public AuthResponse updateProfile(String username, ProfileUpdateRequest profileUpdate) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy thông tin người dùng."));

        if (profileUpdate.getFullName() != null && !profileUpdate.getFullName().isBlank()) {
            user.setFullName(profileUpdate.getFullName().trim());
            user.setName(profileUpdate.getFullName().trim());
        } else if (profileUpdate.getName() != null && !profileUpdate.getName().isBlank()) {
            user.setName(profileUpdate.getName().trim());
            user.setFullName(profileUpdate.getName().trim());
        }
        if (profileUpdate.getPhone() != null) user.setPhone(profileUpdate.getPhone());
        if (profileUpdate.getDob() != null) user.setDob(profileUpdate.getDob());
        if (profileUpdate.getGender() != null) user.setGender(profileUpdate.getGender());
        if (profileUpdate.getNationality() != null) user.setNationality(profileUpdate.getNationality());
        if (profileUpdate.getAddress() != null) user.setAddress(profileUpdate.getAddress());
        if (profileUpdate.getCccd() != null) user.setCccd(profileUpdate.getCccd());
        if (profileUpdate.getCccdDate() != null) user.setCccdDate(profileUpdate.getCccdDate());
        if (profileUpdate.getCccdPlace() != null) user.setCccdPlace(profileUpdate.getCccdPlace());
        if (profileUpdate.getCccdFront() != null) user.setCccdFront(profileUpdate.getCccdFront());
        if (profileUpdate.getCccdBack() != null) user.setCccdBack(profileUpdate.getCccdBack());
        if (profileUpdate.getAvatar() != null) user.setAvatar(profileUpdate.getAvatar());

        user.setUpdatedAt(Instant.now());
        User saved = userRepository.save(user);
        return buildAuthResponse(saved, null);
    }

    private AuthResponse buildAuthResponse(User user, String token) {
        Map<String, Boolean> effectivePermissions = calculateEffectivePermissions(user);
        String displayName = user.getName() != null ? user.getName() : user.getFullName();

        return AuthResponse.builder()
                .token(token)
                .type("Bearer")
                .id(user.getId())
                .username(user.getUsername())
                .fullName(user.getFullName())
                .name(displayName)
                .email(user.getEmail())
                .phone(user.getPhone())
                .roleId(user.getRoleId())
                .roleName(user.getRoleName())
                .status(user.getStatus())
                .avatar(user.getAvatar())
                .dob(user.getDob())
                .gender(user.getGender())
                .nationality(user.getNationality())
                .address(user.getAddress())
                .cccd(user.getCccd())
                .cccdDate(user.getCccdDate())
                .cccdPlace(user.getCccdPlace())
                .cccdFront(user.getCccdFront())
                .cccdBack(user.getCccdBack())
                .roles(user.getRoles())
                .effectivePermissions(effectivePermissions)
                .build();
    }

    private Map<String, Boolean> calculateEffectivePermissions(User user) {
        Map<String, Boolean> permissions = new HashMap<>();

        // Tổng hợp quyền từ tất cả các role của người dùng
        if (user.getRoles() != null) {
            for (String roleId : user.getRoles()) {
                roleRepository.findById(roleId).ifPresent(role -> {
                    if (role.getPermissions() != null) {
                        permissions.putAll(role.getPermissions());
                    }
                });
            }
        }

        // Nếu có custom permissions riêng theo user thì ghi đè
        if (user.getCustomPermissions() != null) {
            permissions.putAll(user.getCustomPermissions());
        }

        return permissions;
    }
}
