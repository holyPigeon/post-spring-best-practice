# Frontend API Guide

프론트엔드 연동을 위한 API 문서입니다. 현재 서버 코드의 Controller, DTO, Security 설정, 전역 예외 처리 기준으로 작성되었습니다.

## 공통 정보

- Base URL: `http://localhost:8080`
- 요청/응답 형식: JSON
- 요청 헤더:

```http
Content-Type: application/json
```

- 인증이 필요한 API 요청 헤더:

```http
Authorization: Bearer <accessToken>
```

- Access token 만료 시간: 30분
- Refresh token 만료 시간: 7일
- 날짜/시간 필드 형식: ISO-8601 문자열

```json
"2026-05-16T10:30:00"
```

### CORS

- 허용 Origin 기본값: `http://localhost:5173` (`cors.allowed-origins` 설정으로 변경 가능)
- 허용 메서드: `GET, POST, PUT, PATCH, DELETE, OPTIONS`
- 자격 증명(`allowCredentials`) 허용

## 인증/권한 정책

사용자는 `USER` 또는 `ADMIN` 권한(role)을 가집니다.

### 공개 API (인증 불필요)

- `POST /api/users` (회원가입)
- `POST /api/auth/login`
- `POST /api/auth/refresh`
- `POST /api/auth/logout`

### USER 또는 ADMIN 권한 필요

- `GET /api/auth/me`
- `/api/users/me` 이하 (내 정보 관련)
- `/api/posts` 이하 (게시글, 좋아요, 댓글 포함)

### ADMIN 권한 전용

- `/api/admin/**` (관리자용 유저 API)

위에 명시되지 않은 요청은 모두 거부됩니다.

## 공통 에러 응답

에러 응답 body는 공통으로 `message` 필드를 사용합니다.

```json
{
  "message": "에러 메시지"
}
```

주요 status code:

| Status | 의미 |
|---|---|
| `400 Bad Request` | 요청 값 검증 실패(Bean Validation), 파라미터 타입 불일치 |
| `401 Unauthorized` | 인증 실패, 로그인 실패, refresh token 오류 |
| `403 Forbidden` | 접근 권한 없음, 리소스 소유자가 아님 |
| `404 Not Found` | 요청한 리소스 없음 |
| `409 Conflict` | 중복 이메일, 중복/동시 처리된 요청 |

### 요청 값 검증(400) 동작

요청 본문에 Bean Validation 제약 조건이 적용되어 있습니다. 검증 실패 시 `400 Bad Request`와 함께 위반 메시지가 반환됩니다. 여러 필드가 동시에 실패하면 **필드명 알파벳 순으로 정렬한 첫 번째 메시지**가 반환됩니다.

각 필드의 제약 조건은 아래 API별 설명에 표기했습니다.

## Auth API

### 로그인

`POST /api/auth/login`

- 인증: 불필요
- 성공 status: `200 OK`

Request:

| 필드 | 타입 | 제약 |
|---|---|---|
| `email` | string | 필수, 이메일 형식, 100자 이하 |
| `password` | string | 필수 |

```json
{
  "email": "test@test.com",
  "password": "password"
}
```

Response:

```json
{
  "accessToken": "access-token",
  "refreshToken": "refresh-token"
}
```

Error:

| Status | 예시 메시지 |
|---|---|
| `401 Unauthorized` | `이메일 또는 비밀번호가 올바르지 않습니다.` |

### 토큰 재발급

`POST /api/auth/refresh`

- 인증: 불필요
- 성공 status: `200 OK`
- 요청한 refresh token은 재발급 과정에서 삭제되고, 새 refresh token이 발급됩니다.

Request:

| 필드 | 타입 | 제약 |
|---|---|---|
| `refreshToken` | string | 필수 |

```json
{
  "refreshToken": "refresh-token"
}
```

Response:

```json
{
  "accessToken": "new-access-token",
  "refreshToken": "new-refresh-token"
}
```

Error:

