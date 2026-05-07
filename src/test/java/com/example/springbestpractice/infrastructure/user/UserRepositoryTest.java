package com.example.springbestpractice.infrastructure.user;

import com.example.springbestpractice.domain.user.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@DisplayName("유저 레포지토리")
class UserRepositoryTest {

    @Autowired
    UserRepository userRepository;

    @Autowired
    TestEntityManager em;

    @Nested
    @DisplayName("이메일 존재 여부 확인")
    class ExistsByEmail {

        @Test
        @DisplayName("저장된 이메일로 조회하면 true를 반환한다")
        void returnTrueWhenEmailExists() {
            // given
            em.persist(User.create("test@test.com", "테스터", "password"));
            em.flush();

            // when
            boolean exists = userRepository.existsByEmail("test@test.com");

            // then
            assertThat(exists).isTrue();
        }

        @Test
        @DisplayName("저장되지 않은 이메일로 조회하면 false를 반환한다")
        void returnFalseWhenEmailNotExists() {
            // when
            boolean exists = userRepository.existsByEmail("nonexistent@test.com");

            // then
            assertThat(exists).isFalse();
        }
    }

    @Nested
    @DisplayName("이메일로 유저 조회")
    class FindByEmail {

        @Test
        @DisplayName("저장된 이메일로 조회하면 유저를 반환한다")
        void returnUserWhenEmailExists() {
            // given
            em.persist(User.create("test@test.com", "테스터", "password"));
            em.flush();

            // when
            Optional<User> result = userRepository.findByEmail("test@test.com");

            // then
            assertThat(result).isPresent();
            assertThat(result.get().getEmail()).isEqualTo("test@test.com");
        }

        @Test
        @DisplayName("저장되지 않은 이메일로 조회하면 빈 값을 반환한다")
        void returnEmptyWhenEmailNotExists() {
            // when
            Optional<User> result = userRepository.findByEmail("nonexistent@test.com");

            // then
            assertThat(result).isEmpty();
        }
    }
}
