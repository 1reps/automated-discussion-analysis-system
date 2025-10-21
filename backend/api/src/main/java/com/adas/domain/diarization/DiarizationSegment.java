package com.adas.domain.diarization;

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
 * 화자 분리(Diarization) 세그먼트 저장 엔티티.
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "diar_segments")
public class DiarizationSegment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "recording_id")
    private Recording recording;

    @Column(name = "speaker_label", length = 50)
    private String speakerLabel;

    @Column(name = "start_ms", nullable = false)
    private Long startMs;

    @Column(name = "end_ms", nullable = false)
    private Long endMs;

    private Double confidence;

    public static DiarizationSegment identifySpeaker(Recording recording, String speakerLabel, Long startMs, Long endMs, Double confidence) {
        validateTimeRange(startMs, endMs);
        validateConfidence(confidence);
        
        DiarizationSegment segment = new DiarizationSegment();
        segment.recording = recording;
        segment.speakerLabel = speakerLabel;
        segment.startMs = startMs;
        segment.endMs = endMs;
        segment.confidence = confidence;
        return segment;
    }

    public void adjustConfidence(Double newConfidence) {
        validateConfidence(newConfidence);
        this.confidence = newConfidence;
    }

    public void correctSpeakerLabel(String correctedLabel) {
        if (correctedLabel == null || correctedLabel.trim().isEmpty()) {
            throw new IllegalArgumentException("Speaker label cannot be null or empty");
        }
        this.speakerLabel = correctedLabel;
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
}