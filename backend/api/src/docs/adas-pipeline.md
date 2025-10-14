# AI ADAS 음성 파이프라인 (Processing-Only)

## 개요
- CM-700USB 마이크 또는 Siri 연동 장치에서 음성을 수집하여 서버에 업로드합니다.
- 업로드된 오디오를 저장하고, 화자 분리(Pyannote)와 STT(Whisper/Google STT)를 처리합니다.
- 이 변형은 DB를 사용하지 않으며 처리 결과를 JSON으로 즉시 반환합니다.
- 전체 구조와 확장 계획은 `docs/architecture.md` 를 참고하세요.

## 빠른 시작
1. **환경 변수 설정**: 리포 루트의 `.env` 파일을 열어 프로바이더/경로를 설정합니다.
2. **의존성 설치 (선택)**
   ```bash
   pip install -r requirements.txt
   ```
3. **서버 실행 (Docker/CPU)**
   ```bash
   docker build -t ai-adas:cpu -f Dockerfile .
   docker run --rm -p 8000:8000 --name ai-adas ai-adas:cpu
   ```
   GPU 환경:
   ```bash
   docker build -t ai-adas:gpu -f Dockerfile.gpu .
   docker run --gpus all --rm -p 8000:8000 -v $PWD:/app -v hf_cache:/app/.cache/huggingface --name ai-adas ai-adas:gpu
   ```
5. 브라우저에서 [http://localhost:8000/docs](http://localhost:8000/docs) 로 이동해 OpenAPI 문서를 확인합니다.

## Whisper 설정
- 기본 STT 프로바이더는 faster-whisper입니다. `pip install -r requirements.txt`를 실행하면 필요한 `faster-whisper`, `ctranslate2`, `tokenizers`가 함께 설치됩니다.
- `.env`에서 다음 값을 조정할 수 있습니다.
    - `STT_PROVIDER=whisper` : Whisper 사용 (패키지 설치 필요).
    - `WHISPER_MODEL_SIZE` : `tiny`/`base`/`small`/`medium`/`large` 등 모델 사이즈.
    - `WHISPER_DEVICE` : `auto`(기본), `cpu`, `cuda` 중 선택.
    - `WHISPER_COMPUTE_TYPE` : `auto`, `int8`, `float16` 등 정밀도.
    - `WHISPER_BEAM_SIZE`, `WHISPER_VAD_FILTER`, `WHISPER_CONDITION_ON_PREVIOUS_TEXT` 도 필요에 따라 조정.
- CUDA 환경이라면 requirements.gpu.txt와 Dockerfile.gpu를 참고해 torch/cuDNN을 설치하세요.

## 사용자 매핑 가이드
- Pyannote/Rule 결과의 `speaker_label`을 후처리 단계에서 사내 사용자 매핑 테이블과 연결하는 것을 권장합니다.

## Google STT 스캐폴드
- `STT_PROVIDER=google`로 전환하면 아직 구현되지 않은 `GoogleSTTProvider`가 사용됩니다 (NotImplementedError 반환).
- `.env`에서 `GOOGLE_PROJECT_ID`, `GOOGLE_CREDENTIALS`, `GOOGLE_LANGUAGE`, `GOOGLE_SAMPLE_RATE`를 채워두면 추후 구현 시 바로 활용할 수 있습니다.
## 주요 엔드포인트 (Processing-Only)
- `POST /process/audio` : 오디오 업로드(파일, 언어 힌트) → JSON 결과 즉시 반환.
- `GET /health` : 서버 헬스체크.

## 동작 흐름 요약 (Processing-Only)
1. 클라이언트가 `/process/audio` 에 오디오를 업로드합니다.
2. 서버는 워크 디렉터리(`settings.WORK_DIR`)에 원본 파일을 저장하고 WAV(16k mono)로 변환합니다.
3. 화자 분리(Pyannote 또는 Rule) → STT(Whisper 또는 Google) → Alignment 순으로 처리합니다.
4. 처리 결과(화자 세그먼트, 전사 세그먼트, 스피커 턴)를 JSON으로 반환합니다.

## Spring Boot 연동 팁
- Spring Boot에서 파일 업로드 수신 → Python `/process/audio` 호출 → JSON 수신 후 트랜잭션/영속화는 Spring에서 처리.

## 테스트 방법
- 터미널 업로드(예):
  ```bash
  curl -F "file=@sample.wav" -F "language=ko" http://localhost:8000/process/audio | jq .
  ```
- 화자 분리만 검증(Pyannote):
  ```bash
  python scripts/run_diarization.py sample.wav --json
  ```

## GPT 연동 로드맵
1. 녹음/세그먼트 데이터에서 사용자별 질문용 프롬프트를 구성하는 `ConversationService`를 추가합니다.
2. OpenAI/GPT API 클라이언트를 래핑한 `app/services/llm.py`를 구현하고, STT 결과를 기반으로 질의 응답을 수행합니다.
3. `POST /recordings/{id}/ask` 같은 엔드포인트를 통해 특정 녹음을 요약하거나 질문하도록 확장합니다.
4. 결과를 `answers` 테이블에 저장하고, 프론트엔드에서 질의 이력과 함께 표시합니다.

## 용어 & 참고 자료
- 주요 용어 정리는 `docs/glossary.md`에서 확인할 수 있습니다.

## 개발 노트
- Processing-Only 변형으로 DB 없이 JSON 결과를 반환합니다.
- 향후 필요 시 큐/객체 스토리지 연동으로 비동기 파이프라인을 확장할 수 있습니다.
- 구조와 향후 계획은 `docs/architecture.md`에서 자세히 설명합니다.

## 경량 Diarization (Rule 기반)
- `DIARIZATION_PROVIDER=rule` 설정 시, 표준 라이브러리만 사용하는 경량 룰 기반 화자 분리를 사용합니다.
- 에너지(RMS) 기반 VAD로 발화 구간을 검출하고, 세그먼트를 교대로 화자에 할당합니다(정확한 화자 인식 아님).
- GPU/torch 의존성이 없어 CPU-only 환경에서 빠르게 동작합니다.

## GPU / Whisper 사용 시
- GPU 환경에서는 `requirements.gpu.txt` 를 참고해 `torch` 및 `faster-whisper`, `pyannote` 패키지를 설치해야 합니다.
- 컨테이너 내부 GPU 확인:
  ```bash
  docker exec -it <api-container> bash
  python3 - <<'PY'
  import torch
  print("cuda_avail:", torch.cuda.is_available())
  print("device_count:", torch.cuda.device_count())
  print("name:", torch.cuda.get_device_name(0) if torch.cuda.is_available() else None)
  PY
  ```
