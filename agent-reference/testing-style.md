# Testing Style

Base: Toss backend testing style. Stack: Java 21, JUnit 5, Mockito, AssertJ.

Use `agent-reference/testing-examples.md` only when concrete examples are needed.

## Test Layer Selection

| Target | Annotation | Mock policy | Spring Context |
|---|---|---|---|
| Service, Domain | `@ExtendWith(MockitoExtension.class)` | `@Mock` / `@InjectMocks` only when needed | None |
| Controller | `@WebMvcTest(XxxController.class)` | `@MockitoBean` for Spring bean replacement | Slice |
| Repository | `@DataJpaTest` | No mocks, use H2 | Slice |
| Full flow | `@SpringBootTest` | Minimize mocks | Full, exceptional |

Principle: if Spring Context is not needed, write a plain unit test. Use `@SpringBootTest` only for critical E2E flows.

## Naming And Structure

- Test class `@DisplayName`: domain plus layer, for example `포스트 서비스`.
- Test method `@DisplayName`: business language ending with a result verb such as `반환한다`, `던진다`, or `저장한다`.
- Test method name: English camelCase. It does not need to repeat the full display name.
- Use `@Nested` to group use cases when a class covers multiple scenarios.
- Use `given / when / then` comments for non-trivial tests.
- Keep the `when` block to one action. If there are multiple actions, split the test.
- Put `verify()` checks at the end of `then`.

## Mock Strategy

Prefer real objects. Over-mocking couples tests to implementation details.

| Situation | Preferred double |
|---|---|
| Most domain/application behavior | Real object |
| External API, remote service, hard infrastructure | Fake |
| Email, notification, or side-effect sink | Dummy |
| Hard-to-prepare returned data | Stub |
| Nothing else is practical | Mock framework |

`verify()` policy:

- Use `verify()` for commands such as save, delete, event publication, or other meaningful side effects.
- Do not verify query methods that were already stubbed with `given(...).willReturn(...)`.

Spring Boot 4 controller slice tests must use `org.springframework.test.context.bean.override.mockito.MockitoBean`. Do not introduce deprecated or removed `@MockBean`.

## Assertions

- Use AssertJ.
- Prefer direct field/value assertions for simple cases.
- Use `extracting(...)` for multi-field or collection assertions.
- For exceptions, assert the type. Assert the message only when it is part of the contract or clarifies the failure.

## Fixtures

Tests must not bypass entity invariants.

| Situation | Creation rule |
|---|---|
| Request DTO | record canonical constructor |
| Expected Response DTO | record canonical constructor |
| Simple entity | production static factory |
| Entity repeated in 3+ tests | `XxxFixture` static factory |
| Persistence-generated field needed | set inside fixture helper only |
| Invalid state test | assert the production factory/method throws |

- Do not use entity `@Builder` in tests.
- Do not add test-only constructors that skip production validation.
- Use `ReflectionTestUtils` only inside fixture helpers, not across test methods.
- Prefer named fixture methods such as `adminUser()`, `deletedUser()`, or `postWrittenBy(user)` over a generic test builder.

## Controller Tests

- Use `@WebMvcTest(XxxController.class)`.
- Verify HTTP status, request validation, serialization, and response body shape.
- Mock the application service with `@MockitoBean`.
- Do not test service/domain behavior in controller tests.

## Repository Tests

- Use `@DataJpaTest`.
- Rely on automatic rollback; no `@AfterEach` cleanup is needed for standard data slice tests.
- Test only custom query methods and query behavior you wrote.
- Do not create repository tests just to check `save`, `findById`, `findAll`, or `deleteById`.

For `@SpringBootTest`, automatic rollback is not guaranteed. Use class-level `@Transactional` or explicit cleanup when database state can leak.

## Package Structure

Mirror the main package structure:

```text
test/java/com/example/springbestpractice/
├── api/{domain}/{Domain}ControllerTest.java
├── application/{domain}/{Domain}ServiceTest.java
└── infrastructure/{domain}/{Domain}RepositoryTest.java
```

## Parameterized Tests

Use `@ParameterizedTest` when one behavior should be checked against multiple inputs, especially boundary values.

- `@ValueSource`: one input value.
- `@CsvSource`: input/output pairs.
- `@MethodSource`: complex objects or larger cases.
