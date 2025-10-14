# 용어 설명 (Glossary)

## STT (Speech-to-Text)

- 음성 데이터를 텍스트로 변환하는 기술입니다.
- 현재는 Whisper(faster-whisper)를 사용하며, 향후 Google STT 등 다른 공급자로 교체할 수 있습니다.

## Pyannote (화자 분리)

- 녹음된 대화에서 어떤 사람이 언제 말했는지를 구분해 주는 라이브러리입니다.
- 파이프라인에서는 `DiarizationService`가 Pyannote 모델을 실행해 화자 세그먼트를 생성합니다.

## ffmpeg (오디오 변환/정규화)

- 다양한 오디오/비디오 포맷을 변환할 수 있는 CLI 도구입니다.
- 업로드된 파일을 공통 포맷(샘플레이트, 비트뎁스)으로 맞추거나 길이를 계산할 때 사용합니다.

## Whisper / faster-whisper

- OpenAI Whisper 모델을 CTranslate2로 최적화한 `faster-whisper` 라이브러리를 사용합니다.
- GPU가 있으면 CUDA로, 없으면 CPU 모드로 동작하며, Google STT 등 다른 공급자로 교체할 수 있도록 인터페이스를 추상화했습니다.

## Recording 테이블

- 하나의 업로드(녹음) 단위를 표현합니다.
- 상태(`pending`, `processing`, `completed`, `failed`), 파일 경로, 용량, 언어 등 메타데이터가 들어갑니다.

## DiarSegment 테이블

- Pyannote가 생성한 화자 구간 정보를 저장합니다.
- `speaker_label`, `user_id`, `user_name`, `start_ms`, `end_ms`, `confidence`가 포함되어 어떤 사람이 언제 말했는지
  확인할 수 있습니다.

## TranscriptSegment 테이블

- STT 결과를 저장하는 테이블입니다.
- 공급자(`provider`), 텍스트, 시작/종료 시각, 신뢰도(`confidence`) 등이 포함됩니다.

## SpeakerTurn 테이블

- 화자 분리와 전사 결과를 결합한 최종 발화 단위입니다.
- `user_id`, `user_name` 테스트 매핑을 포함해 향후 사용자 계정과 연결할 수 있습니다.

## RecordingPipeline

- 업로드 후 실행되는 비동기 파이프라인 개념입니다.
- 현재 리포는 Processing-Only 변형으로 결과를 즉시 JSON으로 반환하도록 구성되어 있습니다.

## BackgroundTasks (FastAPI)

- 요청 처리가 끝난 뒤 별도의 스레드에서 작업을 수행하도록 도와주는 FastAPI 기능입니다.
- `/ingest/audio`에서 파이프라인 실행을 큐에 넣을 때 사용합니다.

## CM-700USB

- 현재 테스트용으로 사용하는 외부 USB 마이크 모델입니다. 브라우저 테스트 페이지에서 기본 소스로 지정합니다.

## Google STT (스캐폴드)

- Google Cloud Speech-to-Text 연동을 위한 골격 코드가 준비되어 있습니다.
- 현재는 미구현 상태로 NotImplementedError를 발생시키며, 향후 자격 증명/설정을 채워 실 구현을 추가할 수 있습니다.
  \n## PipelineLog 테이블\n- 파이프라인 실행 단계별 로그를 저장합니다. (stage/status/message, timestamp)\n- 실패 원인 분석과 GPT
  프롬프트 구성 시 참고 데이터로 사용할 수 있습니다.\n\n## 사용자 매핑 (테스트)\n- 현재 파이프라인은 `SPEAKER_1`, `SPEAKER_2`를 테스트 사용자(
  `user-001`, `user-002`)로 매핑해 저장합니다.\n- 추후 실제 사용자 계정과 연동할 때 참조할 수 있는 구조입니다.\n
