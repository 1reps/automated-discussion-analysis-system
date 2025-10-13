# automated-discussion-analysis-system

자동화된 대화 분석 시스템(Automated Discussion Analysis System)

`GPT Codex README.md 자동완성`

## 빠른 시작 (Docker Compose)
- 요구 사항
  - Docker, Docker Compose
- 실행
  - 프로젝트 루트에서 다음을 실행합니다.
    - `docker compose up -d --build`
  - 포함 서비스 및 포트
    - API: `http://localhost:8081`
    - MySQL: `localhost:3306` (DB: `adas`, USER: `secret`, PASSWORD: `verysecret`)
- 유용한 명령어
  - 로그 보기: `docker compose logs -f api`
  - 상태 확인: `docker compose ps`
  - 중지/정리: `docker compose down -v`

## JDK 설치 (SDKMAN!)
이 저장소의 `backend/api`는 Java 17로 빌드됩니다. 디렉터리 내 `.sdkmanrc`가 설정되어 있어, SDKMAN!의 auto-env 기능으로 손쉽게 버전을 맞출 수 있습니다.

- SDKMAN!이 설치되어 있다면
  - `cd backend/api`
  - `sdk env install`
  - `sdk current` → `java: 17.0.12-tem` 확인

- SDKMAN! 설치가 필요하다면
  - macOS/Linux: `curl -s "https://get.sdkman.io" | bash` 후 `source "$HOME/.sdkman/bin/sdkman-init.sh"`
  - Windows: WSL 사용을 권장합니다. 또는 Temurin JDK 17(https://adoptium.net/)을 설치하세요.

## 로컬 개발 (Docker 없이 API만 실행)
DB는 Docker로 띄우고, API는 Gradle Wrapper로 실행하는 흐름입니다.

1) DB만 실행
- `docker compose up -d db`

2) API 실행
- 필수 환경 변수 (기본값과 동일하게 설정 권장)
  - `DB_HOST=localhost`, `DB_PORT=3306`, `DB_NAME=adas`, `DB_PASSWORD=verysecret`

- macOS/Linux (bash/zsh 등)
  - `cd backend/api`
  - `sdk env install` (최초 1회)
  - `DB_HOST=localhost DB_PORT=3306 DB_NAME=adas DB_PASSWORD=verysecret ./gradlew bootRun`

- Windows PowerShell
  - `cd backend/api`
  - `sdk env install` (WSL에서 실행 권장) 또는 JDK 17 설치 후 진행
  - `$env:DB_HOST='localhost'; $env:DB_PORT='3306'; $env:DB_NAME='adas'; $env:DB_PASSWORD='verysecret'; ./gradlew.bat bootRun`

- 실행 후 접속: `http://localhost:8081`

## 구조 안내
- `backend/api`: Spring Boot 기반 API 서비스 (포트 8081)
- `docker-compose.yml`: MySQL(`db`)과 API(`api`) 서비스 정의
- `backend/diarization`, `backend/stt`: 추가 백엔드 컴포넌트(현재 compose에 주석 처리)

## 트러블슈팅
- 포트 충돌
  - 8081 또는 3306이 이미 사용 중이면 해당 프로세스를 종료하거나 compose 포트를 수정하세요.
- Docker 권한/네트워크 이슈
  - 로그로 원인 파악: `docker compose logs -f`
  - 컨테이너 재시작: `docker compose restart`
- SDKMAN! auto-env 동작 확인
  - `backend/api/.sdkmanrc`에 `java=17.0.12-tem`이 선언되어 있습니다.
  - 필요 시: `sdk use java 17.0.12-tem`

## 자주 쓰는 명령어 모음
- 전체 빌드/실행: `docker compose up -d --build`
- 중지/정리: `docker compose down -v`
- API 로그: `docker compose logs -f api`
- 테스트: `cd backend/api && ./gradlew test` (Windows: `gradlew.bat test`)
