package com.adas.domain.transcript;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TranscriptSegmentRepository extends JpaRepository<TranscriptSegment, Long> {
    
    List<TranscriptSegment> findByRecordingIdOrderByStartMsAsc(Long recordingId);
}