package com.adas.presentation.docs;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Map;
import org.springframework.http.ResponseEntity;

@Tag(name = "Recordings", description = "저장된 녹음/세그먼트/턴 조회")
public interface TranscriptApiDocs {

    @Operation(summary = "Recorder 호환 트랜스크립트 조회", description = "스네이크 케이스 키로 transcripts/speaker_turns 반환")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "성공", content = @Content)})
    ResponseEntity<Map<String, Object>> transcript(Long id);
}