| Status | 예시 메시지 |
|---|---|
| `401 Unauthorized` | `유효하지 않은 리프레시 토큰입니다.` |
| `401 Unauthorized` | `만료된 리프레시 토큰입니다.` |

### 로그아웃

`POST /api/auth/logout`

- 인증: 불필요
- 성공 status: `204 No Content`
- 응답 body 없음
- 서버에 저장된 refresh token이 있으면 삭제합니다.

Request:

| 필드 | 타입 | 제약 |
|---|---|---|
| `refreshToken` | string | 필수 |

```json
{
  "refreshToken": "refresh-token"
}
```

Response:

```http
204 No Content
```

### 내 정보 조회

`GET /api/auth/me`

- 인증: 필요 (USER/ADMIN)
- 성공 status: `200 OK`

Response:

```json
{
  "id": 1,
  "email": "test@test.com",
  "nickname": "테스터"
}
```

## User API

### 회원가입

`POST /api/users`

- 인증: 불필요
- 성공 status: `201 Created`

Request:

| 필드 | 타입 | 제약 |
|---|---|---|
| `email` | string | 필수, 이메일 형식, 100자 이하 |
| `nickname` | string | 필수, 20자 이하 |
| `password` | string | 필수, 8자 이상 64자 이하 |

```json
{
  "email": "test@test.com",
  "nickname": "테스터",
  "password": "password"
}
```

Response:

```json
{
  "id": 1,
  "email": "test@test.com",
  "nickname": "테스터",
  "createdAt": "2026-05-16T10:30:00",
  "updatedAt": "2026-05-16T10:30:00"
}
```

Error:

| Status | 예시 메시지 |
|---|---|
| `409 Conflict` | `이미 사용 중인 이메일입니다. email=test@test.com` |

### 내 정보 조회

`GET /api/users/me`

- 인증: 필요 (USER/ADMIN)
- 성공 status: `200 OK`
- 토큰의 사용자 본인 정보를 반환합니다.

Response:

```json
{
  "id": 1,
  "email": "test@test.com",
  "nickname": "테스터",
  "createdAt": "2026-05-16T10:30:00",
  "updatedAt": "2026-05-16T10:30:00"
}
```

### 내 정보 수정

`PUT /api/users/me`

- 인증: 필요 (USER/ADMIN)
- 성공 status: `200 OK`

Request:

| 필드 | 타입 | 제약 |
|---|---|---|
| `nickname` | string | 필수, 20자 이하 |

```json
{
  "nickname": "새닉네임"
}
```

Response:

```json
{
  "id": 1,
  "email": "test@test.com",
  "nickname": "새닉네임",
  "createdAt": "2026-05-16T10:30:00",
  "updatedAt": "2026-05-16T10:35:00"
}
```

### 비밀번호 변경

`PATCH /api/users/me/password`

- 인증: 필요 (USER/ADMIN)
- 성공 status: `204 No Content`
- 응답 body 없음

Request:

| 필드 | 타입 | 제약 |
|---|---|---|
| `password` | string | 필수, 8자 이상 64자 이하 |

```json
{
  "password": "newpassword"
}
```

Response:

```http
204 No Content
```

### 회원 탈퇴

`DELETE /api/users/me`

- 인증: 필요 (USER/ADMIN)
- 성공 status: `204 No Content`
- 응답 body 없음
- 토큰의 사용자 본인 계정을 삭제합니다.

Response:

```http
204 No Content
```

## Admin User API

`/api/admin/**` 경로는 `ADMIN` 권한 전용입니다. 권한이 없으면 `403 Forbidden`이 반환됩니다.

### 유저 목록 조회

`GET /api/admin/users`

- 인증: 필요 (ADMIN)
- 성공 status: `200 OK`
- 페이징 없이 전체 목록을 배열로 반환합니다.

Response:

```json
[
  {
    "id": 1,
    "email": "test@test.com",
    "nickname": "테스터",
    "role": "USER",
    "createdAt": "2026-05-16T10:30:00",
    "updatedAt": "2026-05-16T10:30:00"
  }
]
```

### 유저 단건 조회

`GET /api/admin/users/{id}`

