package com.adas.infrastructure.external.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

/** Diarization 서비스 응답 DTO (불변 record). */
@JsonIgnoreProperties(ignoreUnknown = true)
public record DiarizationResponse(List<Segment> segments) {
    public record Segment(long start, long end, String speaker, Double confidence) {}
}

