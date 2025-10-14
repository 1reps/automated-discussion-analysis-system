package com.adas.application.dto;

/** 화자 분리(Diarization) 세그먼트 조회 응답 (record). */
public record DiarizationSegmentResponse(
    Long startMs, Long endMs, String speakerLabel, Double confidence) {}

