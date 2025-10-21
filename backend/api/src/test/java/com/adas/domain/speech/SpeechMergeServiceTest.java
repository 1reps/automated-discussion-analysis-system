package com.adas.domain.speech;

import com.adas.infrastructure.external.dto.DiarizationResponse;
import com.adas.infrastructure.external.dto.SttResponse;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.assertj.core.api.Assertions.*;

class SpeechMergeServiceTest {

    private final SpeechMergeService speechMergeService = new SpeechMergeService();

    @Test
    void merge_정상적인_STT와_Diarization_결과를_병합() {
        // given
        List<SttResponse.Word> words = List.of(
            new SttResponse.Word(1000L, 1500L, "안녕", 0.9),
            new SttResponse.Word(1500L, 2000L, "하세요", 0.8),
            new SttResponse.Word(3000L, 3500L, "반갑", 0.9),
            new SttResponse.Word(3500L, 4000L, "습니다", 0.8)
        );
        SttResponse stt = new SttResponse("ko", words);

        List<DiarizationResponse.Segment> segments = List.of(
            new DiarizationResponse.Segment(1000L, 2500L, "SPEAKER_0", 0.85),
            new DiarizationResponse.Segment(2800L, 4500L, "SPEAKER_1", 0.9)
        );
        DiarizationResponse diar = new DiarizationResponse(segments);

        // when
        List<Turn> turns = speechMergeService.merge(stt, diar);

        // then
        assertThat(turns).hasSize(2);
        
        Turn firstTurn = turns.get(0);
        assertThat(firstTurn.getSpeaker()).isEqualTo("SPEAKER_0");
        assertThat(firstTurn.getStart()).isEqualTo(1000L);
        assertThat(firstTurn.getEnd()).isEqualTo(2000L);
        assertThat(firstTurn.getText()).isEqualTo("안녕 하세요");

        Turn secondTurn = turns.get(1);
        assertThat(secondTurn.getSpeaker()).isEqualTo("SPEAKER_1");
        assertThat(secondTurn.getStart()).isEqualTo(3000L);
        assertThat(secondTurn.getEnd()).isEqualTo(4000L);
        assertThat(secondTurn.getText()).isEqualTo("반갑 습니다");
    }

    @Test
    void merge_겹치지않는_단어는_UNKNOWN_화자로_분류() {
        // given
        List<SttResponse.Word> words = List.of(
            new SttResponse.Word(5000L, 5500L, "안녕", 0.9)
        );
        SttResponse stt = new SttResponse("ko", words);

        List<DiarizationResponse.Segment> segments = List.of(
            new DiarizationResponse.Segment(1000L, 2000L, "SPEAKER_0", 0.85)
        );
        DiarizationResponse diar = new DiarizationResponse(segments);

        // when
        List<Turn> turns = speechMergeService.merge(stt, diar);

        // then
        assertThat(turns).hasSize(1);
        Turn turn = turns.get(0);
        assertThat(turn.getSpeaker()).isEqualTo("UNKNOWN");
        assertThat(turn.getText()).isEqualTo("안녕");
    }

    @Test
    void merge_null_STT는_빈_리스트_반환() {
        // given
        List<DiarizationResponse.Segment> segments = List.of(
            new DiarizationResponse.Segment(1000L, 2000L, "SPEAKER_0", 0.85)
        );
        DiarizationResponse diar = new DiarizationResponse(segments);

        // when
        List<Turn> turns = speechMergeService.merge(null, diar);

        // then
        assertThat(turns).isEmpty();
    }

    @Test
    void merge_null_Diarization은_빈_리스트_반환() {
        // given
        List<SttResponse.Word> words = List.of(
            new SttResponse.Word(1000L, 1500L, "안녕", 0.9)
        );
        SttResponse stt = new SttResponse("ko", words);

        // when
        List<Turn> turns = speechMergeService.merge(stt, null);

        // then
        assertThat(turns).isEmpty();
    }

    @Test
    void merge_빈_단어_리스트는_빈_리스트_반환() {
        // given
        SttResponse stt = new SttResponse("ko", List.of());
        List<DiarizationResponse.Segment> segments = List.of(
            new DiarizationResponse.Segment(1000L, 2000L, "SPEAKER_0", 0.85)
        );
        DiarizationResponse diar = new DiarizationResponse(segments);

        // when
        List<Turn> turns = speechMergeService.merge(stt, diar);

        // then
        assertThat(turns).isEmpty();
    }

    @Test
    void merge_단일_화자의_연속된_단어들은_하나의_턴으로_병합() {
        // given
        List<SttResponse.Word> words = List.of(
            new SttResponse.Word(1000L, 1200L, "안녕", 0.9),
            new SttResponse.Word(1200L, 1400L, "하세요", 0.8),
            new SttResponse.Word(1400L, 1600L, "오늘", 0.9),
            new SttResponse.Word(1600L, 1800L, "날씨가", 0.8),
            new SttResponse.Word(1800L, 2000L, "좋네요", 0.9)
        );
        SttResponse stt = new SttResponse("ko", words);

        List<DiarizationResponse.Segment> segments = List.of(
            new DiarizationResponse.Segment(1000L, 2500L, "SPEAKER_0", 0.85)
        );
        DiarizationResponse diar = new DiarizationResponse(segments);

        // when
        List<Turn> turns = speechMergeService.merge(stt, diar);

        // then
        assertThat(turns).hasSize(1);
        Turn turn = turns.get(0);
        assertThat(turn.getSpeaker()).isEqualTo("SPEAKER_0");
        assertThat(turn.getStart()).isEqualTo(1000L);
        assertThat(turn.getEnd()).isEqualTo(2000L);
        assertThat(turn.getText()).isEqualTo("안녕 하세요 오늘 날씨가 좋네요");
    }

    @Test
    void merge_시간순으로_정렬되지_않은_입력도_정상_처리() {
        // given - 의도적으로 순서를 섞음
        List<SttResponse.Word> words = List.of(
            new SttResponse.Word(3000L, 3500L, "반갑", 0.9),
            new SttResponse.Word(1000L, 1500L, "안녕", 0.9),
            new SttResponse.Word(1500L, 2000L, "하세요", 0.8),
            new SttResponse.Word(3500L, 4000L, "습니다", 0.8)
        );
        SttResponse stt = new SttResponse("ko", words);

        List<DiarizationResponse.Segment> segments = List.of(
            new DiarizationResponse.Segment(2800L, 4500L, "SPEAKER_1", 0.9),
            new DiarizationResponse.Segment(1000L, 2500L, "SPEAKER_0", 0.85)
        );
        DiarizationResponse diar = new DiarizationResponse(segments);

        // when
        List<Turn> turns = speechMergeService.merge(stt, diar);

        // then
        assertThat(turns).hasSize(2);
        
        // 시간순으로 정렬되어 반환되어야 함
        Turn firstTurn = turns.get(0);
        assertThat(firstTurn.getSpeaker()).isEqualTo("SPEAKER_0");
        assertThat(firstTurn.getText()).isEqualTo("안녕 하세요");

        Turn secondTurn = turns.get(1);
        assertThat(secondTurn.getSpeaker()).isEqualTo("SPEAKER_1");
        assertThat(secondTurn.getText()).isEqualTo("반갑 습니다");
    }
}