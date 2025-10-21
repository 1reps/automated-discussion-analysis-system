package com.adas.presentation.media;

import com.adas.application.recording.RecordingQueryService;
import com.adas.application.dto.SegmentsResponse;
import com.adas.application.dto.SpeakerTurnResponse;
import com.adas.application.dto.TranscriptSegmentResponse;
import com.adas.presentation.docs.TranscriptApiDocs;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Recorder.vue 호환용 트랜스크립트 폴링 엔드포인트. - GET /api/v1/recordings/{id}/transcript → { status: 'completed', transcripts: [],
 * speaker_turns: [] }
 */
@RestController
@RequestMapping("/api/v1/recordings")
@RequiredArgsConstructor
public class TranscriptController implements TranscriptApiDocs {

    private final RecordingQueryService queryService;

    @GetMapping("/{id}/transcript")
    public ResponseEntity<Map<String, Object>> transcript(@PathVariable Long id) {
        SegmentsResponse segs = queryService.getSegments(id);
        List<SpeakerTurnResponse> turns = queryService.getTurns(id);

        List<Map<String, Object>> transcripts =
            segs.transcripts().stream()
                .map(TranscriptController::toTranscriptMap)
                .toList();
        List<Map<String, Object>> speakerTurns =
            turns.stream().map(TranscriptController::toTurnMap).toList();

        Map<String, Object> body = new HashMap<>();
        body.put("status", "completed");
        body.put("transcripts", transcripts);
        body.put("speaker_turns", speakerTurns);
        body.put("logs", List.of());
        return ResponseEntity.ok(body);
    }

    private static Map<String, Object> toTranscriptMap(TranscriptSegmentResponse t) {
        Map<String, Object> m = new HashMap<>();
        m.put("start_ms", t.startMs());
        m.put("end_ms", t.endMs());
        m.put("text", t.text());
        m.put("confidence", t.confidence());
        m.put("language", t.language());
        m.put("provider", t.provider());
        return m;
    }

    private static Map<String, Object> toTurnMap(SpeakerTurnResponse s) {
        Map<String, Object> m = new HashMap<>();
        m.put("start_ms", s.startMs());
        m.put("end_ms", s.endMs());
        m.put("speaker_label", s.speakerLabel());
        m.put("text", s.text());
        return m;
    }
}
