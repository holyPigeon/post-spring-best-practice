# Commit Convention

## 형식

```text
type(scope): 한국어 설명
```

## 타입

| 타입 | 용도 |
|---|---|
| `feat` | 새로운 기능 추가 |
| `fix` | 버그 수정 |
| `docs` | 문서 수정 |
| `style` | 코드 포맷팅, 세미콜론 누락, 코드 변경이 없는 경우 |
| `refactor` | 리팩토링 |
| `test` | 테스트 코드 추가 및 리팩토링 |
| `chore` | 빌드 업무 수정, 패키지 매니저 수정, production code와 무관한 부분들 |
| `comment` | 주석 추가 및 변경 |
| `remove` | 파일, 폴더 삭제 |
| `rename` | 파일명, 폴더명 수정 |

## 스코프

- 변경된 도메인명 사용 (`post`, `user` 등)
- 특정 도메인에 국한되지 않으면 `global`
