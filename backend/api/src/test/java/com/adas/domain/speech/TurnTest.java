package com.adas.domain.speech;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

class TurnTest {

    @Test
    void startSpeaking_정상적인_파라미터로_생성() {
        // given
        String speaker = "SPEAKER_0";
        long startMs = 1000L;
        long endMs = 2000L;
        String text = "안녕하세요";

        // when
        Turn turn = Turn.startSpeaking(speaker, startMs, endMs, text);

        // then
        assertThat(turn.getSpeaker()).isEqualTo("SPEAKER_0");
        assertThat(turn.getStart()).isEqualTo(1000L);
        assertThat(turn.getEnd()).isEqualTo(2000L);
        assertThat(turn.getText()).isEqualTo("안녕하세요");
        assertThat(turn.getDurationMs()).isEqualTo(1000L);
    }

    @Test
    void startSpeaking_시작시간이_종료시간보다_큰경우_예외발생() {
        // when & then
        assertThatThrownBy(() -> Turn.startSpeaking("SPEAKER_0", 2000L, 1000L, "test"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Start time must be before end time");
    }

    @Test
    void startSpeaking_음수시간_예외발생() {
        // when & then
        assertThatThrownBy(() -> Turn.startSpeaking("SPEAKER_0", -1000L, 2000L, "test"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Times cannot be negative");
    }

    @Test
    void startSpeaking_화자명_null_예외발생() {
        // when & then
        assertThatThrownBy(() -> Turn.startSpeaking(null, 1000L, 2000L, "test"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Speaker cannot be null or empty");
    }

    @Test
    void extendSpeech_발화_연장() {
        // given
        Turn turn = Turn.startSpeaking("SPEAKER_0", 1000L, 2000L, "안녕하세요");

        // when
        turn.extendSpeech(3000L, "반갑습니다");

        // then
        assertThat(turn.getEnd()).isEqualTo(3000L);
        assertThat(turn.getText()).isEqualTo("안녕하세요 반갑습니다");
        assertThat(turn.getDurationMs()).isEqualTo(2000L);
    }

    @Test
    void extendSpeech_null_텍스트_무시() {
        // given
        Turn turn = Turn.startSpeaking("SPEAKER_0", 1000L, 2000L, "안녕하세요");

        // when
        turn.extendSpeech(3000L, null);

        // then
        assertThat(turn.getEnd()).isEqualTo(3000L);
        assertThat(turn.getText()).isEqualTo("안녕하세요");
    }

    @Test
    void extendSpeech_빈_텍스트_무시() {
        // given
        Turn turn = Turn.startSpeaking("SPEAKER_0", 1000L, 2000L, "안녕하세요");

        // when
        turn.extendSpeech(3000L, "   ");

        // then
        assertThat(turn.getEnd()).isEqualTo(3000L);
        assertThat(turn.getText()).isEqualTo("안녕하세요");
    }

    @Test
    void extendSpeech_잘못된_시간범위_예외발생() {
        // given
        Turn turn = Turn.startSpeaking("SPEAKER_0", 1000L, 2000L, "안녕하세요");

        // when & then
        assertThatThrownBy(() -> turn.extendSpeech(500L, "test"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Start time must be before end time");
    }

    @Test
    void isSameSpeaker_동일한_화자() {
        // given
        Turn turn = Turn.startSpeaking("SPEAKER_0", 1000L, 2000L, "test");

        // when & then
        assertThat(turn.isSameSpeaker("SPEAKER_0")).isTrue();
        assertThat(turn.isSameSpeaker("SPEAKER_1")).isFalse();
        assertThat(turn.isSameSpeaker(null)).isFalse();
    }

    @Test
    void getDurationMs_지속시간_계산() {
        // given
        Turn turn = Turn.startSpeaking("SPEAKER_0", 1000L, 3500L, "test");

        // when & then
        assertThat(turn.getDurationMs()).isEqualTo(2500L);
    }
}