package com.example.springbestpractice.infrastructure.security;

import com.example.springbestpractice.common.annotation.CurrentUser;
import com.example.springbestpractice.common.exception.UnauthenticatedException;
import com.example.springbestpractice.common.model.LoginUser;
import com.example.springbestpractice.support.fixture.UserFixture;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("현재 사용자 인자 리졸버")
class CurrentUserArgumentResolverTest {

    CurrentUserArgumentResolver resolver = new CurrentUserArgumentResolver();

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Nested
    @DisplayName("파라미터 지원 여부")
    class SupportsParameter {

        @Test
        @DisplayName("@CurrentUser LoginUser 파라미터를 지원한다")
        void supportCurrentUserLoginUser() throws Exception {
            // given
            MethodParameter parameter = new MethodParameter(
                    Dummy.class.getDeclaredMethod("withAnnotation", LoginUser.class), 0);

            // when & then
            assertThat(resolver.supportsParameter(parameter)).isTrue();
        }

        @Test
        @DisplayName("@CurrentUser 없는 LoginUser 파라미터는 지원하지 않는다")
        void notSupportWithoutAnnotation() throws Exception {
            // given
            MethodParameter parameter = new MethodParameter(
                    Dummy.class.getDeclaredMethod("withoutAnnotation", LoginUser.class), 0);

            // when & then
            assertThat(resolver.supportsParameter(parameter)).isFalse();
        }
    }

    @Nested
    @DisplayName("인자 해석")
    class ResolveArgument {

        @Test
        @DisplayName("SecurityContext의 인증 정보에서 LoginUser를 추출한다")
        void resolveLoginUserFromSecurityContext() {
            // given
            CustomUserDetails userDetails = new CustomUserDetails(UserFixture.userWithId(1L));
            SecurityContextHolder.getContext().setAuthentication(
                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities())
            );

            // when
            LoginUser result = resolver.resolveArgument(null, null, null, null);

            // then
            assertThat(result)
                    .extracting("id", "email", "nickname", "role")
                    .containsExactly(1L, "test@test.com", "tester", "USER");
        }

        @Test
        @DisplayName("인증 정보가 없으면 예외를 던진다")
        void throwWhenAuthenticationIsNull() {
            assertThatThrownBy(() -> resolver.resolveArgument(null, null, null, null))
                    .isInstanceOf(UnauthenticatedException.class);
        }

        @Test
        @DisplayName("Anonymous 인증이면 예외를 던진다")
        void throwWhenAnonymousAuthentication() {
            // given
            SecurityContextHolder.getContext().setAuthentication(
                    new AnonymousAuthenticationToken("key", "anonymousUser",
                            List.of(new SimpleGrantedAuthority("ROLE_ANONYMOUS")))
            );

            // when & then
            assertThatThrownBy(() -> resolver.resolveArgument(null, null, null, null))
                    .isInstanceOf(UnauthenticatedException.class);
        }
    }

    static class Dummy {
        void withAnnotation(@CurrentUser LoginUser loginUser) {
        }

        void withoutAnnotation(LoginUser loginUser) {
        }
    }
}
