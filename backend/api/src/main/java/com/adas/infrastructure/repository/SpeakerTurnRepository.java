package com.adas.infrastructure.repository;

import com.adas.domain.audio.SpeakerTurn;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpeakerTurnRepository extends JpaRepository<SpeakerTurn, Long> {

    List<SpeakerTurn> findByRecordingIdOrderByStartMsAsc(Long recordingId);
}
