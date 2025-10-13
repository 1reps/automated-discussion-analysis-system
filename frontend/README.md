# Vue 3 + Vite

이 프로젝트는 pnpm을 기본 패키지 매니저로 사용합니다.

## 초기 설정

1) Corepack으로 pnpm 활성화

```
corepack enable
corepack prepare pnpm@9.12.3 --activate
```

2) 의존성 설치 (기존 npm 설치 흔적이 있으면 먼저 폴더 삭제)

```
rimraf node_modules
pnpm install
```

## 자주 쓰는 스크립트

```
pnpm dev       # 개발 서버
pnpm build     # 빌드
pnpm preview   # 미리보기
pnpm lint      # ESLint 체크
pnpm lint:fix  # 자동 수정
pnpm format    # Prettier 포맷
```

## 백엔드 연동 (API Base)

다음 중 하나를 선택하세요.

- 환경변수로 직접 호출
  - `.env`(또는 `.env.development`)에 `VITE_API_BASE=http://localhost:8080` 추가
  - Docker(백엔드 8081 노출) 사용 시 `VITE_API_BASE=http://localhost:8081`
  - 예시 파일: `env.example`를 복사해 `.env`로 사용

- Vite 프록시 사용(`/api` → 백엔드)
  - `vite.config.js`의 `server.proxy["/api"].target`을 백엔드 주소로 설정
  - 프론트 코드에서 `fetch('/api/...')`로 호출

IDE 팁: VS Code에서는 저장 시 Prettier로 자동 포맷되며(`.vscode/settings.json`), ESLint Flat Config + Vue 3 권장 규칙이 적용됩니다.
