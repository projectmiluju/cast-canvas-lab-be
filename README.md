# CastCanvas Lab — Backend

CastCanvas Lab은 공간 기반 리서치 워크스페이스입니다.
PDF 문서와 레퍼런스 이미지를 무한 캔버스 위에 자유롭게 배치하고, 노트와 연결선으로 아이디어를 연결할 수 있습니다.

> Notion보다 더 공간적으로, Figma보다 더 문서 친화적으로.

---

## 서비스 구성

CastCanvas Lab은 다음 레포지토리로 구성됩니다.

| 레포                     | 역할                                 |
| ------------------------ | ------------------------------------ |
| `cast-canvas-lab-fe`     | 워크스페이스 프론트엔드 앱           |
| `cast-canvas-lab-be`     | 메인 백엔드 API 서버 (이 레포)       |
| `cast-canvas-lab-collab` | 실시간 협업 서버 (Yjs/WebSocket)     |
| `cast-canvas-lab-site`   | 퍼블릭 랜딩 사이트                   |

시스템 전체 구조와 레포 간 책임 경계는 [ARCHITECTURE.md](./ARCHITECTURE.md)를 참고하세요.

---

## 기술 스택

| 항목 | 버전 / 내용 |
| ---- | ----------- |
| Java | 17 |
| Spring Boot | 3.5.0 |
| Gradle | Groovy DSL |
| Spring Security | Spring Boot 관리 + JWT (jjwt 0.12.7) |
| Spring Data JPA | Spring Boot 관리 |
| PostgreSQL | 드라이버: Spring Boot 관리 |
| Redis | Spring Data Redis (Spring Boot 관리) |
| Flyway | flyway-database-postgresql (Spring Boot 관리) |
| OpenAPI / Swagger | springdoc-openapi 2.8.9 |
| Lombok | Spring Boot 관리 |

---

## 로컬 개발 환경 설정

**요구 사항:** JDK 17, Docker

**1. 인프라 실행 (PostgreSQL + Redis)**

```bash
docker compose up -d
```

**2. 시크릿 설정**

```bash
cp src/main/resources/application-local-secret.example.yml \
   src/main/resources/application-local-secret.yml
```

`application-local-secret.yml`을 열어 JWT secret 값을 채웁니다.

**3. 서버 실행**

```bash
./gradlew bootRun
```

기본 접속 정보 (변경 불필요):

| 항목 | 값 |
| ---- | -- |
| DB host | `localhost:5432` |
| DB name | `cast_canvas_lab` |
| DB user/password | `castcanvas` / `castcanvas` |
| Redis | `localhost:6379` |
| API | `http://localhost:8080` |
| Swagger UI | `http://localhost:8080/docs/swagger-ui` |

---

## 주요 명령어

| 명령어                          | 설명                        |
| ------------------------------- | --------------------------- |
| `./gradlew bootRun`             | 개발 서버 실행              |
| `./gradlew build`               | 빌드 및 테스트              |
| `./gradlew test`                | 테스트 실행                 |
| `./gradlew spotlessApply`       | 코드 포맷 자동 적용         |
| `./gradlew checkstyleMain`      | 코딩 컨벤션 검사            |

---

## 프로젝트 구조

```
src/main/java/com/castcanvaslab/api/
├── common/global/   # 공통 응답/에러, 보안 설정, JPA 설정
├── auth/            # JWT 인증, 회원가입/로그인
├── user/            # 사용자 프로필
├── workspace/       # 워크스페이스 CRUD, 멤버 관리
├── canvas/          # 캔버스 메타데이터
├── node/            # 노드 메타데이터
├── edge/            # 엣지 메타데이터
├── asset/           # S3 에셋, 서명된 업로드 URL
├── document/        # 문서 메타데이터, 처리 상태
├── search/          # 검색 API
└── job/             # 비동기 작업 오케스트레이션
```

각 모듈은 `domain / application / presentation / infrastructure` 4-레이어 구조를 따릅니다.

---

## 코드 기여 규칙

- 커밋 메시지는 [Conventional Commits](https://www.conventionalcommits.org/) 형식을 따릅니다
- 모든 Java 코드는 Google Java Format (AOSP, 들여쓰기 4칸)을 준수합니다
- 커밋 전 `spotlessApply`가 Git Hook으로 자동 실행됩니다
- 코드 수정 후 `checkstyleMain`으로 컨벤션 위반 여부를 확인하세요
