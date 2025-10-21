package com.adas.domain.speaker;

import com.adas.domain.recording.Recording;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

class SpeakerTurnTest {

    @Test
    void createTurn_정상적인_파라미터로_생성() {
        // given
        Recording recording = Recording.createFromUpload("api", "ko", 1024L);
        String speakerLabel = "SPEAKER_0";
        Long startMs = 1000L;
        Long endMs = 2000L;
        String text = "안녕하세요";

        // when
        SpeakerTurn turn = SpeakerTurn.createTurn(recording, speakerLabel, startMs, endMs, text);

        // then
        assertThat(turn.getRecording()).isEqualTo(recording);
        assertThat(turn.getSpeakerLabel()).isEqualTo("SPEAKER_0");
        assertThat(turn.getStartMs()).isEqualTo(1000L);
        assertThat(turn.getEndMs()).isEqualTo(2000L);
        assertThat(turn.getText()).isEqualTo("안녕하세요");
    }

    @Test
    void createTurn_잘못된_시간범위_예외발생() {
        // given
        Recording recording = Recording.createFromUpload("api", "ko", 1024L);

        // when & then
        assertThatThrownBy(() -> SpeakerTurn.createTurn(recording, "SPEAKER_0", 2000L, 1000L, "test"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Start time must be before end time");
    }

    @Test
    void extendTo_끝시간_연장() {
        // given
        Recording recording = Recording.createFromUpload("api", "ko", 1024L);
        SpeakerTurn turn = SpeakerTurn.createTurn(recording, "SPEAKER_0", 1000L, 2000L, "안녕하세요");

        // when
        turn.extendTo(3000L);

        // then
        assertThat(turn.getEndMs()).isEqualTo(3000L);
    }

    @Test
    void extendTo_잘못된_시간_예외발생() {
        // given
        Recording recording = Recording.createFromUpload("api", "ko", 1024L);
        SpeakerTurn turn = SpeakerTurn.createTurn(recording, "SPEAKER_0", 1000L, 2000L, "안녕하세요");

        // when & then
        assertThatThrownBy(() -> turn.extendTo(500L))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Start time must be before end time");
    }

    @Test
    void appendText_텍스트_추가() {
        // given
        Recording recording = Recording.createFromUpload("api", "ko", 1024L);
        SpeakerTurn turn = SpeakerTurn.createTurn(recording, "SPEAKER_0", 1000L, 2000L, "안녕하세요");

        // when
        turn.appendText("반갑습니다");

        // then
        assertThat(turn.getText()).isEqualTo("안녕하세요 반갑습니다");
    }

    @Test
    void appendText_null_텍스트_무시() {
        // given
        Recording recording = Recording.createFromUpload("api", "ko", 1024L);
        SpeakerTurn turn = SpeakerTurn.createTurn(recording, "SPEAKER_0", 1000L, 2000L, "안녕하세요");

        // when
        turn.appendText(null);

        // then
        assertThat(turn.getText()).isEqualTo("안녕하세요");
    }

    @Test
    void appendText_빈_텍스트_무시() {
        // given
        Recording recording = Recording.createFromUpload("api", "ko", 1024L);
        SpeakerTurn turn = SpeakerTurn.createTurn(recording, "SPEAKER_0", 1000L, 2000L, "안녕하세요");

        // when
        turn.appendText("   ");

        // then
        assertThat(turn.getText()).isEqualTo("안녕하세요");
    }

    @Test
    void appendText_기존_텍스트가_null인_경우() {
        // given
        Recording recording = Recording.createFromUpload("api", "ko", 1024L);
        SpeakerTurn turn = SpeakerTurn.createTurn(recording, "SPEAKER_0", 1000L, 2000L, null);

        // when
        turn.appendText("안녕하세요");

        // then
        assertThat(turn.getText()).isEqualTo("안녕하세요");
    }

    @Test
    void assignToSpeaker_화자_재할당() {
        // given
        Recording recording = Recording.createFromUpload("api", "ko", 1024L);
        SpeakerTurn turn = SpeakerTurn.createTurn(recording, "SPEAKER_0", 1000L, 2000L, "test");

        // when
        turn.assignToSpeaker("SPEAKER_1");

        // then
        assertThat(turn.getSpeakerLabel()).isEqualTo("SPEAKER_1");
    }

    @Test
    void assignToSpeaker_null_화자_예외발생() {
        // given
        Recording recording = Recording.createFromUpload("api", "ko", 1024L);
        SpeakerTurn turn = SpeakerTurn.createTurn(recording, "SPEAKER_0", 1000L, 2000L, "test");

        // when & then
        assertThatThrownBy(() -> turn.assignToSpeaker(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Speaker label cannot be null or empty");
    }
}