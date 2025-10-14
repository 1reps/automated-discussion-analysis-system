# Codex 004 — DTO 불변 record 전환 + ffprobe 길이 계산

목표

- DTO 불변성 강화: Java record로 전환(SttResponse, DiarizationResponse, ProcessResponse)
- 오디오 길이 계산: ffprobe를 사용해 duration(ms) 추출, Recording에 저장
- 계층 준수: 인프라(MediaProbeService), 애플리케이션(MediaProcessingService)로 역할 분리

변경 사항(요약)

- DTO record화
    - SttResponse, SttResponse.Word
    - DiarizationResponse, DiarizationResponse.Segment
    - ProcessResponse
- ffprobe 연동
    - 설정 `media.ffprobe-path`, `media.probe-timeout-ms` 추가
    - 인프라 `MediaProbeService` 신설: 임시 파일로 ffprobe 실행해 ms 반환
    - Application 서비스에서 durationMs 설정 후 Recording 저장
- 사용처 업데이트
    - record 접근자(`lang()`, `words()`, `segments()`, `start()` 등)로 전환

확인 포인트

- ffprobe가 PATH 또는 설정 경로에 존재해야 duration이 채워짐(없어도 null로 안전 동작)
- `/api/v1/media/process` 처리 성공 후 DB에 recordings/transcripts/diar_segments/speaker_turns 적재

향후(005 제안)

- 조회 API(`/recordings/{id}`, `/recordings/{id}/segments`, `/recordings/{id}/turns`) 추가
- WireMock 통합 테스트로 외부 서비스 모킹 및 저장 검증
- 대용량 배치 저장 최적화(JPA batch 옵션)
