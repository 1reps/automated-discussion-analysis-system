package com.adas.domain.diarization;

import com.adas.domain.recording.Recording;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

class DiarizationSegmentTest {

    @Test
    void identifySpeaker_정상적인_파라미터로_생성() {
        // given
        Recording recording = Recording.createFromUpload("api", "ko", 1024L);
        String speakerLabel = "SPEAKER_0";
        Long startMs = 1000L;
        Long endMs = 2000L;
        Double confidence = 0.85;

        // when
        DiarizationSegment segment = DiarizationSegment.identifySpeaker(
            recording, speakerLabel, startMs, endMs, confidence);

        // then
        assertThat(segment.getRecording()).isEqualTo(recording);
        assertThat(segment.getSpeakerLabel()).isEqualTo("SPEAKER_0");
        assertThat(segment.getStartMs()).isEqualTo(1000L);
        assertThat(segment.getEndMs()).isEqualTo(2000L);
        assertThat(segment.getConfidence()).isEqualTo(0.85);
    }

    @Test
    void identifySpeaker_잘못된_시간범위_예외발생() {
        // given
        Recording recording = Recording.createFromUpload("api", "ko", 1024L);

        // when & then
        assertThatThrownBy(() -> DiarizationSegment.identifySpeaker(
            recording, "SPEAKER_0", 2000L, 1000L, 0.85))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Start time must be before end time");
    }

    @Test
    void identifySpeaker_잘못된_신뢰도_예외발생() {
        // given
        Recording recording = Recording.createFromUpload("api", "ko", 1024L);

        // when & then
        assertThatThrownBy(() -> DiarizationSegment.identifySpeaker(
            recording, "SPEAKER_0", 1000L, 2000L, -0.1))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Confidence must be between 0.0 and 1.0");
    }

    @Test
    void identifySpeaker_null_신뢰도_허용() {
        // given
        Recording recording = Recording.createFromUpload("api", "ko", 1024L);

        // when
        DiarizationSegment segment = DiarizationSegment.identifySpeaker(
            recording, "SPEAKER_0", 1000L, 2000L, null);

        // then
        assertThat(segment.getConfidence()).isNull();
    }

    @Test
    void adjustConfidence_신뢰도_조정() {
        // given
        Recording recording = Recording.createFromUpload("api", "ko", 1024L);
        DiarizationSegment segment = DiarizationSegment.identifySpeaker(
            recording, "SPEAKER_0", 1000L, 2000L, 0.85);

        // when
        segment.adjustConfidence(0.9);

        // then
        assertThat(segment.getConfidence()).isEqualTo(0.9);
    }

    @Test
    void adjustConfidence_잘못된_범위_예외발생() {
        // given
        Recording recording = Recording.createFromUpload("api", "ko", 1024L);
        DiarizationSegment segment = DiarizationSegment.identifySpeaker(
            recording, "SPEAKER_0", 1000L, 2000L, 0.85);

        // when & then
        assertThatThrownBy(() -> segment.adjustConfidence(1.5))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Confidence must be between 0.0 and 1.0");
    }

    @Test
    void correctSpeakerLabel_화자_레이블_수정() {
        // given
        Recording recording = Recording.createFromUpload("api", "ko", 1024L);
        DiarizationSegment segment = DiarizationSegment.identifySpeaker(
            recording, "SPEAKER_0", 1000L, 2000L, 0.85);

        // when
        segment.correctSpeakerLabel("SPEAKER_1");

        // then
        assertThat(segment.getSpeakerLabel()).isEqualTo("SPEAKER_1");
    }

    @Test
    void correctSpeakerLabel_null_레이블_예외발생() {
        // given
        Recording recording = Recording.createFromUpload("api", "ko", 1024L);
        DiarizationSegment segment = DiarizationSegment.identifySpeaker(
            recording, "SPEAKER_0", 1000L, 2000L, 0.85);

        // when & then
        assertThatThrownBy(() -> segment.correctSpeakerLabel(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Speaker label cannot be null or empty");
    }

    @Test
    void correctSpeakerLabel_빈_레이블_예외발생() {
        // given
        Recording recording = Recording.createFromUpload("api", "ko", 1024L);
        DiarizationSegment segment = DiarizationSegment.identifySpeaker(
            recording, "SPEAKER_0", 1000L, 2000L, 0.85);

        // when & then
        assertThatThrownBy(() -> segment.correctSpeakerLabel("   "))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Speaker label cannot be null or empty");
    }
}