- 인증: 필요 (ADMIN)
- 성공 status: `200 OK`

Path variables:

| 이름 | 타입 | 설명 |
|---|---|---|
| `id` | number | 유저 ID |

Response:

```json
{
  "id": 1,
  "email": "test@test.com",
  "nickname": "테스터",
  "role": "USER",
  "createdAt": "2026-05-16T10:30:00",
  "updatedAt": "2026-05-16T10:30:00"
}
```

Error:

| Status | 예시 메시지 |
|---|---|
| `404 Not Found` | `유저를 찾을 수 없습니다. id=999` |

### 유저 삭제

`DELETE /api/admin/users/{id}`

- 인증: 필요 (ADMIN)
- 성공 status: `204 No Content`
- 응답 body 없음

Path variables:

| 이름 | 타입 | 설명 |
|---|---|---|
| `id` | number | 유저 ID |

Response:

```http
204 No Content
```

Error:

| Status | 예시 메시지 |
|---|---|
| `404 Not Found` | `유저를 찾을 수 없습니다. id=999` |

## Post API

### 게시글 생성

`POST /api/posts`

- 인증: 필요 (USER/ADMIN)
- 성공 status: `201 Created`
- 작성자는 토큰의 사용자로 자동 설정됩니다.

Request:

| 필드 | 타입 | 제약 |
|---|---|---|
| `title` | string | 필수, 100자 이하 |
| `content` | string | 필수, 5000자 이하 |

```json
{
  "title": "제목",
  "content": "내용"
}
```

Response:

```json
{
  "id": 1,
  "title": "제목",
  "content": "내용",
  "author": "테스터",
  "likeCount": 0,
  "createdAt": "2026-05-16T10:30:00",
  "updatedAt": "2026-05-16T10:30:00"
}
```

### 게시글 목록 조회 (페이징)

`GET /api/posts`

- 인증: 필요 (USER/ADMIN)
- 성공 status: `200 OK`
- 페이징된 결과를 반환합니다.

Query parameters:

| 이름 | 타입 | 기본값 | 제약 | 설명 |
|---|---|---|---|---|
| `page` | number | `0` | 0 이상 | 0부터 시작하는 페이지 번호 |
| `size` | number | `20` | 1 이상 100 이하 | 페이지 크기 |
| `sort` | string | `LATEST` | `LATEST` 또는 `OLDEST` | 정렬 기준 (생성일 기준 최신순/오래된순) |

Response:

```json
{
  "content": [
    {
      "id": 1,
      "title": "제목",
      "content": "내용",
      "author": "테스터",
      "likeCount": 3,
      "createdAt": "2026-05-16T10:30:00",
      "updatedAt": "2026-05-16T10:30:00"
    }
  ],
  "page": 0,
  "size": 20,
  "totalElements": 1,
  "totalPages": 1,
  "first": true,
  "last": true
}
```

### 게시글 단건 조회

`GET /api/posts/{id}`

- 인증: 필요 (USER/ADMIN)
- 성공 status: `200 OK`

Path variables:

| 이름 | 타입 | 설명 |
|---|---|---|
| `id` | number | 게시글 ID |

Response:

```json
{
  "id": 1,
  "title": "제목",
  "content": "내용",
  "author": "테스터",
  "likeCount": 3,
  "createdAt": "2026-05-16T10:30:00",
  "updatedAt": "2026-05-16T10:30:00"
}
```

Error:

| Status | 예시 메시지 |
|---|---|
| `404 Not Found` | `게시글을 찾을 수 없습니다. id=999` |

### 게시글 수정

`PUT /api/posts/{id}`

- 인증: 필요 (USER/ADMIN)
- 성공 status: `200 OK`
- 게시글 작성자만 수정할 수 있습니다.

Path variables:

| 이름 | 타입 | 설명 |
|---|---|---|
| `id` | number | 게시글 ID |

Request:

| 필드 | 타입 | 제약 |
|---|---|---|
| `title` | string | 필수, 100자 이하 |
| `content` | string | 필수, 5000자 이하 |

