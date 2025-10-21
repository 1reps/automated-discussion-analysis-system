package com.adas.domain.speaker;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SpeakerTurnRepository extends JpaRepository<SpeakerTurn, Long> {
    
    List<SpeakerTurn> findByRecordingIdOrderByStartMsAsc(Long recordingId);
}