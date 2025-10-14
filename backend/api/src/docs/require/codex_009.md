# Codex 009 — 도메인 시나리오와 테스트(green)

시나리오(DDD 관점)
- Given: 사용자가 오디오 파일(메타: 언어=ko)을 업로드한다.
- When: 애플리케이션 서비스가 업로드를 수신하고 외부 STT/Diarization을 호출한다(인프라 클라이언트).
- And: 병합 서비스가 단어-화자 구간을 겹침 기준으로 매칭해 턴을 생성한다.
- Then: 트랜잭션으로 recordings/transcripts/diar_segments/speaker_turns가 저장된다.
- And: 조회 서비스가 Recording/Segments/Turns를 일관되게 반환한다.

테스트 구성
- 단위: `SpeechMergeServiceTest`
  - STT 단어/화자 구간을 입력 → 기대 턴(화자/텍스트) 확인
- 통합(서비스): `MediaProcessingServiceTest`
  - 외부 의존(`SttClient`, `DiarizationClient`, `MediaProbeService`) 모킹
  - 처리 후 DB에 1개의 recording, 2개의 transcripts, 1개의 diar, 1개의 turn 저장 확인
- 통합(조회): `RecordingQueryServiceTest`
  - 처리로 만든 recording을 조회 서비스로 읽어 기대 DTO 확인

위치
- tests
  - backend/api/src/test/java/com/adas/application/SpeechMergeServiceTest.java
  - backend/api/src/test/java/com/adas/application/MediaProcessingServiceTest.java
  - backend/api/src/test/java/com/adas/application/RecordingQueryServiceTest.java

메모
- H2(in-memory)로 테스트 격리
- ffprobe는 테스트에서 모킹하여 duration_ms를 고정값으로 설정
- 모든 테스트 green (Gradle test 성공)