```json
{
  "title": "새 제목",
  "content": "새 내용"
}
```

Response:

```json
{
  "id": 1,
  "title": "새 제목",
  "content": "새 내용",
  "author": "테스터",
  "likeCount": 3,
  "createdAt": "2026-05-16T10:30:00",
  "updatedAt": "2026-05-16T10:35:00"
}
```

Error:

| Status | 예시 메시지 |
|---|---|
| `403 Forbidden` | `게시글 소유자가 아닙니다.` |
| `404 Not Found` | `게시글을 찾을 수 없습니다. id=999` |

### 게시글 삭제

`DELETE /api/posts/{id}`

- 인증: 필요 (USER/ADMIN)
- 성공 status: `204 No Content`
- 응답 body 없음
- 게시글 작성자만 삭제할 수 있습니다.

Path variables:

| 이름 | 타입 | 설명 |
|---|---|---|
| `id` | number | 게시글 ID |

Response:

```http
204 No Content
```

Error:

| Status | 예시 메시지 |
|---|---|
| `403 Forbidden` | `게시글 소유자가 아닙니다.` |
| `404 Not Found` | `게시글을 찾을 수 없습니다. id=999` |

## Post Like API

좋아요 API는 멱등(idempotent)하게 동작합니다. 이미 좋아요한 상태에서 다시 좋아요해도, 좋아요하지 않은 상태에서 취소해도 현재 상태와 좋아요 수를 그대로 반환합니다.

### 좋아요

`POST /api/posts/{postId}/likes`

- 인증: 필요 (USER/ADMIN)
- 성공 status: `200 OK`

Path variables:

| 이름 | 타입 | 설명 |
|---|---|---|
| `postId` | number | 게시글 ID |

Response:

```json
{
  "postId": 1,
  "likeCount": 4,
  "liked": true
}
```

Error:

| Status | 예시 메시지 |
|---|---|
| `404 Not Found` | `게시글을 찾을 수 없습니다. id=999` |
| `409 Conflict` | `이미 처리된 요청이거나 중복된 데이터입니다.` (동시 중복 요청 시) |

### 좋아요 취소

`DELETE /api/posts/{postId}/likes`

- 인증: 필요 (USER/ADMIN)
- 성공 status: `200 OK`

Path variables:

| 이름 | 타입 | 설명 |
|---|---|---|
| `postId` | number | 게시글 ID |

Response:

```json
{
  "postId": 1,
  "likeCount": 3,
  "liked": false
}
```

Error:

| Status | 예시 메시지 |
|---|---|
| `404 Not Found` | `게시글을 찾을 수 없습니다. id=999` |

## Comment API

### 댓글 생성

`POST /api/posts/{postId}/comments`

- 인증: 필요 (USER/ADMIN)
- 성공 status: `201 Created`
- 작성자는 토큰의 사용자로 자동 설정됩니다.

Path variables:

| 이름 | 타입 | 설명 |
|---|---|---|
| `postId` | number | 게시글 ID |

Request:

| 필드 | 타입 | 제약 |
|---|---|---|
| `content` | string | 필수, 1000자 이하 |

```json
{
  "content": "댓글 내용"
}
```

Response:

```json
{
  "id": 1,
  "postId": 1,
  "content": "댓글 내용",
  "author": "테스터",
  "createdAt": "2026-05-16T10:30:00",
  "updatedAt": "2026-05-16T10:30:00"
}
```

Error:

| Status | 예시 메시지 |
|---|---|
| `404 Not Found` | `게시글을 찾을 수 없습니다. id=999` |

### 댓글 목록 조회

`GET /api/posts/{postId}/comments`

- 인증: 필요 (USER/ADMIN)
- 성공 status: `200 OK`
- 페이징 없이 ID 오름차순으로 전체 목록을 배열로 반환합니다.

Path variables:

| 이름 | 타입 | 설명 |
|---|---|---|
| `postId` | number | 게시글 ID |

Response:

