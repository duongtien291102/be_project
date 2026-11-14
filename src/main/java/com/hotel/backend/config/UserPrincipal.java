package com.hotel.backend.config;

import com.hotel.backend.models.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Getter
@AllArgsConstructor
public class UserPrincipal implements UserDetails {

    private final String id;
    private final String username;
    private final String password;
    private final String email;
    private final String fullName;
    private final boolean active;
    private final Collection<? extends GrantedAuthority> authorities;

    public static UserPrincipal create(User user) {
        return create(user, Collections.emptyMap());
    }

    public static UserPrincipal create(User user, Map<String, Boolean> effectivePermissions) {
        Set<GrantedAuthority> authorities = new HashSet<>();

        if (user.getRoles() != null) {
            for (String role : user.getRoles()) {
                if (role == null || role.isBlank()) continue;
                String formattedRole = role.startsWith("ROLE_") ? role.toUpperCase() : "ROLE_" + role.toUpperCase();
                authorities.add(new SimpleGrantedAuthority(formattedRole));
            }
        }

        if (user.getRoleId() != null && !user.getRoleId().isBlank()) {
            String roleId = user.getRoleId().trim();
            String formattedRoleId = roleId.startsWith("ROLE_") ? roleId.toUpperCase() : roleId;
            authorities.add(new SimpleGrantedAuthority(formattedRoleId));
        }

        if (effectivePermissions != null) {
            effectivePermissions.forEach((permKey, granted) -> {
                if (Boolean.TRUE.equals(granted) && permKey != null && !permKey.isBlank()) {
                    authorities.add(new SimpleGrantedAuthority(permKey));
                }
            });
        }

        return new UserPrincipal(
                user.getId(),
                user.getUsername(),
                user.getPassword(),
                user.getEmail(),
                user.getFullName(),
                user.isActive(),
                authorities
        );
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return active;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return active;
    }
}
