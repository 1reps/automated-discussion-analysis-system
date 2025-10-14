package com.adas.application;

import com.adas.infrastructure.external.dto.DiarizationResponse;
import com.adas.infrastructure.external.dto.SttResponse;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SpeechMergeServiceTest {

  private final SpeechMergeService service = new SpeechMergeService();

  @Test
  @DisplayName("단어와 화자 구간을 겹침 기준으로 매칭해 턴을 생성한다")
  void merge_basic() {
    SttResponse stt =
        new SttResponse(
            "ko",
            List.of(
                new SttResponse.Word(0, 500, "안녕", 0.9),
                new SttResponse.Word(600, 1000, "하세요", 0.9),
                new SttResponse.Word(1200, 1500, "반가워요", 0.95)));

    DiarizationResponse diar =
        new DiarizationResponse(
            List.of(
                new DiarizationResponse.Segment(0, 800, "SPEAKER_1", 0.7),
                new DiarizationResponse.Segment(800, 2000, "SPEAKER_2", 0.8)));

    var turns = service.merge(stt, diar);

    assertThat(turns).hasSize(2);
    assertThat(turns.get(0).getSpeaker()).isEqualTo("SPEAKER_1");
    assertThat(turns.get(0).getText()).isEqualTo("안녕 하세요");
    assertThat(turns.get(1).getSpeaker()).isEqualTo("SPEAKER_2");
    assertThat(turns.get(1).getText()).isEqualTo("반가워요");
  }
}
