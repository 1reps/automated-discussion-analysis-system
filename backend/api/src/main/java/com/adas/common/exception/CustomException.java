package com.adas.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class CustomException extends RuntimeException {

    private String message;

    public CustomException(String message) {
        super(message);
    }
}
