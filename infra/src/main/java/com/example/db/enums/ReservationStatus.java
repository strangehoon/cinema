package com.example.db.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
@Getter
public enum ReservationStatus {
    NONE("예약 아님",  "NONE"),
    IN_PROGRESS("결제 진행중", "IN_PROGRESS"),
    COMPLETED("결제 완료","COMPLETED");

    private final String value;
    private final String code;
}