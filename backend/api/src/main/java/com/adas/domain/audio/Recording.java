package com.adas.domain.audio;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

/**
 * 업로드된 오디오에 대한 메타데이터 레코드.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "recordings")
public class Recording {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 업로드 출처(예: api, stt, diarization 등)
    @Column(nullable = false, length = 50)
    private String source;

    // 언어 힌트(예: ko, en)
    @Column(length = 10)
    private String language;

    // 원본 파일 크기(바이트)
    @Column(name = "size_bytes")
    private Long sizeBytes;

    // 전체 길이(ms) - 추후 ffprobe로 채움
    @Column(name = "duration_ms")
    private Long durationMs;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;
}

