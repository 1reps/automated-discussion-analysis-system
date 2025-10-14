package com.adas.infrastructure.repository;

import com.adas.domain.audio.DiarizationSegmentEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DiarizationSegmentRepository extends JpaRepository<DiarizationSegmentEntity, Long> {

    List<DiarizationSegmentEntity> findByRecordingIdOrderByStartMsAsc(Long recordingId);
}
