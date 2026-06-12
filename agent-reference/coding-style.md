# Coding Style

Base: Toss backend style. Scale: small project with future multi-module migration.

## Layer Rules

- Keep the package structure and dependency direction from `agent-reference/project-context.md`.
- Controller handles HTTP concerns only. Application service orchestrates use cases. Domain owns business rules. Infrastructure owns persistence and external details.
- Do not put business logic in `common`.

## Entity And Domain Rules

- No setters. Change state through domain methods such as `confirm()` or `cancel()`.
- Use `@NoArgsConstructor(access = AccessLevel.PROTECTED)` for JPA entities.
- Use a private constructor plus a named static factory such as `Post.create(...)`.
- Use `@Enumerated(EnumType.STRING)`, never `ORDINAL`.
- Add `@Column(nullable = false)` to every non-null field.
- Validate business invariants inside the entity or domain object, not in the service.

## Defensive Checks And Invariants

Prefer making invalid states unrepresentable over adding defensive checks everywhere.

Do not add redundant null checks when all of these are true:

- The constructor or method is private.
- The value is not accepted from request DTOs or external input.
- The value is supplied internally by static factories as a fixed enum or constant.
- There is no normal application code path that can pass null.

Still validate values from:

- Request DTOs
- Public methods
- Persistence hydration assumptions
- External APIs
- Message queues
- Security context
- Cross-module boundaries

Before adding a defensive check, ask:

> Can this invalid value occur through a normal code path, or should the structure make it impossible?

## JPA Relationship Rules

- Prefer object relationships when entities collaborate in domain behavior or share the same lifecycle.
- Use `@ManyToOne(fetch = FetchType.LAZY, optional = false)` for N:1 relationships. Use `@OneToOne` only for true 1:1 domain cardinality.
- Use ID references when the target is another aggregate/module boundary, changes on a different lifecycle, or must stay loosely coupled.
- Do not keep both `authorId` and `author` as a vague pair. If history is required, use an explicit snapshot field such as `authorNickname`.
- Do not accept ownership fields such as `authorId`, `userId`, `role`, or `author` from request DTOs. Derive them from the authenticated user in the application layer.
- For read-only screens, batch jobs, and high-volume queries, prefer explicit queries, fetch joins, entity graphs, or DTO projections over widening entity relationships for response convenience.

## Object Creation Rules

| Object type | Default | Builder policy |
|---|---|---|
| JPA Entity | private constructor plus named static factory | Do not add public/class-level `@Builder` |
| Value Object or `@Embeddable` | static factory | Do not use Builder by default |
| Request DTO record | canonical constructor | Do not use Builder |
| Response DTO record | `XxxResponse.from(entity)` | Do not use Builder for simple mapping |
| Application Command/Input | record constructor or named static factory | Builder allowed only with 5+ fields and 2+ optional fields or repeated same-type/boolean arguments |
| Config/Options object | static factory for required values | Builder allowed only with 5+ fields and 3+ optional config fields |

- Entity factories/constructors must validate all domain-required and non-null fields.
- If an existing entity has `@Builder`, avoid it in new production and test code. Remove it in a separate refactor when practical.
- Use a Mapper/Assembler or query projection when mapping 2+ aggregates, 3+ scalar extras, current-user permission flags, or high-volume list/search responses.

## Service And Transaction Rules

- Put `@Transactional(readOnly = true)` on service classes by default.
- Put `@Transactional` on write methods.
- Keep services thin. They coordinate repositories, domain methods, and external collaborators.
- Remember Spring AOP does not apply `@Transactional` to same-class self-invocation.

## DTO And Command Rules

- Request and response DTOs are Java `record`s.
- Keep request/response DTOs under `application/{domain}/dto` so `application` never depends on `api`.
- Response records may expose `XxxResponse.from(entity)` for simple mapping.
- Controllers must not return entities.
- Command records live under `application/{domain}/command` when introduced.
- Do not add `request.toEntity()` or `Entity.from(request)`.

Service may accept a Request DTO directly only when all conditions are true:

- The request body fields exactly match the use case input.
- The use case needs no authenticated user, path variable, tenant id, server-generated value, or permission-derived field.
- Only one controller endpoint calls the use case.
- The request does not need ownership/security fields.

Introduce an Application Command when any condition above is false. Build it in the controller with a factory such as `PostCreateCommand.from(request, loginUser, postId)`.

## Exception And DI Rules

- Use domain-specific exceptions such as `PostNotFoundException`.
- Handle API exceptions in `GlobalExceptionHandler` with `@RestControllerAdvice`.
- Keep the basic error body as `record ErrorResponse(String message)` until the project needs error codes or timestamps.
- Use constructor injection with `@RequiredArgsConstructor`. Do not use field injection.
- Prefer Spring REST Docs over Swagger annotations.

## Scale Rules

- Current structure is layer-first. When scaling, split `api`, `application`, `domain`, `infrastructure`, and `common` into Gradle modules.
- In the module target, `domain` must not depend on Spring/JPA.
- Do not move complexity into `common`; shared code must stay small and clearly cross-cutting.
