package com.adas.presentation.media;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.adas.application.RecordingQueryService;
import com.adas.application.dto.DiarizationSegmentResponse;
import com.adas.application.dto.RecordingResponse;
import com.adas.application.dto.SegmentsResponse;
import com.adas.application.dto.TranscriptSegmentResponse;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(classes = com.adas.presentation.ApiApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class RecordingControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private RecordingQueryService service;

    @Test
    @DisplayName("/recordings/{id} 기본 정보 조회는 ApiResponse로 200 반환")
    void getRecording_ok() throws Exception {
        when(service.getRecording(anyLong()))
            .thenReturn(new RecordingResponse(1L, "api", "ko", 10L, 1000L, Instant.now()));

        mvc.perform(get("/api/v1/recordings/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.isSuccess").value(true))
            .andExpect(jsonPath("$.data.id").value(1));
    }

    @Test
    @DisplayName("/recordings/{id}/segments는 transcripts/diarization 배열을 반환")
    void getSegments_ok() throws Exception {
        var segs =
            new SegmentsResponse(
                List.of(new TranscriptSegmentResponse(0L, 1000L, "안녕", 0.9, "ko", "stt")),
                List.of(new DiarizationSegmentResponse(0L, 1000L, "S1", 0.8)));
        when(service.getSegments(anyLong())).thenReturn(segs);

        mvc.perform(get("/api/v1/recordings/1/segments"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.isSuccess").value(true))
            .andExpect(jsonPath("$.data.transcripts[0].text").value("안녕"))
            .andExpect(jsonPath("$.data.diarization[0].speakerLabel").value("S1"));
    }
}
