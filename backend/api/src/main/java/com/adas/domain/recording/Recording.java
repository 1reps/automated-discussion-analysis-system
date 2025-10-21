package com.adas.domain.recording;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

/**
 * 업로드된 오디오에 대한 메타데이터 레코드.
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "recordings")
public class Recording {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String source;

    @Column(length = 10)
    private String language;

    @Column(name = "size_bytes")
    private Long sizeBytes;

    @Column(name = "duration_ms")
    private Long durationMs;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    public static Recording createFromUpload(String source, String language, Long sizeBytes) {
        Recording recording = new Recording();
        recording.source = source;
        recording.language = language;
        recording.sizeBytes = sizeBytes;
        return recording;
    }

    public void specifyDuration(Long durationMs) {
        if (durationMs != null && durationMs < 0) {
            throw new IllegalArgumentException("Duration cannot be negative");
        }
        this.durationMs = durationMs;
    }

    public void markAsProcessedBy(String source) {
        if (source == null || source.trim().isEmpty()) {
            throw new IllegalArgumentException("Source cannot be null or empty");
        }
        this.source = source;
    }
}