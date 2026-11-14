package com.hotel.backend.services;

import com.hotel.backend.config.UserPrincipal;
import com.hotel.backend.models.User;
import com.hotel.backend.repositories.RoleRepository;
import com.hotel.backend.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    @Override
    public UserDetails loadUserByUsername(String usernameOrEmail) throws UsernameNotFoundException {
        User user = userRepository.findByUsernameOrEmail(usernameOrEmail, usernameOrEmail)
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy người dùng với tài khoản/email: " + usernameOrEmail));

        Map<String, Boolean> effectivePermissions = resolveEffectivePermissions(user);
        return UserPrincipal.create(user, effectivePermissions);
    }

    private Map<String, Boolean> resolveEffectivePermissions(User user) {
        Map<String, Boolean> permissions = new HashMap<>();
        Set<String> candidateRoleIds = new LinkedHashSet<>();

        if (user.getRoles() != null) {
            candidateRoleIds.addAll(user.getRoles());
        }
        if (user.getRoleId() != null && !user.getRoleId().isBlank()) {
            candidateRoleIds.add(user.getRoleId());
        }

        for (String roleId : candidateRoleIds) {
            if (roleId == null || roleId.isBlank()) continue;
            roleRepository.findById(roleId).ifPresent(role -> {
                if (role.getPermissions() != null) {
                    permissions.putAll(role.getPermissions());
                }
            });
        }

        if (user.getCustomPermissions() != null) {
            permissions.putAll(user.getCustomPermissions());
        }

        return permissions;
    }
}
