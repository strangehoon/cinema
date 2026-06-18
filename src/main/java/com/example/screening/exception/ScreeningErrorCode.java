package com.example.screening.exception;

import com.example.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ScreeningErrorCode implements ErrorCode {

    SCREENING_NOT_FOUND("400_1", "존재하지 않는 상영 정보입니다.");

    private final String code;
    private final String message;
}
