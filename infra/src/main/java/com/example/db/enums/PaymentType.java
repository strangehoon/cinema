package com.example.db.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum PaymentType {
    NORMAL("일반결제", "NORMAL"),
    BILLING("자동결제", "BILLING"),
    BRANDPAY("브랜드페이", "BRANDPAY");

    private final String value;
    private final String code;

}