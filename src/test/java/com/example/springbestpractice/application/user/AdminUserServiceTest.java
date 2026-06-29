package com.example.springbestpractice.application.user;

import com.example.springbestpractice.application.user.command.AdminUserDeleteCommand;
import com.example.springbestpractice.application.user.dto.AdminUserPageRequest;
import com.example.springbestpractice.application.user.dto.AdminUserResponse;
import com.example.springbestpractice.common.dto.PageResponse;
import com.example.springbestpractice.common.exception.ConflictException;
import com.example.springbestpractice.common.model.LoginUser;
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
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.groups.Tuple.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
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
        @DisplayName("returns a page of users")
        void returnUserPage() {
            // given
            given(userRepository.findAll(any(Pageable.class)))
                    .willReturn(new PageImpl<>(List.of(user, admin)));

            // when
            PageResponse<AdminUserResponse> result = adminUserService.getUsers(new AdminUserPageRequest(0, 20));

            // then
            assertThat(result.content()).hasSize(2)
                    .extracting("email", "role")
                    .containsExactly(
                            tuple("user@test.com", UserRole.USER),
                            tuple("admin@test.com", UserRole.ADMIN)
                    );
            assertThat(result.totalElements()).isEqualTo(2);
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
        @DisplayName("deletes another user")
        void deleteUser() {
            // given
            given(userRepository.findById(1L)).willReturn(Optional.of(user));

            // when
            adminUserService.deleteUser(AdminUserDeleteCommand.from(1L, requester(2L)));

            // then
            verify(userRepository).delete(user);
        }

        @Test
        @DisplayName("deletes an admin when other admins remain")
        void deleteAdminWhenOthersRemain() {
            // given
            given(userRepository.findById(2L)).willReturn(Optional.of(admin));
            given(userRepository.countByRole(UserRole.ADMIN)).willReturn(2L);

            // when
            adminUserService.deleteUser(AdminUserDeleteCommand.from(2L, requester(3L)));

            // then
            verify(userRepository).delete(admin);
        }

        @Test
        @DisplayName("throws ConflictException when deleting self")
        void throwWhenDeletingSelf() {
            // given
            given(userRepository.findById(2L)).willReturn(Optional.of(admin));

            // when & then
            assertThatThrownBy(() -> adminUserService.deleteUser(AdminUserDeleteCommand.from(2L, requester(2L))))
                    .isInstanceOf(ConflictException.class);
            verify(userRepository, never()).delete(any());
        }

        @Test
        @DisplayName("throws ConflictException when deleting the last admin")
        void throwWhenDeletingLastAdmin() {
            // given
            given(userRepository.findById(2L)).willReturn(Optional.of(admin));
            given(userRepository.countByRole(UserRole.ADMIN)).willReturn(1L);

            // when & then
            assertThatThrownBy(() -> adminUserService.deleteUser(AdminUserDeleteCommand.from(2L, requester(3L))))
                    .isInstanceOf(ConflictException.class);
            verify(userRepository, never()).delete(any());
        }

        @Test
        @DisplayName("throws UserNotFoundException when user does not exist")
        void throwExceptionWhenNotFound() {
            // given
            given(userRepository.findById(999L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> adminUserService.deleteUser(AdminUserDeleteCommand.from(999L, requester(2L))))
                    .isInstanceOf(UserNotFoundException.class);
        }
    }

    private LoginUser requester(Long id) {
        return new LoginUser(id, "requester@test.com", "requester", "ADMIN");
    }
}
