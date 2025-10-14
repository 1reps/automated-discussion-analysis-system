package com.adas.application;

import com.adas.infrastructure.external.dto.DiarizationResponse;
import com.adas.infrastructure.external.dto.SttResponse;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * STT 단어 배열과 Diarization 세그먼트를 겹침 기준으로 라벨링하고 연속된 동일 화자 단어들을 하나의 턴으로 묶는 간단한 병합 서비스.
 */
@Service
public class SpeechMergeService {

    /**
     * STT 단어 단위 결과와 Diarization 구간을 겹침 기준으로 매칭해 화자 턴을 생성한다. 1) 각 단어를 최대로 겹치는 화자 세그먼트에 라벨링 2) 동일 화자 레이블이 연속되는 단어들을 하나의
     * 턴으로 병합
     */
    public List<Turn> merge(SttResponse stt, DiarizationResponse diar) {
        if (stt == null || stt.words() == null || stt.words().isEmpty()
            || diar == null || diar.segments() == null || diar.segments().isEmpty()) {
            return List.of();
        }

        List<SttResponse.Word> words = new ArrayList<>(stt.words());
        List<DiarizationResponse.Segment> segs = new ArrayList<>(diar.segments());
        words.sort(Comparator.comparingLong(SttResponse.Word::start));
        segs.sort(Comparator.comparingLong(DiarizationResponse.Segment::start));

        // Assign best-overlap speaker to each word.
        List<LabeledWord> labeled = new ArrayList<>();
        for (SttResponse.Word w : words) {
            DiarizationResponse.Segment best = bestOverlap(w, segs);
            String speaker = best != null ? best.speaker() : "UNKNOWN";
            labeled.add(new LabeledWord(w, speaker));
        }

        // Group contiguous words with same speaker into turns.
        List<Turn> turns = new ArrayList<>();
        Turn current = null;
        for (LabeledWord lw : labeled) {
            if (current == null || !Objects.equals(current.getSpeaker(), lw.speaker)) {
                if (current != null) {
                    turns.add(current);
                }
                current = new Turn(lw.speaker, lw.word.start(), lw.word.end(), lw.word.text());
            } else {
                current.setEnd(lw.word.end());
                current.setText(current.getText() + " " + lw.word.text());
            }
        }
        if (current != null) {
            turns.add(current);
        }
        return turns;
    }

    private static DiarizationResponse.Segment bestOverlap(SttResponse.Word w,
        List<DiarizationResponse.Segment> segments) {
        long best = Long.MIN_VALUE;
        DiarizationResponse.Segment chosen = null;
        for (DiarizationResponse.Segment s : segments) {
            long overlap = overlap(w.start(), w.end(), s.start(), s.end());
            if (overlap > best) {
                best = overlap;
                chosen = s;
            }
        }
        return chosen;
    }

    private static long overlap(long aStart, long aEnd, long bStart, long bEnd) {
        long start = Math.max(aStart, bStart);
        long end = Math.min(aEnd, bEnd);
        return end - start; // can be negative if no overlap
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Turn {

        private String speaker;
        private long start;
        private long end;
        private String text;
    }

    private record LabeledWord(SttResponse.Word word, String speaker) {

    }
}
