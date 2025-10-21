package com.adas.application.recording;

import com.adas.application.dto.DiarizationSegmentResponse;
import com.adas.application.dto.RecordingResponse;
import com.adas.application.dto.SegmentsResponse;
import com.adas.application.dto.SpeakerTurnResponse;
import com.adas.application.dto.TranscriptSegmentResponse;
import com.adas.common.exception.NotFoundException;
import com.adas.domain.diarization.DiarizationSegment;
import com.adas.domain.recording.Recording;
import com.adas.domain.recording.RecordingRepository;
import com.adas.domain.speaker.SpeakerTurn;
import com.adas.domain.transcript.TranscriptSegment;
import com.adas.domain.diarization.DiarizationSegmentRepository;
import com.adas.domain.speaker.SpeakerTurnRepository;
import com.adas.domain.transcript.TranscriptSegmentRepository;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 조회용 애플리케이션 서비스: Recording 및 세그먼트/턴 조회.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@ConditionalOnProperty(name = "spring.jpa.hibernate.ddl-auto")
public class RecordingQueryService {

    private final RecordingRepository recordingRepository;
    private final TranscriptSegmentRepository transcriptRepo;
    private final DiarizationSegmentRepository diarizationRepo;
    private final SpeakerTurnRepository turnRepo;

    public RecordingResponse getRecording(Long id) {
        Recording r =
            recordingRepository
                .findById(id)
                .orElseThrow(() -> new NotFoundException("Recording not found: " + id));
        return new RecordingResponse(
            r.getId(), r.getSource(), r.getLanguage(), r.getSizeBytes(), r.getDurationMs(), r.getCreatedAt());
    }

    public SegmentsResponse getSegments(Long id) {
        List<TranscriptSegmentResponse> transcripts =
            transcriptRepo.findByRecordingIdOrderByStartMsAsc(id).stream()
                .map(RecordingQueryService::toTranscriptDto)
                .collect(Collectors.toList());
        List<DiarizationSegmentResponse> diarization =
            diarizationRepo.findByRecordingIdOrderByStartMsAsc(id).stream()
                .map(RecordingQueryService::toDiarDto)
                .collect(Collectors.toList());
        return new SegmentsResponse(transcripts, diarization);
    }

    public List<SpeakerTurnResponse> getTurns(Long id) {
        return turnRepo.findByRecordingIdOrderByStartMsAsc(id).stream()
            .map(RecordingQueryService::toTurnDto)
            .collect(Collectors.toList());
    }

    private static TranscriptSegmentResponse toTranscriptDto(TranscriptSegment t) {
        return new TranscriptSegmentResponse(
            t.getStartMs(), t.getEndMs(), t.getText(), t.getConfidence(), t.getLanguage(), t.getProvider());
    }

    private static DiarizationSegmentResponse toDiarDto(DiarizationSegment d) {
        return new DiarizationSegmentResponse(d.getStartMs(), d.getEndMs(), d.getSpeakerLabel(), d.getConfidence());
    }

    private static SpeakerTurnResponse toTurnDto(SpeakerTurn s) {
        return new SpeakerTurnResponse(s.getStartMs(), s.getEndMs(), s.getSpeakerLabel(), s.getText());
    }
}