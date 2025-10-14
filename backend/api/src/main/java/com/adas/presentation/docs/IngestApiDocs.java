package com.adas.presentation.docs;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "Ingest", description = "프런트 Recorder.vue 호환 업로드/폴링 API")
public interface IngestApiDocs {

  @Operation(summary = "오디오 업로드(Recorder 호환)", description = "recording_id와 transcript_url을 반환")
  @ApiResponses({@ApiResponse(responseCode = "200", description = "성공", content = @Content)})
  ResponseEntity<Map<String, Object>> ingest(MultipartFile file, String language, String source);
}

