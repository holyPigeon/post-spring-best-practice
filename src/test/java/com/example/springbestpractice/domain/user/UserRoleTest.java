package com.example.springbestpractice.domain.user;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("User role")
class UserRoleTest {

    @Test
    @DisplayName("regular user has USER role")
    void createUserWithUserRole() {
        // when
        User user = User.create("user@test.com", "user", "password");

        // then
        assertThat(user.getRole()).isEqualTo(UserRole.USER);
    }

    @Test
    @DisplayName("admin user has ADMIN role")
    void createAdminWithAdminRole() {
        // when
        User user = User.createAdmin("admin@test.com", "admin", "password");

        // then
        assertThat(user.getRole()).isEqualTo(UserRole.ADMIN);
    }

    @Test
    @DisplayName("role returns Spring Security authority")
    void returnAuthority() {
        assertThat(UserRole.ADMIN.getAuthority()).isEqualTo("ROLE_ADMIN");
    }
}
