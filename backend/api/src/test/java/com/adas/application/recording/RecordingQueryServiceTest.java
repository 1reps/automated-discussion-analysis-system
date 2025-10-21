package com.adas.application.recording;

import com.adas.application.dto.DiarizationSegmentResponse;
import com.adas.application.dto.RecordingResponse;
import com.adas.application.dto.SegmentsResponse;
import com.adas.application.dto.SpeakerTurnResponse;
import com.adas.application.dto.TranscriptSegmentResponse;
import com.adas.common.exception.NotFoundException;
import com.adas.domain.diarization.DiarizationSegment;
import com.adas.domain.diarization.DiarizationSegmentRepository;
import com.adas.domain.recording.Recording;
import com.adas.domain.recording.RecordingRepository;
import com.adas.domain.speaker.SpeakerTurn;
import com.adas.domain.speaker.SpeakerTurnRepository;
import com.adas.domain.transcript.TranscriptSegment;
import com.adas.domain.transcript.TranscriptSegmentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RecordingQueryServiceTest {

    @Mock
    private RecordingRepository recordingRepository;
    
    @Mock
    private TranscriptSegmentRepository transcriptRepo;
    
    @Mock
    private DiarizationSegmentRepository diarizationRepo;
    
    @Mock
    private SpeakerTurnRepository turnRepo;

    @InjectMocks
    private RecordingQueryService recordingQueryService;

    @Test
    void getRecording_존재하는_ID로_조회_성공() {
        // given
        Long recordingId = 1L;
        Recording recording = Recording.createFromUpload("api", "ko", 1024L);
        recording.specifyDuration(30000L);
        
        when(recordingRepository.findById(recordingId)).thenReturn(Optional.of(recording));

        // when
        RecordingResponse result = recordingQueryService.getRecording(recordingId);

        // then
        assertThat(result.id()).isEqualTo(recording.getId());
        assertThat(result.source()).isEqualTo("api");
        assertThat(result.language()).isEqualTo("ko");
        assertThat(result.sizeBytes()).isEqualTo(1024L);
        assertThat(result.durationMs()).isEqualTo(30000L);
        assertThat(result.createdAt()).isEqualTo(recording.getCreatedAt());
    }

    @Test
    void getRecording_존재하지않는_ID로_조회_예외발생() {
        // given
        Long recordingId = 999L;
        when(recordingRepository.findById(recordingId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> recordingQueryService.getRecording(recordingId))
            .isInstanceOf(NotFoundException.class)
            .hasMessage("Recording not found: 999");
    }

    @Test
    void getSegments_전사와_화자분리_세그먼트_조회() {
        // given
        Long recordingId = 1L;
        Recording recording = Recording.createFromUpload("api", "ko", 1024L);
        
        TranscriptSegment transcriptSegment = TranscriptSegment.transcribeSegment(
            recording, 1000L, 2000L, "안녕하세요", 0.9, "ko", "google");
        DiarizationSegment diarizationSegment = DiarizationSegment.identifySpeaker(
            recording, "SPEAKER_0", 1000L, 2000L, 0.85);

        when(transcriptRepo.findByRecordingIdOrderByStartMsAsc(recordingId))
            .thenReturn(List.of(transcriptSegment));
        when(diarizationRepo.findByRecordingIdOrderByStartMsAsc(recordingId))
            .thenReturn(List.of(diarizationSegment));

        // when
        SegmentsResponse result = recordingQueryService.getSegments(recordingId);

        // then
        assertThat(result.transcripts()).hasSize(1);
        TranscriptSegmentResponse transcriptDto = result.transcripts().get(0);
        assertThat(transcriptDto.startMs()).isEqualTo(1000L);
        assertThat(transcriptDto.endMs()).isEqualTo(2000L);
        assertThat(transcriptDto.text()).isEqualTo("안녕하세요");
        assertThat(transcriptDto.confidence()).isEqualTo(0.9);
        assertThat(transcriptDto.language()).isEqualTo("ko");
        assertThat(transcriptDto.provider()).isEqualTo("google");

        assertThat(result.diarization()).hasSize(1);
        DiarizationSegmentResponse diarizationDto = result.diarization().get(0);
        assertThat(diarizationDto.startMs()).isEqualTo(1000L);
        assertThat(diarizationDto.endMs()).isEqualTo(2000L);
        assertThat(diarizationDto.speakerLabel()).isEqualTo("SPEAKER_0");
        assertThat(diarizationDto.confidence()).isEqualTo(0.85);
    }

    @Test
    void getTurns_화자_턴_리스트_조회() {
        // given
        Long recordingId = 1L;
        Recording recording = Recording.createFromUpload("api", "ko", 1024L);
        
        SpeakerTurn speakerTurn = SpeakerTurn.createTurn(
            recording, "SPEAKER_0", 1000L, 2000L, "안녕하세요 반갑습니다");

        when(turnRepo.findByRecordingIdOrderByStartMsAsc(recordingId))
            .thenReturn(List.of(speakerTurn));

        // when
        List<SpeakerTurnResponse> result = recordingQueryService.getTurns(recordingId);

        // then
        assertThat(result).hasSize(1);
        SpeakerTurnResponse turnDto = result.get(0);
        assertThat(turnDto.startMs()).isEqualTo(1000L);
        assertThat(turnDto.endMs()).isEqualTo(2000L);
        assertThat(turnDto.speakerLabel()).isEqualTo("SPEAKER_0");
        assertThat(turnDto.text()).isEqualTo("안녕하세요 반갑습니다");
    }

    @Test
    void getSegments_빈_결과도_정상_처리() {
        // given
        Long recordingId = 1L;
        when(transcriptRepo.findByRecordingIdOrderByStartMsAsc(recordingId))
            .thenReturn(List.of());
        when(diarizationRepo.findByRecordingIdOrderByStartMsAsc(recordingId))
            .thenReturn(List.of());

        // when
        SegmentsResponse result = recordingQueryService.getSegments(recordingId);

        // then
        assertThat(result.transcripts()).isEmpty();
        assertThat(result.diarization()).isEmpty();
    }

    @Test
    void getTurns_빈_결과도_정상_처리() {
        // given
        Long recordingId = 1L;
        when(turnRepo.findByRecordingIdOrderByStartMsAsc(recordingId))
            .thenReturn(List.of());

        // when
        List<SpeakerTurnResponse> result = recordingQueryService.getTurns(recordingId);

        // then
        assertThat(result).isEmpty();
    }
}