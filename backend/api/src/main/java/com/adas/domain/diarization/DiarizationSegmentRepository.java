package com.adas.domain.diarization;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DiarizationSegmentRepository extends JpaRepository<DiarizationSegment, Long> {
    
    List<DiarizationSegment> findByRecordingIdOrderByStartMsAsc(Long recordingId);
}