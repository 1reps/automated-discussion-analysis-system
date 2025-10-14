package com.adas.infrastructure.repository;

import com.adas.domain.audio.Recording;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecordingRepository extends JpaRepository<Recording, Long> {

}

