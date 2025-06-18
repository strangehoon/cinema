package com.example.movie.exception;

import com.example.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MovieErrorCode implements ErrorCode {

    MOVIE_NOT_FOUND("400_1", "존재하지 않는 영화 정보입니다.");

    private final String code;
    private final String message;
}