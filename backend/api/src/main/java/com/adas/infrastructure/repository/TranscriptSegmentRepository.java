package com.adas.infrastructure.repository;

import com.adas.domain.audio.TranscriptSegment;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TranscriptSegmentRepository extends JpaRepository<TranscriptSegment, Long> {

    List<TranscriptSegment> findByRecordingIdOrderByStartMsAsc(Long recordingId);
}
