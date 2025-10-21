package com.adas.application.media;

import com.adas.application.dto.ProcessResponse;
import com.adas.domain.diarization.DiarizationSegmentRepository;
import com.adas.domain.recording.Recording;
import com.adas.domain.recording.RecordingRepository;
import com.adas.domain.speaker.SpeakerTurnRepository;
import com.adas.domain.speech.SpeechMergeService;
import com.adas.domain.speech.Turn;
import com.adas.domain.transcript.TranscriptSegmentRepository;
import com.adas.infrastructure.external.DiarizationClient;
import com.adas.infrastructure.external.SttClient;
import com.adas.infrastructure.external.dto.DiarizationResponse;
import com.adas.infrastructure.external.dto.SttResponse;
import com.adas.infrastructure.media.MediaProbeService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MediaProcessingServiceTest {

    @Mock
    private SttClient sttClient;
    
    @Mock
    private DiarizationClient diarizationClient;
    
    @Mock
    private SpeechMergeService mergeService;
    
    @Mock
    private RecordingRepository recordingRepository;
    
    @Mock
    private TranscriptSegmentRepository transcriptSegmentRepository;
    
    @Mock
    private DiarizationSegmentRepository diarizationSegmentRepository;
    
    @Mock
    private SpeakerTurnRepository speakerTurnRepository;
    
    @Mock
    private MediaProbeService mediaProbeService;
    
    @Mock
    private MultipartFile multipartFile;

    @InjectMocks
    private MediaProcessingService mediaProcessingService;

    @Test
    void process_정상적인_파일_처리_흐름() {
        // given
        String language = "ko";
        Integer maxSpeakers = 2;
        Long fileSize = 1024L;
        Long duration = 30000L;
        
        Recording savedRecording = Recording.createFromUpload("api", language, fileSize);
        savedRecording.specifyDuration(duration);
        
        List<SttResponse.Word> words = List.of(
            new SttResponse.Word(1000L, 1500L, "안녕", 0.9),
            new SttResponse.Word(1500L, 2000L, "하세요", 0.8)
        );
        SttResponse sttResponse = new SttResponse(language, words);
        
        List<DiarizationResponse.Segment> segments = List.of(
            new DiarizationResponse.Segment(1000L, 2500L, "SPEAKER_0", 0.85)
        );
        DiarizationResponse diarResponse = new DiarizationResponse(segments);
        
        List<Turn> turns = List.of(
            Turn.startSpeaking("SPEAKER_0", 1000L, 2000L, "안녕 하세요")
        );

        when(multipartFile.getSize()).thenReturn(fileSize);
        when(mediaProbeService.safeProbeDurationMs(multipartFile)).thenReturn(duration);
        when(recordingRepository.save(any(Recording.class))).thenReturn(savedRecording);
        when(sttClient.transcribe(multipartFile, language)).thenReturn(sttResponse);
        when(diarizationClient.diarize(multipartFile, language, maxSpeakers)).thenReturn(diarResponse);
        when(mergeService.merge(sttResponse, diarResponse)).thenReturn(turns);

        // when
        ProcessResponse result = mediaProcessingService.process(multipartFile, language, maxSpeakers);

        // then
        assertThat(result.recordingId()).isNotNull();
        assertThat(result.lang()).isEqualTo(language);
        assertThat(result.words()).isEqualTo(words);
        assertThat(result.segments()).isEqualTo(segments);
        assertThat(result.turns()).isEqualTo(turns);

        verify(recordingRepository).save(any(Recording.class));
        verify(transcriptSegmentRepository).saveAll(anyList());
        verify(diarizationSegmentRepository).saveAll(anyList());
        verify(speakerTurnRepository).saveAll(anyList());
    }

    @Test
    void process_null_파일도_처리_가능() {
        // given
        String language = "ko";
        Integer maxSpeakers = 2;
        
        Recording savedRecording = Recording.createFromUpload("api", language, null);
        SttResponse sttResponse = new SttResponse(language, List.of());
        DiarizationResponse diarResponse = new DiarizationResponse(List.of());
        List<Turn> turns = List.of();

        when(mediaProbeService.safeProbeDurationMs(null)).thenReturn(null);
        when(recordingRepository.save(any(Recording.class))).thenReturn(savedRecording);
        when(sttClient.transcribe(null, language)).thenReturn(sttResponse);
        when(diarizationClient.diarize(null, language, maxSpeakers)).thenReturn(diarResponse);
        when(mergeService.merge(sttResponse, diarResponse)).thenReturn(turns);

        // when
        ProcessResponse result = mediaProcessingService.process(null, language, maxSpeakers);

        // then
        assertThat(result.recordingId()).isNotNull();
        assertThat(result.lang()).isEqualTo(language);
        assertThat(result.words()).isEmpty();
        assertThat(result.segments()).isEmpty();
        assertThat(result.turns()).isEmpty();
    }

    @Test
    void process_null_STT_응답_처리() {
        // given
        String language = "ko";
        Recording savedRecording = Recording.createFromUpload("api", language, 1024L);
        DiarizationResponse diarResponse = new DiarizationResponse(List.of());
        List<Turn> turns = List.of();

        when(multipartFile.getSize()).thenReturn(1024L);
        when(mediaProbeService.safeProbeDurationMs(multipartFile)).thenReturn(30000L);
        when(recordingRepository.save(any(Recording.class))).thenReturn(savedRecording);
        when(sttClient.transcribe(multipartFile, language)).thenReturn(null);
        when(diarizationClient.diarize(multipartFile, language, null)).thenReturn(diarResponse);
        when(mergeService.merge(null, diarResponse)).thenReturn(turns);

        // when
        ProcessResponse result = mediaProcessingService.process(multipartFile, language, null);

        // then
        assertThat(result.lang()).isNull();
        assertThat(result.words()).isNull();
        
        // STT 결과가 null이어도 transcriptSegmentRepository.saveAll()은 호출되지 않아야 함
        verify(transcriptSegmentRepository, never()).saveAll(anyList());
        verify(diarizationSegmentRepository, never()).saveAll(anyList()); // 빈 리스트여서 호출되지 않음
        verify(speakerTurnRepository, never()).saveAll(anyList());
    }

    @Test
    void process_빈_결과들도_정상_처리() {
        // given
        String language = "ko";
        Recording savedRecording = Recording.createFromUpload("api", language, 1024L);
        SttResponse sttResponse = new SttResponse(language, List.of());
        DiarizationResponse diarResponse = new DiarizationResponse(List.of());
        List<Turn> turns = List.of();

        when(multipartFile.getSize()).thenReturn(1024L);
        when(mediaProbeService.safeProbeDurationMs(multipartFile)).thenReturn(30000L);
        when(recordingRepository.save(any(Recording.class))).thenReturn(savedRecording);
        when(sttClient.transcribe(multipartFile, language)).thenReturn(sttResponse);
        when(diarizationClient.diarize(multipartFile, language, null)).thenReturn(diarResponse);
        when(mergeService.merge(sttResponse, diarResponse)).thenReturn(turns);

        // when
        ProcessResponse result = mediaProcessingService.process(multipartFile, language, null);

        // then
        assertThat(result.words()).isEmpty();
        assertThat(result.segments()).isEmpty();
        assertThat(result.turns()).isEmpty();
        
        // 빈 리스트들도 saveAll 메서드는 호출되지 않음
        verify(transcriptSegmentRepository, never()).saveAll(anyList());
        verify(diarizationSegmentRepository, never()).saveAll(anyList());
        verify(speakerTurnRepository, never()).saveAll(anyList());
    }
}