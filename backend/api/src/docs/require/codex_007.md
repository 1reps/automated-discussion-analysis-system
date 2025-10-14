# Codex 007 — 코드 스타일(4칸 들여쓰기) + Import 규칙 정비

요구사항

- 들여쓰기를 4칸으로 변경(프로젝트 전반 적용)
- import 규칙 정비: `java.util.List`를 반환/파라미터에서 직접 쓰지 말고 `import java.util.List;` 후 `List` 사용

적용 내용

- 포매터 도구는 추가하지 않음(사용자 요청)
- Repository 시그니처 수정 및 import 추가
    - `TranscriptSegmentRepository`, `DiarizationSegmentRepository`, `SpeakerTurnRepository`
    - `java.util.List` → `List`로 표기하고 상단 import 추가

확인 방법

- 코드 스타일은 리뷰/PR 규칙으로 준수(4칸 들여쓰기, import 정리)
