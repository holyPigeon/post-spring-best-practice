package com.example.springbestpractice.application.user;

import com.example.springbestpractice.application.user.command.UserDeleteCommand;
import com.example.springbestpractice.application.user.command.UserPasswordUpdateCommand;
import com.example.springbestpractice.application.user.command.UserUpdateCommand;
import com.example.springbestpractice.application.user.dto.UserCreateRequest;
import com.example.springbestpractice.application.user.dto.UserPasswordUpdateRequest;
import com.example.springbestpractice.application.user.dto.UserResponse;
import com.example.springbestpractice.application.user.dto.UserUpdateRequest;
import com.example.springbestpractice.common.model.LoginUser;
import com.example.springbestpractice.common.model.Role;
import com.example.springbestpractice.domain.user.DuplicateEmailException;
import com.example.springbestpractice.domain.user.User;
import com.example.springbestpractice.domain.user.UserNotFoundException;
import com.example.springbestpractice.infrastructure.user.UserRepository;
import com.example.springbestpractice.support.fixture.UserFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@DisplayName("User service")
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    UserRepository userRepository;

    @Mock
    PasswordEncoder passwordEncoder;

    @InjectMocks
    UserService userService;

    private User user;
    private LoginUser loginUser;

    @BeforeEach
    void setUp() {
        user = UserFixture.userWithId(1L);
        loginUser = loginUser(1L);
    }

    @Nested
    @DisplayName("Create user")
    class Create {

        @Test
        @DisplayName("throws DuplicateEmailException for duplicate email")
        void throwExceptionWhenDuplicateEmail() {
            // given
            UserCreateRequest request = new UserCreateRequest("test@test.com", "tester", "password");
            given(userRepository.existsByEmail("test@test.com")).willReturn(true);

            // when & then
            assertThatThrownBy(() -> userService.createUser(request))
                    .isInstanceOf(DuplicateEmailException.class);
        }

        @Test
        @DisplayName("saves and returns a user")
        void createUser() {
            // given
            UserCreateRequest request = new UserCreateRequest("test@test.com", "tester", "password");
            given(userRepository.existsByEmail("test@test.com")).willReturn(false);
            given(passwordEncoder.encode("password")).willReturn("encoded-password");
            given(userRepository.save(any(User.class))).willReturn(user);

            // when
            UserResponse result = userService.createUser(request);

            // then
            assertThat(result)
                    .extracting("id", "email", "nickname")
                    .containsExactly(1L, "test@test.com", "tester");

            ArgumentCaptor<User> savedUser = ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(savedUser.capture());
            assertThat(savedUser.getValue())
                    .extracting("email", "nickname", "password")
                    .containsExactly("test@test.com", "tester", "encoded-password");
        }
    }

    @Nested
    @DisplayName("Get my profile")
    class GetMyProfile {

        @Test
        @DisplayName("throws UserNotFoundException when current user does not exist")
        void throwExceptionWhenNotFound() {
            // given
            given(userRepository.findById(999L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> userService.getMyProfile(loginUser(999L)))
                    .isInstanceOf(UserNotFoundException.class);
        }

        @Test
        @DisplayName("returns the current user")
        void returnUserResponse() {
            // given
            given(userRepository.findById(1L)).willReturn(Optional.of(user));

            // when
            UserResponse result = userService.getMyProfile(loginUser);

            // then
            assertThat(result)
                    .extracting("id", "email", "nickname")
                    .containsExactly(1L, "test@test.com", "tester");
        }

        @Test
        @DisplayName("throws AccessDeniedException when login user is null")
        void throwExceptionWhenLoginUserIsNull() {
            assertThatThrownBy(() -> userService.getMyProfile(null))
                    .isInstanceOf(AccessDeniedException.class);
        }
    }

    @Nested
    @DisplayName("Update my profile")
    class UpdateMyProfile {

        @Test
        @DisplayName("throws UserNotFoundException when current user does not exist")
        void throwExceptionWhenNotFound() {
            // given
            UserUpdateRequest request = new UserUpdateRequest("updated");
            given(userRepository.findById(999L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> userService.updateMyProfile(
                    UserUpdateCommand.from(request, loginUser(999L))
            )).isInstanceOf(UserNotFoundException.class);
        }

        @Test
        @DisplayName("updates the current user's nickname")
        void updateNickname() {
            // given
            UserUpdateRequest request = new UserUpdateRequest("updated");
            given(userRepository.findById(1L)).willReturn(Optional.of(user));

            // when
            UserResponse result = userService.updateMyProfile(UserUpdateCommand.from(request, loginUser));

            // then
            assertThat(result.nickname()).isEqualTo("updated");
        }

        @Test
        @DisplayName("throws AccessDeniedException when login user is null")
        void throwExceptionWhenLoginUserIsNull() {
            // given
            UserUpdateRequest request = new UserUpdateRequest("updated");

            // when & then
            assertThatThrownBy(() -> userService.updateMyProfile(UserUpdateCommand.from(request, null)))
                    .isInstanceOf(AccessDeniedException.class);
        }
    }

    @Nested
    @DisplayName("Update my password")
    class UpdateMyPassword {

        @Test
        @DisplayName("throws UserNotFoundException when current user does not exist")
        void throwExceptionWhenNotFound() {
            // given
            UserPasswordUpdateRequest request = new UserPasswordUpdateRequest("newpassword");
            given(userRepository.findById(999L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> userService.updatePassword(
                    UserPasswordUpdateCommand.from(request, loginUser(999L))
            )).isInstanceOf(UserNotFoundException.class);
        }

        @Test
        @DisplayName("encodes and updates the current user's password")
        void updatePassword() {
            // given
            UserPasswordUpdateRequest request = new UserPasswordUpdateRequest("newpassword");
            given(userRepository.findById(1L)).willReturn(Optional.of(user));
            given(passwordEncoder.encode("newpassword")).willReturn("encoded-newpassword");

            // when
            userService.updatePassword(UserPasswordUpdateCommand.from(request, loginUser));

            // then
            assertThat(user.getPassword()).isEqualTo("encoded-newpassword");
            verify(passwordEncoder).encode("newpassword");
        }

        @Test
        @DisplayName("throws AccessDeniedException when login user is null")
        void throwExceptionWhenLoginUserIsNull() {
            // given
            UserPasswordUpdateRequest request = new UserPasswordUpdateRequest("newpassword");

            // when & then
            assertThatThrownBy(() -> userService.updatePassword(UserPasswordUpdateCommand.from(request, null)))
                    .isInstanceOf(AccessDeniedException.class);
        }
    }

    @Nested
    @DisplayName("Delete my account")
    class DeleteMyAccount {

        @Test
        @DisplayName("throws UserNotFoundException when current user does not exist")
        void throwExceptionWhenNotFound() {
            // given
            given(userRepository.findById(999L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> userService.deleteMyAccount(UserDeleteCommand.from(loginUser(999L))))
                    .isInstanceOf(UserNotFoundException.class);
        }

        @Test
        @DisplayName("deletes the current user")
        void deleteMyAccount() {
            // given
            given(userRepository.findById(1L)).willReturn(Optional.of(user));

            // when
            userService.deleteMyAccount(UserDeleteCommand.from(loginUser));

            // then
            verify(userRepository).delete(user);
        }

        @Test
        @DisplayName("throws AccessDeniedException when login user is null")
        void throwExceptionWhenLoginUserIsNull() {
            assertThatThrownBy(() -> userService.deleteMyAccount(UserDeleteCommand.from(null)))
                    .isInstanceOf(AccessDeniedException.class);
        }
    }

    private LoginUser loginUser(Long id) {
        return new LoginUser(id, "test@test.com", "tester", Role.USER);
    }
}
