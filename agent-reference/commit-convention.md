# Commit Convention

## Format

```text
type(scope): 한국어 설명
```

Examples:

```text
feat(post): 게시글 작성 기능 추가
fix(auth): 만료된 토큰 검증 오류 수정
docs(global): AI 작업 지침 정리
```

## Types

| Type | Use |
|---|---|
| `feat` | New feature |
| `fix` | Bug fix |
| `docs` | Documentation only |
| `style` | Formatting or style-only code changes |
| `refactor` | Refactor without behavior change |
| `test` | Test addition or test refactor |
| `chore` | Build, dependency, package manager, or non-production maintenance |
| `comment` | Comment-only additions or updates |
| `remove` | File or folder removal |
| `rename` | File or folder rename |

## Scope

- Use the changed domain name, for example `post`, `user`, `auth`, or `comment`.
- Use `global` when the change is cross-cutting or not domain-specific.
- Use `build`, `docs`, or `test` when that is clearer than a domain scope.

## Message Rules

- Write the subject in Korean.
- Keep the subject concise and specific.
- Do not end the subject with a period.
- Add a body only when the change has context, migration notes, risks, or follow-up work that the subject cannot express.
- When creating a commit, include only files related to the requested change.
