package com.adas.presentation;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import java.time.Instant;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@JsonInclude(Include.NON_NULL)
public record ApiResponse<T>(
    boolean isSuccess,
    T data,
    ApiError error,
    Instant timestamp
) {

    public ApiResponse {
        if (timestamp == null) {
            timestamp = Instant.now();
        }
    }

    public static <T> ApiResponse<T> successBody(T data) {
        return new ApiResponse<>(true, data, null, null);
    }

    public static ApiResponse<Void> failBody(String code, String message) {
        return new ApiResponse<>(false, null, new ApiError(code, message, null), null);
    }

    public static ApiResponse<Void> failBody(String code, String message, Map<String, Object> details) {
        return new ApiResponse<>(false, null, new ApiError(code, message, details), null);
    }

    public static <T> ResponseEntity<ApiResponse<T>> success(T data) {
        return ResponseEntity.ok(ApiResponse.successBody(data));
    }

    public static ResponseEntity<ApiResponse<Void>> fail(HttpStatus status, String code, String message) {
        return ResponseEntity.status(status).body(failBody(code, message));
    }

    public static ResponseEntity<ApiResponse<Void>> fail(
        HttpStatus status, String code, String message, Map<String, Object> details
    ) {
        return ResponseEntity.status(status).body(failBody(code, message, details));
    }

    @JsonInclude(Include.NON_NULL)
    private record ApiError(
        String code,
        String message,
        Map<String, Object> details
    ) {
        // ...
    }
}
