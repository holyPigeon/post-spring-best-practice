package com.example.springbestpractice.application.user;

import com.example.springbestpractice.api.user.dto.UserCreateRequest;
import com.example.springbestpractice.api.user.dto.UserPasswordUpdateRequest;
import com.example.springbestpractice.api.user.dto.UserResponse;
import com.example.springbestpractice.api.user.dto.UserUpdateRequest;
import com.example.springbestpractice.domain.user.DuplicateEmailException;
import com.example.springbestpractice.domain.user.User;
import com.example.springbestpractice.domain.user.UserNotFoundException;
import com.example.springbestpractice.infrastructure.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .email("test@test.com")
                .nickname("테스터")
                .password("password")
                .build();
    }

    @Nested
    @DisplayName("유저 생성")
    class Create {

        @Test
        @DisplayName("이메일이 중복이면 DuplicateEmailException을 던진다")
        void throwExceptionWhenDuplicateEmail() {
            // given
            UserCreateRequest request = new UserCreateRequest("test@test.com", "테스터", "password");
            given(userRepository.existsByEmail("test@test.com")).willReturn(true);

            // when & then
            assertThatThrownBy(() -> userService.createUser(request))
                    .isInstanceOf(DuplicateEmailException.class)
                    .hasMessage("이미 사용 중인 이메일입니다. email=test@test.com");
        }

        @Test
        @DisplayName("정상 입력이면 유저를 저장하고 UserResponse를 반환한다")
        void createUser() {
            // given
            UserCreateRequest request = new UserCreateRequest("test@test.com", "테스터", "password");
            given(userRepository.existsByEmail("test@test.com")).willReturn(false);
            given(passwordEncoder.encode("password")).willReturn("encoded-password");
            given(userRepository.save(any(User.class))).willReturn(user);

            // when
            UserResponse result = userService.createUser(request);

            // then
            assertThat(result.id()).isEqualTo(1L);
            assertThat(result.email()).isEqualTo("test@test.com");
            assertThat(result.nickname()).isEqualTo("테스터");
            verify(userRepository).save(any(User.class));
        }
    }

    @Nested
    @DisplayName("유저 단건 조회")
    class Find {

        @Test
        @DisplayName("존재하지 않는 ID면 UserNotFoundException을 던진다")
        void throwExceptionWhenNotFound() {
            // given
            given(userRepository.findById(999L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> userService.getUser(999L))
                    .isInstanceOf(UserNotFoundException.class)
                    .hasMessage("유저를 찾을 수 없습니다. id=999");
        }

        @Test
        @DisplayName("존재하는 ID면 UserResponse를 반환한다")
        void returnUserResponse() {
            // given
            given(userRepository.findById(1L)).willReturn(Optional.of(user));

            // when
            UserResponse result = userService.getUser(1L);

            // then
            assertThat(result.id()).isEqualTo(1L);
            assertThat(result.email()).isEqualTo("test@test.com");
        }
    }

    @Nested
    @DisplayName("유저 전체 조회")
    class FindAll {

        @Test
        @DisplayName("유저가 없으면 빈 리스트를 반환한다")
        void returnEmptyList() {
            // given
            given(userRepository.findAll()).willReturn(List.of());

            // when
            List<UserResponse> result = userService.getAllUsers();

            // then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("유저가 있으면 전체 목록을 반환한다")
        void returnAllUsers() {
            // given
            User another = User.builder().id(2L).email("other@test.com").nickname("다른유저").password("pw").build();
            given(userRepository.findAll()).willReturn(List.of(user, another));

            // when
            List<UserResponse> result = userService.getAllUsers();

            // then
            assertThat(result).hasSize(2)
                    .extracting("email")
                    .containsExactly("test@test.com", "other@test.com");
        }
    }

    @Nested
    @DisplayName("유저 정보 수정")
    class Update {

        @Test
        @DisplayName("존재하지 않는 ID면 UserNotFoundException을 던진다")
        void throwExceptionWhenNotFound() {
            // given
            UserUpdateRequest request = new UserUpdateRequest("새닉네임");
            given(userRepository.findById(999L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> userService.updateUser(999L, request))
                    .isInstanceOf(UserNotFoundException.class);
        }

        @Test
        @DisplayName("존재하는 ID면 닉네임을 변경하고 반환한다")
        void updateNickname() {
            // given
            UserUpdateRequest request = new UserUpdateRequest("새닉네임");
            given(userRepository.findById(1L)).willReturn(Optional.of(user));

            // when
            UserResponse result = userService.updateUser(1L, request);

            // then
            assertThat(result.nickname()).isEqualTo("새닉네임");
        }
    }

    @Nested
    @DisplayName("비밀번호 변경")
    class UpdatePassword {

        @Test
        @DisplayName("존재하지 않는 ID면 UserNotFoundException을 던진다")
        void throwExceptionWhenNotFound() {
            // given
            UserPasswordUpdateRequest request = new UserPasswordUpdateRequest("newpassword");
            given(userRepository.findById(999L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> userService.updatePassword(999L, request))
                    .isInstanceOf(UserNotFoundException.class);
        }

        @Test
        @DisplayName("존재하는 ID면 비밀번호를 인코딩하여 변경한다")
        void updatePassword() {
            // given
            UserPasswordUpdateRequest request = new UserPasswordUpdateRequest("newpassword");
            given(userRepository.findById(1L)).willReturn(Optional.of(user));
            given(passwordEncoder.encode("newpassword")).willReturn("encoded-newpassword");

            // when
            userService.updatePassword(1L, request);

            // then
            verify(passwordEncoder).encode("newpassword");
            assertThat(user.getPassword()).isEqualTo("encoded-newpassword");
        }
    }

    @Nested
    @DisplayName("유저 삭제")
    class Delete {

        @Test
        @DisplayName("존재하지 않는 ID면 UserNotFoundException을 던진다")
        void throwExceptionWhenNotFound() {
            // given
            given(userRepository.findById(999L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> userService.deleteUser(999L))
                    .isInstanceOf(UserNotFoundException.class);
        }

        @Test
        @DisplayName("존재하는 ID면 유저를 삭제한다")
        void deleteUser() {
            // given
            given(userRepository.findById(1L)).willReturn(Optional.of(user));

            // when
            userService.deleteUser(1L);

            // then
            verify(userRepository).delete(user);
        }
    }
}
