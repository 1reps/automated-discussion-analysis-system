# Codex 006 — 조회 API 추가(Recording/Segments/Turns)

목표

- 조회 엔드포인트 제공: Recording 기본 정보, 전사/화자분리 세그먼트, 병합된 턴
- Layered Architecture 유지: application 쿼리 서비스 추가, presentation 컨트롤러 분리
- DTO는 record로 정의해 불변성 유지

추가 사항

- Application
    - `RecordingQueryService`: Recording/Segments/Turns 조회 및 엔티티→DTO 매핑
- Infrastructure
    - Repository 메서드 추가: `findByRecordingIdOrderByStartMsAsc`
- Presentation
    - `RecordingController` (GET)
        - `/api/v1/recordings/{id}` → RecordingResponse
        - `/api/v1/recordings/{id}/segments` → SegmentsResponse { transcripts[], diarization[] }
        - `/api/v1/recordings/{id}/turns` → List<SpeakerTurnResponse>
- DTO(record)
    - RecordingResponse, TranscriptSegmentResponse, DiarizationSegmentResponse, SpeakerTurnResponse,
      SegmentsResponse
- 예외
    - `NotFoundException` 추가 및 GlobalControllerAdvice에서 404 매핑

확인 포인트

- 저장된 `recordings` 엔트리의 id로 각 조회 API 호출 시 기대 데이터 반환
- 존재하지 않는 id → 404 ApiResponse 실패 바디
