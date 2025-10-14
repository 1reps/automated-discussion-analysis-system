# Codex 008 — 전역 예외 응답을 ProblemDetail로 전환

요구사항

- ApiResponse 기반 실패 응답 대신, Spring의 최신 `ProblemDetail`을 사용해 표준화된 에러 바디를 제공

적용 사항

- `GlobalControllerAdvice` 수정: `ProblemDetail` 반환으로 일원화
    - 매핑:
        - 400: `MethodArgumentNotValidException`, `MissingServletRequestParameterException`,
          `CustomException`
        - 404: `NotFoundException`
        - 415: `MultipartException`, `MaxUploadSizeExceededException`
        - 502: `WebClientResponseException`, `HttpStatusCodeException`, `WebClientRequestException`(
          네트워크 오류)
        - 504: `WebClientRequestException`의 Read/WriteTimeout
        - 500: 그 외 모든 예외
    - 공통 속성: `title`, `timestamp`, `exception`(클래스명)

비고

- 정상 응답은 기존처럼 `ApiResponse.success(...)` 사용
- 오류 응답만 `ProblemDetail` 형태로 통일되어, 클라이언트가 표준 JSON Problem 스펙으로 처리 가능
