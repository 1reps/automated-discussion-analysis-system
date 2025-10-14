package com.adas.presentation.config;

import com.adas.common.exception.CustomException;
import com.adas.common.exception.NotFoundException;
import com.adas.presentation.ApiResponse;
import io.netty.handler.timeout.ReadTimeoutException;
import io.netty.handler.timeout.WriteTimeoutException;
import java.util.HashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;

/**
 * 전역 예외 처리기.
 * - 모든 예외를 ApiResponse 실패 바디로 변환해 일관된 에러 형식을 유지한다.
 */
@RestControllerAdvice
public class GlobalControllerAdvice {

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ApiResponse<Void>> handleCustom(CustomException exception) {
        return ApiResponse.fail(HttpStatus.BAD_REQUEST, "BAD_REQUEST", exception.getMessage());
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotFound(NotFoundException exception) {
        return ApiResponse.fail(HttpStatus.NOT_FOUND, "NOT_FOUND", exception.getMessage());
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, MissingServletRequestParameterException.class})
    public ResponseEntity<ApiResponse<Void>> handleBadRequest(Exception exception) {
        Map<String, Object> details = new HashMap<>();
        details.put("exception", exception.getClass().getSimpleName());
        return ApiResponse.fail(HttpStatus.BAD_REQUEST, "BAD_REQUEST", safeMessage(exception), details);
    }

    @ExceptionHandler({MultipartException.class, MaxUploadSizeExceededException.class})
    public ResponseEntity<ApiResponse<Void>> handleUnsupported(Exception exception) {
        Map<String, Object> details = new HashMap<>();
        details.put("exception", exception.getClass().getSimpleName());
        return ApiResponse.fail(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "UNSUPPORTED_MEDIA_TYPE", safeMessage(exception), details);
    }

    @ExceptionHandler({WebClientResponseException.class, HttpStatusCodeException.class})
    public ResponseEntity<ApiResponse<Void>> handleUpstreamResponse(Exception exception) {
        Map<String, Object> details = new HashMap<>();
        details.put("exception", exception.getClass().getSimpleName());
        // Python 서비스가 4xx/5xx를 반환한 경우 → 게이트웨이는 502로 래핑
        return ApiResponse.fail(HttpStatus.BAD_GATEWAY, "BAD_GATEWAY", safeMessage(exception), details);
    }

    @ExceptionHandler({WebClientRequestException.class})
    public ResponseEntity<ApiResponse<Void>> handleUpstreamRequest(WebClientRequestException exception) {
        Map<String, Object> details = new HashMap<>();
        details.put("exception", exception.getClass().getSimpleName());
        // 타임아웃은 504, 그 외 네트워크 오류는 502
        if (exception.getCause() instanceof ReadTimeoutException || exception.getCause() instanceof WriteTimeoutException) {
            return ApiResponse.fail(HttpStatus.GATEWAY_TIMEOUT, "GATEWAY_TIMEOUT", safeMessage(exception), details);
        }
        return ApiResponse.fail(HttpStatus.BAD_GATEWAY, "BAD_GATEWAY", safeMessage(exception), details);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(Exception exception) {
        Map<String, Object> details = new HashMap<>();
        details.put("exception", exception.getClass().getSimpleName());
        return ApiResponse.fail(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_SERVER_ERROR", safeMessage(exception), details);
    }

    private static String safeMessage(Throwable t) {
        String m = t.getMessage();
        return (m == null || m.isBlank()) ? t.getClass().getSimpleName() : m;
    }
}
