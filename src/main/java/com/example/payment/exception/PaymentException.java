package com.example.payment.exception;

import com.example.common.exception.ErrorCode;
import lombok.Getter;

@Getter
public class PaymentException extends RuntimeException {
    private final ErrorCode errorCode;

    public PaymentException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public String getCode() {
        return errorCode.getCode();
    }
}