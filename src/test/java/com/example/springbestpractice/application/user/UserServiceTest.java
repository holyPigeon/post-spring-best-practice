package com.example.springbestpractice.application.user;

import com.example.springbestpractice.application.user.command.UserDeleteCommand;
import com.example.springbestpractice.application.user.command.UserPasswordUpdateCommand;
import com.example.springbestpractice.application.user.command.UserUpdateCommand;
import com.example.springbestpractice.application.user.dto.UserCreateRequest;
import com.example.springbestpractice.application.user.dto.UserPasswordUpdateRequest;
import com.example.springbestpractice.application.user.dto.UserResponse;
import com.example.springbestpractice.application.user.dto.UserUpdateRequest;
import com.example.springbestpractice.common.model.LoginUser;
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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@DisplayName("유저 서비스")
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
        loginUser = new LoginUser(1L, "test@test.com", "테스터");
    }

    @Nested
    @DisplayName("유저 생성")
    class Create {

        @Test
        @DisplayName("이메일이 중복이면 예외를 던진다")
        void throwExceptionWhenDuplicateEmail() {
            // given
            UserCreateRequest request = new UserCreateRequest("test@test.com", "테스터", "password");
            given(userRepository.existsByEmail("test@test.com")).willReturn(true);

            // when & then
            assertThatThrownBy(() -> userService.createUser(request))
                    .isInstanceOf(DuplicateEmailException.class);
        }

        @Test
        @DisplayName("정상 입력이면 유저를 저장하고 응답을 반환한다")
        void createUser() {
            // given
            UserCreateRequest request = new UserCreateRequest("test@test.com", "테스터", "password");
            given(userRepository.existsByEmail("test@test.com")).willReturn(false);
            given(passwordEncoder.encode("password")).willReturn("encoded-password");
            given(userRepository.save(any(User.class))).willReturn(user);

            // when
            UserResponse result = userService.createUser(request);

            // then
            assertThat(result)
                    .extracting("id", "email", "nickname")
                    .containsExactly(1L, "test@test.com", "tester");
            verify(userRepository).save(any(User.class));
        }
    }

    @Nested
    @DisplayName("유저 조회")
    class Find {

        @Test
        @DisplayName("존재하지 않는 ID면 예외를 던진다")
        void throwExceptionWhenNotFound() {
            // given
            given(userRepository.findById(999L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> userService.getUser(999L, new LoginUser(999L, "other@test.com", "다른유저")))
                    .isInstanceOf(UserNotFoundException.class);
        }

        @Test
        @DisplayName("본인 ID면 응답을 반환한다")
        void returnUserResponse() {
            // given
            given(userRepository.findById(1L)).willReturn(Optional.of(user));

            // when
            UserResponse result = userService.getUser(1L, loginUser);

            // then
            assertThat(result.email()).isEqualTo("test@test.com");
        }

        @Test
        @DisplayName("본인 ID가 아니면 예외를 던진다")
        void throwExceptionWhenNotSelf() {
            assertThatThrownBy(() -> userService.getUser(2L, loginUser))
                    .isInstanceOf(AccessDeniedException.class);
        }
    }

    @Test
    @DisplayName("유저가 있으면 전체 목록을 반환한다")
    void returnAllUsers() {
        // given
        User another = UserFixture.userWithId(2L, "other@test.com", "다른유저", "pw");
        given(userRepository.findAll()).willReturn(List.of(user, another));

        // when
        List<UserResponse> result = userService.getAllUsers();

        // then
        assertThat(result).hasSize(2)
                .extracting("email")
                .containsExactly("test@test.com", "other@test.com");
    }

    @Nested
    @DisplayName("유저 정보 수정")
    class Update {

        @Test
        @DisplayName("존재하는 ID면 닉네임을 변경한다")
        void updateNickname() {
            // given
            UserUpdateRequest request = new UserUpdateRequest("새닉네임");
            given(userRepository.findById(1L)).willReturn(Optional.of(user));

            // when
            UserResponse result = userService.updateUser(UserUpdateCommand.from(1L, request, loginUser));

            // then
            assertThat(result.nickname()).isEqualTo("새닉네임");
        }

        @Test
        @DisplayName("본인 ID가 아니면 예외를 던진다")
        void throwExceptionWhenNotSelf() {
            // given
            UserUpdateRequest request = new UserUpdateRequest("새닉네임");

            // when & then
            assertThatThrownBy(() -> userService.updateUser(UserUpdateCommand.from(2L, request, loginUser)))
                    .isInstanceOf(AccessDeniedException.class);
        }
    }

    @Test
    @DisplayName("비밀번호를 인코딩하여 변경한다")
    void updatePassword() {
        // given
        UserPasswordUpdateRequest request = new UserPasswordUpdateRequest("newpassword");
        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(passwordEncoder.encode("newpassword")).willReturn("encoded-newpassword");

        // when
        userService.updatePassword(UserPasswordUpdateCommand.from(1L, request, loginUser));

        // then
        assertThat(user.getPassword()).isEqualTo("encoded-newpassword");
        verify(passwordEncoder).encode("newpassword");
    }

    @Test
    @DisplayName("존재하는 ID면 유저를 삭제한다")
    void deleteUser() {
        // given
        given(userRepository.findById(1L)).willReturn(Optional.of(user));

        // when
        userService.deleteUser(UserDeleteCommand.from(1L, loginUser));

        // then
        verify(userRepository).delete(user);
    }
}
