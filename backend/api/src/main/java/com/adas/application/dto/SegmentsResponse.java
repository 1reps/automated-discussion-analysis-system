package com.adas.application.dto;

import java.util.List;

/** 전사/화자 분리 세그먼트를 함께 반환하는 응답 (record). */
public record SegmentsResponse(
    List<TranscriptSegmentResponse> transcripts, List<DiarizationSegmentResponse> diarization) {}

