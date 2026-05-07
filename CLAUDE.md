# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Quick Reference

| 주제 | 파일 |
|---|---|
| 코딩 스타일 | `.claude/coding-style.md` |
| 커밋 컨벤션 | `.claude/commit-convention.md` |
| 테스트 스타일 | `.claude/testing-style.md` |

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
- **spring-boot-starter-data-jpa** + H2 runtime — JPA with in-memory H2 database
- **Lombok** — annotationProcessor wired for both main and test

## Architecture

계층 우선 패키지 구조 (멀티 모듈 전환 대비). 1차 패키지 = 미래 모듈 경계.

```
com.example.springbestpractice/
├── api/             # presentation: controller + 요청/응답 DTO
│   └── {domain}/{Controller, dto/}
├── application/     # use case: service
│   └── {domain}/
├── domain/          # 핵심 도메인: entity, 도메인 예외
│   └── {domain}/
├── infrastructure/  # 기술 세부사항: JPA repository 등
│   └── {domain}/
└── common/          # 횡단 관심사: 전역 예외 처리 등
    └── exception/
```

의존 방향: `api → application → domain`, `infrastructure → domain`, 모두 → `common`. 역방향 금지.

## Database

H2 in-memory, 별도 datasource 설정 불필요. 콘솔: `http://localhost:8080/h2-console` (JDBC URL: `jdbc:h2:mem:testdb`)
