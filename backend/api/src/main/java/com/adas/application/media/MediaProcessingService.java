package com.adas.application.media;

import com.adas.application.dto.ProcessResponse;
import com.adas.domain.diarization.DiarizationSegment;
import com.adas.domain.recording.Recording;
import com.adas.domain.speaker.SpeakerTurn;
import com.adas.domain.transcript.TranscriptSegment;
import com.adas.domain.speech.Turn;
import com.adas.domain.speech.SpeechMergeService;
import com.adas.infrastructure.external.DiarizationClient;
import com.adas.infrastructure.external.SttClient;
import com.adas.infrastructure.external.dto.DiarizationResponse;
import com.adas.infrastructure.external.dto.SttResponse;
import com.adas.infrastructure.media.MediaProbeService;
import com.adas.domain.diarization.DiarizationSegmentRepository;
import com.adas.domain.recording.RecordingRepository;
import com.adas.domain.speaker.SpeakerTurnRepository;
import com.adas.domain.transcript.TranscriptSegmentRepository;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

/**
 * 애플리케이션 서비스 계층: 업로드 파일을 받아 외부 STT/Diarization 호출, 병합, 영속화까지 담당.
 */
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "spring.jpa.hibernate.ddl-auto")
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
        Recording rec = Recording.createFromUpload("api", language, file != null ? file.getSize() : null);
        Long duration = mediaProbeService.safeProbeDurationMs(file);
        rec.specifyDuration(duration);
        rec = recordingRepository.save(rec);

        // 2) 외부 서비스 호출
        SttResponse stt = sttClient.transcribe(file, language);
        DiarizationResponse diar = diarizationClient.diarize(file, language, maxSpeakers);

        // 3) 병합(턴 생성)
        List<Turn> turns = mergeService.merge(stt, diar);

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
            TranscriptSegment t = TranscriptSegment.transcribeSegment(
                rec, w.start(), w.end(), w.text(), w.confidence(), stt.lang(), "stt");
            list.add(t);
        }
        transcriptSegmentRepository.saveAll(list);
    }

    private void persistDiarizationSegments(Recording rec, DiarizationResponse diar) {
        if (diar == null || diar.segments() == null || diar.segments().isEmpty()) {
            return;
        }
        List<DiarizationSegment> list = new ArrayList<>();
        for (DiarizationResponse.Segment s : diar.segments()) {
            DiarizationSegment d = DiarizationSegment.identifySpeaker(
                rec, s.speaker(), s.start(), s.end(), s.confidence());
            list.add(d);
        }
        diarizationSegmentRepository.saveAll(list);
    }

    private void persistSpeakerTurns(Recording rec, List<Turn> turns) {
        if (turns == null || turns.isEmpty()) {
            return;
        }
        List<SpeakerTurn> list = new ArrayList<>();
        for (Turn t : turns) {
            SpeakerTurn st = SpeakerTurn.createTurn(
                rec, t.getSpeaker(), t.getStart(), t.getEnd(), t.getText());
            list.add(st);
        }
        speakerTurnRepository.saveAll(list);
    }
}