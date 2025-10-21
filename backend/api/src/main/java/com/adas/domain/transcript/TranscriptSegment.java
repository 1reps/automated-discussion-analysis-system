package com.adas.domain.transcript;

import com.adas.domain.recording.Recording;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * STT 전사 세그먼트(단어 또는 구간 단위) 저장 엔티티.
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "transcripts")
public class TranscriptSegment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "recording_id")
    private Recording recording;

    @Column(name = "start_ms", nullable = false)
    private Long startMs;

    @Column(name = "end_ms", nullable = false)
    private Long endMs;

    @Column(length = 2000)
    private String text;

    private Double confidence;

    @Column(length = 10)
    private String language;

    @Column(length = 50)
    private String provider;

    public static TranscriptSegment transcribeSegment(Recording recording, Long startMs, Long endMs, 
                                                    String text, Double confidence, String language, String provider) {
        validateTimeRange(startMs, endMs);
        validateConfidence(confidence);
        validateProvider(provider);
        
        TranscriptSegment segment = new TranscriptSegment();
        segment.recording = recording;
        segment.startMs = startMs;
        segment.endMs = endMs;
        segment.text = text;
        segment.confidence = confidence;
        segment.language = language;
        segment.provider = provider;
        return segment;
    }

    public void correctTranscription(String correctedText) {
        if (correctedText != null && correctedText.length() > 2000) {
            throw new IllegalArgumentException("Text cannot exceed 2000 characters");
        }
        this.text = correctedText;
    }

    public void adjustConfidence(Double newConfidence) {
        validateConfidence(newConfidence);
        this.confidence = newConfidence;
    }

    public void specifyLanguage(String detectedLanguage) {
        if (detectedLanguage != null && detectedLanguage.length() > 10) {
            throw new IllegalArgumentException("Language code cannot exceed 10 characters");
        }
        this.language = detectedLanguage;
    }

    private static void validateTimeRange(Long startMs, Long endMs) {
        if (startMs == null || endMs == null) {
            throw new IllegalArgumentException("Start and end times cannot be null");
        }
        if (startMs < 0 || endMs < 0) {
            throw new IllegalArgumentException("Times cannot be negative");
        }
        if (startMs >= endMs) {
            throw new IllegalArgumentException("Start time must be before end time");
        }
    }

    private static void validateConfidence(Double confidence) {
        if (confidence != null && (confidence < 0.0 || confidence > 1.0)) {
            throw new IllegalArgumentException("Confidence must be between 0.0 and 1.0");
        }
    }

    private static void validateProvider(String provider) {
        if (provider == null || provider.trim().isEmpty()) {
            throw new IllegalArgumentException("Provider cannot be null or empty");
        }
        if (provider.length() > 50) {
            throw new IllegalArgumentException("Provider name cannot exceed 50 characters");
        }
    }
}