package com.adas.infrastructure.external;

import com.adas.infrastructure.external.dto.SttResponse;
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
 * STT(Speech-To-Text) Python 서비스 호출용 인프라 클라이언트.
 */
@Component
public class SttClient {

    private final WebClient webClient;

    @Autowired
    public SttClient(@Qualifier("sttWebClient") WebClient webClient) {
        this.webClient = webClient;
    }

    /**
     * 파일 전사 요청.
     */
    public SttResponse transcribe(MultipartFile file, String language) {
        MultipartBodyBuilder mb = new MultipartBodyBuilder();
        String filename = file.getOriginalFilename() != null ? file.getOriginalFilename() : "upload";
        mb.part("file", file.getResource()).filename(filename);
        if (language != null && !language.isBlank()) {
            mb.part("language", language);
        }
        MultiValueMap<String, HttpEntity<?>> body = mb.build();

        return webClient.post()
            .uri("/stt/transcribe")
            .contentType(MediaType.MULTIPART_FORM_DATA)
            .body(BodyInserters.fromMultipartData(body))
            .retrieve()
            .bodyToMono(SttResponse.class)
            .block();
    }
}
