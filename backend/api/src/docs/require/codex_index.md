# Codex 작업 인덱스/핸드오프 가이드

이 문서는 Codex 세션이 종료되더라도 다음 세션에서 빠르게 이어서 작업할 수 있도록, 완료/진행/다음 단계 정보를 요약합니다. 자세한 내용은 각 단계 문서를 참고하세요.

진행 이력
- 001 — 아키텍처/요구사항 요약: backend/api/src/docs/require/codex_001.md
- 002 — 네이밍/예외 기본 정비: backend/api/src/docs/require/codex_002.md
- 003 — DB 스키마 + 계층 리팩토링: backend/api/src/docs/require/codex_003.md
- 004 — DTO record 전환 + ffprobe: backend/api/src/docs/require/codex_004.md
- 005 — 계층 정리(presentation→application) + 스타일: backend/api/src/docs/require/codex_005.md
- 006 — 조회 API 추가(Recording/Segments/Turns): backend/api/src/docs/require/codex_006.md
- 007 — 코드 스타일/Import 정비(도구 미사용): backend/api/src/docs/require/codex_007.md
- 008 — GlobalException을 ProblemDetail로 전환: backend/api/src/docs/require/codex_008.md
- 009 — 도메인 시나리오 테스트(green): backend/api/src/docs/require/codex_009.md

현재 상태(요약)
- Spring Boot 게이트웨이: 빌드 성공, 서비스/조회 테스트 green
- Python STT/Diarization: 도커로 기동됨(사용자 환경). Spring의 external.* URL과 포트 일치 필요
- 전역 예외: ProblemDetail로 통일. 정상 응답은 ApiResponse 사용
- 계층 의존성: presentation → application → domain/infrastructure 방향으로 정리

검토/리팩터링 메모
- 의존 방향: application에서 presentation 패키지 의존 제거 완료
- 캡슐화: 엔티티는 기본 세터 사용(간단 모델). 필요 시 생성자/팩토리 적용 여지 있음
- 가독성: DTO는 record, 서비스/컨트롤러 주석 보강. 4칸 들여쓰기 유지(도구 미사용, 에디터에서 적용 권장)

다음 단계(To‑Do)
1) 컨트롤러(Web 레이어) 테스트(MockMvc)
   - /api/v1/media/process 성공/에러 케이스 스냅샷
   - /api/v1/recordings/{id} 조회 200/404
2) docker-compose.dev 샘플/문서 정리
   - Spring ↔ Python URL/포트, 환경변수, build context/Dockerfile 경로 명확화
3) 병합 알고리즘 개선 옵션 문서화
   - 겹침 임계(tolerance) 파라미터, 경계 보정 옵션
4) 운영 항목
   - traceId 전파(요청 헤더→외부 호출→응답), 로그 필드 표준화
5) 성능/확장
   - JPA batch 옵션 검토, 인덱스 설계

다음 세션 시작 체크리스트
- 도커 Python 서비스 기동/헬스 체크: 8001/8002 /api/v1/health
- Spring application.yml의 external.stt/diarization URL이 포트와 일치하는지 확인
- 필요 시 ffprobe 설치 확인(없어도 null로 동작)

