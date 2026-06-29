package com.example.springbestpractice.infrastructure.user;

import com.example.springbestpractice.domain.user.User;
import com.example.springbestpractice.domain.user.UserRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.time.LocalDateTime;
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

    @Nested
    @DisplayName("검색")
    class Search {

        @Test
        @DisplayName("키워드로 이메일/닉네임을 대소문자 구분 없이 검색한다")
        void searchByKeyword() {
            // given
            em.persist(User.create("alice@test.com", "alice", "password"));
            em.persist(User.create("bob@test.com", "Builder", "password"));
            em.persist(User.create("carol@test.com", "carol", "password"));
            em.flush();
            em.clear();

            // when
            Page<User> byNickname = userRepository.search("build", null, latestPage(0, 10));
            Page<User> byEmail = userRepository.search("ALICE", null, latestPage(0, 10));

            // then
            assertThat(byNickname.getContent()).extracting(User::getNickname).containsExactly("Builder");
            assertThat(byEmail.getContent()).extracting(User::getEmail).containsExactly("alice@test.com");
        }

        @Test
        @DisplayName("역할로 필터링한다")
        void filterByRole() {
            // given
            em.persist(User.create("user@test.com", "user", "password"));
            em.persist(User.createAdmin("admin@test.com", "admin", "password"));
            em.flush();
            em.clear();

            // when
            Page<User> admins = userRepository.search(null, UserRole.ADMIN, latestPage(0, 10));

            // then
            assertThat(admins.getContent()).extracting(User::getEmail).containsExactly("admin@test.com");
            assertThat(admins.getTotalElements()).isEqualTo(1);
        }

        @Test
        @DisplayName("최신순 정렬과 오프셋 페이징을 적용한다")
        void pageAndSort() {
            // given
            persistUserAt("first@test.com", "first", LocalDateTime.of(2026, 1, 1, 0, 0));
            persistUserAt("second@test.com", "second", LocalDateTime.of(2026, 1, 2, 0, 0));
            persistUserAt("third@test.com", "third", LocalDateTime.of(2026, 1, 3, 0, 0));

            // when (최신순, size 2의 두 번째 페이지 => 가장 오래된 1건)
            Page<User> result = userRepository.search(null, null, latestPage(1, 2));

            // then
            assertThat(result.getContent()).extracting(User::getEmail).containsExactly("first@test.com");
            assertThat(result.getTotalElements()).isEqualTo(3);
            assertThat(result.getTotalPages()).isEqualTo(2);
            assertThat(result.isLast()).isTrue();
        }
    }

    private PageRequest latestPage(int page, int size) {
        return PageRequest.of(page, size, Sort.by(
                Sort.Order.desc("createdAt"),
                Sort.Order.desc("id")
        ));
    }

    private void persistUserAt(String email, String nickname, LocalDateTime createdAt) {
        User saved = em.persist(User.create(email, nickname, "password"));
        em.flush();
        em.getEntityManager().createQuery("update User u set u.createdAt = :createdAt where u.id = :id")
                .setParameter("createdAt", createdAt)
                .setParameter("id", saved.getId())
                .executeUpdate();
        em.clear();
    }
}
