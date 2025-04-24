package com.example.reservation.exception;

import com.example.common.ExceptionType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ReservationErrorCode implements ExceptionType {

    MAX_SEAT_LIMIT_EXCEEDED("400_1", "1인당 최대 5좌석까지만 예매할 수 있습니다."),
    INVALID_SEAT("400_2",  "유효하지 않은 좌석이 포함되어 있습니다."),
    NOT_SAME_ROW("400_3",  "좌석은 같은 행(row)이어야 합니다."),
    NOT_ADJACENT_SEAT("400_4",  "좌석은 같은 행에서 인접해야 합니다."),
    RESERVATION_DATA_INCOMPLETE("400_5",  "요청한 좌석에 대한 Reservation 데이터가 일부 누락되었습니다."),
    ALREADY_RESERVED_SEAT("409_1",  "이미 예약된 좌석이 포함되어 있습니다.");

    private final String code;
    private final String message;
}
