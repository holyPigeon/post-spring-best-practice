# Project Context

Shared context for Codex and Claude Code.

## Java Environment

Always run Gradle with JDK 21. Some environments default to Java 8, so set `JAVA_HOME` before Gradle commands.

Windows PowerShell:

```powershell
$env:JAVA_HOME = "C:\Zulu\zulu-21"; $env:PATH = "$env:JAVA_HOME\bin;$env:PATH"; .\gradlew <task>
```

macOS shell:

```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 21)
export PATH="$JAVA_HOME/bin:$PATH"
./gradlew <task>
```

## Build, Run, Test

Windows PowerShell:

```powershell
$env:JAVA_HOME = "C:\Zulu\zulu-21"; $env:PATH = "$env:JAVA_HOME\bin;$env:PATH"; .\gradlew build
$env:JAVA_HOME = "C:\Zulu\zulu-21"; $env:PATH = "$env:JAVA_HOME\bin;$env:PATH"; .\gradlew bootRun
$env:JAVA_HOME = "C:\Zulu\zulu-21"; $env:PATH = "$env:JAVA_HOME\bin;$env:PATH"; .\gradlew test
$env:JAVA_HOME = "C:\Zulu\zulu-21"; $env:PATH = "$env:JAVA_HOME\bin;$env:PATH"; .\gradlew test --tests "FullyQualifiedClassName"
$env:JAVA_HOME = "C:\Zulu\zulu-21"; $env:PATH = "$env:JAVA_HOME\bin;$env:PATH"; .\gradlew test --tests "ClassName.methodName"
```

macOS shell:

```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 21)
export PATH="$JAVA_HOME/bin:$PATH"
./gradlew build
./gradlew bootRun
./gradlew test
./gradlew test --tests "FullyQualifiedClassName"
./gradlew test --tests "ClassName.methodName"
```

Use `@WebMvcTest` for controller slices and `@DataJpaTest` for repository/data slices.

## Stack

- Spring Boot 4.0.6, Java 21, Gradle
- `spring-boot-starter-webmvc`: synchronous servlet Spring MVC, not WebFlux
- `spring-boot-starter-data-jpa` with MySQL runtime for local/prod
- H2 only for test runtime repository/data slice tests
- Lombok annotation processors configured for main and test

## Operational Assumptions

This project currently has no production server, production database, or deployed table setup.

- Treat production profile, database migration, index deployment, and EXPLAIN checks as learning and future-readiness topics unless the user explicitly says they are preparing an operational environment.
- Do not frame production-readiness work as urgent, blocking, or required for the current task before the user mentions creating or deploying a production server.
- In reviews, separate current local/test learning risks from future production considerations. Prioritize issues that affect the current code, tests, local execution, or stated learning goal.
- It is acceptable to briefly note future operational work when directly relevant, but do not repeatedly push it as an immediate improvement.

## Architecture

Use layer-first packages. The first package segment is the future module boundary.

```text
com.example.springbestpractice/
├── api/             # presentation: controller
│   └── {domain}/{Controller}
├── application/     # use case: service, request/response DTO, command
│   └── {domain}/{Service, dto/, command/}
├── domain/          # core domain: entity, domain exception
│   └── {domain}/
├── infrastructure/  # technical details: JPA repository, external adapters
│   └── {domain}/
└── common/          # cross-cutting concerns: global exception handling
    └── exception/
```

Dependency direction: `api -> application -> domain`, `infrastructure -> domain`, all layers may depend on `common`. Reverse dependencies are forbidden.

### Architecture Tiebreaker

Prefer the current layer-first monolith. Do not introduce hexagonal architecture or port/adapter packages as incidental cleanup.

It is acceptable for application services to use Spring Data repositories from `infrastructure` directly while this project remains a monolith.

Introduce outbound ports/interfaces only when:

- The task is an explicit architecture migration.
- The project is being split into Gradle modules and `application` must compile without depending on `infrastructure`.

## Database

Default runtime profile is `local` and uses MySQL. For local execution, either run `docker compose up --build` or provide `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, and `SPRING_DATASOURCE_PASSWORD`.

- Local default JDBC URL: `jdbc:mysql://localhost:3306/spring_best_practice`
- Docker Compose internal JDBC URL: `jdbc:mysql://mysql:3306/spring_best_practice`
- Test runtime: H2 in-memory
- Prod default DDL strategy: `validate`
