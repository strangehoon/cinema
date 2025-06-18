package com.example.screening.exception;

import com.example.common.exception.ErrorCode;
import lombok.Getter;

@Getter
public class ScreeningException extends RuntimeException{

    private final ErrorCode errorCode;

    public ScreeningException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public String getCode() {
        return errorCode.getCode();
    }
}
