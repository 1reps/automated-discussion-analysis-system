package com.adas.presentation.media;

import com.adas.application.MediaProcessingService;
import com.adas.application.dto.ProcessResponse;
import com.adas.presentation.docs.IngestApiDocs;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * 프런트 Recorder.vue 호환용 업로드 엔드포인트. - POST /api/ingest/audio → { recording_id, transcript_url } - 이후
 * /api/v1/recordings/{id}/transcript를 폴링하면 완료 결과를 즉시 받을 수 있다.
 */
@RestController
@RequestMapping("/api/ingest")
@RequiredArgsConstructor
public class IngestController implements IngestApiDocs {

    private final MediaProcessingService mediaProcessingService;

    @PostMapping(path = "/audio", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Object>> ingest(
        @RequestPart("file") MultipartFile file,
        @RequestParam(value = "language", required = false) String language,
        @RequestParam(value = "source", required = false) String source
    ) {
        ProcessResponse resp = mediaProcessingService.process(file, language, null);
        Map<String, Object> body = new HashMap<>();
        body.put("recording_id", resp.recordingId());
        body.put("transcript_url", "/api/v1/recordings/" + resp.recordingId() + "/transcript");
        return ResponseEntity.ok(body);
    }
}
