package com.hotel.backend.services;

import com.hotel.backend.config.JwtTokenProvider;
import com.hotel.backend.config.UserPrincipal;
import com.hotel.backend.dto.AuthResponse;
import com.hotel.backend.dto.LoginRequest;
import com.hotel.backend.dto.RegisterRequest;
import com.hotel.backend.models.Role;
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
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;

    public AuthResponse login(LoginRequest request) {
        // Tìm kiếm theo username hoặc email
        User user = userRepository.findByUsernameOrEmail(request.getUsernameOrEmail(), request.getUsernameOrEmail())
                .orElseThrow(() -> new BadCredentialsException("Tài khoản hoặc mật khẩu không chính xác"));

        if (!user.isActive()) {
            throw new BadCredentialsException("Tài khoản của bạn đã bị khóa hoặc vô hiệu hóa.");
        }

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(user.getUsername(), request.getPassword())
        );

        String token = tokenProvider.generateToken(authentication);
        Map<String, Boolean> effectivePermissions = calculateEffectivePermissions(user);

        return AuthResponse.builder()
                .token(token)
                .type("Bearer")
                .id(user.getId())
                .username(user.getUsername())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .roles(user.getRoles())
                .effectivePermissions(effectivePermissions)
                .build();
    }

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Tên đăng nhập đã tồn tại trong hệ thống.");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Địa chỉ email đã được đăng ký.");
        }

        // Quy định nghiệp vụ: Tài khoản tạo ra bình thường mặc định có quyền ROLE_USER
        User newUser = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .roles(Set.of("ROLE_USER"))
                .active(true)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        User savedUser = userRepository.save(newUser);

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        String token = tokenProvider.generateToken(authentication);
        Map<String, Boolean> effectivePermissions = calculateEffectivePermissions(savedUser);

        return AuthResponse.builder()
                .token(token)
                .type("Bearer")
                .id(savedUser.getId())
                .username(savedUser.getUsername())
                .fullName(savedUser.getFullName())
                .email(savedUser.getEmail())
                .phone(savedUser.getPhone())
                .roles(savedUser.getRoles())
                .effectivePermissions(effectivePermissions)
                .build();
    }

    public AuthResponse getCurrentUser(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy thông tin người dùng."));

        Map<String, Boolean> effectivePermissions = calculateEffectivePermissions(user);

        return AuthResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .roles(user.getRoles())
                .effectivePermissions(effectivePermissions)
                .build();
    }

    private Map<String, Boolean> calculateEffectivePermissions(User user) {
        Map<String, Boolean> permissions = new HashMap<>();

        // Tổng hợp quyền từ tất cả các role của người dùng
        for (String roleId : user.getRoles()) {
            roleRepository.findById(roleId).ifPresent(role -> {
                if (role.getPermissions() != null) {
                    permissions.putAll(role.getPermissions());
                }
            });
        }

        // Nếu có custom permissions riêng theo user thì ghi đè
        if (user.getCustomPermissions() != null) {
            permissions.putAll(user.getCustomPermissions());
        }

        return permissions;
    }
}
