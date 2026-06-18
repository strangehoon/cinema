package com.example.theater.exception;

import com.example.common.exception.ErrorCode;
import lombok.Getter;

@Getter
public class TheaterException extends RuntimeException {

    private final ErrorCode errorCode;

    public TheaterException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public String getCode() {
        return errorCode.getCode();
    }
}