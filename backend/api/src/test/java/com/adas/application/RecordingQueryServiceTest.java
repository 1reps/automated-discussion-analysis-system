package com.adas.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.adas.application.dto.RecordingResponse;
import com.adas.application.dto.SegmentsResponse;
import com.adas.application.dto.SpeakerTurnResponse;
import com.adas.infrastructure.external.DiarizationClient;
import com.adas.infrastructure.external.SttClient;
import com.adas.infrastructure.external.dto.DiarizationResponse;
import com.adas.infrastructure.external.dto.SttResponse;
import com.adas.infrastructure.media.MediaProbeService;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.multipart.MultipartFile;

@SpringBootTest(classes = com.adas.presentation.ApiApplication.class)
@ActiveProfiles("test")
class RecordingQueryServiceTest {

    @Autowired
    private MediaProcessingService processingService;
    @Autowired
    private RecordingQueryService queryService;

    @MockitoBean
    private SttClient sttClient;
    @MockitoBean
    private DiarizationClient diarClient;
    @MockitoBean
    private MediaProbeService mediaProbeService;

    @Test
    @DisplayName("저장된 Recording을 조회 API 서비스로 읽을 수 있다")
    void query_after_process() {
        // 준비: 처리 파이프라인으로 하나 저장
        SttResponse stt =
            new SttResponse(
                "ko",
                List.of(new SttResponse.Word(0, 500, "테스트", 0.9)));
        DiarizationResponse diar =
            new DiarizationResponse(List.of(new DiarizationResponse.Segment(0, 500, "S1", 0.8)));
        when(sttClient.transcribe(any(MultipartFile.class), any())).thenReturn(stt);
        when(diarClient.diarize(any(MultipartFile.class), any(), any())).thenReturn(diar);
        when(mediaProbeService.safeProbeDurationMs(any(MultipartFile.class))).thenReturn(500L);

        MockMultipartFile file =
            new MockMultipartFile(
                "file", "test.wav", "audio/wav", "x".getBytes(StandardCharsets.UTF_8));
        var process = processingService.process(file, "ko", 1);
        Long id = Long.valueOf(process.recordingId());

        // 검증: Recording 조회
        RecordingResponse rec = queryService.getRecording(id);
        assertThat(rec.id()).isEqualTo(id);
        assertThat(rec.durationMs()).isEqualTo(500L);

        // 검증: 세그먼트 조회
        SegmentsResponse seg = queryService.getSegments(id);
        assertThat(seg.transcripts()).hasSize(1);
        assertThat(seg.diarization()).hasSize(1);

        // 검증: 턴 조회
        List<SpeakerTurnResponse> turns = queryService.getTurns(id);
        assertThat(turns).hasSize(1);
        assertThat(turns.get(0).speakerLabel()).isEqualTo("S1");
    }
}
