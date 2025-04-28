package com.example.payment.exception;

import com.example.common.ErrorCode;
import lombok.Getter;

@Getter
public class PaymentConfirmException extends RuntimeException {
    private final ErrorCode errorCode;

    public PaymentConfirmException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public String getCode() {
        return errorCode.getCode();
    }
}