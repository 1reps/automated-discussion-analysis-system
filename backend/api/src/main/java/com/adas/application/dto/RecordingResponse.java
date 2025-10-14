package com.adas.application.dto;

import java.time.Instant;

/** Recording 기본 정보 조회 응답 (record). */
public record RecordingResponse(
    Long id, String source, String language, Long sizeBytes, Long durationMs, Instant createdAt) {}

