package com.adas.presentation.docs;

import com.adas.application.dto.RecordingResponse;
import com.adas.application.dto.SegmentsResponse;
import com.adas.application.dto.SpeakerTurnResponse;
import com.adas.presentation.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.http.ResponseEntity;

@Tag(name = "Recordings", description = "저장된 녹음/세그먼트/턴 조회")
public interface RecordingApiDocs {

    @Operation(summary = "녹음 기본 정보 조회")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공", content = @Content)})
    ResponseEntity<ApiResponse<RecordingResponse>> getRecording(Long id);

    @Operation(summary = "세그먼트(전사/화자분리) 조회")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공", content = @Content)})
    ResponseEntity<ApiResponse<SegmentsResponse>> getSegments(Long id);

    @Operation(summary = "병합된 화자 턴 조회")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공", content = @Content)})
    ResponseEntity<ApiResponse<List<SpeakerTurnResponse>>> getTurns(Long id);
}
