# Codex 005 — 계층 정리(presentation→application) + Google Java Style 적용

목표

- Service 계층 구분 명확화: 병합 로직 `SpeechMergeService`를 presentation → application 계층으로 이동
- 코드 스타일: Google Java Style을 적용해 일관된 포맷 유지

변경 사항(요약)

- 파일 이동
    - `com.adas.presentation.service.SpeechMergeService` → `com.adas.application.SpeechMergeService`
    - import/참조 경로 갱신(`MediaProcessingService`, `ProcessResponse`)
- 스타일 적용
    - Gradle에 Spotless + google-java-format 설정 추가
    - 주석/가독성 개선(문장 끝 주석 마침표, 간격 등 소폭 정리)

설명: 왜 presentation의 service와 application의 service가 다른가?

- presentation(service): 컨트롤러 주변의 웹 레이어 보조(요청/응답 변환, 인증/인가 보조 등)를 의미. 비즈니스 오케스트레이션은 지양
- application(service): 유스케이스 오케스트레이션 담당(외부 시스템 호출, 트랜잭션, 도메인 서비스/리포지토리 조합)
- domain(service): 순수 도메인 규칙/알고리즘(부작용 없음 또는 최소화)
- 본 프로젝트에선 병합 로직이 웹(표현) 레이어와 무관하며, 외부 DTO를 다루되 오케스트레이션에 밀접해 application 쪽으로 배치하는 것이 적절

확인 포인트

- 빌드 시 Spotless가 google-java-format으로 포맷 적용(네트워크 필요). 로컬에서 `./gradlew spotlessApply`로 자동 정렬 가능
- 컨트롤러는 application 서비스만 호출하고 오케스트레이션 로직을 갖지 않음
