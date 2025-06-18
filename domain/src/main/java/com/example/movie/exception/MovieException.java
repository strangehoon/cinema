package com.example.movie.exception;

import com.example.common.exception.ErrorCode;
import lombok.Getter;

@Getter
public class MovieException extends RuntimeException {
    private final ErrorCode errorCode;

    public MovieException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public String getCode() {
        return errorCode.getCode();
    }
}