# Automated Discussion Analysis System (ADAS)

자동화된 대화 분석 시스템. Spring Boot(API Gateway) + Python STT/Diarization + Vite(Frontend)로 구성됩니다.

문서/요구/설계 세부는 `backend/api/src/docs/require/`의 codex_xxx 문서를 참고하세요.

## 빠른 시작(개발용, 전체 스택)

요구 사항
- Docker, Docker Compose

실행(루트에서)
- `docker compose -f docker-compose.dev.yml up --build`

서비스/포트
- API(Spring): `http://localhost:8081`
  - Swagger UI: `http://localhost:8081/docs`
  - API Docs(JSON): `http://localhost:8081/v3/api-docs`
- STT(FastAPI): `http://localhost:8001/api/v1/health`
- Diarization(FastAPI): `http://localhost:8002/api/v1/health`
- Frontend(Vite): `http://localhost:5173`

정리
- `docker compose -f docker-compose.dev.yml down -v`

## 로컬 개발(서비스 개별 실행)

Spring API(8080)
- JDK 17 필요(SDKMAN! 권장)
- macOS/Linux
  - `cd backend/api && sdk env install && ./gradlew bootRun`
- Windows(WSL 권장)
  - `cd backend/api && ./gradlew.bat bootRun`
- Swagger: `http://localhost:8080/docs`

Python STT(8001)
- `cd backend/api/src/stt`
- 가상환경 생성/활성화 후 `pip install -r requirements.txt`
- `PYTHONPATH=../python-core uvicorn stt_main:app --host 0.0.0.0 --port 8001`

Python Diarization(8002)
- `cd backend/api/src/diarization`
- 가상환경/설치 후 `PYTHONPATH=../python-core uvicorn diar_main:app --host 0.0.0.0 --port 8002`

Frontend(5173)
- `cd frontend && corepack enable && corepack prepare pnpm@9.12.3 --activate`
- `pnpm install && pnpm dev`
- 프록시(권장): `vite.config.js`의 `server.proxy['/api'].target` → `http://localhost:8080`
- 또는 `.env`의 `VITE_API_BASE=http://localhost:8080`

## 핵심 API

- 업로드(게이트웨이)
  - `POST /api/v1/media/process` (multipart: `file`, `language?`, `maxSpeakers?`)
  - 응답: `ApiResponse<{ recordingId, lang, words[], segments[], turns[] }>`

- 업로드(Frontend 호환)
  - `POST /api/ingest/audio` → `{ recording_id, transcript_url }`
  - `GET /api/v1/recordings/{id}/transcript` → `{ status, transcripts[], speaker_turns[] }`

- 조회
  - `GET /api/v1/recordings/{id}` / `{id}/segments` / `{id}/turns`

## 환경 변수(요약)

Spring(application.yml)
- `external.stt.base-url`: `http://localhost:8001/api/v1`
- `external.diarization.base-url`: `http://localhost:8002/api/v1`
- 타임아웃(ms): connect/response/read/write

Python(FastAPI)
- `WORK_DIR`, `ALLOWED_ORIGINS`, `STT_PROVIDER`, `DIARIZATION_PROVIDER`, `WHISPER_*` 등

## 테스트
- 백엔드 테스트: `cd backend/api && ./gradlew test` (Windows: `gradlew.bat test`)
- 포함: 병합 서비스 단위, 처리/조회 서비스 통합(Mock), WebMvc

## e2e 스모크(로컬)

무음 WAV 생성(1초)
```
python - << 'PY'
import wave
fr=8000
with wave.open('test.wav','w') as w:
  w.setnchannels(1); w.setsampwidth(2); w.setframerate(fr)
  w.writeframes(b'\x00\x00'*fr)
PY
```

게이트웨이 업로드
```
curl -X POST http://localhost:8080/api/v1/media/process \
  -F "file=@test.wav" -F "language=ko" -F "maxSpeakers=2" \
  -H "Accept: application/json"
```

Frontend 호환 업로드/폴링
```
curl -X POST http://localhost:8080/api/ingest/audio \
  -F "file=@test.wav" -F "language=ko" -H "Accept: application/json"
```
응답의 `transcript_url` GET
```
curl http://localhost:8080/api/v1/recordings/{recordingId}/transcript
```

## 구조
- backend/api
  - presentation: 컨트롤러, 전역 예외(ProblemDetail), Swagger 문서 인터페이스
  - application: 서비스/DTO/병합 로직
  - domain: 엔티티
  - infrastructure: 외부 클라이언트(WebClient), 리포지토리, 미디어 유틸(ffprobe)
  - docs/require: codex_xxx 작업 로그/요구/설계
- frontend: Vite + Vue(Recorder/샘플)

## 트러블슈팅
- 포트 충돌: 8080/8081/8001/8002/5173 사용 여부 확인
- Python 종속성: ffmpeg/ffprobe 필요(도커 이미지는 포함)
- CORS: Spring/WebClient 및 FastAPI의 `ALLOWED_ORIGINS` 확인

## 자주 쓰는 명령
- 전체 개발 스택: `docker compose -f docker-compose.dev.yml up --build`
- 중지/정리: `docker compose -f docker-compose.dev.yml down -v`
- API 테스트: `cd backend/api && ./gradlew test`
