# Testing Style Guide

> 토스 기술블로그(test-strategy-server) · 토스페이먼츠 단위테스트 문서 · SLASH 21(이응준) 기반.  
> 스택: Java 21, JUnit 5, Mockito, AssertJ.

---

## 1. 테스트 계층 선택

| 대상 | 어노테이션 | Mock | Spring Context |
|------|-----------|------|---------------|
| Service · Domain | `@ExtendWith(MockitoExtension.class)` | `@Mock` / `@InjectMocks` | 없음 (빠름) |
| Controller | `@WebMvcTest(XxxController.class)` | `@MockitoBean` | Slice |
| Repository | `@DataJpaTest` | 없음 (H2 실사용) | Slice |
| 전체 플로우 | `@SpringBootTest` | 최소화 | Full (느림, 예외적) |

**원칙:** Spring Context가 필요 없으면 순수 단위 테스트. `@SpringBootTest`는 E2E 검증에만.

---

## 2. 네이밍

```java
@DisplayName("이메일이 중복되면 예외를 던진다")  // 한글, "~하면 ~한다" 형식
@Test
void throwExceptionWhenDuplicateEmail() {        // camelCase 영문, DisplayName과 중복 설명 불필요
```

- `@DisplayName`: 비즈니스 언어로, 결과 동사로 끝남 ("반환한다" / "던진다" / "저장한다")
- 클래스: `@DisplayName("포스트 서비스")` — 도메인 + 계층명

---

## 3. @Nested 구조

```java
@DisplayName("유저 서비스")
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock UserRepository userRepository;
    @InjectMocks UserService userService;

    @Nested
    @DisplayName("유저 생성")
    class Create {
        @Test @DisplayName("이메일 중복이면 예외를 던진다") void duplicateEmail() { ... }
        @Test @DisplayName("정상 입력이면 유저를 저장한다") void success() { ... }
    }

    @Nested
    @DisplayName("유저 조회")
    class Find {
        @Test @DisplayName("존재하지 않는 ID면 예외를 던진다") void notFound() { ... }
        @Test @DisplayName("존재하는 ID면 유저를 반환한다") void found() { ... }
    }
}
```

---

## 4. given / when / then

```java
@Test
@DisplayName("유효한 제목과 내용으로 포스트를 생성한다")
void createPost() {
    // given
    PostCreateRequest request = new PostCreateRequest("제목", "내용");
    given(postRepository.save(any())).willReturn(Post.builder().id(1L).title("제목").content("내용").build());

    // when
    PostResponse result = postService.create(request);  // 단 한 줄

    // then
    assertThat(result.id()).isEqualTo(1L);
    assertThat(result.title()).isEqualTo("제목");
    verify(postRepository).save(any(Post.class));       // verify는 then 마지막에
}
```

- `when` 블록은 반드시 **한 줄** (여러 동작 = 테스트 분리)
- `verify()`는 `then` 끝에 모아서

---

## 5. Mock 전략 (토스: 실제 객체 우선)

테스트 더블 선택 기준 (우선순위 순):

| 상황 | 선택 | 이유 |
|------|------|------|
| 대부분의 경우 | **실제 객체** | 과도한 Mock은 구현과 강결합 |
| 외부 API / DB 등 인프라 | **Fake** (실제처럼 동작하는 구현체) | 프로덕션 부적합한 인메모리 구현 |
| 이메일·알림 등 부수효과 | **Dummy** (빈 구현체) | 호출만 받고 아무것도 안 함 |
| 데이터 준비가 어려운 경우 | **Stub** (`given().willReturn()`) | 미리 정해진 응답 반환 |
| 최후의 수단 | **Mock 프레임워크** (`@Mock`) | 위 모두 불가능할 때만 |

**`verify()` 사용 기준 (토스페이먼츠 원칙):**
- **Command** (저장, 삭제, 이벤트 발행 등 void 메서드) → `verify()` 사용
- **Query** (조회, 이미 `given()`으로 stubbing한 메서드) → `verify()` **금지** (Stub 상호작용 검증은 안티패턴)

```java
// 올바른 verify 사용 — Command
verify(postRepository).save(any(Post.class));     // ✅ 저장 호출 여부 확인

// 잘못된 verify 사용 — Stub
given(userRepository.findById(1L)).willReturn(Optional.of(user));
verify(userRepository).findById(1L);              // ❌ 이미 stubbing한 것 중복 검증
```

**Mock 위치별 선택:**

| 테스트 계층 | Mock 방식 |
|-----------|---------|
| Service (순수 단위) | `@Mock` — Spring 없이 |
| `@WebMvcTest` 컨트롤러 | `@MockitoBean` — Spring 빈 교체 |
| `@DataJpaTest` 쿼리 | Mock 없이 H2 실사용 |

Spring Boot 4에서는 컨트롤러 슬라이스 테스트의 Spring 빈 교체에
`org.springframework.test.context.bean.override.mockito.MockitoBean`을 사용한다.
이 프로젝트에서는 deprecated/removed될 수 있는 `@MockBean`을 새로 추가하지 않는다.

---

## 6. AssertJ 패턴

