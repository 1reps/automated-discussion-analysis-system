package com.adas.infrastructure.media;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 미디어 도구 관련 설정.
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "media")
public class MediaProperties {

    /**
     * ffprobe 실행 파일 경로(또는 PATH 내 명령어명)
     */
    private String ffprobePath = "ffprobe";
    /**
     * ffprobe 실행 타임아웃(ms)
     */
    private long probeTimeoutMs = 5000;
}

