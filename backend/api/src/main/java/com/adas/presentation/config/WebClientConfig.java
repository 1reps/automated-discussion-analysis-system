package com.adas.presentation.config;

import com.adas.presentation.config.ExternalServicesProperties.ServiceProperties;
import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

/**
 * 외부 Python 서비스(STT, Diarization) 호출용 WebClient 설정. 서비스별로 연결/응답/읽기/쓰기 타임아웃을 개별 설정한다.
 */
@Configuration
@EnableConfigurationProperties(ExternalServicesProperties.class)
public class WebClientConfig {

    /**
     * STT 서비스용 WebClient.
     */
    @Bean(name = "sttWebClient")
    public WebClient sttWebClient(ExternalServicesProperties props) {
        return build(props.getStt());
    }

    /**
     * Diarization 서비스용 WebClient. 'external.diarization' 우선, 미설정 시 'external.diar'로 폴백.
     */
    @Bean(name = "diarizationWebClient")
    public WebClient diarizationWebClient(ExternalServicesProperties props) {
        return build(props.getEffectiveDiarization());
    }

    /**
     * 공통 WebClient 빌더. Reactor Netty 기반 타임아웃/핸들러를 구성한다.
     */
    private static WebClient build(ServiceProperties p) {
        HttpClient httpClient = HttpClient.create()
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, p.getConnectTimeoutMs())
            .responseTimeout(Duration.ofMillis(p.getResponseTimeoutMs()))
            .doOnConnected(conn -> {
                long readSec = (long) Math.ceil(p.getReadTimeoutMs() / 1000.0);
                long writeSec = (long) Math.ceil(p.getWriteTimeoutMs() / 1000.0);
                conn.addHandlerLast(new ReadTimeoutHandler(readSec, TimeUnit.SECONDS));
                conn.addHandlerLast(new WriteTimeoutHandler(writeSec, TimeUnit.SECONDS));
            });

        return WebClient.builder()
            .baseUrl(p.getBaseUrl())
            .clientConnector(new ReactorClientHttpConnector(httpClient))
            .build();
    }
}