```java
// 단일 값
assertThat(result).isNotNull();
assertThat(result.getName()).isEqualTo("홍길동");

// 여러 필드 동시 검증
assertThat(result)
    .extracting("id", "title", "content")
    .containsExactly(1L, "제목", "내용");

// 컬렉션
assertThat(results)
    .hasSize(2)
    .extracting("email")
    .containsExactlyInAnyOrder("a@test.com", "b@test.com");

// 예외 — 타입만 검증
assertThatThrownBy(() -> userService.findById(999L))
    .isInstanceOf(UserNotFoundException.class);

// 예외 — 메시지까지 검증
assertThatThrownBy(() -> userService.findById(999L))
    .isInstanceOf(UserNotFoundException.class)
    .hasMessage("유저를 찾을 수 없습니다. id=999");
```

---

## 7. 테스트 독립성 (@BeforeEach)

각 테스트는 독립적이어야 한다. 공유 상태는 `@BeforeEach`로 초기화.

```java
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock UserRepository userRepository;
    @InjectMocks UserService userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
            .id(1L)
            .email("test@test.com")
            .password("pw")
            .build();
    }

    @Test
    @DisplayName("존재하는 ID면 유저를 반환한다")
    void findById() {
        given(userRepository.findById(1L)).willReturn(Optional.of(testUser));
        // ...
    }
}
```

- `@BeforeEach`는 공통 Mock 설정이나 공유 픽스처 초기화에만 사용
- 테스트 특화 데이터는 각 테스트 내 `// given`에서 직접 선언

---

## 8. 픽스처 생성 패턴

**현재: Builder 직접 사용**

```java
// given
User user = User.builder()
    .email("test@test.com")
    .password("pw")
    .build();
```

각 테스트에서 필요한 값만 명시적으로 설정. 테스트 코드 자체가 자기설명적.

> **규모가 커지면 `XxxFixture` 클래스로 전환 권장**  
> 도메인당 테스트 파일이 3개 이상이거나, 여러 테스트에서 같은 객체를 반복 생성하면  
> `src/test/java/.../fixture/UserFixture.java` 같은 정적 팩토리 클래스로 공통화한다.  
> 토스는 멀티모듈에서 `java-test-fixtures` Gradle 플러그인으로 모듈 간 픽스처를 공유한다.

---

## 9. @ParameterizedTest (경계값 · 다중 케이스)

같은 로직을 여러 입력값으로 검증할 때 사용.

```java
@ParameterizedTest
@DisplayName("유효하지 않은 이메일 형식이면 예외를 던진다")
@ValueSource(strings = {"notanemail", "missing@", "@nodomain.com", ""})
void invalidEmail(String email) {
    assertThatThrownBy(() -> new User(email, "pw"))
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

- `@ValueSource`: 단일 값 목록
- `@CsvSource`: 입력/출력 쌍 (경계값 테스트에 적합)
- `@MethodSource`: 복잡한 객체 인자가 필요할 때

---

## 10. Controller (@WebMvcTest) 패턴

```java
@WebMvcTest(UserController.class)
@DisplayName("유저 API")
class UserControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @MockitoBean UserService userService;

    @Test
    @DisplayName("POST /users - 201과 생성된 유저를 반환한다")
    void createUser() throws Exception {
        // given
        given(userService.create(any())).willReturn(new UserResponse(1L, "test@test.com"));

        // when & then
        mockMvc.perform(post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new UserCreateRequest("test@test.com", "pw"))))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(1L))
            .andExpect(jsonPath("$.email").value("test@test.com"));
    }
}
```

---

## 11. Repository (@DataJpaTest) 패턴

`@DataJpaTest`는 각 테스트 후 **자동 롤백**. `@AfterEach`로 직접 정리할 필요 없음.

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
        em.persist(User.builder().email("test@test.com").password("pw").build());
        em.flush();

        // when
        boolean exists = userRepository.existsByEmail("test@test.com");

        // then
        assertThat(exists).isTrue();
    }
}
```

> **주의:** `@SpringBootTest`에서는 자동 롤백이 없음.  
> 테스트 간 DB 오염을 막으려면 `@Transactional`을 클래스에 붙이거나  
> `@AfterEach`에서 `repository.deleteAll()`로 직접 정리.

> **테스트 대상:** 직접 작성한 쿼리 메서드만 검증한다. `save`, `findById`, `findAll`, `deleteById` 등 JpaRepository 기본 메서드는 Spring Data JPA가 이미 검증하므로 테스트하지 않는다. 커스텀 메서드가 없으면 레포지토리 테스트 파일을 만들지 않는다.

---

## 12. 패키지 구조 (테스트)

main과 동일한 패키지 구조 유지:
```
test/java/com/example/springbestpractice/
├── api/user/UserControllerTest.java             (@WebMvcTest)
├── api/post/PostControllerTest.java
├── application/user/UserServiceTest.java        (@ExtendWith(MockitoExtension))
├── application/post/PostServiceTest.java
├── infrastructure/user/UserRepositoryTest.java  (@DataJpaTest)
└── infrastructure/post/PostRepositoryTest.java
```
