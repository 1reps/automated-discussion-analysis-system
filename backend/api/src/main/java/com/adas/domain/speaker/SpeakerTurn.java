package com.adas.domain.speaker;

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
 * 병합된 화자 턴 엔티티.
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "speaker_turns")
public class SpeakerTurn {

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

    @Column(length = 4000)
    private String text;

    public static SpeakerTurn createTurn(Recording recording, String speakerLabel, Long startMs, Long endMs, String text) {
        validateTimeRange(startMs, endMs);
        
        SpeakerTurn turn = new SpeakerTurn();
        turn.recording = recording;
        turn.speakerLabel = speakerLabel;
        turn.startMs = startMs;
        turn.endMs = endMs;
        turn.text = text;
        return turn;
    }

    public void extendTo(Long newEndMs) {
        validateTimeRange(this.startMs, newEndMs);
        this.endMs = newEndMs;
    }

    public void appendText(String additionalText) {
        if (additionalText != null && !additionalText.trim().isEmpty()) {
            this.text = this.text == null ? additionalText : this.text + " " + additionalText;
        }
    }

    public void assignToSpeaker(String speakerLabel) {
        if (speakerLabel == null || speakerLabel.trim().isEmpty()) {
            throw new IllegalArgumentException("Speaker label cannot be null or empty");
        }
        this.speakerLabel = speakerLabel;
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
}