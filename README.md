# spring-best-practice

`spring-best-practice`는 게시판 서비스를 소재로 Spring 백엔드 애플리케이션의 좋은 구조와 구현 방식을 연습하기 위한 프로젝트입니다.

단순히 게시글과 사용자를 CRUD로 다루는 데서 끝내지 않고, 기능이 늘어나도 유지보수하기 쉬운 코드가 되려면 어떤 책임을 어디에 두어야 하는지 고민하는 데 초점을 둡니다. Spring을 "동작하게 만드는 것"에서 한 걸음 더 나아가, 계층 분리, 도메인 모델링, 예외 처리, 인증, 테스트 같은 기본기를 일관된 방식으로 적용해보는 것이 목적입니다.

## 프로젝트 의도

게시판은 익숙하고 단순한 도메인이지만, 백엔드 애플리케이션의 핵심 요소를 연습하기에 충분한 주제를 제공합니다. 사용자를 만들고, 게시글을 작성하고, 요청을 검증하고, 인증된 사용자를 다루고, 예외를 응답으로 변환하는 흐름 안에서 Spring 애플리케이션의 기본 설계를 반복해서 점검할 수 있습니다.

이 프로젝트는 특정 기능을 많이 넣는 것보다, 작은 기능이라도 명확한 책임과 예측 가능한 구조로 구현하는 것을 중요하게 봅니다.

## 다루는 관심사

- API, 애플리케이션, 도메인, 인프라 계층의 역할 분리
- 게시글과 사용자 도메인을 중심으로 한 기본 비즈니스 로직 구성
- 전역 예외 처리와 일관된 오류 응답
- Spring Security 기반 인증 흐름
- 테스트를 통한 계층별 동작 검증
- 기능 추가 시에도 읽기 쉬운 패키지 구조와 코드 흐름 유지

## 지향점

이 프로젝트의 목표는 완성된 게시판 서비스를 만드는 것보다, Spring 백엔드 프로젝트를 설계하고 확장할 때 기준이 될 수 있는 작은 예제를 만드는 것입니다.

새로운 기능을 추가하더라도 컨트롤러가 모든 책임을 갖지 않고, 서비스가 유스케이스를 조율하며, 도메인과 인프라가 각자의 역할 안에서 변경되도록 구성하는 방향을 지향합니다.

결과적으로 `spring-best-practice`는 Spring을 사용하는 방식에 대한 개인적인 실험이자, 더 나은 백엔드 구조를 연습하기 위한 기준점입니다.

## Docker Compose 실행

로컬에서 Spring 애플리케이션과 MySQL을 함께 실행하려면 Docker Compose를 사용할 수 있습니다. 기본 프로파일은 `local`이며, Hibernate DDL 전략은 `update`입니다.

```bash
cp .env.example .env
docker compose up --build
```

애플리케이션은 기본적으로 `http://localhost:8080`에서 실행되고, MySQL은 로컬 `3306` 포트로 노출됩니다.

```bash
docker compose logs -f spring
docker compose down
```

헬스체크는 Actuator health endpoint를 사용합니다.

```bash
curl http://localhost:8080/actuator/health
```

MySQL 데이터는 `mysql-data` Docker volume에 저장됩니다. 데이터를 포함해 완전히 초기화하려면 다음 명령을 사용합니다.

```bash
docker compose down -v
```

기본 접속 정보는 다음과 같습니다.

| 항목 | 기본값 |
|---|---|
| Database | `spring_best_practice` |
| User | `spring` |
| Password | `spring` |
| Root password | `root` |
| JDBC URL | `jdbc:mysql://mysql:3306/spring_best_practice` |

주요 환경변수는 `.env`에서 변경할 수 있습니다.

| 환경변수 | 설명 |
|---|---|
| `SPRING_PROFILES_ACTIVE` | Spring 활성 프로파일. 로컬 기본값은 `local`, 운영은 `prod` |
| `APP_PORT` | 호스트에 노출할 Spring 애플리케이션 포트 |
| `SERVER_PORT` | 컨테이너 내부 Spring 애플리케이션 포트 |
| `MYSQL_PORT` | 호스트에 노출할 MySQL 포트 |
| `JAVA_TOOL_OPTIONS` | 컨테이너 JVM 옵션 |
| `MYSQL_DATABASE` | MySQL 초기 데이터베이스 이름 |
| `MYSQL_USER` | MySQL 애플리케이션 사용자 |
| `MYSQL_PASSWORD` | MySQL 애플리케이션 사용자 비밀번호 |
| `MYSQL_ROOT_PASSWORD` | MySQL root 비밀번호 |
| `SPRING_DATASOURCE_URL` | Spring datasource JDBC URL |
| `SPRING_DATASOURCE_USERNAME` | Spring datasource 사용자 |
| `SPRING_DATASOURCE_PASSWORD` | Spring datasource 비밀번호 |
| `SPRING_JPA_HIBERNATE_DDL_AUTO` | Hibernate DDL 전략 |
| `DB_POOL_MAX_SIZE` | HikariCP maximum pool size |
| `DB_POOL_MIN_IDLE` | HikariCP minimum idle connection 수 |
| `DB_CONNECTION_TIMEOUT` | HikariCP connection timeout(ms) |
| `DB_IDLE_TIMEOUT` | HikariCP idle timeout(ms) |
| `DB_MAX_LIFETIME` | HikariCP max lifetime(ms) |
| `JWT_SECRET` | JWT 서명 secret |
| `JWT_ACCESS_TOKEN_EXPIRATION` | access token 만료 시간(ms) |
| `JWT_REFRESH_TOKEN_EXPIRATION` | refresh token 만료 시간(ms) |

운영 배포에서는 `SPRING_PROFILES_ACTIVE=prod`를 사용합니다. `prod` 프로파일은 DB 접속 정보와 JWT 설정을 기본값 없이 요구하며, Hibernate DDL 전략은 기본적으로 `validate`입니다. 운영 DB 스키마는 애플리케이션 실행 전에 별도로 준비되어 있어야 합니다.
