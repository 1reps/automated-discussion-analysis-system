package com.adas.application.dto;

/**
 * 병합된 화자 턴 조회 응답 (record).
 */
public record SpeakerTurnResponse(Long startMs, Long endMs, String speakerLabel, String text) {

}

