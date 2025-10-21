package com.adas.presentation.media;

import com.adas.application.media.MediaProcessingService;
import com.adas.application.dto.ProcessResponse;
import com.adas.presentation.ApiResponse;
import com.adas.presentation.docs.MediaApiDocs;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping(path = "/api/v1/media")
@RequiredArgsConstructor
public class MediaController implements MediaApiDocs {

    private static final Logger log = LoggerFactory.getLogger(MediaController.class);
    private final MediaProcessingService mediaProcessingService;

    @PostMapping(path = "/process", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<ProcessResponse>> process(
        @RequestPart("file") MultipartFile file,
        @RequestParam(value = "language", required = false) String language,
        @RequestParam(value = "maxSpeakers", required = false) Integer maxSpeakers
    ) {
        log.info("/media/process called: filename={}, size={} bytes", file.getOriginalFilename(), file.getSize());

        ProcessResponse resp = mediaProcessingService.process(file, language, maxSpeakers);

        return ApiResponse.success(resp);
    }
}
