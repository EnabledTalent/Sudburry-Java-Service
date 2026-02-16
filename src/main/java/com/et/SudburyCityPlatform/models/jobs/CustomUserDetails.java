package com.et.SudburyCityPlatform.models.jobs;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public class CustomUserDetails implements UserDetails {

    private Long userId;
    private Long employerId;
    private String username;
    private String role;

    public CustomUserDetails(Long userId, Long employerId, String username, String role) {
        this.userId = userId;
        this.employerId = employerId;
        this.username = username;
        this.role = role;
    }

    public Long getUserId() {
        return userId;
    }

    public Long getEmployerId() {
        return employerId;
    }

    public String getRole() {
        return role;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role));
    }

    @Override public String getPassword() { return null; }
    @Override public String getUsername() { return username; }
    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() { return true; }
}
