package com.adas.domain.transcript;

import com.adas.domain.recording.Recording;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

class TranscriptSegmentTest {

    @Test
    void transcribeSegment_정상적인_파라미터로_생성() {
        // given
        Recording recording = Recording.createFromUpload("api", "ko", 1024L);
        Long startMs = 1000L;
        Long endMs = 2000L;
        String text = "안녕하세요";
        Double confidence = 0.95;
        String language = "ko";
        String provider = "google";

        // when
        TranscriptSegment segment = TranscriptSegment.transcribeSegment(
            recording, startMs, endMs, text, confidence, language, provider);

        // then
        assertThat(segment.getRecording()).isEqualTo(recording);
        assertThat(segment.getStartMs()).isEqualTo(1000L);
        assertThat(segment.getEndMs()).isEqualTo(2000L);
        assertThat(segment.getText()).isEqualTo("안녕하세요");
        assertThat(segment.getConfidence()).isEqualTo(0.95);
        assertThat(segment.getLanguage()).isEqualTo("ko");
        assertThat(segment.getProvider()).isEqualTo("google");
    }

    @Test
    void transcribeSegment_잘못된_시간범위_예외발생() {
        // given
        Recording recording = Recording.createFromUpload("api", "ko", 1024L);

        // when & then
        assertThatThrownBy(() -> TranscriptSegment.transcribeSegment(
            recording, 2000L, 1000L, "test", 0.9, "ko", "google"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Start time must be before end time");
    }

    @Test
    void transcribeSegment_잘못된_신뢰도_예외발생() {
        // given
        Recording recording = Recording.createFromUpload("api", "ko", 1024L);

        // when & then
        assertThatThrownBy(() -> TranscriptSegment.transcribeSegment(
            recording, 1000L, 2000L, "test", 1.5, "ko", "google"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Confidence must be between 0.0 and 1.0");
    }

    @Test
    void transcribeSegment_null_provider_예외발생() {
        // given
        Recording recording = Recording.createFromUpload("api", "ko", 1024L);

        // when & then
        assertThatThrownBy(() -> TranscriptSegment.transcribeSegment(
            recording, 1000L, 2000L, "test", 0.9, "ko", null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Provider cannot be null or empty");
    }

    @Test
    void correctTranscription_텍스트_수정() {
        // given
        Recording recording = Recording.createFromUpload("api", "ko", 1024L);
        TranscriptSegment segment = TranscriptSegment.transcribeSegment(
            recording, 1000L, 2000L, "안녕하세여", 0.9, "ko", "google");

        // when
        segment.correctTranscription("안녕하세요");

        // then
        assertThat(segment.getText()).isEqualTo("안녕하세요");
    }

    @Test
    void correctTranscription_긴_텍스트_예외발생() {
        // given
        Recording recording = Recording.createFromUpload("api", "ko", 1024L);
        TranscriptSegment segment = TranscriptSegment.transcribeSegment(
            recording, 1000L, 2000L, "test", 0.9, "ko", "google");
        String longText = "a".repeat(2001);

        // when & then
        assertThatThrownBy(() -> segment.correctTranscription(longText))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Text cannot exceed 2000 characters");
    }

    @Test
    void adjustConfidence_신뢰도_조정() {
        // given
        Recording recording = Recording.createFromUpload("api", "ko", 1024L);
        TranscriptSegment segment = TranscriptSegment.transcribeSegment(
            recording, 1000L, 2000L, "test", 0.9, "ko", "google");

        // when
        segment.adjustConfidence(0.8);

        // then
        assertThat(segment.getConfidence()).isEqualTo(0.8);
    }

    @Test
    void adjustConfidence_잘못된_범위_예외발생() {
        // given
        Recording recording = Recording.createFromUpload("api", "ko", 1024L);
        TranscriptSegment segment = TranscriptSegment.transcribeSegment(
            recording, 1000L, 2000L, "test", 0.9, "ko", "google");

        // when & then
        assertThatThrownBy(() -> segment.adjustConfidence(1.5))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Confidence must be between 0.0 and 1.0");
    }

    @Test
    void specifyLanguage_언어_지정() {
        // given
        Recording recording = Recording.createFromUpload("api", "ko", 1024L);
        TranscriptSegment segment = TranscriptSegment.transcribeSegment(
            recording, 1000L, 2000L, "test", 0.9, "ko", "google");

        // when
        segment.specifyLanguage("en");

        // then
        assertThat(segment.getLanguage()).isEqualTo("en");
    }

    @Test
    void specifyLanguage_긴_언어코드_예외발생() {
        // given
        Recording recording = Recording.createFromUpload("api", "ko", 1024L);
        TranscriptSegment segment = TranscriptSegment.transcribeSegment(
            recording, 1000L, 2000L, "test", 0.9, "ko", "google");

        // when & then
        assertThatThrownBy(() -> segment.specifyLanguage("a".repeat(11)))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Language code cannot exceed 10 characters");
    }
}