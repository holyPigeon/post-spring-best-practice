package com.example.springbestpractice.infrastructure.security;

import com.example.springbestpractice.common.model.LoginUser;
import com.example.springbestpractice.common.model.Role;
import com.example.springbestpractice.domain.user.User;
import com.example.springbestpractice.domain.user.UserRole;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

@Getter
public class CustomUserDetails implements UserDetails {

    private final User user;

    public CustomUserDetails(User user) {
        this.user = user;
    }

    public LoginUser toLoginUser() {
        return new LoginUser(user.getId(), user.getEmail(), user.getNickname(), toRole(user.getRole()));
    }

    private static Role toRole(UserRole role) {
        return switch (role) {
            case USER -> Role.USER;
            case ADMIN -> Role.ADMIN;
        };
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return user.getRole().getAuthorities().stream()
                .map(SimpleGrantedAuthority::new)
                .toList();
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getEmail();
    }
}
