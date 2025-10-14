package com.adas.presentation.media;

import com.adas.application.RecordingQueryService;
import com.adas.application.dto.RecordingResponse;
import com.adas.application.dto.SegmentsResponse;
import com.adas.application.dto.SpeakerTurnResponse;
import com.adas.presentation.ApiResponse;
import com.adas.presentation.docs.RecordingApiDocs;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Recording 및 세그먼트/턴 조회용 컨트롤러.
 */
@RestController
@RequestMapping("/api/v1/recordings")
@RequiredArgsConstructor
public class RecordingController implements RecordingApiDocs {

    private final RecordingQueryService service;

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<RecordingResponse>> getRecording(@PathVariable Long id) {
        return ApiResponse.success(service.getRecording(id));
    }

    @GetMapping("/{id}/segments")
    public ResponseEntity<ApiResponse<SegmentsResponse>> getSegments(@PathVariable Long id) {
        return ApiResponse.success(service.getSegments(id));
    }

    @GetMapping("/{id}/turns")
    public ResponseEntity<ApiResponse<List<SpeakerTurnResponse>>> getTurns(@PathVariable Long id) {
        return ApiResponse.success(service.getTurns(id));
    }
}
