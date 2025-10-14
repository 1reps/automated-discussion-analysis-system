package com.adas.common.exception;

/**
 * 리소스를 찾을 수 없을 때 사용하는 예외.
 */
public class NotFoundException extends RuntimeException {

    public NotFoundException(String message) {
        super(message);
    }
}

