# Coding Style — Claude Instruction Set

Base: Toss backend style. Scale: small project, multi-module migration planned.

## Package Structure
```
api/{domain}/          Controller, Request/Response DTO
application/{domain}/  Service (use case orchestration)
domain/{domain}/       Entity, domain exceptions
infrastructure/{domain}/ JPA Repository
common/exception/      GlobalExceptionHandler
```
Dependency direction (never reverse): `api → application → domain`, `infrastructure → domain`, all → `common`

## Entity Rules
- NEVER setter. State changes via domain methods only (e.g. `confirm()`, `cancel()`)
- `@NoArgsConstructor(access = PROTECTED)` always
- private constructor + `public static XxxName(...)` static factory
- `@Enumerated(EnumType.STRING)` always — never ORDINAL
- `@Column(nullable = false)` on all non-null fields
- Business rule validation inside entity, not service

```java
public void confirm() {
    if (status != PENDING) throw new IllegalStateException("...");
    this.status = CONFIRMED;
}
```

## Service Rules
- Class-level `@Transactional(readOnly = true)`, write methods get `@Transactional`
- Service = thin orchestrator only. Delegate logic to entity
- WARN: `@Transactional` does NOT apply to same-class self-invocation (Spring AOP proxy)

## DTO Rules
- Request/Response: Java `record`
- `XxxResponse.from(Entity)` static factory in Response record
- Never return Entity from Controller

## Exception Rules
- Domain-specific exception per entity (e.g. `PostNotFoundException`)
- All handled in `GlobalExceptionHandler` via `@RestControllerAdvice`
- Response body: `record ErrorResponse(String message)` — not `Map`

## DI Rules
- Constructor injection only via `@RequiredArgsConstructor`
- Never `@Autowired` field injection

## Test Strategy
| Layer | Annotation | Scope |
|---|---|---|
| Controller | `@WebMvcTest` | HTTP, serialization, status |
| Service | plain unit test | mock repo, business logic |
| Repository | `@DataJpaTest` | query correctness |
| E2E | `@SpringBootTest` | critical paths only |

Prefer Spring REST Docs over Swagger annotations.

## Scale Thresholds
| Now | When scaling |
|---|---|
| Layer-first packages (api/application/domain/infrastructure/common) | Separate into actual Gradle submodules |
| `ErrorResponse(String message)` | Add error code + timestamp |
| Unit tests on core logic | ATDD + coverage enforcement |
| Single DB `@Transactional` | Saga pattern |

## Multi-module Target Structure (reference only)
```
-api            Controller, Request/Response DTO
-application    Service (use case orchestration)
-domain         Entity, domain exceptions — NO Spring/JPA deps
-infrastructure JPA Repository, external APIs
-common         GlobalExceptionHandler, shared utilities
```
Never accumulate business logic in common module.
