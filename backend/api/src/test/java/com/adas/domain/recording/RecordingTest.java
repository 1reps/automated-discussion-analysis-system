package com.adas.domain.recording;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

class RecordingTest {

    @Test
    void createFromUpload_정상적인_파라미터로_생성() {
        // given
        String source = "api";
        String language = "ko";
        Long sizeBytes = 1024L;

        // when
        Recording recording = Recording.createFromUpload(source, language, sizeBytes);

        // then
        assertThat(recording.getSource()).isEqualTo("api");
        assertThat(recording.getLanguage()).isEqualTo("ko");
        assertThat(recording.getSizeBytes()).isEqualTo(1024L);
        assertThat(recording.getDurationMs()).isNull();
    }

    @Test
    void specifyDuration_양수값_설정() {
        // given
        Recording recording = Recording.createFromUpload("api", "ko", 1024L);
        Long duration = 30000L;

        // when
        recording.specifyDuration(duration);

        // then
        assertThat(recording.getDurationMs()).isEqualTo(30000L);
    }

    @Test
    void specifyDuration_음수값_예외발생() {
        // given
        Recording recording = Recording.createFromUpload("api", "ko", 1024L);

        // when & then
        assertThatThrownBy(() -> recording.specifyDuration(-1000L))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Duration cannot be negative");
    }

    @Test
    void specifyDuration_null값_허용() {
        // given
        Recording recording = Recording.createFromUpload("api", "ko", 1024L);

        // when
        recording.specifyDuration(null);

        // then
        assertThat(recording.getDurationMs()).isNull();
    }

    @Test
    void markAsProcessedBy_정상적인_소스_설정() {
        // given
        Recording recording = Recording.createFromUpload("api", "ko", 1024L);
        String newSource = "stt";

        // when
        recording.markAsProcessedBy(newSource);

        // then
        assertThat(recording.getSource()).isEqualTo("stt");
    }

    @Test
    void markAsProcessedBy_null값_예외발생() {
        // given
        Recording recording = Recording.createFromUpload("api", "ko", 1024L);

        // when & then
        assertThatThrownBy(() -> recording.markAsProcessedBy(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Source cannot be null or empty");
    }

    @Test
    void markAsProcessedBy_빈문자열_예외발생() {
        // given
        Recording recording = Recording.createFromUpload("api", "ko", 1024L);

        // when & then
        assertThatThrownBy(() -> recording.markAsProcessedBy("   "))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Source cannot be null or empty");
    }
}