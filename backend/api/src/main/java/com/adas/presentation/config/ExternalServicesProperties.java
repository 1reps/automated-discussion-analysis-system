package com.adas.presentation.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 외부 Python 서비스(STT, Diarization) 연결 설정 바인딩. application.yml의 `external.*` 값을 매핑한다.
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "external")
public class ExternalServicesProperties {

    // STT(Speech-To-Text) 설정
    private ServiceProperties stt = new ServiceProperties();
    // Diarization 설정(신규 권장)
    private ServiceProperties diarization = new ServiceProperties();
    // Diar(축약) 설정(하위호환용, 존재 시 폴백)
    private ServiceProperties diar = new ServiceProperties();

    /**
     * Diarization 설정을 우선 사용하고, 미설정 시(또는 baseUrl 미지정) 'diar' 값을 폴백으로 사용한다.
     */
    public ServiceProperties getEffectiveDiarization() {
        if (diarization != null && diarization.getBaseUrl() != null && !diarization.getBaseUrl().isBlank()) {
            return diarization;
        }
        return diar;
    }

    @Getter
    @Setter
    public static class ServiceProperties {

        /**
         * 서비스 베이스 URL (예: http://stt:8000/api/v1)
         */
        private String baseUrl;
        /**
         * TCP 연결 타임아웃(ms)
         */
        private int connectTimeoutMs = 5_000;
        /**
         * 전체 응답 타임아웃(ms)
         */
        private int responseTimeoutMs = 60_000;
        /**
         * 읽기 타임아웃(ms)
         */
        private int readTimeoutMs = 60_000;
        /**
         * 쓰기 타임아웃(ms)
         */
        private int writeTimeoutMs = 60_000;
    }
}
