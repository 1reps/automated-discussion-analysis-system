package com.adas.application;

import com.adas.application.dto.ProcessResponse;
import com.adas.domain.audio.DiarizationSegmentEntity;
import com.adas.domain.audio.Recording;
import com.adas.domain.audio.SpeakerTurn;
import com.adas.domain.audio.TranscriptSegment;
import com.adas.infrastructure.external.DiarizationClient;
import com.adas.infrastructure.external.SttClient;
import com.adas.infrastructure.external.dto.DiarizationResponse;
import com.adas.infrastructure.external.dto.SttResponse;
import com.adas.infrastructure.media.MediaProbeService;
import com.adas.infrastructure.repository.DiarizationSegmentRepository;
import com.adas.infrastructure.repository.RecordingRepository;
import com.adas.infrastructure.repository.SpeakerTurnRepository;
import com.adas.infrastructure.repository.TranscriptSegmentRepository;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

/**
 * 애플리케이션 서비스 계층: 업로드 파일을 받아 외부 STT/Diarization 호출, 병합, 영속화까지 담당.
 */
@Service
@RequiredArgsConstructor
public class MediaProcessingService {

    private final SttClient sttClient;
    private final DiarizationClient diarizationClient;
    private final SpeechMergeService mergeService;

    private final RecordingRepository recordingRepository;
    private final TranscriptSegmentRepository transcriptSegmentRepository;
    private final DiarizationSegmentRepository diarizationSegmentRepository;
    private final SpeakerTurnRepository speakerTurnRepository;
    private final MediaProbeService mediaProbeService;

    /**
     * 업로드 파일 처리 전체 흐름.
     */
    @Transactional
    public ProcessResponse process(MultipartFile file, String language, Integer maxSpeakers) {
        // 1) Recording 생성 저장
        Recording rec = new Recording();
        rec.setSource("api");
        rec.setLanguage(language);
        rec.setSizeBytes(file != null ? file.getSize() : null);
        Long duration = mediaProbeService.safeProbeDurationMs(file);
        rec.setDurationMs(duration);
        rec = recordingRepository.save(rec);

        // 2) 외부 서비스 호출
        SttResponse stt = sttClient.transcribe(file, language);
        DiarizationResponse diar = diarizationClient.diarize(file, language, maxSpeakers);

        // 3) 병합(턴 생성)
        List<SpeechMergeService.Turn> turns = mergeService.merge(stt, diar);

        // 4) 세그먼트/턴 영속화
        persistTranscriptSegments(rec, stt);
        persistDiarizationSegments(rec, diar);
        persistSpeakerTurns(rec, turns);

        // 5) 응답 DTO 구성
        return new ProcessResponse(
            String.valueOf(rec.getId()),
            stt != null ? stt.lang() : null,
            stt != null ? stt.words() : null,
            diar != null ? diar.segments() : null,
            turns
        );
    }

    private void persistTranscriptSegments(Recording rec, SttResponse stt) {
        if (stt == null || stt.words() == null || stt.words().isEmpty()) {
            return;
        }
        List<TranscriptSegment> list = new ArrayList<>();
        for (SttResponse.Word w : stt.words()) {
            TranscriptSegment t = new TranscriptSegment();
            t.setRecording(rec);
            t.setStartMs(w.start());
            t.setEndMs(w.end());
            t.setText(w.text());
            t.setConfidence(w.confidence());
            t.setLanguage(stt.lang());
            t.setProvider("stt");
            list.add(t);
        }
        transcriptSegmentRepository.saveAll(list);
    }

    private void persistDiarizationSegments(Recording rec, DiarizationResponse diar) {
        if (diar == null || diar.segments() == null || diar.segments().isEmpty()) {
            return;
        }
        List<DiarizationSegmentEntity> list = new ArrayList<>();
        for (DiarizationResponse.Segment s : diar.segments()) {
            DiarizationSegmentEntity d = new DiarizationSegmentEntity();
            d.setRecording(rec);
            d.setSpeakerLabel(s.speaker());
            d.setStartMs(s.start());
            d.setEndMs(s.end());
            d.setConfidence(s.confidence());
            list.add(d);
        }
        diarizationSegmentRepository.saveAll(list);
    }

    private void persistSpeakerTurns(Recording rec, List<SpeechMergeService.Turn> turns) {
        if (turns == null || turns.isEmpty()) {
            return;
        }
        List<SpeakerTurn> list = new ArrayList<>();
        for (SpeechMergeService.Turn t : turns) {
            SpeakerTurn st = new SpeakerTurn();
            st.setRecording(rec);
            st.setSpeakerLabel(t.getSpeaker());
            st.setStartMs(t.getStart());
            st.setEndMs(t.getEnd());
            st.setText(t.getText());
            list.add(st);
        }
        speakerTurnRepository.saveAll(list);
    }
}
