package com.adas.application.dto;

import com.adas.domain.speech.Turn;
import com.adas.infrastructure.external.dto.DiarizationResponse;
import com.adas.infrastructure.external.dto.SttResponse;
import java.util.List;

/**
 * 업로드 처리 결과 응답 (애플리케이션 DTO, 불변 record).
 */
public record ProcessResponse(
    String recordingId,
    String lang,
    List<SttResponse.Word> words,
    List<DiarizationResponse.Segment> segments,
    List<Turn> turns) {

}

