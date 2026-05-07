package com.example.springbestpractice.domain.user;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("유저 도메인")
class UserTest {

    @Nested
    @DisplayName("유저 생성")
    class Create {

        @Test
        @DisplayName("이메일·닉네임·패스워드로 유저를 생성한다")
        void createUser() {
            // when
            User user = User.create("test@test.com", "테스터", "password");

            // then
            assertThat(user)
                    .extracting("email", "nickname", "password")
                    .containsExactly("test@test.com", "테스터", "password");
        }
    }

    @Nested
    @DisplayName("닉네임 변경")
    class UpdateNickname {

        @Test
        @DisplayName("닉네임을 변경하면 닉네임이 업데이트된다")
        void updateNickname() {
            // given
            User user = User.create("test@test.com", "테스터", "password");

            // when
            user.updateNickname("새닉네임");

            // then
            assertThat(user.getNickname()).isEqualTo("새닉네임");
        }
    }

    @Nested
    @DisplayName("패스워드 변경")
    class UpdatePassword {

        @Test
        @DisplayName("패스워드를 변경하면 패스워드가 업데이트된다")
        void updatePassword() {
            // given
            User user = User.create("test@test.com", "테스터", "password");

            // when
            user.updatePassword("newpassword");

            // then
            assertThat(user.getPassword()).isEqualTo("newpassword");
        }
    }
}
