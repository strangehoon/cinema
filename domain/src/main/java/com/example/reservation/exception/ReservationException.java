package com.example.reservation.exception;

import com.example.common.ErrorCode;
import lombok.Getter;

@Getter
public class ReservationException extends RuntimeException {
    private final ErrorCode errorCode;

    public ReservationException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public String getCode() {
        return errorCode.getCode();
    }
}