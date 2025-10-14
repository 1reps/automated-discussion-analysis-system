# Codex 011 — Swagger 문서 분리(인터페이스) + 컨트롤러 응답 통일

목표
- Swagger 애노테이션을 전용 인터페이스로 분리해 import 충돌(ApiResponse) 제거
- 컨트롤러는 비즈니스/매핑만 유지하고, 응답은 `ResponseEntity<ApiResponse<T>>`로 통일(호환 API 제외)

변경 사항
- 문서 전용 인터페이스 추가
  - `com.adas.presentation.docs.MediaApiDocs`
  - `com.adas.presentation.docs.RecordingApiDocs`
  - `com.adas.presentation.docs.IngestApiDocs`
  - `com.adas.presentation.docs.TranscriptApiDocs`
  - 인터페이스 내부에서만 Swagger 애노테이션(@Operation, @ApiResponses) 사용
  - Swagger `@ApiResponse`는 FQN 사용, 우리 `ApiResponse`는 import 사용
- 컨트롤러 정리
  - `MediaController`, `RecordingController`, `IngestController`, `TranscriptController`가 각 인터페이스 구현
  - 컨트롤러는 Swagger import 제거, 응답은 `ApiResponse.success(...)`로 래핑
  - Recorder.vue 호환 API(Ingest/Transcript)는 기존 스키마 유지(원하면 래핑 가능)

검증
- 컴파일/테스트 모두 성공
- Swagger UI(/docs)에서 문서 정상 노출

다음 제안
- 응답/요청 스키마에 예시(@ExampleObject) 추가로 문서 가독성 개선
- Ingest/Transcript도 ApiResponse 래핑으로 통일하고 프런트 파싱 로직 업데이트(옵션)
