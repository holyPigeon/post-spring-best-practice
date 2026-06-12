# Testing Examples

Read this file only when concrete test examples are useful.

## Naming

```java
@DisplayName("이메일이 중복되면 예외를 던진다")
@Test
void throwExceptionWhenDuplicateEmail() {
}
```

## Nested Structure

```java
@DisplayName("유저 서비스")
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock UserRepository userRepository;
    @InjectMocks UserService userService;

    @Nested
    @DisplayName("유저 생성")
    class Create {

        @Test
        @DisplayName("이메일 중복이면 예외를 던진다")
        void duplicateEmail() {
            // ...
        }

        @Test
        @DisplayName("정상 입력이면 유저를 저장한다")
        void success() {
            // ...
        }
    }
}
```

## Given When Then

```java
@Test
@DisplayName("유효한 제목과 내용으로 포스트를 생성한다")
void createPost() {
    // given
    PostCreateRequest request = new PostCreateRequest("제목", "내용");
    LoginUser loginUser = new LoginUser(1L, "test@test.com", "테스터");
    given(postRepository.save(any(Post.class))).willReturn(PostFixture.postWithId(1L));

    // when
    PostResponse result = postService.createPost(PostCreateCommand.from(request, loginUser));

    // then
    assertThat(result.id()).isEqualTo(1L);
    assertThat(result.title()).isEqualTo("제목");
    verify(postRepository).save(any(Post.class));
}
```

## Verify Policy

```java
// Correct: command side effect
verify(postRepository).save(any(Post.class));

// Avoid: already stubbed query
given(userRepository.findById(1L)).willReturn(Optional.of(user));
verify(userRepository).findById(1L);
```

## AssertJ

```java
assertThat(result).isNotNull();
assertThat(result.getName()).isEqualTo("홍길동");

assertThat(result)
    .extracting("id", "title", "content")
    .containsExactly(1L, "제목", "내용");

assertThat(results)
    .hasSize(2)
    .extracting("email")
    .containsExactlyInAnyOrder("a@test.com", "b@test.com");

assertThatThrownBy(() -> userService.findById(999L))
    .isInstanceOf(UserNotFoundException.class);
```

## Fixture Helper

```java
public final class UserFixture {

    private UserFixture() {
    }

    public static User user() {
        return User.create("test@test.com", "테스터", "password");
    }

    public static User userWithId(Long id) {
        User user = user();
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }
}
```

## Controller Slice

```java
@WebMvcTest(UserController.class)
@DisplayName("유저 API")
class UserControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @MockitoBean UserService userService;

    @Test
    @DisplayName("POST /api/users - 201과 생성된 유저를 반환한다")
    void createUser() throws Exception {
        // given
        given(userService.createUser(any()))
            .willReturn(new UserResponse(1L, "test@test.com", "테스터", null, null));

        // when & then
        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                    new UserCreateRequest("test@test.com", "테스터", "password"))))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(1L))
            .andExpect(jsonPath("$.email").value("test@test.com"));
    }
}
```

## Repository Slice

```java
@DataJpaTest
@DisplayName("유저 레포지토리")
class UserRepositoryTest {

    @Autowired UserRepository userRepository;
    @Autowired TestEntityManager em;

    @Test
    @DisplayName("이메일로 유저 존재 여부를 확인한다")
    void existsByEmail() {
        // given
        em.persist(User.create("test@test.com", "테스터", "password"));
        em.flush();

        // when
        boolean exists = userRepository.existsByEmail("test@test.com");

        // then
        assertThat(exists).isTrue();
    }
}
```

## Parameterized Test

```java
@ParameterizedTest
@DisplayName("유효하지 않은 이메일 형식이면 예외를 던진다")
@ValueSource(strings = {"notanemail", "missing@", "@nodomain.com", ""})
void invalidEmail(String email) {
    assertThatThrownBy(() -> Email.from(email))
        .isInstanceOf(IllegalArgumentException.class);
}

@ParameterizedTest
@DisplayName("나이에 따른 프로모션 참여 가능 여부")
@CsvSource({
    "17, true",
    "18, true",
    "19, false",
    "0,  true"
})
void promotionEligibility(int age, boolean expected) {
    assertThat(promotionPolicy.isEligible(age)).isEqualTo(expected);
}
```
