# Codex 002 — 네이밍 정비와 예외 처리 개선

목표

- 약어 사용 최소화: Diar → Diarization로 전면 정비(클래스/빈/프로퍼티/파일명)
- 예외 응답 일원화: GlobalControllerAdvice가 ApiResponse를 통해 일관된 JSON 에러 바디 반환
- 주석 보강: 새/수정된 클래스에 Javadoc 스타일 주석 추가

변경 사항(요약)

- 프로퍼티 키: `external.diar` → `external.diarization` (하위호환: 기존 키가 존재하면 자동 폴백)
- WebClient 빈: `diarWebClient` → `diarizationWebClient`
- 클라이언트/DTO 클래스:
    - DiarClient → DiarizationClient
    - DiarResponse → DiarizationResponse
- 예외 처리: GlobalControllerAdvice가 `ApiResponse.fail(...)`로 변환하여 400/415/502/504/500 등을 매핑

테스트/확인 포인트

- application.yml에서 `external.diarization.base-url` 설정 후 `/api/v1/media/process` 업로드 성공
- 기존 `external.diar.base-url`만 있어도 정상 동작(하위호환 폴백 로직 확인)
- Python 서비스 다운/타임아웃 시 502/504 에러 바디 형태 확인

향후(다음 003 제안)

- DB 스키마/리포지토리 추가 및 recordingId 실제 키로 대체
- 업로드 용량/포맷 검증 강화 및 400/415 상세화
- 통합 테스트(WireMock)로 STT/Diarization 모킹
