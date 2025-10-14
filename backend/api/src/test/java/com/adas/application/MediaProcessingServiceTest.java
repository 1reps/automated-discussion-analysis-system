package com.adas.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.adas.application.dto.ProcessResponse;
import com.adas.infrastructure.external.DiarizationClient;
import com.adas.infrastructure.external.SttClient;
import com.adas.infrastructure.external.dto.DiarizationResponse;
import com.adas.infrastructure.external.dto.SttResponse;
import com.adas.infrastructure.media.MediaProbeService;
import com.adas.infrastructure.repository.DiarizationSegmentRepository;
import com.adas.infrastructure.repository.RecordingRepository;
import com.adas.infrastructure.repository.SpeakerTurnRepository;
import com.adas.infrastructure.repository.TranscriptSegmentRepository;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@SpringBootTest(classes = com.adas.presentation.ApiApplication.class)
@ActiveProfiles("test")
class MediaProcessingServiceTest {

    @Autowired
    private MediaProcessingService service;

    @Autowired
    private RecordingRepository recordingRepository;
    @Autowired
    private TranscriptSegmentRepository transcriptRepository;
    @Autowired
    private DiarizationSegmentRepository diarRepository;
    @Autowired
    private SpeakerTurnRepository turnRepository;

    @MockitoBean
    private SttClient sttClient;
    @MockitoBean
    private DiarizationClient diarClient;
    @MockitoBean
    private MediaProbeService mediaProbeService;

    @Test
    @Transactional
    @DisplayName("업로드 처리: STT/DIAR 모킹 → 병합/저장 완료")
    void process_savesAll() {
        // given: 외부 서비스 응답 모킹
        SttResponse stt =
            new SttResponse(
                "ko",
                List.of(
                    new SttResponse.Word(0, 500, "안녕", 0.9),
                    new SttResponse.Word(600, 1000, "하세요", 0.9)));
        DiarizationResponse diar =
            new DiarizationResponse(
                List.of(new DiarizationResponse.Segment(0, 1000, "SPEAKER_1", 0.8)));

        when(sttClient.transcribe(any(MultipartFile.class), any())).thenReturn(stt);
        when(diarClient.diarize(any(MultipartFile.class), any(), any())).thenReturn(diar);
        when(mediaProbeService.safeProbeDurationMs(any(MultipartFile.class))).thenReturn(1000L);

        MockMultipartFile file =
            new MockMultipartFile(
                "file", "test.wav", "audio/wav", "dummy".getBytes(StandardCharsets.UTF_8));

        // when
        ProcessResponse resp = service.process(file, "ko", 2);

        // then: 응답
        assertThat(resp.recordingId()).isNotBlank();
        assertThat(resp.lang()).isEqualTo("ko");
        assertThat(resp.words()).hasSize(2);
        assertThat(resp.segments()).hasSize(1);
        assertThat(resp.turns()).hasSize(1);

        // then: 저장 확인
        Long id = Long.valueOf(resp.recordingId());
        assertThat(recordingRepository.findById(id)).isPresent();
        assertThat(transcriptRepository.findByRecordingIdOrderByStartMsAsc(id)).hasSize(2);
        assertThat(diarRepository.findByRecordingIdOrderByStartMsAsc(id)).hasSize(1);
        assertThat(turnRepository.findByRecordingIdOrderByStartMsAsc(id)).hasSize(1);
    }
}
