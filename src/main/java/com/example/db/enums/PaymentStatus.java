package com.example.db.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum PaymentStatus {

    READY("READY", "결제 준비"),
    IN_PROGRESS("IN_PROGRESS", "결제 인증 완료"),
    WAITING_FOR_DEPOSIT("WAITING_FOR_DEPOSIT", "입금 대기 중"),
    DONE("DONE", "결제 승인 완료"),
    CANCELED("CANCELED", "결제 취소 완료"),
    PARTIAL_CANCELED("PARTIAL_CANCELED", "결제 부분 취소 완료"),
    ABORTED("ABORTED", "결제 실패"),
    EXPIRED("EXPIRED", "결제 유효시간 만료");

    private final String value;
    private final String code;

}
