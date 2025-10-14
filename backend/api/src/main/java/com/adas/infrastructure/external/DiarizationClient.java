package com.adas.infrastructure.external;

import com.adas.infrastructure.external.dto.DiarizationResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Diarization(화자 분리) Python 서비스 호출용 인프라 클라이언트.
 */
@Component
public class DiarizationClient {

    private final WebClient webClient;

    @Autowired
    public DiarizationClient(@Qualifier("diarizationWebClient") WebClient webClient) {
        this.webClient = webClient;
    }

    /**
     * 화자 분리 요청.
     */
    public DiarizationResponse diarize(MultipartFile file, String language, Integer maxSpeakers) {
        MultipartBodyBuilder mb = new MultipartBodyBuilder();
        String filename = file.getOriginalFilename() != null ? file.getOriginalFilename() : "upload";
        mb.part("file", file.getResource()).filename(filename);
        if (language != null && !language.isBlank()) {
            mb.part("language", language);
        }
        if (maxSpeakers != null) {
            mb.part("max_speakers", maxSpeakers);
        }
        MultiValueMap<String, HttpEntity<?>> body = mb.build();

        return webClient.post()
            .uri("/diarize")
            .contentType(MediaType.MULTIPART_FORM_DATA)
            .body(BodyInserters.fromMultipartData(body))
            .retrieve()
            .bodyToMono(DiarizationResponse.class)
            .block();
    }
}
