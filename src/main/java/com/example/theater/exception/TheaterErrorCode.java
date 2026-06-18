package com.example.theater.exception;

import com.example.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TheaterErrorCode implements ErrorCode {

    THEATER_NOT_FOUND("400_1", "존재하지 않는 극장 정보입니다.");

    private final String code;
    private final String message;
}
