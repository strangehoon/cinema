package com.example.db.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum PaymentMethod {

    CARD("카드 결제", "CARD"),
    VIRTUAL_ACCOUNT("가상계좌", "VIRTUAL_ACCOUNT"),
    EASY_PAYMENT("간편결제", "EASY_PAYMENT"),
    MOBILE_PHONE("휴대폰 결제", "MOBILE_PHONE"),
    ACCOUNT_TRANSFER("계좌이체", "ACCOUNT_TRANSFER"),
    CULTURE_GIFT_CERT("문화상품권", "CULTURE_GIFT_CERT"),
    BOOK_GIFT_CERT("도서문화상품권", "BOOK_GIFT_CERT"),
    GAME_GIFT_CERT("게임문화상품권", "GAME_GIFT_CERT");

    private final String value;
    private final String code;

    public static PaymentMethod fromValue(String value) {
        for (PaymentMethod method : PaymentMethod.values()) {
            if (method.value.equals(value)) { // 여기 value 비교!
                return method;
            }
        }
        throw new IllegalArgumentException("Unknown PaymentMethod value: " + value);
    }
}
