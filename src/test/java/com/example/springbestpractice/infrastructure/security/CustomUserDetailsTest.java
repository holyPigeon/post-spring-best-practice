package com.example.springbestpractice.infrastructure.security;

import com.example.springbestpractice.domain.user.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Custom user details")
class CustomUserDetailsTest {

    @Test
    @DisplayName("returns ROLE_USER authority for regular user")
    void returnUserAuthority() {
        // given
        CustomUserDetails userDetails = new CustomUserDetails(User.create("user@test.com", "user", "password"));

        // when & then
        assertThat(userDetails.getAuthorities())
                .extracting(GrantedAuthority::getAuthority)
                .containsExactly("ROLE_USER");
    }

    @Test
    @DisplayName("returns ROLE_ADMIN and ROLE_USER authorities for admin user")
    void returnAdminAuthority() {
        // given
        CustomUserDetails userDetails = new CustomUserDetails(User.createAdmin("admin@test.com", "admin", "password"));

        // when & then
        assertThat(userDetails.getAuthorities())
                .extracting(GrantedAuthority::getAuthority)
                .containsExactly("ROLE_ADMIN", "ROLE_USER");
    }
}