```json
[
  {
    "id": 1,
    "postId": 1,
    "content": "댓글 내용",
    "author": "테스터",
    "createdAt": "2026-05-16T10:30:00",
    "updatedAt": "2026-05-16T10:30:00"
  }
]
```

Error:

| Status | 예시 메시지 |
|---|---|
| `404 Not Found` | `게시글을 찾을 수 없습니다. id=999` |

### 댓글 단건 조회

`GET /api/posts/{postId}/comments/{commentId}`

- 인증: 필요 (USER/ADMIN)
- 성공 status: `200 OK`

Path variables:

| 이름 | 타입 | 설명 |
|---|---|---|
| `postId` | number | 게시글 ID |
| `commentId` | number | 댓글 ID |

Response:

```json
{
  "id": 1,
  "postId": 1,
  "content": "댓글 내용",
  "author": "테스터",
  "createdAt": "2026-05-16T10:30:00",
  "updatedAt": "2026-05-16T10:30:00"
}
```

Error:

| Status | 예시 메시지 |
|---|---|
| `404 Not Found` | `게시글을 찾을 수 없습니다. id=999` |
| `404 Not Found` | `댓글을 찾을 수 없습니다. id=999` |

### 댓글 수정

`PUT /api/posts/{postId}/comments/{commentId}`

- 인증: 필요 (USER/ADMIN)
- 성공 status: `200 OK`
- 댓글 작성자만 수정할 수 있습니다.

Path variables:

| 이름 | 타입 | 설명 |
|---|---|---|
| `postId` | number | 게시글 ID |
| `commentId` | number | 댓글 ID |

Request:

| 필드 | 타입 | 제약 |
|---|---|---|
| `content` | string | 필수, 1000자 이하 |

```json
{
  "content": "수정한 댓글 내용"
}
```

Response:

```json
{
  "id": 1,
  "postId": 1,
  "content": "수정한 댓글 내용",
  "author": "테스터",
  "createdAt": "2026-05-16T10:30:00",
  "updatedAt": "2026-05-16T10:35:00"
}
```

Error:

| Status | 예시 메시지 |
|---|---|
| `403 Forbidden` | `댓글 소유자가 아닙니다.` |
| `404 Not Found` | `게시글을 찾을 수 없습니다. id=999` |
| `404 Not Found` | `댓글을 찾을 수 없습니다. id=999` |

### 댓글 삭제

`DELETE /api/posts/{postId}/comments/{commentId}`

- 인증: 필요 (USER/ADMIN)
- 성공 status: `204 No Content`
- 응답 body 없음
- 댓글 작성자만 삭제할 수 있습니다.

Path variables:

| 이름 | 타입 | 설명 |
|---|---|---|
| `postId` | number | 게시글 ID |
| `commentId` | number | 댓글 ID |

Response:

```http
204 No Content
```

Error:

| Status | 예시 메시지 |
|---|---|
| `403 Forbidden` | `댓글 소유자가 아닙니다.` |
| `404 Not Found` | `게시글을 찾을 수 없습니다. id=999` |
| `404 Not Found` | `댓글을 찾을 수 없습니다. id=999` |

## 프론트엔드 구현 참고

- access token은 인증 필요 API 호출 시 `Authorization` 헤더에 넣습니다.
- access token이 만료되어 `401 Unauthorized`가 발생하면 `POST /api/auth/refresh`로 토큰을 재발급받은 뒤 원래 요청을 재시도할 수 있습니다.
- refresh token 재발급에 성공하면 기존 refresh token 대신 응답으로 받은 새 refresh token을 저장해야 합니다.
- 로그아웃 시 `POST /api/auth/logout`에 refresh token을 전달하고, 프론트엔드에 저장된 access token과 refresh token을 함께 삭제합니다.
- 게시글 목록은 페이징 응답입니다. `page`/`size`/`sort` 쿼리 파라미터로 조회하고, 응답의 `totalPages`, `last` 등을 활용해 페이지네이션 UI를 구성합니다.
- 좋아요/좋아요 취소는 멱등하게 동작하므로, 응답의 `liked`와 `likeCount`로 화면 상태를 갱신하면 됩니다.
