package com.example.user.exception;

import com.example.common.exception.ErrorCode;
import lombok.Getter;

@Getter
public class UserException extends RuntimeException{

    private final ErrorCode errorCode;

    public UserException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public String getCode() {
        return errorCode.getCode();
    }
}
