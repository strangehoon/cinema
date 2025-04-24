package com.example.reservation.exception;

import com.example.common.ExceptionType;
import lombok.Getter;

@Getter
public class ReservationException extends RuntimeException {
    private final ExceptionType exceptionType;

    public ReservationException(ExceptionType exceptionType) {
        super(exceptionType.getMessage());
        this.exceptionType = exceptionType;
    }

    public String getCode() {
        return exceptionType.getCode();
    }
}