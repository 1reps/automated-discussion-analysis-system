package com.adas.application.dto;

/**
 * 전사(Transcript) 세그먼트 조회 응답 (record).
 */
public record TranscriptSegmentResponse(
    Long startMs, Long endMs, String text, Double confidence, String language, String provider) {

}

