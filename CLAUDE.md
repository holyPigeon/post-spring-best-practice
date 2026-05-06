# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Run

```bash
./gradlew build          # Compile and package
./gradlew bootRun        # Start the application
./gradlew clean build    # Full clean rebuild
```

## Testing

```bash
./gradlew test                                    # Run all tests
./gradlew test --tests "FullyQualifiedClassName"  # Run a single test class
./gradlew test --tests "ClassName.methodName"     # Run a single test method
./gradlew test --info                             # Run with verbose logging
```

Test slice dependencies are already declared:
- `spring-boot-starter-webmvc-test` — use `@WebMvcTest` for controller slice tests
- `spring-boot-starter-data-jpa-test` — use `@DataJpaTest` for repository slice tests

## Stack

- **Spring Boot 4.0.6**, Java 21, Gradle
- **spring-boot-starter-webmvc** — Spring MVC (synchronous, servlet-based; not reactive WebFlux)
- **spring-boot-starter-data-jpa** + H2 runtime — JPA with in-memory H2 database
- **spring-boot-h2console** — H2 web console at `/h2-console`
- **Lombok** — use `@Data`, `@Getter`, `@Setter`, `@Builder`, `@RequiredArgsConstructor`, etc. to reduce boilerplate; annotationProcessor is wired for both main and test

## Architecture

도메인 우선 패키지 구조 (멀티 모듈 전환 대비). 각 도메인 패키지가 미래의 모듈 경계가 된다.

```
com.example.springbestpractice/
├── {domain}/           # 도메인별 패키지 (post, user, ...)
│   ├── controller/
│   ├── service/
│   ├── repository/
│   ├── entity/
│   ├── dto/
│   └── exception/
└── global/             # 횡단 관심사 (GlobalExceptionHandler 등)
    └── exception/
```

## Coding Style

토스(Toss) 백엔드 스타일을 따른다.

### 엔티티
- setter 금지 — 상태 변경은 도메인 메서드로 표현 (`update()`, `cancel()` 등)
- `@NoArgsConstructor(access = AccessLevel.PROTECTED)` — JPA용 기본 생성자는 protected
- 정적 팩토리 메서드(`create()`) 사용 — `new` 직접 호출 대신
- `private` 생성자 + `public static create(...)` 조합

### DTO
- Java record 사용 (불변)
- 요청/응답 DTO 분리 (`XxxRequest` / `XxxResponse`)
- `XxxResponse.from(Entity)` 정적 팩토리 메서드로 변환

### 서비스
- 클래스에 `@Transactional(readOnly = true)` 기본 적용
- 쓰기 메서드에만 `@Transactional` 추가
- 비즈니스 로직은 엔티티에 위임, 서비스는 얇게 유지

### 의존성 주입
- 생성자 주입만 사용 (`@RequiredArgsConstructor`)
- `@Autowired` 필드 주입 금지

### 예외 처리
- 도메인별 커스텀 예외 (`XxxNotFoundException` 등)
- `global/exception/GlobalExceptionHandler`에서 `@RestControllerAdvice`로 전역 처리

## Commit Message

```
type(scope): 한국어 설명
```

| 타입 | 용도 |
|---|---|
| `feat` | 새 기능 |
| `fix` | 버그 수정 |
| `refactor` | 코드 개선 |
| `chore` | 설정·잡무 |
| `perf` | 성능 개선 |

스코프는 변경된 도메인명(`post`, `user` 등), 범위가 넓으면 `global`.

## Database

H2 runs in-memory by default (no `application.yaml` datasource config needed for development). To access the console when the app is running: `http://localhost:8080/h2-console` — JDBC URL is `jdbc:h2:mem:testdb` by default.
