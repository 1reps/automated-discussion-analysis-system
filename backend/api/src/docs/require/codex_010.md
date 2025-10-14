# Codex 010 — docker-compose.dev 추가 및 연동 가이드

목표
- Spring Boot(API), Python(STT/Diarization), Frontend(Vite) 로컬 개발용 docker-compose 제공
- Python Dockerfile의 COPY 경로를 리포 구조에 맞게 정리
- Spring ↔ Python 베이스 URL 환경변수 연결

추가/변경 파일
- docker-compose.dev.yml
  - api(8081→8080), stt(8001→8000), diarization(8002→8000), frontend(5173)
  - API 환경변수: `EXTERNAL_STT_BASE_URL=http://stt:8000/api/v1`, `EXTERNAL_DIARIZATION_BASE_URL=http://diarization:8000/api/v1`
- backend/api/src/stt/Dockerfile
  - COPY 경로: `backend/api/src/python-core` → `/opt/python-core`, `backend/api/src/stt` → `/app`
- backend/api/src/diarization/Dockerfile.gpu
  - COPY 경로: `backend/api/src/python-core` → `/opt/python-core`, `backend/api/src/diarization` → `/app`
- frontend/Dockerfile
  - pnpm 기반 Vite dev 서버(5173) 실행

사용 방법
1) 도커 네트워크/서비스 기동
```
docker compose -f docker-compose.dev.yml up --build
```
2) 확인
- Spring API: http://localhost:8081/sample (문자열 OK)
- STT: http://localhost:8001/api/v1/health
- Diarization: http://localhost:8002/api/v1/health
- Frontend: http://localhost:5173
3) 프런트 업로드 흐름
- Recorder.vue는 `/ingest/audio`로 업로드 후 `transcript_url`을 폴링
- 백엔드가 `/api/ingest/audio`와 `/api/v1/recordings/{id}/transcript`를 제공하므로 바로 연동됨

비고
- GPU 환경에서 diarization 컨테이너의 런타임 옵션(nvidia) 추가 필요 시 compose에 `deploy/resources/reservations/devices` 또는 `--gpus` 옵션을 수동 추가하세요.
- ffprobe가 Python 컨테이너에 설치되어 있으므로 Recording.duration_ms 계산 가능(파일 형식에 따라 null일 수 있음)
