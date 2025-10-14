• 아래 내용을 Spring Boot 리포의 README.md에 넣으면, 다음 단계와 Codex에게 요청할 작업이 명확해집니다.

개요

- 이 리포는 게이트웨이(API)(Spring Boot)와 두 개의 파이썬 마이크로서비스(STT, Diarization)를 함께 운 영합니다.
- 경량 기본값: STT=Whisper, Diarization=Rule 기반. 향후 Google STT/pyannote로 교체 가능.
- 모든 외부 API는 /api/v1 프리픽스 사용.

디렉터리

- src/stt: STT 서비스(FastAPI), stt_main.py, requirements.txt, Dockerfile
- src/diarization: Diarization 서비스(FastAPI), diar_main.py, requirements.txt, Dockerfile.gpu
- src/python-core/processing_core: 파이썬 공용 코어(설정/서비스/DTO)

환경 변수

- 루트 .env 예시
    - STT_PROVIDER=whisper
    - DIARIZATION_PROVIDER=rule (또는 pyannote + HF_TOKEN)
    - WHISPER_MODEL_SIZE=small, WHISPER_WORD_TIMESTAMPS=true
    - WORK_DIR=/tmp/ai-voice, ALLOWED_ORIGINS=*

Docker Compose(개발용)

- spring 리포 루트의 docker-compose.dev.yml 예시
    - api(Spring Boot): STT_URL=http://stt:8000/api/v1, DIAR_URL=http://diarization:8000/api/v1
    - stt: build.context=. / dockerfile=src/stt/Dockerfile, 포트 8001:8000
    - diarization: build.context=. / dockerfile=src/diarization/Dockerfile.gpu, 포트 8002:8000, GPU
      예약
- 주의: 각 Dockerfile의 COPY 경로는 Spring 리포 기준으로 되어야 함
    - STT Dockerfile: COPY src/python-core /opt/python-core, COPY src/stt /app
    - Diar Dockerfile: COPY src/python-core /opt/python-core, COPY src/diarization /app             
      게이트웨이(API) 작업(요청할 항목)

- 설정 추가
    - application.yml: stt.base-url, diar.base-url, 업로드 제한/타임아웃
    - .env 로딩(필요 시 Spring Dotenv 혹은 OS 환경변수)
- HTTP 클라이언트
    - WebClient Bean 구성: 연결/읽기 타임아웃 분리(STT 짧게, Diar 길게), 재시도는 네트워크 오류 한정
- 외부 클라이언트 구현
    - SttClient: POST {stt.base-url}/stt/transcribe 멀티파트(file, language) → {lang, words[]}
    - DiarClient: POST {diar.base-url}/diarize 멀티파트(file, language, max_speakers?) →
      {segments[]}
- 병합 서비스
    - SpeechMergeService: words[]와 segments[]를 겹침 기준으로 매칭 → turns[] = [{speaker, text,    
      start, end}]
    - 기본 로직: 겹치는 화자 구간을 찾아 STT 세그먼트에 스피커 태깅(중복 병합/경계 보정 최소화)
- 업로드 엔드포인트
    - POST /api/v1/media/process (게이트웨이 외부 API)
        - form: file, language?
        - 플로우: 파일 수신 → STT 호출 → Diar 호출 → 병합 → DB 저장 → 최종 JSON 반환
        - 응답: {recordingId, lang, words[], segments[], turns[]}
- DB 저장(간단 스키마)
    - recordings(id, source, language, size_bytes, duration_ms, created_at, ...)
    - transcripts(recording_id, start_ms, end_ms, text, confidence, language, provider)
    - diar_segments(recording_id, speaker_label, start_ms, end_ms, confidence)
    - speaker_turns(recording_id, speaker_label, start_ms, end_ms, text)
    - 트랜잭션: 최종 병합 후 한번에 커밋
- 예외/오류 처리
    - 400(유효성), 415(포맷), 502(Python 서비스 오류 래핑), 504(타임아웃)
    - idempotency 키(선택): 업로드 해시로 중복 처리 방지

테스트(요청할 항목)

- 단위 테스트
    - SpeechMergeService 병합 로직 케이스(경계/겹침/빈 결과)
- 통합 테스트
    - MockWebServer/WireMock으로 STT/Diar 응답 흉내 → API 엔드투엔드 검증
    - 대용량 파일 업로드 경계 테스트(시간 제한/메모리)
- 로컬 e2e(옵션)
    - docker compose -f docker-compose.dev.yml up --build 후 실제 파이썬 서비스와 연결

보안/운영(요청할 항목)

- 비밀/키 분리: Google STT 자격증명은 Spring 또는 STT 컨테이너에만 주입
- 헬스체크: /actuator/health(Spring), /health(파이썬) 연계
- 로깅/트레이싱: trace_id를 Python 호출 시 헤더로 전파, 응답에도 포함

정의역/계약(참고)

- STT 응답: { "lang": "ko", "words": [{ "start": 300, "end": 980, "text": "안녕하세요",             
  "confidence": 0.9 }] }
- Diar 응답: { "segments": [{ "start": 0, "end": 4200, "speaker": "SPEAKER_1" }] }
- 최종 응답: { "turns": [{ "speaker": "SPEAKER_1", "start": 300, "end": 980, "text": "안녕하세      
  요" }] }

이 README를 바탕으로, Spring Boot Codex에게 다음 구현을 순서대로 요청하면 됩니다:

1. WebClient 설정/프로퍼티 추가, 2) SttClient/DiarClient 구현, 3) 병합 서비스 구현, 4) 업로드 API 구
   현, 5) DB 스키마/리포지토리/트랜잭션, 6) 테스트 추가, 7) docker-compose.dev 연동 확인.   