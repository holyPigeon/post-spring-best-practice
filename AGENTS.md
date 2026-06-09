# AGENTS.md

This file provides guidance to Codex (Codex.ai/code) when working with code in this repository.

## Quick Reference

| 주제 | 파일 |
|---|---|
| 코딩 스타일 | `.Codex/coding-style.md` |
| 커밋 컨벤션 | `.Codex/commit-convention.md` |
| 테스트 스타일 | `.Codex/testing-style.md` |

---

## Task Routing

`AGENTS.md`는 Codex의 루트 라우터 문서다. 작업을 시작할 때 아래 규칙에 따라 필요한 `.Codex` 문서를 먼저 확인하고, 해당 문서의 세부 규칙을 현재 작업에 적용한다.

| 작업 유형 | 반드시 참고할 문서 |
|---|---|
| Java/Spring 코드 작성, 수정, 리팩토링, 패키지 이동 | `.Codex/coding-style.md` |
| 테스트 작성, 테스트 수정, 테스트 실패 분석 | `.Codex/testing-style.md` |
| 커밋 메시지 작성, 커밋 생성, 변경사항 요약 | `.Codex/commit-convention.md` |

여러 작업 유형이 동시에 해당하면 관련 문서를 모두 참고한다. 예를 들어 기능 구현과 테스트 추가를 함께 수행하면 `.Codex/coding-style.md`와 `.Codex/testing-style.md`를 모두 확인한다.

문서 간 우선순위는 다음과 같다.

1. 사용자 요청
2. `AGENTS.md`의 필수 환경/아키텍처 규칙
3. 작업 유형별 `.Codex/*.md` 세부 규칙
4. 기존 코드베이스의 일관성

`.claude/settings*.json`은 Claude 전용 플러그인/권한 설정이므로 Codex 작업 규칙으로 해석하지 않는다.

---

## Java 환경 (필수)

시스템 기본 JVM이 Java 8로 잡혀 있어 Gradle 실행 시 실패한다. **모든 Gradle 명령 전에 반드시 JAVA_HOME을 Zulu 21로 지정해야 한다.**

```powershell
$env:JAVA_HOME = "C:\Zulu\zulu-21"
$env:PATH = "$env:JAVA_HOME\bin;$env:PATH"
```

한 줄로 Gradle 명령과 함께 실행:

```powershell
$env:JAVA_HOME = "C:\Zulu\zulu-21"; $env:PATH = "$env:JAVA_HOME\bin;$env:PATH"; .\gradlew <task>
```

## Build & Run

```powershell
$env:JAVA_HOME = "C:\Zulu\zulu-21"; $env:PATH = "$env:JAVA_HOME\bin;$env:PATH"; .\gradlew build
$env:JAVA_HOME = "C:\Zulu\zulu-21"; $env:PATH = "$env:JAVA_HOME\bin;$env:PATH"; .\gradlew bootRun
$env:JAVA_HOME = "C:\Zulu\zulu-21"; $env:PATH = "$env:JAVA_HOME\bin;$env:PATH"; .\gradlew clean build
```

## Testing

```powershell
$env:JAVA_HOME = "C:\Zulu\zulu-21"; $env:PATH = "$env:JAVA_HOME\bin;$env:PATH"; .\gradlew test
$env:JAVA_HOME = "C:\Zulu\zulu-21"; $env:PATH = "$env:JAVA_HOME\bin;$env:PATH"; .\gradlew test --tests "FullyQualifiedClassName"
$env:JAVA_HOME = "C:\Zulu\zulu-21"; $env:PATH = "$env:JAVA_HOME\bin;$env:PATH"; .\gradlew test --tests "ClassName.methodName"
```

Test slices: `@WebMvcTest` (controller), `@DataJpaTest` (repository)

## Stack

- **Spring Boot 4.0.6**, Java 21, Gradle
- **spring-boot-starter-webmvc** — Spring MVC (synchronous, servlet-based; not reactive WebFlux)
- **spring-boot-starter-data-jpa** + MySQL runtime — local/prod runtime database
- **H2** — test runtime only for repository/data slice tests
- **Lombok** — annotationProcessor wired for both main and test

## Architecture

계층 우선 패키지 구조 (멀티 모듈 전환 대비). 1차 패키지 = 미래 모듈 경계.

```
com.example.springbestpractice/
├── api/             # presentation: controller
│   └── {domain}/{Controller}
├── application/     # use case: service + 요청/응답 DTO
│   └── {domain}/{Service, dto/}
├── domain/          # 핵심 도메인: entity, 도메인 예외
│   └── {domain}/
├── infrastructure/  # 기술 세부사항: JPA repository 등
│   └── {domain}/
└── common/          # 횡단 관심사: 전역 예외 처리 등
    └── exception/
```

의존 방향: `api → application → domain`, `infrastructure → domain`, 모두 → `common`. 역방향 금지.

## Database

기본 실행 프로파일은 `local`이며 MySQL을 사용한다. 로컬 실행 시 `docker compose up --build`로 MySQL과 애플리케이션을 함께 띄우거나, `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD`를 직접 지정한다.

- local 기본 JDBC URL: `jdbc:mysql://localhost:3306/spring_best_practice`
- Docker Compose 내부 JDBC URL: `jdbc:mysql://mysql:3306/spring_best_practice`
- test runtime: H2 in-memory
- prod 기본 DDL 전략: `validate`
