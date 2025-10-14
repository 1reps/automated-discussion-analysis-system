# Codex 003 — DB 스키마/리포지토리 및 계층 리팩터링

목표

- Layered Architecture 준수: presentation ↔ application ↔ domain ↔ infrastructure 분리
- DB 스키마 도입: recordings, transcripts, diar_segments, speaker_turns
- 트랜잭션 처리: 업로드 처리 플로우를 서비스 계층에서 원자적으로 커밋

변경 사항(요약)

- domain(audio): Recording, TranscriptSegment, DiarizationSegmentEntity, SpeakerTurn 엔티티 추가
- infrastructure(repository): 각 엔티티용 JpaRepository 추가
- application: MediaProcessingService 도입(외부 호출 → 병합 → 영속화 오케스트레이션, @Transactional)
- presentation: MediaController가 Service를 호출하도록 리팩터링(컨트롤러는 오케스트레이션 로직 제거)

주의/확인 포인트

- DB 설정은 환경에 맞게 구성 필요(MySQL/H2 등). 엔티티/리포지토리만 추가됨
- 응답 recordingId는 DB 생성 키(Long)를 문자열로 반환
- durationMs는 추후 ffprobe 연동 시 채움(TODO)
- 예외는 GlobalControllerAdvice에서 ApiResponse로 일원화됨(이전 단계 002)

향후(004 제안)

- ffprobe를 통한 duration 계산 및 저장
- 대용량 업로드/배치 저장 최적화(JPA flush/batch 옵션)
- 통합 테스트로 저장 검증 및 조회 API 추가(`/recordings/{id}` 등)
