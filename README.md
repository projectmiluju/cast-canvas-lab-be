# CastCanvas Lab Backend

CastCanvas Lab은 PDF 문서, 이미지, 노트를 하나의 무한 캔버스 위에서 연결해 리서치 흐름을 정리하는 공간형 워크스페이스입니다.

> Notion보다 더 공간적으로, Figma보다 더 문서 친화적으로.

## 프로젝트 개요

CastCanvas Lab은 아래 4개 레포지토리로 구성됩니다.

| 레포 | 역할 |
| --- | --- |
| `cast-canvas-lab-fe` | 워크스페이스 프론트엔드 |
| `cast-canvas-lab-be` | 메인 백엔드 API 서버 |
| `cast-canvas-lab-collab` | 실시간 협업 서버 |
| `cast-canvas-lab-site` | 퍼블릭 랜딩 사이트 |

시스템 경계와 레포 간 책임은 [ARCHITECTURE.md](./ARCHITECTURE.md)를 기준으로 유지합니다.

## 이 레포의 역할

`cast-canvas-lab-be`는 제품의 중심 API 서버입니다. 아키텍처 기준으로는 사용자 인증, 워크스페이스 및 멤버 관리, 캔버스 메타데이터, 문서 및 에셋 메타데이터, 검색, 비동기 작업 오케스트레이션을 담당합니다.

아키텍처상 이 레포가 담당하는 범위:

- 회원가입, 로그인, JWT 인증
- 사용자 프로필과 워크스페이스 관리
- 캔버스, 노드, 엣지 메타데이터 API
- 문서/에셋 메타데이터 및 업로드 연동
- 향후 검색 API와 비동기 작업 orchestration

현재 구현된 범위:

- 공통 API 응답/에러 처리
- 시스템 상태 확인 API
- 사용자 프로필 조회 및 닉네임 수정 API
- PostgreSQL/Flyway 기반 기본 인프라 설정

MVP 기준으로 검색/인덱싱 기능은 제외 항목이며, 관련 모듈도 아직 구현되지 않았습니다.

이 레포가 담당하지 않는 범위:

- 브라우저 UI와 캔버스 렌더링
- Yjs 기반 실시간 동기화 전송 계층
- 퍼블릭 마케팅 페이지

## 기술 스택

| 항목 | 내용 |
| --- | --- |
| Language | Java 17 |
| Framework | Spring Boot 3.5.0 |
| Build | Gradle (Groovy DSL) |
| Security | Spring Security, JWT (`jjwt` 0.12.7) |
| Data | Spring Data JPA, PostgreSQL, Redis |
| Migration | Flyway |
| API Docs | springdoc-openapi 2.8.9 |
| Quality | Spotless, Checkstyle |

## 시작하기

요구 사항:

- JDK 17
- Docker

1. 인프라를 실행합니다.

```bash
docker compose up -d
```

2. 로컬 시크릿 파일을 생성합니다.

```bash
cp src/main/resources/application-local-secret.example.yml \
  src/main/resources/application-local-secret.yml
```

3. `src/main/resources/application-local-secret.yml`에 JWT secret 값을 채웁니다.

4. 개발 서버를 실행합니다.

```bash
./gradlew bootRun
```

기본 로컬 접속 정보:

| 항목 | 값 |
| --- | --- |
| API | `http://localhost:8080` |
| Swagger UI | `http://localhost:8080/docs/swagger-ui` |
| PostgreSQL | `localhost:5432` |
| DB Name | `cast_canvas_lab` |
| DB User / Password | `castcanvas` / `castcanvas` |
| Redis | `localhost:6379` |

## 주요 명령어

| 명령어 | 설명 |
| --- | --- |
| `./gradlew bootRun` | 개발 서버 실행 |
| `./gradlew build` | 빌드 및 테스트 |
| `./gradlew test` | 테스트 실행 |
| `./gradlew spotlessApply` | Java 포맷 자동 적용 |
| `./gradlew checkstyleMain` | 메인 소스 Checkstyle 검사 |

## 프로젝트 구조

```text
src/main/java/com/castcanvaslab/api/
├── common/global/   # 공통 응답, 예외 처리, 보안 및 JPA 설정
├── auth/            # 인증 및 JWT
├── user/            # 사용자 프로필
├── workspace/       # 워크스페이스와 멤버 관리
├── canvas/          # 캔버스 메타데이터
├── node/            # 노드 메타데이터
├── edge/            # 엣지 메타데이터
├── asset/           # 에셋 업로드와 저장소 연동
├── document/        # 문서 메타데이터와 처리 상태
├── search/          # 검색 API
└── job/             # 비동기 작업 orchestration
```

각 도메인 모듈은 `domain / application / presentation / infrastructure` 레이어 구조를 목표로 합니다.
현재는 `user` 모듈이 기준 구현에 가장 가깝고, 다른 모듈은 주로 스켈레톤 상태입니다.

## 협업 규칙

- 커밋 메시지는 [Conventional Commits](https://www.conventionalcommits.org/) 형식을 사용합니다.
- Java 코드는 Spotless 기준의 Google Java Format AOSP 스타일을 따릅니다.
- 코드 수정 후 `./gradlew spotlessApply`와 `./gradlew checkstyleMain`으로 포맷과 컨벤션을 확인합니다.
- 문서화된 API/아키텍처 설명은 [ARCHITECTURE.md](./ARCHITECTURE.md)와 충돌하지 않게 유지합니다.
