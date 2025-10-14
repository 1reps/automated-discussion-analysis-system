package com.adas.presentation.config;

import com.adas.common.exception.CustomException;
import com.adas.common.exception.NotFoundException;
import io.netty.handler.timeout.ReadTimeoutException;
import io.netty.handler.timeout.WriteTimeoutException;
import java.time.OffsetDateTime;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
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
 * 전역 예외 처리기(ProblemDetail 기반). - Spring ProblemDetail(JSON Problem)로 일관된 오류 응답을 제공한다.
 */
@RestControllerAdvice
public class GlobalControllerAdvice {

    @ExceptionHandler(CustomException.class)
    public ProblemDetail handleCustom(CustomException exception) {
        return problem(HttpStatus.BAD_REQUEST, exception);
    }

    @ExceptionHandler(NotFoundException.class)
    public ProblemDetail handleNotFound(NotFoundException exception) {
        return problem(HttpStatus.NOT_FOUND, exception);
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, MissingServletRequestParameterException.class})
    public ProblemDetail handleBadRequest(Exception exception) {
        return problem(HttpStatus.BAD_REQUEST, exception);
    }

    @ExceptionHandler({MultipartException.class, MaxUploadSizeExceededException.class})
    public ProblemDetail handleUnsupported(Exception exception) {
        return problem(HttpStatus.UNSUPPORTED_MEDIA_TYPE, exception);
    }

    @ExceptionHandler({WebClientResponseException.class, HttpStatusCodeException.class})
    public ProblemDetail handleUpstreamResponse(Exception exception) {
        // 상류 서비스가 4xx/5xx 응답 → 게이트웨이는 502로 래핑
        return problem(HttpStatus.BAD_GATEWAY, exception);
    }

    @ExceptionHandler(WebClientRequestException.class)
    public ProblemDetail handleUpstreamRequest(WebClientRequestException exception) {
        HttpStatus status =
            (exception.getCause() instanceof ReadTimeoutException
                || exception.getCause() instanceof WriteTimeoutException)
                ? HttpStatus.GATEWAY_TIMEOUT
                : HttpStatus.BAD_GATEWAY;
        return problem(status, exception);
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleException(Exception exception) {
        return problem(HttpStatus.INTERNAL_SERVER_ERROR, exception);
    }

    private static ProblemDetail problem(HttpStatus status, Exception exception) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(status, safeMessage(exception));
        pd.setTitle(status.getReasonPhrase());
        pd.setProperty("timestamp", OffsetDateTime.now());
        pd.setProperty("exception", exception.getClass().getSimpleName());
        return pd;
    }

    private static String safeMessage(Throwable t) {
        String m = t.getMessage();
        return (m == null || m.isBlank()) ? t.getClass().getSimpleName() : m;
    }
}
