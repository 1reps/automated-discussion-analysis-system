package com.adas.presentation.docs;

import com.adas.application.dto.ProcessResponse;
import com.adas.presentation.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "Media", description = "업로드 처리(STT+Diarization) 엔드포인트")
public interface MediaApiDocs {

  @Operation(summary = "오디오 업로드 처리", description = "파일을 업로드하여 STT와 Diarization 처리 후 병합 결과를 반환합니다.")
  @ApiResponses({
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공", content = @Content(mediaType = "application/json")),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "415", description = "지원하지 않는 미디어 타입", content = @Content),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "502", description = "상류 서비스 오류", content = @Content),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "504", description = "상류 서비스 타임아웃", content = @Content)
  })
  ResponseEntity<ApiResponse<ProcessResponse>> process(
      @Parameter(description = "업로드 오디오 파일", required = true) MultipartFile file,
      @Parameter(description = "언어 힌트(예: ko, en)") String language,
      @Parameter(description = "최대 화자 수 힌트") Integer maxSpeakers
  );
}
