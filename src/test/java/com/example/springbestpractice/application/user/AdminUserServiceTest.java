package com.example.springbestpractice.application.user;

import com.example.springbestpractice.application.user.dto.AdminUserResponse;
import com.example.springbestpractice.domain.user.User;
import com.example.springbestpractice.domain.user.UserNotFoundException;
import com.example.springbestpractice.domain.user.UserRole;
import com.example.springbestpractice.infrastructure.user.UserRepository;
import com.example.springbestpractice.support.fixture.UserFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.groups.Tuple.tuple;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@DisplayName("Admin user service")
@ExtendWith(MockitoExtension.class)
class AdminUserServiceTest {

    @Mock
    UserRepository userRepository;

    @InjectMocks
    AdminUserService adminUserService;

    private User user;
    private User admin;

    @BeforeEach
    void setUp() {
        user = UserFixture.userWithId(1L, "user@test.com", "user", "password");
        admin = UserFixture.adminWithId(2L);
    }

    @Nested
    @DisplayName("Get users")
    class GetUsers {

        @Test
        @DisplayName("returns all users")
        void returnAllUsers() {
            // given
            given(userRepository.findAll()).willReturn(List.of(user, admin));

            // when
            List<AdminUserResponse> result = adminUserService.getUsers();

            // then
            assertThat(result).hasSize(2)
                    .extracting("email", "role")
                    .containsExactly(
                            tuple("user@test.com", UserRole.USER),
                            tuple("admin@test.com", UserRole.ADMIN)
                    );
        }
    }

    @Nested
    @DisplayName("Get user")
    class GetUser {

        @Test
        @DisplayName("returns a user")
        void returnUser() {
            // given
            given(userRepository.findById(1L)).willReturn(Optional.of(user));

            // when
            AdminUserResponse result = adminUserService.getUser(1L);

            // then
            assertThat(result)
                    .extracting("id", "email", "role")
                    .containsExactly(1L, "user@test.com", UserRole.USER);
        }

        @Test
        @DisplayName("throws UserNotFoundException when user does not exist")
        void throwExceptionWhenNotFound() {
            // given
            given(userRepository.findById(999L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> adminUserService.getUser(999L))
                    .isInstanceOf(UserNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("Delete user")
    class DeleteUser {

        @Test
        @DisplayName("deletes a user")
        void deleteUser() {
            // given
            given(userRepository.findById(1L)).willReturn(Optional.of(user));

            // when
            adminUserService.deleteUser(1L);

            // then
            verify(userRepository).delete(user);
        }

        @Test
        @DisplayName("throws UserNotFoundException when user does not exist")
        void throwExceptionWhenNotFound() {
            // given
            given(userRepository.findById(999L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> adminUserService.deleteUser(999L))
                    .isInstanceOf(UserNotFoundException.class);
        }
    }

}
