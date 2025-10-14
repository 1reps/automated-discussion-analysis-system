package com.adas.domain.audio;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * STT 전사 세그먼트(단어 또는 구간 단위) 저장 엔티티.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "transcripts")
public class TranscriptSegment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "recording_id")
    private Recording recording;

    @Column(name = "start_ms", nullable = false)
    private Long startMs;

    @Column(name = "end_ms", nullable = false)
    private Long endMs;

    @Column(length = 2000)
    private String text;

    private Double confidence;

    @Column(length = 10)
    private String language;

    @Column(length = 50)
    private String provider;
}

