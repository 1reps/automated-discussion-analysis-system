package com.adas.presentation.media;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.adas.application.media.MediaProcessingService;
import com.adas.domain.speech.Turn;
import com.adas.application.dto.ProcessResponse;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(classes = com.adas.presentation.ApiApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class MediaControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private MediaProcessingService mediaProcessingService;

    @Test
    @DisplayName("/media/process 성공 시 ApiResponse 래핑으로 200을 반환한다")
    void process_ok() throws Exception {
        ProcessResponse resp =
            new ProcessResponse("1", "ko", List.of(), List.of(), List.of(Turn.startSpeaking("S1", 0, 1000, "hi")));
        when(mediaProcessingService.process(any(), eq("ko"), eq(2))).thenReturn(resp);

        MockMultipartFile file =
            new MockMultipartFile(
                "file", "test.wav", "audio/wav", "x".getBytes(StandardCharsets.UTF_8));

        mvc.perform(
                multipart("/api/v1/media/process")
                    .file(file)
                    .param("language", "ko")
                    .param("maxSpeakers", "2")
                    .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.isSuccess").value(true))
            .andExpect(jsonPath("$.data.recordingId").value("1"))
            .andExpect(jsonPath("$.data.lang").value("ko"));
    }
}
