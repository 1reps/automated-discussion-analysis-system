package com.adas.domain.speech;

import lombok.Getter;

/**
 * 화자 턴 도메인 모델 - 병합된 화자 발화 구간을 나타낸다.
 */
@Getter
public class Turn {

    private final String speaker;
    private long start;
    private long end;
    private String text;

    private Turn(String speaker, long start, long end, String text) {
        this.speaker = speaker;
        this.start = start;
        this.end = end;
        this.text = text;
    }

    public static Turn startSpeaking(String speaker, long startMs, long endMs, String initialText) {
        validateTimeRange(startMs, endMs);
        validateSpeaker(speaker);
        
        return new Turn(speaker, startMs, endMs, initialText);
    }

    public void extendSpeech(long newEndMs, String additionalText) {
        validateTimeRange(this.start, newEndMs);
        
        this.end = newEndMs;
        if (additionalText != null && !additionalText.trim().isEmpty()) {
            this.text = this.text == null ? additionalText : this.text + " " + additionalText;
        }
    }

    public boolean isSameSpeaker(String speakerLabel) {
        return this.speaker != null && this.speaker.equals(speakerLabel);
    }

    public long getDurationMs() {
        return this.end - this.start;
    }

    private static void validateTimeRange(long startMs, long endMs) {
        if (startMs < 0 || endMs < 0) {
            throw new IllegalArgumentException("Times cannot be negative");
        }
        if (startMs >= endMs) {
            throw new IllegalArgumentException("Start time must be before end time");
        }
    }

    private static void validateSpeaker(String speaker) {
        if (speaker == null || speaker.trim().isEmpty()) {
            throw new IllegalArgumentException("Speaker cannot be null or empty");
        }
    }
}