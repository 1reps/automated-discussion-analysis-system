package com.adas.infrastructure.external.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

/**
 * STT 서비스 응답 DTO (불변 record).
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record SttResponse(String lang, List<Word> words) {

    /**
     * 단어 단위 전사 결과(불변 record).
     */
    public record Word(long start, long end, String text, Double confidence) {

    }
}

