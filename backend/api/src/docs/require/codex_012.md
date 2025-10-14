# Codex 012 — 다음 추천 작업과 계획(핸드오프)

목표
- 프런트/백엔드/파이썬 e2e를 안정화하고, 문서/운영/관찰성을 보강합니다.

추천 작업(우선순위 제안)
1) Ingest/Transcript API 래핑 통일
   - 내용: `/api/ingest/audio`, `/api/v1/recordings/{id}/transcript`를 `ApiResponse` 래핑으로 통일
   - 영향: 프런트 `Recorder.vue`의 응답 파싱을 `data.recordingId`, `data.transcriptUrl` 등으로 변경
   - 가치: API 일관성 확보, 에러 처리(ProblemDetail/ApiResponse) 표준화

2) Swagger 문서 보강(예시/스키마)
   - 내용: `@ExampleObject`로 성공/에러 응답 예시 추가, 요청 파라미터 제약(빈도/허용 범위) 주석화
   - 파일: `presentation.docs` 인터페이스에만 추가
   - 가치: 문서 가독성 향상, 프런트/QA 협업 효율 증대

3) docker-compose.dev 개선(GPU/프로필)
   - 내용: diarization에 GPU 리소스 설정(옵션), 프로필 별 compose 파일 분리(dev/gpu)
   - 가치: 환경 별 실행 편의성 향상, 실환경과의 괴리 축소

4) 병합 알고리즘 파라미터화
   - 내용: 겹침 허용치, 경계 보정 옵션을 설정으로 노출하고 Swagger 문서화
   - 가치: 다양한 녹음 조건(발화 빈도/겹침)에 대한 튜닝 용이성 확보

5) 로깅/트레이싱 표준화
   - 내용: traceId 헤더 전파(Spring → Python), ApiResponse/ProblemDetail에 traceId 포함(옵션)
   - 가치: 장애 분석/추적 용이성

6) 테스트 확장
   - 내용: MockMvc로 /media/process 성공/에러 스냅샷 테스트, WireMock으로 STT/Diar 모킹 통합 테스트
   - 가치: 회귀 방지, 변경 안전성 확보

체크리스트(세션 재개 시)
- Python 컨테이너 헬스: 8001/8002 `/api/v1/health`
- Spring external URL 확인: `application.yml` 또는 환경변수(Compose)
- ffprobe 확인(없어도 null로 안전 동작)

비고
- 현재 상태: 빌드/테스트 green, Swagger UI(/docs) 정상, Recorder 업로드/폴링 호환 API 제공
